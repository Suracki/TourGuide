package tourGuide.service;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.*;
import java.util.concurrent.*;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import com.jsoniter.output.JsonStream;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import gpsUtil.GpsUtil;
import gpsUtil.location.Attraction;
import gpsUtil.location.Location;
import gpsUtil.location.VisitedLocation;
import tourGuide.helper.InternalTestHelper;
import tourGuide.outputEntities.NearbyAttraction;
import tourGuide.outputEntities.UserLocation;
import tourGuide.tracker.Tracker;
import tourGuide.user.User;
import tourGuide.user.UserReward;
import tripPricer.Provider;
import tripPricer.TripPricer;

@Service
public class TourGuideService {
	private Logger logger = LoggerFactory.getLogger(TourGuideService.class);
	private final GpsService gpsService;
	private final RewardsService rewardsService;
	private final UserService userService;
	private final TripPricer tripPricer = new TripPricer();
	public final Tracker tracker;
	boolean testMode = true;
	private ExecutorService executorService = Executors.newFixedThreadPool(10000);


	public TourGuideService(GpsService gpsService, RewardsService rewardsService, UserService userService) {
		this.gpsService = gpsService;
		this.rewardsService = rewardsService;
		this.userService = userService;
		
		if(testMode) {
			logger.info("TestMode enabled");
			logger.debug("Initializing users");
			initializeInternalUsers();
			logger.debug("Finished initializing users");
		}
		tracker = new Tracker(this);
		addShutDownHook();
	}
	
	public List<UserReward> getUserRewards(User user) {
		return user.getUserRewards();
	}
	
	public VisitedLocation getUserLocation(User user) {
		VisitedLocation visitedLocation = (user.getVisitedLocations().size() > 0) ?
			user.getLastVisitedLocation() :
			trackUserLocation(user);
		return visitedLocation;
	}
	
	public User getUser(String userName) {
		return userService.getUserByUsername(userName);
	}
	
	public List<User> getAllUsers() {
		return userService.getAllUsers();
	}
	
	public void addUser(User user) {
		userService.addUser(user);
	}
	
	public List<Provider> getTripDeals(User user) {
		int cumulatativeRewardPoints = user.getUserRewards().stream().mapToInt(i -> i.getRewardPoints()).sum();
		List<Provider> providers = tripPricer.getPrice(tripPricerApiKey, user.getUserId(), user.getUserPreferences().getNumberOfAdults(), 
				user.getUserPreferences().getNumberOfChildren(), user.getUserPreferences().getTripDuration(), cumulatativeRewardPoints);
		user.setTripDeals(providers);
		return providers;
	}

	public VisitedLocation trackUserLocation(User user) {

		VisitedLocation visitedLocation = gpsService.getUserLocation(user.getUserId());

		CompletableFuture.supplyAsync(()-> {
			return userService.addToVisitedLocations(visitedLocation, user.getUserName());
		}, executorService)
				.thenAccept(n -> {rewardsService.calculateRewards(user);});

		return visitedLocation;
	}

	public void trackAllUserLocations() {
		List<User> allUsers = userService.getAllUsers();

		ArrayList<Thread> threads = new ArrayList<>();

		System.out.println("Creating threads for " + allUsers.size() + " user(s)");
		allUsers.forEach((n)-> {
			threads.add(
					new Thread( ()-> {
						userService.addToVisitedLocations(gpsService.getUserLocation(n.getUserId()), n.getUserName());
					})
					);
		});
		System.out.println("Thread array size: " + threads.size());
		System.out.println("Starting threads...");
		threads.forEach((n)->n.start());
		System.out.println("Joining threads...");
		threads.forEach((n)-> {
			try {
				n.join();
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		});
		System.out.println("Done!");

	}

	public void trackAllUserLocationsAndProcess() {


		List<User> allUsers = userService.getAllUsers();

		ArrayList<CompletableFuture> futures = new ArrayList<>();

		System.out.println("Creating threads for " + allUsers.size() + " user(s)");
		allUsers.forEach((n)-> {
			futures.add(
			CompletableFuture.supplyAsync(()-> {
						return userService.addToVisitedLocations(gpsService.getUserLocation(n.getUserId()), n.getUserName());
					}, executorService)
					.thenAccept(y -> {rewardsService.calculateRewards(n);})
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
		return userService.getAllCurrentLocations();
	}

	//Returns 5 nearest attractions
	public List<NearbyAttraction> getNearByAttractions(VisitedLocation visitedLocation) {
		List<Attraction> attractionsList;
		Map<Double, Attraction> attractionsMap = new HashMap<>();

		//Create Map of Distance/Location, place into TreeMap to sort by distance
		gpsService.getAttractions().forEach((n)-> {
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
				getDistance(n, visitedLocation.location), rewardsService.getRewardValue(n, visitedLocation.userId)));

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
		        tracker.stopTracking();
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

			userService.addUser(user);
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
	
}

//class CalcRewards extends Thread {
//	private User user;
//	private RewardsService rewardsService;
//	private VisitedLocation visitedLocation;
//	public CalcRewards(User user, RewardsService rewardsService, VisitedLocation visitedLocation) {
//		this.user = user;
//		this.rewardsService = rewardsService;
//		this.visitedLocation = visitedLocation;
//	}
//	@Override
//	public void run() {
//		user.addToVisitedLocations(visitedLocation);
//		rewardsService.calculateRewardsFutures(user);
//	}
//}
