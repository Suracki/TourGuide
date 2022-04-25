package tourGuide.service;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.*;
import java.util.concurrent.*;
import java.util.stream.IntStream;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import gpsUtil.location.Attraction;
import gpsUtil.location.Location;
import gpsUtil.location.VisitedLocation;
import tourGuide.remote.GpsRemote;
import tourGuide.remote.GpsRetro;
import tourGuide.remote.RewardsRemote;
import tourGuide.remote.UserRemote;
import tourGuide.helper.InternalTestHelper;
import tourGuide.outputEntities.NearbyAttraction;
import tourGuide.outputEntities.UserLocation;
import tourGuide.tracker.Tracker;
import tourGuide.dockers.userDocker.model.User;
import tourGuide.dockers.userDocker.model.UserReward;
import tripPricer.Provider;
import tripPricer.TripPricer;

@Service
public class TourGuideService {
	private Logger logger = LoggerFactory.getLogger(TourGuideService.class);
	private final GpsRemote gpsRemote;
	private final RewardsRemote rewardsRemote;
	private final UserRemote userRemote;
	private final TripPricer tripPricer = new TripPricer();
	public final Tracker tracker;
	boolean testMode = true;
	private ExecutorService executorService = Executors.newFixedThreadPool(10000);

	public TourGuideService(GpsRemote gpsRemote, RewardsRemote rewardsRemote, UserRemote userRemote) {
		this.gpsRemote = gpsRemote;
		this.rewardsRemote = rewardsRemote;
		this.userRemote = userRemote;
		
		if(testMode) {
			logger.info("TestMode enabled");
			logger.debug("Initializing users");
			initializeInternalUsers();
			logger.debug("Finished initializing users");
		}
		tracker = new Tracker(this);
		addShutDownHook();
	}
	
	public List<UserReward> getUserRewards(String userName) {
		return userRemote.getUserRewardsByUsername(userName);
	}
	
	public VisitedLocation getUserLocation(String userName) {
		VisitedLocation visitedLocation = (userRemote.getVisitedLocationsByUsername(userName).size() > 0) ?
				userRemote.getLastVisitedLocationByName(userName) :
				trackUserLocationByName(userName);
		return visitedLocation;
	}
	
	public User getUser(String userName) {
		return userRemote.getUserByUsername(userName);
	}
	
	public int getUserCount() {
		return userRemote.getUserCount();
	}

	public List<Provider> getTripDeals(String userName) {
		User user = userRemote.getUserByUsername(userName);

		int cumulatativeRewardPoints = user.getUserRewards().stream().mapToInt(i -> i.getRewardPoints()).sum();
		List<Provider> providers = tripPricer.getPrice(tripPricerApiKey, user.getUserId(), user.getUserPreferences().getNumberOfAdults(), 
				user.getUserPreferences().getNumberOfChildren(), user.getUserPreferences().getTripDuration(), cumulatativeRewardPoints);
		user.setTripDeals(providers);
		return providers;
	}

	public VisitedLocation trackUserLocationByName(String userName) {

		VisitedLocation visitedLocation = gpsRemote.getUserLocation(userRemote.getUserIdByUsername(userName));

		CompletableFuture.supplyAsync(()-> {
					return userRemote.addToVisitedLocations(visitedLocation, userName);
				}, executorService)
				.thenAccept(n -> {rewardsRemote.calculateRewardsByUsername(userName);});

		return visitedLocation;
	}

	public VisitedLocation trackUserLocation(User user) {

		VisitedLocation visitedLocation = gpsRemote.getUserLocation(user.getUserId());

		CompletableFuture.supplyAsync(()-> {
			return userRemote.addToVisitedLocations(visitedLocation, user.getUserName());
		}, executorService)
				.thenAccept(n -> {rewardsRemote.calculateRewardsByUsername(user.getUserName());});

		return visitedLocation;
	}

	public void trackAllUserLocations() {
		userRemote.trackAllUserLocations();
	}

	public void trackAllUserLocationsAndProcess() {


		List<User> allUsers = userRemote.getAllUsers();

		ArrayList<CompletableFuture> futures = new ArrayList<>();

		System.out.println("Creating threads for " + allUsers.size() + " user(s)");
		allUsers.forEach((n)-> {
			futures.add(
			CompletableFuture.supplyAsync(()-> {
						return userRemote.addToVisitedLocations(gpsRemote.getUserLocation(n.getUserId()), n.getUserName());
					}, executorService)
					.thenAccept(y -> {rewardsRemote.calculateRewardsByUsername(n.getUserName());})
			);
		});
		System.out.println("Futures created: " + futures.size());
		System.out.println("Getting futures...");
		futures.forEach((n)-> {
			try {
				n.get();
			} catch (InterruptedException e) {
				e.printStackTrace();
			} catch (ExecutionException e) {
				e.printStackTrace();
			}
		});
		System.out.println("Done!");

	}

