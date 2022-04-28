package tourGuide.service;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.*;
import java.util.concurrent.*;
import java.util.stream.IntStream;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

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

/**
 * TourGuideService performs operations for TourGuideController, as well as performing services for Tracker thread
 */
@Service
public class TourGuideService {
	private Logger logger = LoggerFactory.getLogger(TourGuideService.class);
	private final GpsService gpsService;
	private final RewardsService rewardsService;
	private final UserService userService;
	private final TripService tripService;
	public final Tracker tracker;
	boolean testMode = true;
	@Value("${thread.pool.size}")
	private int threadPoolSize = 500;
	private ExecutorService executorService = Executors.newFixedThreadPool(threadPoolSize);


	public TourGuideService(GpsService gpsService, RewardsService rewardsService, UserService userService, TripService tripService) {
		this.gpsService = gpsService;
		this.rewardsService = rewardsService;
		this.userService = userService;
		this.tripService = tripService;
		
		if(testMode) {
			logger.info("TestMode enabled");
			logger.debug("Initializing users");
			initializeInternalUsers();
			logger.debug("Finished initializing users");
		}
		tracker = new Tracker(this);
		addShutDownHook();
	}

	/**
	 * Get all current UserRewards for provided user
	 *
	 * @param userName
	 * @return List<UserReward>
	 */
	public List<UserReward> getUserRewards(String userName) {
		return getUserByUsername(userName).getUserRewards();
	}

	/**
	 * Get current location of provided user
	 *
	 * @param userName
	 * @return VisitedLocation
	 */
	public VisitedLocation getUserLocation(String userName) {
		User user = getUserByUsername(userName);
		if (user==null) {
			return null;
		}
		VisitedLocation visitedLocation = (user.getVisitedLocations().size() > 0) ?
			user.getLastVisitedLocation() :
			trackUserLocation(user);
		return visitedLocation;
	}

	/**
	 * Get User object for provided user name
	 *
	 * @param userName
	 * @return User
	 */
	public User getUserByUsername(String userName) {
		return userService.getUserByUsername(userName);
	}

	/**
	 * Get User objects for all users currently in system
	 *
	 * @return List<User>
	 */
	public List<User> getAllUsers() {
		return userService.getAllUsers();
	}

	/**
	 * Get number of users currently stored in system
	 *
	 * @return int
	 */
	public int getUserCount() {
		return userService.getUserCount();
	}

	/**
	 * Add a user to the system
	 *
	 * @param user object
	 */
	public void addUser(User user) {
		userService.addUser(user);
	}

	/**
	 * Get trip deals for a provided user from the TripService
	 *
	 * @param userName
	 * @return List<Provider>
	 */
	public List<Provider> getTripDeals(String userName) {
		User user = getUserByUsername(userName);
		if (user == null) {
			return null;
		}
		return tripService.getTripDeals(getUserByUsername(userName));
	}

	/**
	 * Track user's current location
	 *
	 * Gets users location from GpsService
	 * Then adds this location to user's visited locations
	 * Then asks reward service to calculate user's rewards to update with new location
	 *
	 * @param user
	 * @return VisitedLocation user's current location
	 */
	public VisitedLocation trackUserLocation(User user) {

		VisitedLocation visitedLocation = gpsService.getUserLocation(user.getUserId());

		CompletableFuture.supplyAsync(()-> {
			return userService.addToVisitedLocations(visitedLocation, user.getUserName());
		}, executorService)
				.thenAccept(n -> {rewardsService.calculateRewards(user);});

		return visitedLocation;
	}

	/**
	 * Track all users' current location
	 *
	 * For each user:
	 * Gets users location from GpsService
	 * Then adds this location to user's visited locations
	 *
	 */
	public void trackAllUserLocations() {
		List<User> allUsers = userService.getAllUsers();

		ArrayList<CompletableFuture> futures = new ArrayList<>();

		System.out.println("Creating threads for " + allUsers.size() + " user(s)");
		allUsers.forEach((n)-> {
			futures.add(
					CompletableFuture.supplyAsync(()-> {
						return userService.addToVisitedLocations(gpsService.getUserLocation(n.getUserId()), n.getUserName());
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

	/**
	 * Track all users' current location, then check if any new rewards are triggered
	 *
	 * For each user:
	 * Gets users location from GpsService
	 * Then adds this location to user's visited locations
	 * Then asks reward service to calculate user's rewards to update with new location
	 *
	 */
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

	/**
	 * Process any pending rewards for all users
	 *
	 * For each user:
	 * Asks reward service to calculate user's rewards to update with any new visited locations
	 *
	 */
	public void processAllUserRewards() {


		List<User> allUsers = userService.getAllUsers();

		ArrayList<CompletableFuture> futures = new ArrayList<>();

		System.out.println("Creating threads for " + allUsers.size() + " user(s)");
		allUsers.forEach((n)-> {
			futures.add(
					CompletableFuture.supplyAsync(()-> {
								return rewardsService.calculateRewardsReturn(n);
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

	/**
	 * Get all users' current locations from UserService
	 *
	 * @return List<UserLocation> all users' current location
	 */
	public List<UserLocation> getAllCurrentLocations() {
		return userService.getAllCurrentLocations();
	}

	/**
	 * Get five nearest attractions for provided location
	 *
	 * Gets all attractions from GpsService, and sorts by distance from provided location.
	 * Then returns the nearest five (or as many as possible, if GpsService has less than 5 total).
	 *
	 * @param visitedLocation
	 * @return List<NearbyAttraction>
	 */
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
	private double getDistance(Location loc1, Location loc2) {
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
			System.out.println(userName);
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
