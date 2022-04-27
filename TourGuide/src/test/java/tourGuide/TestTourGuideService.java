package tourGuide;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.List;
import java.util.UUID;

import com.jsoniter.output.JsonStream;
import org.assertj.core.api.Assertions;
import org.junit.Ignore;
import org.junit.Test;

import gpsUtil.GpsUtil;
import gpsUtil.location.Attraction;
import gpsUtil.location.VisitedLocation;
import org.w3c.dom.Attr;
import rewardCentral.RewardCentral;
import tourGuide.helper.InternalTestHelper;
import tourGuide.outputEntities.NearbyAttraction;
import tourGuide.outputEntities.UserLocation;
import tourGuide.service.*;
import tourGuide.user.User;
import tripPricer.Provider;
import tripPricer.TripPricer;

public class TestTourGuideService {

	@Test
	public void getUserLocation() {
		GpsUtil gpsUtil = new GpsUtil();
		GpsService gpsService = new GpsService(gpsUtil);
		UserService userService = new UserService();
		TripService tripService = new TripService(new TripPricer());
		RewardsService rewardsService = new RewardsService(gpsService, new RewardCentral(), userService);
		InternalTestHelper.setInternalUserNumber(0);
		TourGuideService tourGuideService = new TourGuideService(gpsService, rewardsService, userService, tripService);
		
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
		TripService tripService = new TripService(new TripPricer());
		RewardsService rewardsService = new RewardsService(gpsService, new RewardCentral(), userService);
		InternalTestHelper.setInternalUserNumber(0);
		TourGuideService tourGuideService = new TourGuideService(gpsService, rewardsService, userService, tripService);
		
		User user = new User(UUID.randomUUID(), "jon", "000", "jon@tourGuide.com");
		User user2 = new User(UUID.randomUUID(), "jon2", "000", "jon2@tourGuide.com");

		tourGuideService.addUser(user);
		tourGuideService.addUser(user2);
		
		User retrivedUser = tourGuideService.getUserByUsername(user.getUserName());
		User retrivedUser2 = tourGuideService.getUserByUsername(user2.getUserName());

		tourGuideService.tracker.stopTracking();
		
		assertEquals(user, retrivedUser);
		assertEquals(user2, retrivedUser2);
	}
	
	@Test
	public void getAllUsers() {
		GpsUtil gpsUtil = new GpsUtil();
		GpsService gpsService = new GpsService(gpsUtil);
		UserService userService = new UserService();
		TripService tripService = new TripService(new TripPricer());
		RewardsService rewardsService = new RewardsService(gpsService, new RewardCentral(), userService);
		InternalTestHelper.setInternalUserNumber(0);
		TourGuideService tourGuideService = new TourGuideService(gpsService, rewardsService, userService, tripService);
		
		User user = new User(UUID.randomUUID(), "jon", "000", "jon@tourGuide.com");
		User user2 = new User(UUID.randomUUID(), "jon2", "000", "jon2@tourGuide.com");

		tourGuideService.addUser(user);
		tourGuideService.addUser(user2);
		
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
		TripService tripService = new TripService(new TripPricer());
		RewardsService rewardsService = new RewardsService(gpsService, new RewardCentral(), userService);
		InternalTestHelper.setInternalUserNumber(0);
		TourGuideService tourGuideService = new TourGuideService(gpsService, rewardsService, userService, tripService);

		User user = new User(UUID.randomUUID(), "jon", "000", "jon@tourGuide.com");
		VisitedLocation visitedLocationOne = tourGuideService.trackUserLocation(user);
		user.addToVisitedLocations(visitedLocationOne);

		User userTwo = new User(UUID.randomUUID(), "jon2", "000", "jon2@tourGuide.com");
		VisitedLocation visitedLocationTwo = tourGuideService.trackUserLocation(userTwo);
		userTwo.addToVisitedLocations(visitedLocationTwo);

		tourGuideService.addUser(user);
		tourGuideService.addUser(userTwo);

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
		TripService tripService = new TripService(new TripPricer());
		RewardsService rewardsService = new RewardsService(gpsService, new RewardCentral(), userService);
		InternalTestHelper.setInternalUserNumber(0);
		TourGuideService tourGuideService = new TourGuideService(gpsService, rewardsService, userService, tripService);
		
		User user = new User(UUID.randomUUID(), "jon", "000", "jon@tourGuide.com");
		VisitedLocation visitedLocation = tourGuideService.trackUserLocation(user);
		
		tourGuideService.tracker.stopTracking();
		
		assertEquals(user.getUserId(), visitedLocation.userId);
	}

