package tourGuide;

import static org.junit.Assert.*;

import java.util.Date;
import java.util.List;
import java.util.Random;
import java.util.UUID;

import org.junit.Ignore;
import org.junit.Test;

import gpsUtil.GpsUtil;
import gpsUtil.location.Attraction;
import gpsUtil.location.VisitedLocation;
import rewardCentral.RewardCentral;
import tourGuide.dockers.rewardsDocker.controller.RewardsServiceController;
import tourGuide.helper.InternalTestHelper;
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
import tourGuide.dockers.userDocker.model.UserReward;

public class TestRewardsService {

	@Test
	public void userGetRewards() {
		GpsRetro gpsRetro = new GpsRetro();
		UserRetro userRetro = new UserRetro();
		RewardsRetro rewardsRetro = new RewardsRetro();


		InternalTestHelper.setInternalUserNumber(0);
		TourGuideService tourGuideService = new TourGuideService(gpsRetro, rewardsRetro, userRetro);


		User user = new User(UUID.randomUUID(), generateRandomTestUsername(), "000", "jon@tourGuide.com");
		System.out.println("Adding user: " + user.getUserId());
		userRetro.addUser(user);

		Attraction attraction = gpsRetro.getAttractions().get(0);
		System.out.println("Got attraction: " + attraction.attractionName);

		VisitedLocation visitedLocation = new VisitedLocation(user.getUserId(), attraction, new Date());
		System.out.println("Adding visited location to : " + user.getUserName() + " - " + user.getUserId());
		String adding = userRetro.addToVisitedLocations(visitedLocation, user.getUserName());
		String adding2 = userRetro.addToVisitedLocations(new VisitedLocation(user.getUserId(), attraction, new Date()), user.getUserName());
		String adding3 = userRetro.addToVisitedLocations(new VisitedLocation(user.getUserId(), attraction, new Date()), user.getUserName());

		System.out.println("Added location: " + adding);

		System.out.println("Getting all visited locations for user");
		List<VisitedLocation> locations = userRetro.getVisitedLocationsByUsername(user.getUserName());
		System.out.println("Got "+ locations.size() +  " visited locations for user");


		VisitedLocation visitedLocation1 = tourGuideService.trackUserLocation(user);
		System.out.println("Tracked User Location: " + visitedLocation1.location);
		System.out.println("Calling Calulate Rewards...");
		rewardsRetro.calculateRewardsByUsername(user.getUserName());
		System.out.println("Getting User Rewards...");
		List<UserReward> userRewards = userRetro.getUserRewardsByUsername(user.getUserName());
		System.out.println("Size: " + userRewards.size());

		//System.out.println("Adding Reward");
		//boolean add = userRetro.addUserReward(user.getUserName(), visitedLocation1, new Attraction("NAME", "CITY", "STATE", 123, 456),50);
		//System.out.println("Added Reward? " + add);

		tourGuideService.tracker.stopTracking();

		assertTrue(userRewards.size() == 2);
	}
	
	@Test
	public void isWithinAttractionProximity() {
		GpsRetro gpsRetro = new GpsRetro();
		UserRemote userRemote = new UserRemote(new UserServiceController(new UserService(gpsRetro)));
		RewardsService rewardsService = new RewardsService(gpsRetro, new RewardCentral(), userRemote);
		Attraction attraction = gpsRetro.getAttractions().get(0);
		assertTrue(rewardsService.isWithinAttractionProximity(attraction, attraction));
	}

	//TODO: update to call rewardsRetro
	@Ignore
	@Test
	public void nearAllAttractions() {
		GpsUtil gpsUtil = new GpsUtil();
		GpsRetro gpsRetro = new GpsRetro();
		UserRetro userRetro = new UserRetro();
		RewardsRetro rewardsRetro = new RewardsRetro();
		//rewardsRetro.setProximityBuffer(Integer.MAX_VALUE);
		InternalTestHelper.setInternalUserNumber(1);
		TourGuideService tourGuideService = new TourGuideService(gpsRetro, rewardsRetro, userRetro);


		rewardsRetro.calculateRewardsByUsername(tourGuideService.getAllUserNames().get(0));
		List<UserReward> userRewards = tourGuideService.getUserRewards(tourGuideService.getAllUserNames().get(0));
		tourGuideService.tracker.stopTracking();

		assertEquals(gpsUtil.getAttractions().size(), userRewards.size());
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
