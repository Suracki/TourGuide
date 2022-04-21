package tourGuide;

import java.util.*;

import com.jsoniter.output.JsonStream;
import gpsUtil.location.Location;
import org.assertj.core.api.Assertions;
import org.junit.Test;

import gpsUtil.location.VisitedLocation;
import rewardCentral.RewardCentral;
import tourGuide.dockers.rewardsDocker.controller.RewardsServiceController;
import tourGuide.dockers.userDocker.model.UserPreferences;
import tourGuide.helper.InternalTestHelper;
import tourGuide.outputEntities.NearbyAttraction;
import tourGuide.outputEntities.UserLocation;
import tourGuide.remote.gps.GpsRetro;
import tourGuide.remote.RewardsRemote;
import tourGuide.remote.UserRemote;
import tourGuide.dockers.rewardsDocker.service.RewardsService;
import tourGuide.remote.rewards.RewardsRetro;
import tourGuide.remote.user.UserRetro;
import tourGuide.service.TourGuideService;
import tourGuide.dockers.userDocker.controller.UserServiceController;
import tourGuide.dockers.userDocker.service.UserService;
import tourGuide.dockers.userDocker.model.User;
import tripPricer.Provider;

import static org.junit.Assert.*;

public class TestTourGuideService {

	@Test
	public void trackUserLocation() {
		GpsRetro gpsRetro = new GpsRetro();
		UserRetro userRetro = new UserRetro();
		RewardsRetro rewardsRetro = new RewardsRetro();
		InternalTestHelper.setInternalUserNumber(0);
		TourGuideService tourGuideService = new TourGuideService(gpsRetro, rewardsRetro, userRetro);
		
		User user = new User(UUID.randomUUID(), generateRandomTestUsername(), "000", "jon@tourGuide.com");
		VisitedLocation visitedLocation = tourGuideService.trackUserLocation(user);
		tourGuideService.tracker.stopTracking();
		assertTrue(visitedLocation.userId.equals(user.getUserId()));
	}

	@Test
	public void addToVisitedLocation() {
		GpsRetro gpsRetro = new GpsRetro();
		UserRetro userRetro = new UserRetro();
		RewardsRetro rewardsRetro = new RewardsRetro();
		InternalTestHelper.setInternalUserNumber(0);
		TourGuideService tourGuideService = new TourGuideService(gpsRetro, rewardsRetro, userRetro);

		User user = new User(UUID.randomUUID(), generateRandomTestUsername(), "000", "jon@tourGuide.com");

		System.out.println("Adding User " + user.getUserName());
		boolean add = userRetro.addUser(user);

		System.out.println("Track User Location "  + user.getUserName());
		VisitedLocation visitedLocation = tourGuideService.trackUserLocation(user);

		System.out.println("Get All User Locations "  + user.getUserName());
		List<VisitedLocation> startingLocations = userRetro.getVisitedLocationsByUsername(user.getUserName());

		System.out.println("Add New Location "  + user.getUserName());
		VisitedLocation newLocation = new VisitedLocation(user.getUserId(),new Location(123,456),new Date());
		String result = userRetro.addToVisitedLocations(newLocation, user.getUserName());

		System.out.println("Get All User Locations "  + user.getUserName());
		List<VisitedLocation> updatedLocations = userRetro.getVisitedLocationsByUsername(user.getUserName());

		tourGuideService.tracker.stopTracking();

		assertNotEquals(startingLocations.size(),updatedLocations.size());
	}

	@Test
	public void getUserLocation() {
		GpsRetro gpsRetro = new GpsRetro();
		UserRetro userRetro = new UserRetro();
		RewardsRetro rewardsRetro = new RewardsRetro();
		InternalTestHelper.setInternalUserNumber(0);
		TourGuideService tourGuideService = new TourGuideService(gpsRetro, rewardsRetro, userRetro);
		User user = new User(UUID.randomUUID(), generateRandomTestUsername(), "000", "jon@tourGuide.com");
		userRetro.addUser(user);

		VisitedLocation visitedLocation = tourGuideService.getUserLocation(user.getUserName());

		System.out.println("User ID: " + user.getUserId());
		System.out.println("Location User ID: " + visitedLocation.userId);

		tourGuideService.tracker.stopTracking();

		assertTrue(visitedLocation.userId.equals(user.getUserId()));
	}

