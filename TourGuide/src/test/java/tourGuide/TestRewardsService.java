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
import tourGuide.helper.InternalTestHelper;
import tourGuide.remote.GpsRemote;
import tourGuide.remote.RewardsRemote;
import tourGuide.remote.UserRemote;
import gpsDocker.service.GpsService;
import rewardsDocker.service.RewardsService;
import tourGuide.service.TourGuideService;
import userDocker.service.UserService;
import userDocker.model.User;
import userDocker.model.UserReward;

public class TestRewardsService {

	@Test
	public void userGetRewards() {
		GpsRemote gpsRemote = new GpsRemote(new GpsService(new GpsUtil()));
		UserRemote userRemote = new UserRemote(new UserService(gpsRemote));
		RewardsRemote rewardsRemote = new RewardsRemote(new RewardsService(gpsRemote, new RewardCentral(), userRemote));


		InternalTestHelper.setInternalUserNumber(0);
		TourGuideService tourGuideService = new TourGuideService(gpsRemote, rewardsRemote, userRemote);


		User user = new User(UUID.randomUUID(), "jon", "000", "jon@tourGuide.com");
		Attraction attraction = gpsRemote.getAttractions().get(0);
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
		GpsService gpsService = new GpsService(new GpsUtil());
		GpsRemote gpsRemote = new GpsRemote(gpsService);
		UserService userService = new UserService(gpsRemote);
		UserRemote userRemote = new UserRemote(userService);
		RewardsService rewardsService = new RewardsService(gpsRemote, new RewardCentral(), userRemote);
		Attraction attraction = gpsService.getAttractions().get(0);
		assertTrue(rewardsService.isWithinAttractionProximity(attraction, attraction));
	}
	
	//@Ignore // Needs fixed - can throw ConcurrentModificationException
	@Test
	public void nearAllAttractions() {
		GpsUtil gpsUtil = new GpsUtil();
		GpsService gpsService = new GpsService(gpsUtil);
		GpsRemote gpsRemote = new GpsRemote(gpsService);
		UserService userService = new UserService(gpsRemote);
		UserRemote userRemote = new UserRemote(userService);
		RewardsService rewardsService = new RewardsService(gpsRemote, new RewardCentral(), userRemote);
		RewardsRemote rewardsRemote = new RewardsRemote(rewardsService);
		rewardsService.setProximityBuffer(Integer.MAX_VALUE);
		InternalTestHelper.setInternalUserNumber(1);
		TourGuideService tourGuideService = new TourGuideService(gpsRemote, rewardsRemote, userRemote);


		rewardsService.calculateRewardsByUsername(tourGuideService.getAllUserNames().get(0));
		List<UserReward> userRewards = tourGuideService.getUserRewards(tourGuideService.getAllUserNames().get(0));
		tourGuideService.tracker.stopTracking();

		assertEquals(gpsUtil.getAttractions().size(), userRewards.size());
	}
	
}
