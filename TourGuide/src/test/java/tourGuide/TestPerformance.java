package tourGuide;

import static org.junit.Assert.assertTrue;

import java.util.Date;
import java.util.List;
import java.util.concurrent.*;

import tourGuide.dockers.gpsDocker.controller.GpsServiceController;
import org.apache.commons.lang3.time.StopWatch;
import org.junit.Test;

import gpsUtil.GpsUtil;
import gpsUtil.location.Attraction;
import gpsUtil.location.VisitedLocation;
import rewardCentral.RewardCentral;
import tourGuide.dockers.rewardsDocker.controller.RewardsServiceController;
import tourGuide.helper.InternalTestHelper;
import tourGuide.remote.GpsRemote;
import tourGuide.remote.RewardsRemote;
import tourGuide.remote.UserRemote;
import tourGuide.dockers.gpsDocker.service.GpsService;
import tourGuide.dockers.rewardsDocker.service.RewardsService;
import tourGuide.service.TourGuideService;
import tourGuide.dockers.userDocker.controller.UserServiceController;
import tourGuide.dockers.userDocker.service.UserService;
import tourGuide.dockers.userDocker.model.User;


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

	private static final int NUMBER_OF_TEST_USERS = 100;
	
	@Test
	public void highVolumeTrackLocationConc() {
		GpsRemote gpsRemote = new GpsRemote(new GpsServiceController(new GpsService(new GpsUtil())));
		UserRemote userRemote = new UserRemote(new UserServiceController(new UserService(gpsRemote)));
		RewardsRemote rewardsRemote = new RewardsRemote(new RewardsServiceController(new RewardsService(gpsRemote, new RewardCentral(), userRemote)));
		// Users should be incremented up to 100,000, and test finishes within 15 minutes
		InternalTestHelper.setInternalUserNumber(NUMBER_OF_TEST_USERS);
		TourGuideService tourGuideService = new TourGuideService(gpsRemote, rewardsRemote, userRemote);

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
		GpsRemote gpsRemote = new GpsRemote(new GpsServiceController(new GpsService(new GpsUtil())));
		UserRemote userRemote = new UserRemote(new UserServiceController(new UserService(gpsRemote)));
		RewardsRemote rewardsRemote = new RewardsRemote(new RewardsServiceController(new RewardsService(gpsRemote, new RewardCentral(), userRemote)));
		// Users should be incremented up to 100,000, and test finishes within 15 minutes
		InternalTestHelper.setInternalUserNumber(NUMBER_OF_TEST_USERS);
		TourGuideService tourGuideService = new TourGuideService(gpsRemote, rewardsRemote, userRemote);

		StopWatch stopWatch = new StopWatch();
		stopWatch.start();
		tourGuideService.trackAllUserLocationsAndProcess();
		stopWatch.stop();
		tourGuideService.tracker.stopTracking();

		System.out.println("highVolumeTrackLocation: Time Elapsed: " + TimeUnit.MILLISECONDS.toSeconds(stopWatch.getTime()) + " seconds.");
		assertTrue(TimeUnit.MINUTES.toSeconds(15) >= TimeUnit.MILLISECONDS.toSeconds(stopWatch.getTime()));
	}

//	@Ignore
//	@Test
//	public void highVolumeGetRewards() {
//		GpsRemote gpsRemote = new GpsRemote(new GpsService(new GpsUtil()));
//		UserRemote userRemote = new UserRemote(new UserService(gpsRemote));
//		RewardsRemote rewardsRemote = new RewardsRemote(new RewardsService(gpsRemote, new RewardCentral(), userRemote));
//
//		// Users should be incremented up to 100,000, and test finishes within 20 minutes
//		InternalTestHelper.setInternalUserNumber(NUMBER_OF_TEST_USERS);
//		StopWatch stopWatch = new StopWatch();
//		stopWatch.start();
//		TourGuideService tourGuideService = new TourGuideService(gpsRemote, rewardsRemote, userRemote);
//
//	    Attraction attraction = gpsRemote.getAttractions().get(0);
//		List<User> allUsers = new ArrayList<>();
//		allUsers = tourGuideService.getAllUsers();
//		allUsers.forEach(u -> u.addToVisitedLocations(new VisitedLocation(u.getUserId(), attraction, new Date())));
//
//	    allUsers.forEach(u -> rewardsRemote.calculateRewardsByUsername(u.getUserName()));
//
//		for(User user : allUsers) {
//			assertTrue(user.getUserRewards().size() > 0);
//		}
//
//		stopWatch.stop();
//		tourGuideService.tracker.stopTracking();
//
//		System.out.println("highVolumeGetRewards: Time Elapsed: " + TimeUnit.MILLISECONDS.toSeconds(stopWatch.getTime()) + " seconds.");
//		assertTrue(TimeUnit.MINUTES.toSeconds(20) >= TimeUnit.MILLISECONDS.toSeconds(stopWatch.getTime()));
//	}

