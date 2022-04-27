package tourGuide;

import static org.junit.Assert.*;

import java.util.Date;
import java.util.List;
import java.util.UUID;

import org.junit.Ignore;
import org.junit.Test;

import gpsUtil.GpsUtil;
import gpsUtil.location.Attraction;
import gpsUtil.location.VisitedLocation;
import rewardCentral.RewardCentral;
import tourGuide.helper.InternalTestHelper;
import tourGuide.service.*;
import tourGuide.user.User;
import tourGuide.user.UserReward;
import tripPricer.TripPricer;

public class TestRewardsService {

	@Test
	public void userGetRewards() {
		GpsUtil gpsUtil = new GpsUtil();
		GpsService gpsService = new GpsService(gpsUtil);
		UserService userService = new UserService();
		TripService tripService = new TripService(new TripPricer());
		RewardsService rewardsService = new RewardsService(gpsService, new RewardCentral(), userService);

		InternalTestHelper.setInternalUserNumber(0);
		TourGuideService tourGuideService = new TourGuideService(gpsService, rewardsService, userService, tripService);
		
		User user = new User(UUID.randomUUID(), "jon", "000", "jon@tourGuide.com");
		Attraction attraction = gpsUtil.getAttractions().get(0);
		user.addToVisitedLocations(new VisitedLocation(user.getUserId(), attraction, new Date()));
		tourGuideService.trackUserLocation(user);
		List<UserReward> userRewards = user.getUserRewards();
		System.out.println("Size " + userRewards.size());
		tourGuideService.tracker.stopTracking();
		assertTrue(userRewards.size() == 2);
	}
	
	@Test
	public void isWithinAttractionProximity() {
		GpsService gpsService = new GpsService(new GpsUtil());
		UserService userService = new UserService();
		RewardsService rewardsService = new RewardsService(gpsService, new RewardCentral(), userService);
		Attraction attraction = gpsService.getAttractions().get(0);
		assertTrue(rewardsService.isWithinAttractionProximity(attraction, attraction));
	}
	
	@Test
	public void nearAllAttractions() {
		GpsUtil gpsUtil = new GpsUtil();
		GpsService gpsService = new GpsService(gpsUtil);
		UserService userService = new UserService();
		TripService tripService = new TripService(new TripPricer());
		RewardsService rewardsService = new RewardsService(gpsService, new RewardCentral(), userService);
		rewardsService.setProximityBuffer(Integer.MAX_VALUE);

		InternalTestHelper.setInternalUserNumber(1);
		TourGuideService tourGuideService = new TourGuideService(gpsService, rewardsService, userService, tripService);
		
		rewardsService.calculateRewards(tourGuideService.getAllUsers().get(0));
		List<UserReward> userRewards = tourGuideService.getUserRewards(tourGuideService.getAllUsers().get(0));
		tourGuideService.tracker.stopTracking();

		assertEquals(gpsUtil.getAttractions().size(), userRewards.size());
	}
	
}
