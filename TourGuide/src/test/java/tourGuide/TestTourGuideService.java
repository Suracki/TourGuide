package tourGuide;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.List;
import java.util.UUID;

import com.jsoniter.output.JsonStream;
import org.assertj.core.api.Assertions;
import org.junit.Test;

import gpsUtil.location.VisitedLocation;
import rewardCentral.RewardCentral;
import tourGuide.dockers.rewardsDocker.controller.RewardsServiceController;
import tourGuide.helper.InternalTestHelper;
import tourGuide.outputEntities.NearbyAttraction;
import tourGuide.outputEntities.UserLocation;
import tourGuide.remote.gps.GpsRetro;
import tourGuide.remote.RewardsRemote;
import tourGuide.remote.UserRemote;
import tourGuide.dockers.rewardsDocker.service.RewardsService;
import tourGuide.service.TourGuideService;
import tourGuide.dockers.userDocker.controller.UserServiceController;
import tourGuide.dockers.userDocker.service.UserService;
import tourGuide.dockers.userDocker.model.User;
import tripPricer.Provider;

public class TestTourGuideService {

	@Test
	public void trackUserLocation() {
		GpsRetro gpsRetro = new GpsRetro();
		UserRemote userRemote = new UserRemote(new UserServiceController(new UserService(gpsRetro)));
		RewardsRemote rewardsRemote = new RewardsRemote(new RewardsServiceController(new RewardsService(gpsRetro, new RewardCentral(), userRemote)));
		InternalTestHelper.setInternalUserNumber(0);
		TourGuideService tourGuideService = new TourGuideService(gpsRetro, rewardsRemote, userRemote);
		
		User user = new User(UUID.randomUUID(), "jon", "000", "jon@tourGuide.com");
		VisitedLocation visitedLocation = tourGuideService.trackUserLocation(user);
		tourGuideService.tracker.stopTracking();
		assertTrue(visitedLocation.userId.equals(user.getUserId()));
	}

	@Test
	public void getUserLocation() {
		GpsRetro gpsRetro = new GpsRetro();
		UserRemote userRemote = new UserRemote(new UserServiceController(new UserService(gpsRetro)));
		RewardsRemote rewardsRemote = new RewardsRemote(new RewardsServiceController(new RewardsService(gpsRetro, new RewardCentral(), userRemote)));
		InternalTestHelper.setInternalUserNumber(0);
		TourGuideService tourGuideService = new TourGuideService(gpsRetro, rewardsRemote, userRemote);
		User user = new User(UUID.randomUUID(), "jon", "000", "jon@tourGuide.com");
		userRemote.addUser(user);
		VisitedLocation visitedLocation = tourGuideService.getUserLocation(user.getUserName());
		tourGuideService.tracker.stopTracking();
		assertTrue(visitedLocation.userId.equals(user.getUserId()));
	}

	//@TODO: improve asserts
	@Test
	public void addUser() {
		GpsRetro gpsRetro = new GpsRetro();
		UserRemote userRemote = new UserRemote(new UserServiceController(new UserService(gpsRetro)));
		RewardsRemote rewardsRemote = new RewardsRemote(new RewardsServiceController(new RewardsService(gpsRetro, new RewardCentral(), userRemote)));
		InternalTestHelper.setInternalUserNumber(0);
		TourGuideService tourGuideService = new TourGuideService(gpsRetro, rewardsRemote, userRemote);
		
		User user = new User(UUID.randomUUID(), "jon", "000", "jon@tourGuide.com");
		User user2 = new User(UUID.randomUUID(), "jon2", "000", "jon2@tourGuide.com");

		userRemote.addUser(user);
		userRemote.addUser(user2);
		
		User retrivedUser = tourGuideService.getUser(user.getUserName());
		User retrivedUser2 = tourGuideService.getUser(user2.getUserName());

		tourGuideService.tracker.stopTracking();
		
		assertEquals(user.getUserName(), retrivedUser.getUserName());
		assertEquals(user2.getUserName(), retrivedUser2.getUserName());
	}
	
	@Test
	public void getAllUsers() {
		GpsRetro gpsRetro = new GpsRetro();
		UserService userService = new UserService(gpsRetro);
		UserRemote userRemote = new UserRemote(new UserServiceController(userService));
		RewardsRemote rewardsRemote = new RewardsRemote(new RewardsServiceController(new RewardsService(gpsRetro, new RewardCentral(), userRemote)));
		InternalTestHelper.setInternalUserNumber(0);
		TourGuideService tourGuideService = new TourGuideService(gpsRetro, rewardsRemote, userRemote);
		
		User user = new User(UUID.randomUUID(), "jon", "000", "jon@tourGuide.com");
		User user2 = new User(UUID.randomUUID(), "jon2", "000", "jon2@tourGuide.com");

		userRemote.addUser(user);
		userRemote.addUser(user2);
		
		List<User> allUsers = userService.getAllUsers();

		tourGuideService.tracker.stopTracking();
		
		assertTrue(allUsers.contains(user));
		assertTrue(allUsers.contains(user2));
	}