//	@Ignore
//	@Test
//	public void highVolumeGetRewardsCallConc() {
//		GpsRemote gpsRemote = new GpsRemote(new GpsService(new GpsUtil()));
//		UserRemote userRemote = new UserRemote(new UserService(gpsRemote));
//		RewardsRemote rewardsRemote = new RewardsRemote(new RewardsService(gpsRemote, new RewardCentral(), userRemote));
//
//		// Users should be incremented up to 100,000, and test finishes within 20 minutes
//		InternalTestHelper.setInternalUserNumber(NUMBER_OF_TEST_USERS);
//		StopWatch stopWatch = new StopWatch();
//		stopWatch.start();
//		TourGuideService tourGuideService = new TourGuideService(gpsRemote, rewardsRemote, userRemote);
//
//		Attraction attraction = gpsRemote.getAttractions().get(0);
//		List<User> allUsers = new ArrayList<>();
//		allUsers = tourGuideService.getAllUsers();
//		allUsers.forEach(u -> u.addToVisitedLocations(new VisitedLocation(u.getUserId(), attraction, new Date())));
//
//		ArrayList<CompletableFuture> futures = new ArrayList<>();
//
//		System.out.println(allUsers.size() + "Users Found");
//
//		ExecutorService executorService = Executors.newFixedThreadPool(10000);
//
//		for(User user : allUsers) {
//			futures.add(
//					CompletableFuture.runAsync(()-> {
//						rewardsRemote.calculateRewardsByUsername(user.getUserName());
//					},executorService)
//			);
//		}
//
//		System.out.println(futures.size() + "Futures Created. Getting...");
//
//		futures.forEach((n)-> {
//			try {
//				n.get();
//			} catch (InterruptedException e) {
//				e.printStackTrace();
//			} catch (ExecutionException e) {
//				e.printStackTrace();
//			}
//		});
//
//		System.out.println("Futures Got");
//
//		for(User user : allUsers) {
//			assertTrue(user.getUserRewards().size() > 0);
//		}
//
//		stopWatch.stop();
//		executorService.shutdown();
//		tourGuideService.tracker.stopTracking();
//
//		System.out.println("highVolumeGetRewards: Time Elapsed: " + TimeUnit.MILLISECONDS.toSeconds(stopWatch.getTime()) + " seconds.");
//		assertTrue(TimeUnit.MINUTES.toSeconds(20) >= TimeUnit.MILLISECONDS.toSeconds(stopWatch.getTime()));
//	}

	@Test
	public void highVolumeGetRewardsOneCall() {
		GpsRemote gpsRemote = new GpsRemote(new GpsServiceController(new GpsService(new GpsUtil())));
		UserRemote userRemote = new UserRemote(new UserServiceController(new UserService(gpsRemote)));
		RewardsRemote rewardsRemote = new RewardsRemote(new RewardsServiceController(new RewardsService(gpsRemote, new RewardCentral(), userRemote)));

		// Users should be incremented up to 100,000, and test finishes within 20 minutes
		InternalTestHelper.setInternalUserNumber(NUMBER_OF_TEST_USERS);
		StopWatch stopWatch = new StopWatch();
		stopWatch.start();
		TourGuideService tourGuideService = new TourGuideService(gpsRemote, rewardsRemote, userRemote);

		System.out.println("Starting Adding Locations");

		Attraction attraction = gpsRemote.getAttractions().get(0);
		List<User> allUsers = userRemote.getAllUsers();
		allUsers.forEach(u -> userRemote.addToVisitedLocations((new VisitedLocation(u.getUserId(), attraction, new Date())), u.getUserName()));

		System.out.println("Done Adding Locations");
		System.out.println("Starting Calculating Rewards");

		tourGuideService.processAllUserRewards();
		allUsers = userRemote.getAllUsers();

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