	@Test
	public void getUsersVisitedLocations() {
		GpsRetro gpsRetro = new GpsRetro();
		UserRetro userRetro = new UserRetro();
		RewardsRetro rewardsRetro = new RewardsRetro();
		InternalTestHelper.setInternalUserNumber(0);
		TourGuideService tourGuideService = new TourGuideService(gpsRetro, rewardsRetro, userRetro);
		User user = new User(UUID.randomUUID(), generateRandomTestUsername(), "000", "jon@tourGuide.com");
		userRetro.addUser(user);


		userRetro.trackAllUserLocations();
		userRetro.trackAllUserLocations();
		VisitedLocation visitedLocation = tourGuideService.getUserLocation(user.getUserName());
		List<VisitedLocation> visitedLocations = userRetro.getVisitedLocationsByUsername(user.getUserName());

		System.out.println("User ID: " + user.getUserId());
		System.out.println("Location User ID: " + visitedLocation.userId);
		System.out.println("VisitedLocations size: " + visitedLocations.size());

		tourGuideService.tracker.stopTracking();

		assertTrue(visitedLocation.userId.equals(user.getUserId()));
	}

	//@TODO: improve asserts
	@Test
	public void addUser() {
		GpsRetro gpsRetro = new GpsRetro();
		UserRetro userRetro = new UserRetro();
		RewardsRetro rewardsRetro = new RewardsRetro();
		InternalTestHelper.setInternalUserNumber(0);
		TourGuideService tourGuideService = new TourGuideService(gpsRetro, rewardsRetro, userRetro);
		
		User user = new User(UUID.randomUUID(), generateRandomTestUsername(), "000", "jon@tourGuide.com");
		User user2 = new User(UUID.randomUUID(), generateRandomTestUsername(), "000", "jon2@tourGuide.com");

		userRetro.addUser(user);
		userRetro.addUser(user2);
		
		User retrivedUser = tourGuideService.getUser(user.getUserName());
		User retrivedUser2 = tourGuideService.getUser(user2.getUserName());

		tourGuideService.tracker.stopTracking();
		
		assertEquals(user.getUserName(), retrivedUser.getUserName());
		assertEquals(user2.getUserName(), retrivedUser2.getUserName());
	}
	
	@Test
	public void getAllUsers() {
		GpsRetro gpsRetro = new GpsRetro();
		UserRetro userRetro = new UserRetro();
		RewardsRetro rewardsRetro = new RewardsRetro();
		InternalTestHelper.setInternalUserNumber(0);
		TourGuideService tourGuideService = new TourGuideService(gpsRetro, rewardsRetro, userRetro);
		
		User user = new User(UUID.randomUUID(), generateRandomTestUsername(), "000", "jon@tourGuide.com");
		User user2 = new User(UUID.randomUUID(), generateRandomTestUsername(), "000", "jon2@tourGuide.com");

		userRetro.addUser(user);
		userRetro.addUser(user2);
		
		List<User> allUsers = userRetro.getAllUsers();

		tourGuideService.tracker.stopTracking();

		List<String> receivedNames = new ArrayList<>();
		allUsers.forEach(v -> receivedNames.add(v.getUserName()));

		assertTrue(receivedNames.contains(user.getUserName()));
		assertTrue(receivedNames.contains(user2.getUserName()));
	}

	@Test
	public void getUser() {
		GpsRetro gpsRetro = new GpsRetro();
		UserRetro userRetro = new UserRetro();
		RewardsRetro rewardsRetro = new RewardsRetro();
		InternalTestHelper.setInternalUserNumber(0);
		TourGuideService tourGuideService = new TourGuideService(gpsRetro, rewardsRetro, userRetro);

		User user = new User(UUID.randomUUID(), generateRandomTestUsername(), "000", "jon@tourGuide.com");
		User user2 = new User(UUID.randomUUID(), generateRandomTestUsername(), "000", "jon2@tourGuide.com");

		userRetro.addUser(user);
		userRetro.addUser(user2);

		User getUser = tourGuideService.getUser(user.getUserName());

		tourGuideService.tracker.stopTracking();

		assertTrue(getUser.getUserName().equals(user.getUserName()));
		assertTrue(getUser.getEmailAddress().equals(user.getEmailAddress()));
	}

