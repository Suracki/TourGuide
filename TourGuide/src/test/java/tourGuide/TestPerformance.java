package tourGuide;

import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.concurrent.*;

import org.apache.commons.lang3.time.StopWatch;
import org.junit.Ignore;
import org.junit.Test;

import gpsUtil.GpsUtil;
import gpsUtil.location.Attraction;
import gpsUtil.location.VisitedLocation;
import rewardCentral.RewardCentral;
import tourGuide.helper.InternalTestHelper;
import tourGuide.service.*;
import tourGuide.user.User;
import tripPricer.TripPricer;


public class TestPerformance {
	
	/*
	 * A note on performance improvements:
	 *     
	 *     The number of users generated for the high volume tests can be easily adjusted via this method:
	 *     
	 *     		InternalTestHelper.setInternalUserNumber(100000);
	 *     
	 *     
	 *     These tests can be modified to suit new solutions, just as long as the performance metrics
	 *     at the end of the tests remains consistent. 
	 * 
	 *     These are performance metrics that we are trying to hit:
	 *     
	 *     highVolumeTrackLocation: 100,000 users within 15 minutes:
	 *     		assertTrue(TimeUnit.MINUTES.toSeconds(15) >= TimeUnit.MILLISECONDS.toSeconds(stopWatch.getTime()));
     *
     *     highVolumeGetRewards: 100,000 users within 20 minutes:
	 *          assertTrue(TimeUnit.MINUTES.toSeconds(20) >= TimeUnit.MILLISECONDS.toSeconds(stopWatch.getTime()));
	 */

	private static final int NUMBER_OF_TEST_USERS = 100000;
	
	@Test
	public void highVolumeTrackLocation() {
		GpsService gpsService = new GpsService(new GpsUtil());
		UserService userService = new UserService();
		TripService tripService = new TripService(new TripPricer());
		RewardsService rewardsService = new RewardsService(gpsService, new RewardCentral(), userService);
		// Users should be incremented up to 100,000, and test finishes within 15 minutes
		InternalTestHelper.setInternalUserNumber(NUMBER_OF_TEST_USERS);
		TourGuideService tourGuideService = new TourGuideService(gpsService, rewardsService, userService, tripService);

		StopWatch stopWatch = new StopWatch();
		stopWatch.start();
		tourGuideService.trackAllUserLocations();
		stopWatch.stop();
		tourGuideService.tracker.stopTracking();

		System.out.println("highVolumeTrackLocation: Time Elapsed: " + TimeUnit.MILLISECONDS.toSeconds(stopWatch.getTime()) + " seconds.");
		assertTrue(TimeUnit.MINUTES.toSeconds(15) >= TimeUnit.MILLISECONDS.toSeconds(stopWatch.getTime()));
	}

	@Test
	public void highVolumeTrackLocationAndProcessConc() {
		GpsService gpsService = new GpsService(new GpsUtil());
		UserService userService = new UserService();
		TripService tripService = new TripService(new TripPricer());
		RewardsService rewardsService = new RewardsService(gpsService, new RewardCentral(), userService);
		// Users should be incremented up to 100,000, and test finishes within 15 minutes
		InternalTestHelper.setInternalUserNumber(NUMBER_OF_TEST_USERS);
		TourGuideService tourGuideService = new TourGuideService(gpsService, rewardsService, userService, tripService);

		StopWatch stopWatch = new StopWatch();
		stopWatch.start();
		tourGuideService.trackAllUserLocations();
		tourGuideService.processAllUserRewards();
		stopWatch.stop();
		tourGuideService.tracker.stopTracking();

		System.out.println("highVolumeTrackLocation: Time Elapsed: " + TimeUnit.MILLISECONDS.toSeconds(stopWatch.getTime()) + " seconds.");
		assertTrue(TimeUnit.MINUTES.toSeconds(15) >= TimeUnit.MILLISECONDS.toSeconds(stopWatch.getTime()));
	}

	@Test
	public void highVolumeGetRewards() {
		GpsService gpsService = new GpsService(new GpsUtil());
		UserService userService = new UserService();
		TripService tripService = new TripService(new TripPricer());
		RewardsService rewardsService = new RewardsService(gpsService, new RewardCentral(), userService);

		// Users should be incremented up to 100,000, and test finishes within 20 minutes
		InternalTestHelper.setInternalUserNumber(NUMBER_OF_TEST_USERS);
		StopWatch stopWatch = new StopWatch();
		stopWatch.start();
		TourGuideService tourGuideService = new TourGuideService(gpsService, rewardsService, userService, tripService);

		System.out.println("Starting Adding Locations");

		Attraction attraction = gpsService.getAttractions().get(0);
		List<User> allUsers = tourGuideService.getAllUsers();
		allUsers.forEach(u -> userService.addToVisitedLocations((new VisitedLocation(u.getUserId(), attraction, new Date())), u.getUserName()));

		System.out.println("Done Adding Locations");
		System.out.println("Starting Calculating Rewards");

		tourGuideService.processAllUserRewards();

		System.out.println("Done Calculating Rewards");
		System.out.println("Starting Asserting");

		for(User user : allUsers) {
			assertTrue(user.getUserRewards().size() > 0);
		}
		System.out.println("Done Asserting");

		stopWatch.stop();
		tourGuideService.tracker.stopTracking();

		System.out.println("highVolumeGetRewards: Time Elapsed: " + TimeUnit.MILLISECONDS.toSeconds(stopWatch.getTime()) + " seconds.");
		assertTrue(TimeUnit.MINUTES.toSeconds(20) >= TimeUnit.MILLISECONDS.toSeconds(stopWatch.getTime()));
	}
	
}
