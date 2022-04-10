package tourGuide;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.List;
import java.util.UUID;

import com.jsoniter.output.JsonStream;
import org.assertj.core.api.Assertions;
import org.junit.Test;

import gpsUtil.GpsUtil;
import gpsUtil.location.VisitedLocation;
import rewardCentral.RewardCentral;
import tourGuide.helper.InternalTestHelper;
import tourGuide.outputEntities.NearbyAttraction;
import tourGuide.outputEntities.UserLocation;
import tourGuide.service.GpsService;
import tourGuide.service.RewardsService;
import tourGuide.service.TourGuideService;
import userDocker.service.UserService;
import userDocker.model.User;
import tripPricer.Provider;

public class TestTourGuideService {

	@Test
	public void getUserLocation() {
		GpsUtil gpsUtil = new GpsUtil();
		GpsService gpsService = new GpsService(gpsUtil);
		UserService userService = new UserService();
		RewardsService rewardsService = new RewardsService(gpsService, new RewardCentral(), userService);
		InternalTestHelper.setInternalUserNumber(0);
		TourGuideService tourGuideService = new TourGuideService(gpsService, rewardsService, new UserService());
		
		User user = new User(UUID.randomUUID(), "jon", "000", "jon@tourGuide.com");
		VisitedLocation visitedLocation = tourGuideService.trackUserLocation(user);
		tourGuideService.tracker.stopTracking();
		assertTrue(visitedLocation.userId.equals(user.getUserId()));
	}
	
	@Test
	public void addUser() {
		GpsUtil gpsUtil = new GpsUtil();
		GpsService gpsService = new GpsService(gpsUtil);
		UserService userService = new UserService();
		RewardsService rewardsService = new RewardsService(gpsService, new RewardCentral(), userService);
		InternalTestHelper.setInternalUserNumber(0);
		TourGuideService tourGuideService = new TourGuideService(gpsService, rewardsService, userService);
		
		User user = new User(UUID.randomUUID(), "jon", "000", "jon@tourGuide.com");
		User user2 = new User(UUID.randomUUID(), "jon2", "000", "jon2@tourGuide.com");

		userService.addUser(user);
		userService.addUser(user2);
		
		User retrivedUser = tourGuideService.getUser(user.getUserName());
		User retrivedUser2 = tourGuideService.getUser(user2.getUserName());

		tourGuideService.tracker.stopTracking();
		
		assertEquals(user, retrivedUser);
		assertEquals(user2, retrivedUser2);
	}
	
	@Test
	public void getAllUsers() {
		GpsUtil gpsUtil = new GpsUtil();
		GpsService gpsService = new GpsService(gpsUtil);
		UserService userService = new UserService();
		RewardsService rewardsService = new RewardsService(gpsService, new RewardCentral(), userService);
		InternalTestHelper.setInternalUserNumber(0);
		TourGuideService tourGuideService = new TourGuideService(gpsService, rewardsService, userService);
		
		User user = new User(UUID.randomUUID(), "jon", "000", "jon@tourGuide.com");
		User user2 = new User(UUID.randomUUID(), "jon2", "000", "jon2@tourGuide.com");

		userService.addUser(user);
		userService.addUser(user2);
		
		List<User> allUsers = tourGuideService.getAllUsers();

		tourGuideService.tracker.stopTracking();
		
		assertTrue(allUsers.contains(user));
		assertTrue(allUsers.contains(user2));
	}

	@Test
	public void getAllUsersLocations() {
		GpsUtil gpsUtil = new GpsUtil();
		GpsService gpsService = new GpsService(gpsUtil);
		UserService userService = new UserService();
		RewardsService rewardsService = new RewardsService(gpsService, new RewardCentral(), userService);
		InternalTestHelper.setInternalUserNumber(0);
		TourGuideService tourGuideService = new TourGuideService(gpsService, rewardsService, userService);

		User user = new User(UUID.randomUUID(), "jon", "000", "jon@tourGuide.com");
		VisitedLocation visitedLocationOne = tourGuideService.trackUserLocation(user);
		user.addToVisitedLocations(visitedLocationOne);

		User userTwo = new User(UUID.randomUUID(), "jon2", "000", "jon2@tourGuide.com");
		VisitedLocation visitedLocationTwo = tourGuideService.trackUserLocation(userTwo);
		userTwo.addToVisitedLocations(visitedLocationTwo);

		userService.addUser(user);
		userService.addUser(userTwo);

		List<UserLocation> allUserLocations = tourGuideService.getAllCurrentLocations();


		tourGuideService.tracker.stopTracking();

		System.out.println((allUserLocations.size()));
		System.out.println(JsonStream.serialize(allUserLocations));

		Assertions.assertThat(allUserLocations)
				.hasSize(2)
				.extracting(UserLocation::getUserID)
				.containsExactlyInAnyOrder(user.getUserId().toString(), userTwo.getUserId().toString());
	}
	
	@Test
	public void trackUser() {
		GpsUtil gpsUtil = new GpsUtil();
		GpsService gpsService = new GpsService(gpsUtil);
		UserService userService = new UserService();
		RewardsService rewardsService = new RewardsService(gpsService, new RewardCentral(), userService);
		InternalTestHelper.setInternalUserNumber(0);
		TourGuideService tourGuideService = new TourGuideService(gpsService, rewardsService, new UserService());
		
		User user = new User(UUID.randomUUID(), "jon", "000", "jon@tourGuide.com");
		VisitedLocation visitedLocation = tourGuideService.trackUserLocation(user);
		
		tourGuideService.tracker.stopTracking();
		
		assertEquals(user.getUserId(), visitedLocation.userId);
	}
	
	//@Ignore // Not yet implemented
	@Test
	public void getNearbyAttractions() {
		GpsUtil gpsUtil = new GpsUtil();
		GpsService gpsService = new GpsService(gpsUtil);
		UserService userService = new UserService();
		RewardsService rewardsService = new RewardsService(gpsService, new RewardCentral(), userService);
		InternalTestHelper.setInternalUserNumber(0);
		TourGuideService tourGuideService = new TourGuideService(gpsService, rewardsService, new UserService());
		
		User user = new User(UUID.randomUUID(), "jon", "000", "jon@tourGuide.com");
		VisitedLocation visitedLocation = tourGuideService.trackUserLocation(user);
		
		//List<Attraction> attractions = tourGuideService.getNearByAttractions(visitedLocation);
		List<NearbyAttraction> minfiveattractions = tourGuideService.getNearByAttractions(visitedLocation);
		
		tourGuideService.tracker.stopTracking();
		
		assertEquals(5, minfiveattractions.size());
	}

	@Test
	public void getTripDeals() {
		GpsUtil gpsUtil = new GpsUtil();
		GpsService gpsService = new GpsService(gpsUtil);
		UserService userService = new UserService();
		RewardsService rewardsService = new RewardsService(gpsService, new RewardCentral(), userService);
		InternalTestHelper.setInternalUserNumber(0);
		TourGuideService tourGuideService = new TourGuideService(gpsService, rewardsService, userService);
		
		User user = new User(UUID.randomUUID(), "jon", "000", "jon@tourGuide.com");
		userService.addUser(user);

		List<Provider> providers = tourGuideService.getTripDeals(user.getUserName());
		
		tourGuideService.tracker.stopTracking();
		
		assertEquals(5, providers.size());
	}
	
	
}
