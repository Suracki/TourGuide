package tourGuide;

import static org.junit.Assert.*;

import java.util.Date;
import java.util.List;
import java.util.UUID;

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
import tourGuide.service.TourGuideService;
import tourGuide.dockers.userDocker.controller.UserServiceController;
import tourGuide.dockers.userDocker.service.UserService;
import tourGuide.dockers.userDocker.model.User;
import tourGuide.dockers.userDocker.model.UserReward;

public class TestRewardsService {

	@Test
	public void userGetRewards() {
		GpsRetro gpsRetro = new GpsRetro();
		UserRemote userRemote = new UserRemote(new UserServiceController(new UserService(gpsRetro)));
		RewardsRemote rewardsRemote = new RewardsRemote(new RewardsServiceController(new RewardsService(gpsRetro, new RewardCentral(), userRemote)));


		InternalTestHelper.setInternalUserNumber(0);
		TourGuideService tourGuideService = new TourGuideService(gpsRetro, rewardsRemote, userRemote);


		User user = new User(UUID.randomUUID(), "jon", "000", "jon@tourGuide.com");
		Attraction attraction = gpsRetro.getAttractions().get(0);
		user.addToVisitedLocations(new VisitedLocation(user.getUserId(), attraction, new Date()));
		userRemote.addUser(user);

		//tourGuideService.trackUserLocation(user);
		rewardsRemote.calculateRewardsByUsername(user.getUserName());
		List<UserReward> userRewards = user.getUserRewards();
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
	
	@Test
	public void nearAllAttractions() {
		GpsUtil gpsUtil = new GpsUtil();

		GpsRetro gpsRetro = new GpsRetro();
		UserRemote userRemote = new UserRemote(new UserServiceController(new UserService(gpsRetro)));
		RewardsService rewardsService = new RewardsService(gpsRetro, new RewardCentral(), userRemote);
		RewardsRemote rewardsRemote = new RewardsRemote(new RewardsServiceController(rewardsService));
		rewardsService.setProximityBuffer(Integer.MAX_VALUE);
		InternalTestHelper.setInternalUserNumber(1);
		TourGuideService tourGuideService = new TourGuideService(gpsRetro, rewardsRemote, userRemote);


		rewardsService.calculateRewardsByUsername(tourGuideService.getAllUserNames().get(0));
		List<UserReward> userRewards = tourGuideService.getUserRewards(tourGuideService.getAllUserNames().get(0));
		tourGuideService.tracker.stopTracking();

		assertEquals(gpsUtil.getAttractions().size(), userRewards.size());
	}
	
}