	@Test
	public void getNearbyAttractions() {
		GpsUtil gpsUtil = new GpsUtil();
		GpsService gpsService = new GpsService(gpsUtil);
		UserService userService = new UserService();
		TripService tripService = new TripService(new TripPricer());
		RewardsService rewardsService = new RewardsService(gpsService, new RewardCentral(), userService);
		InternalTestHelper.setInternalUserNumber(0);
		TourGuideService tourGuideService = new TourGuideService(gpsService, rewardsService, userService, tripService);
		
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
		TripService tripService = new TripService(new TripPricer());
		RewardsService rewardsService = new RewardsService(gpsService, new RewardCentral(), userService);
		InternalTestHelper.setInternalUserNumber(0);
		TourGuideService tourGuideService = new TourGuideService(gpsService, rewardsService, userService, tripService);
		
		User user = new User(UUID.randomUUID(), "jon", "000", "jon@tourGuide.com");
		userService.addUser(user);

		List<Provider> providers = tourGuideService.getTripDeals(user.getUserName());
		
		tourGuideService.tracker.stopTracking();
		
		assertEquals(5, providers.size());
	}

	@Test
	public void tripDealsUsesUserPreferencesWhenObtainingPrices() {
		GpsUtil gpsUtil = new GpsUtil();
		GpsService gpsService = new GpsService(gpsUtil);
		UserService userService = new UserService();
		TripService tripService = new TripService(new TripPricer());
		RewardsService rewardsService = new RewardsService(gpsService, new RewardCentral(), userService);
		InternalTestHelper.setInternalUserNumber(0);
		TourGuideService tourGuideService = new TourGuideService(gpsService, rewardsService, userService, tripService);

		User userGroup = new User(UUID.randomUUID(), "jon", "000", "jon@tourGuide.com");
		userGroup.getUserPreferences().setNumberOfAdults(10);
		userGroup.getUserPreferences().setNumberOfChildren(30);
		userGroup.getUserPreferences().setAttractionProximity(50);
		userService.addUser(userGroup);

		User userSolo = new User(UUID.randomUUID(), "john", "111", "john@tourGuide.com");
		userSolo.getUserPreferences().setNumberOfAdults(1);
		userSolo.getUserPreferences().setNumberOfChildren(0);
		userSolo.getUserPreferences().setAttractionProximity(500);
		userService.addUser(userSolo);

		List<Provider> providersGroup = tourGuideService.getTripDeals(userGroup.getUserName());
		List<Provider> providersSolo = tourGuideService.getTripDeals(userSolo.getUserName());

		System.out.println("Number of providers solo:" + providersSolo);
		System.out.println("Number of providers family:" + providersGroup);

		int groupPrice = 0;
		int soloPrice = 0;
		for (Provider p : providersGroup) {
			groupPrice += p.price;
		}
		for (Provider p : providersSolo) {
			soloPrice += p.price;
		}

		System.out.println("Number of providers solo:" + providersSolo.size());
		System.out.println("Number of providers family:" + providersGroup.size());

		tourGuideService.tracker.stopTracking();

		System.out.println("Total price of providers solo:" + soloPrice);
		System.out.println("Total price of providers family:" + groupPrice);


		assertEquals(5, providersSolo.size());
		assertEquals(5, providersGroup.size());
		assertTrue(groupPrice > soloPrice);
	}
	
	
}