	@Test
	public void getUser() {
		GpsRetro gpsRetro = new GpsRetro();
		UserRemote userRemote = new UserRemote(new UserServiceController(new UserService(gpsRetro)));
		RewardsRemote rewardsRemote = new RewardsRemote(new RewardsServiceController(new RewardsService(gpsRetro, new RewardCentral(), userRemote)));
		InternalTestHelper.setInternalUserNumber(0);
		TourGuideService tourGuideService = new TourGuideService(gpsRetro, rewardsRemote, userRemote);

		User user = new User(UUID.randomUUID(), "jon", "000", "jon@tourGuide.com");
		User user2 = new User(UUID.randomUUID(), "jon2", "000", "jon2@tourGuide.com");

		userRemote.addUser(user);
		userRemote.addUser(user2);

		User getUser = tourGuideService.getUser("jon");

		tourGuideService.tracker.stopTracking();

		assertTrue(getUser.getUserName().equals("jon"));
		assertTrue(getUser.getEmailAddress().equals("jon@tourGuide.com"));
	}

	@Test
	public void getAllUsersLocations() {
		GpsRetro gpsRetro = new GpsRetro();
		UserRemote userRemote = new UserRemote(new UserServiceController(new UserService(gpsRetro)));
		RewardsRemote rewardsRemote = new RewardsRemote(new RewardsServiceController(new RewardsService(gpsRetro, new RewardCentral(), userRemote)));
		InternalTestHelper.setInternalUserNumber(0);
		TourGuideService tourGuideService = new TourGuideService(gpsRetro, rewardsRemote, userRemote);

		User user = new User(UUID.randomUUID(), "jon", "000", "jon@tourGuide.com");
		VisitedLocation visitedLocationOne = tourGuideService.trackUserLocation(user);
		user.addToVisitedLocations(visitedLocationOne);

		User userTwo = new User(UUID.randomUUID(), "jon2", "000", "jon2@tourGuide.com");
		VisitedLocation visitedLocationTwo = tourGuideService.trackUserLocation(userTwo);
		userTwo.addToVisitedLocations(visitedLocationTwo);

		userRemote.addUser(user);
		userRemote.addUser(userTwo);

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
		GpsRetro gpsRetro = new GpsRetro();
		UserRemote userRemote = new UserRemote(new UserServiceController(new UserService(gpsRetro)));
		RewardsRemote rewardsRemote = new RewardsRemote(new RewardsServiceController(new RewardsService(gpsRetro, new RewardCentral(), userRemote)));
		InternalTestHelper.setInternalUserNumber(0);
		TourGuideService tourGuideService = new TourGuideService(gpsRetro, rewardsRemote, userRemote);
		
		User user = new User(UUID.randomUUID(), "jon", "000", "jon@tourGuide.com");
		VisitedLocation visitedLocation = tourGuideService.trackUserLocation(user);
		
		tourGuideService.tracker.stopTracking();
		
		assertEquals(user.getUserId(), visitedLocation.userId);
	}
	
	//@Ignore // Not yet implemented
	@Test
	public void getNearbyAttractions() {
		GpsRetro gpsRetro = new GpsRetro();
		UserRemote userRemote = new UserRemote(new UserServiceController(new UserService(gpsRetro)));
		RewardsRemote rewardsRemote = new RewardsRemote(new RewardsServiceController(new RewardsService(gpsRetro, new RewardCentral(), userRemote)));
		InternalTestHelper.setInternalUserNumber(0);
		TourGuideService tourGuideService = new TourGuideService(gpsRetro, rewardsRemote, userRemote);
		
		User user = new User(UUID.randomUUID(), "jon", "000", "jon@tourGuide.com");
		VisitedLocation visitedLocation = tourGuideService.trackUserLocation(user);
		
		//List<Attraction> attractions = tourGuideService.getNearByAttractions(visitedLocation);
		List<NearbyAttraction> minfiveattractions = tourGuideService.getNearByAttractions(visitedLocation);
		
		tourGuideService.tracker.stopTracking();
		
		assertEquals(5, minfiveattractions.size());
	}

	@Test
	public void getTripDeals() {
		GpsRetro gpsRetro = new GpsRetro();
		UserRemote userRemote = new UserRemote(new UserServiceController(new UserService(gpsRetro)));
		RewardsRemote rewardsRemote = new RewardsRemote(new RewardsServiceController(new RewardsService(gpsRetro, new RewardCentral(), userRemote)));
		InternalTestHelper.setInternalUserNumber(0);
		TourGuideService tourGuideService = new TourGuideService(gpsRetro, rewardsRemote, userRemote);
		
		User user = new User(UUID.randomUUID(), "jon", "000", "jon@tourGuide.com");
		userRemote.addUser(user);

		List<Provider> providers = tourGuideService.getTripDeals(user.getUserName());
		
		tourGuideService.tracker.stopTracking();
		
		assertEquals(5, providers.size());
	}
	
	
}