	public void processAllUserRewards() {


		List<User> allUsers = userRemote.getAllUsers();

		ArrayList<CompletableFuture> futures = new ArrayList<>();

		System.out.println("Creating threads for " + allUsers.size() + " user(s)");
		allUsers.forEach((n)-> {
			futures.add(
					CompletableFuture.supplyAsync(()-> {
						return rewardsRemote.calculateRewardsByUsername(n.getUserName());
					}, executorService)
			);
		});
		System.out.println("Futures created: " + futures.size());
		System.out.println("Getting futures...");
		futures.forEach((n)-> {
			try {
				n.get();
			} catch (InterruptedException e) {
				e.printStackTrace();
			} catch (ExecutionException e) {
				e.printStackTrace();
			}
		});
		System.out.println("Done!");

	}

	public List<UserLocation> getAllCurrentLocations() {
		return userRemote.getAllCurrentLocations();
	}

	//Returns 5 nearest attractions
	public List<NearbyAttraction> getNearByAttractions(VisitedLocation visitedLocation) {
		List<Attraction> attractionsList;
		Map<Double, Attraction> attractionsMap = new HashMap<>();

		//Create Map of Distance/Location, place into TreeMap to sort by distance
		gpsRemote.getAttractions().forEach((n)-> {
			attractionsMap.put(getDistance(n, visitedLocation.location), n);
		});
		TreeMap<Double, Attraction> sortedAttractionMap = new TreeMap<>(attractionsMap);

		//Create ArrayList containing closest 5 attractions
		if (sortedAttractionMap.size() >= 5) {
			attractionsList = new ArrayList<>(sortedAttractionMap.values()).subList(0,5);
		}
		else {
			attractionsList = new ArrayList<>(sortedAttractionMap.values()).subList(0,sortedAttractionMap.size());
		}

		//Create list of output entities containing only desired data
		List<NearbyAttraction> output = new ArrayList<>();
		attractionsList.forEach((n)-> {output.add(new NearbyAttraction(n.attractionName,
				n.latitude, n.longitude, visitedLocation.location.latitude, visitedLocation.location.longitude,
				getDistance(n, visitedLocation.location), rewardsRemote.getRewardValue(n.attractionId, visitedLocation.userId)));

		});

		return output;
	}

	private static final double STATUTE_MILES_PER_NAUTICAL_MILE = 1.15077945;
	private int defaultProximityBuffer = 10;
	private int proximityBuffer = defaultProximityBuffer;
	private int attractionProximityRange = 200;

	private boolean nearAttraction(VisitedLocation visitedLocation, Attraction attraction) {
		return getDistance(attraction, visitedLocation.location) > proximityBuffer ? false : true;
	}
	public double getDistance(Location loc1, Location loc2) {
		double lat1 = Math.toRadians(loc1.latitude);
		double lon1 = Math.toRadians(loc1.longitude);
		double lat2 = Math.toRadians(loc2.latitude);
		double lon2 = Math.toRadians(loc2.longitude);

		double angle = Math.acos(Math.sin(lat1) * Math.sin(lat2)
				+ Math.cos(lat1) * Math.cos(lat2) * Math.cos(lon1 - lon2));

		double nauticalMiles = 60 * Math.toDegrees(angle);
		double statuteMiles = STATUTE_MILES_PER_NAUTICAL_MILE * nauticalMiles;
		return statuteMiles;
	}
	
	private void addShutDownHook() {
		Runtime.getRuntime().addShutdownHook(new Thread() { 
		      public void run() {
		        executorService.shutdown();tracker.stopTracking();
		      } 
		    }); 
	}
	
	/**********************************************************************************
	 * 
	 * Methods Below: For Internal Testing
	 * 
	 **********************************************************************************/
	private static final String tripPricerApiKey = "test-server-api-key";
	// Database connection will be used for external users, but for testing purposes internal users are provided and stored in memory
	//private final Map<String, User> internalUserMap = new HashMap<>();
	private void initializeInternalUsers() {
		IntStream.range(0, InternalTestHelper.getInternalUserNumber()).forEach(i -> {
			String userName = "internalUser" + i;
			String phone = "000";
			String email = userName + "@tourGuide.com";
			User user = new User(UUID.randomUUID(), userName, phone, email);
			generateUserLocationHistory(user);

			userRemote.addUser(user);
		});
		logger.debug("Created " + InternalTestHelper.getInternalUserNumber() + " internal test users.");
	}
	
	private void generateUserLocationHistory(User user) {
		IntStream.range(0, 3).forEach(i-> {
			user.addToVisitedLocations(new VisitedLocation(user.getUserId(), new Location(generateRandomLatitude(), generateRandomLongitude()), getRandomTime()));
		});
	}
	
	private double generateRandomLongitude() {
		double leftLimit = -180;
	    double rightLimit = 180;
	    return leftLimit + new Random().nextDouble() * (rightLimit - leftLimit);
	}
	
	private double generateRandomLatitude() {
		double leftLimit = -85.05112878;
	    double rightLimit = 85.05112878;
	    return leftLimit + new Random().nextDouble() * (rightLimit - leftLimit);
	}
	
	private Date getRandomTime() {
		LocalDateTime localDateTime = LocalDateTime.now().minusDays(new Random().nextInt(30));
	    return Date.from(localDateTime.toInstant(ZoneOffset.UTC));
	}

	public List<String> getAllUserNames() {
		return userRemote.getAllUserNames();
	}
}