	@Test
	public void getAllUsersLocations() {
		GpsRetro gpsRetro = new GpsRetro();
		UserRetro userRetro = new UserRetro();
		RewardsRetro rewardsRetro = new RewardsRetro();
		InternalTestHelper.setInternalUserNumber(0);
		TourGuideService tourGuideService = new TourGuideService(gpsRetro, rewardsRetro, userRetro);

		User user = new User(UUID.randomUUID(), generateRandomTestUsername(), "000", "jon@tourGuide.com");
		userRetro.addUser(user);

		User userTwo = new User(UUID.randomUUID(), generateRandomTestUsername(), "000", "jon2@tourGuide.com");
		userRetro.addUser(userTwo);

		userRetro.trackAllUserLocations();
		int userCount = userRetro.getUserCount();

		List<UserLocation> allUserLocations = tourGuideService.getAllCurrentLocations();

		tourGuideService.tracker.stopTracking();

		System.out.println((allUserLocations.size()));
		System.out.println(JsonStream.serialize(allUserLocations));

		System.out.println("Number of users: " + userCount);
		System.out.println("User locations size: " + allUserLocations.size());

		Assertions.assertThat(allUserLocations)
				.hasSize(userCount)
				.extracting(UserLocation::getUserID)
				.contains(user.getUserId().toString(), userTwo.getUserId().toString());
	}
	
	//@Ignore // Not yet implemented
	@Test
	public void getNearbyAttractions() {
		GpsRetro gpsRetro = new GpsRetro();
		UserRetro userRetro = new UserRetro();
		RewardsRetro rewardsRetro = new RewardsRetro();
		InternalTestHelper.setInternalUserNumber(0);
		TourGuideService tourGuideService = new TourGuideService(gpsRetro, rewardsRetro, userRetro);
		
		User user = new User(UUID.randomUUID(), generateRandomTestUsername(), "000", "jon@tourGuide.com");
		VisitedLocation visitedLocation = tourGuideService.trackUserLocation(user);
		
		//List<Attraction> attractions = tourGuideService.getNearByAttractions(visitedLocation);
		List<NearbyAttraction> minfiveattractions = tourGuideService.getNearByAttractions(visitedLocation);
		
		tourGuideService.tracker.stopTracking();
		
		assertEquals(5, minfiveattractions.size());
	}

	@Test
	public void getTripDeals() {
		GpsRetro gpsRetro = new GpsRetro();
		UserRetro userRetro = new UserRetro();
		RewardsRetro rewardsRetro = new RewardsRetro();
		InternalTestHelper.setInternalUserNumber(0);
		TourGuideService tourGuideService = new TourGuideService(gpsRetro, rewardsRetro, userRetro);
		
		User user = new User(UUID.randomUUID(), generateRandomTestUsername(), "000", "jon@tourGuide.com");
		UserPreferences userPreferences = new UserPreferences();
		userPreferences.setNumberOfAdults(2);
		userPreferences.setNumberOfChildren(3);
		user.setUserPreferences(userPreferences);
		userRetro.addUser(user);

		List<Provider> providers = tourGuideService.getTripDeals(user.getUserName());
		
		tourGuideService.tracker.stopTracking();
		
		assertEquals(5, providers.size());
	}


	private String generateRandomTestUsername() {
		int leftLimit = 97; // letter 'a'
		int rightLimit = 122; // letter 'z'
		int targetStringLength = 10;
		Random random = new Random();

		String generatedString = random.ints(leftLimit, rightLimit + 1)
				.limit(targetStringLength)
				.collect(StringBuilder::new, StringBuilder::appendCodePoint, StringBuilder::append)
				.toString();

		return generatedString;
	}
	
	
}
