package tourGuide.service;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.*;

import org.springframework.stereotype.Service;

import gpsUtil.location.Attraction;
import gpsUtil.location.Location;
import gpsUtil.location.VisitedLocation;
import rewardCentral.RewardCentral;
import tourGuide.user.User;


@Service
public class RewardsService {
    private static final double STATUTE_MILES_PER_NAUTICAL_MILE = 1.15077945;

	// proximity in miles
    private int defaultProximityBuffer = 100;
	private int proximityBuffer = defaultProximityBuffer;
	private int attractionProximityRange = 200;
	private final GpsService gpsService;
	private final RewardCentral rewardsCentral;
	private final UserService userService;

	//new
	private ExecutorService executorService = Executors.newFixedThreadPool(10000);

	public RewardsService(GpsService gpsService, RewardCentral rewardCentral, UserService userService) {
		this.gpsService = gpsService;
		this.rewardsCentral = rewardCentral;
		this.userService = userService;
	}
	
	public void setProximityBuffer(int proximityBuffer) {
		this.proximityBuffer = proximityBuffer;
	}
	
	public void setDefaultProximityBuffer() {
		proximityBuffer = defaultProximityBuffer;
	}
	
	public boolean isWithinAttractionProximity(Attraction attraction, Location location) {
		return getDistance(attraction, location) > attractionProximityRange ? false : true;
	}
	
	private boolean nearAttraction(VisitedLocation visitedLocation, Attraction attraction) {
		return getDistance(attraction, visitedLocation.location) > proximityBuffer ? false : true;
	}
	
	private int getRewardPoints(Attraction attraction, UUID userid) {
		return rewardsCentral.getAttractionRewardPoints(attraction.attractionId, userid);
	}

	public int getRewardValue(Attraction attraction, UUID userid) {
		return rewardsCentral.getAttractionRewardPoints(attraction.attractionId, userid);
	}
	
	public double getDistance(Location loc1, Location loc2) {
        double lat1 = Math.toRadians(loc1.latitude);
        double lon1 = Math.toRadians(loc1.longitude);
        double lat2 = Math.toRadians(loc2.latitude);
        double lon2 = Math.toRadians(loc2.longitude);

        double angle = Math.acos(Math.sin(lat1) * Math.sin(lat2)
                               + Math.cos(lat1) * Math.cos(lat2) * Math.cos(lon1 - lon2));

        double nauticalMiles = 60 * Math.toDegrees(angle);
        double statuteMiles = STATUTE_MILES_PER_NAUTICAL_MILE * nauticalMiles;
        return statuteMiles;
	}

	public void calculateRewards(User user) {

		List<VisitedLocation> userLocations = user.getVisitedLocations();
		List<Attraction> attractions = gpsService.getAttractions();

		ArrayList<CompletableFuture> futures = new ArrayList<>();

		for(VisitedLocation visitedLocation : userLocations) {
			for (Attraction attr : attractions) {
				futures.add(
						CompletableFuture.runAsync(()-> {
							if(user.getUserRewards().stream().filter(r -> r.attraction.attractionName.equals(attr.attractionName)).count() == 0) {

								if(nearAttraction(visitedLocation, attr)) {
									userService.addUserReward(user.getUserName(), visitedLocation, attr, getRewardPoints(attr, user.getUserId()));
								}
							}
						},executorService)
				);
			}
		}

		futures.forEach((n)-> {
			try {
				n.get();
			} catch (InterruptedException e) {
				e.printStackTrace();
			} catch (ExecutionException e) {
				e.printStackTrace();
			}
		});
	}

	public String calculateRewardsReturn(User user) {

		List<VisitedLocation> userLocations = user.getVisitedLocations();
		List<Attraction> attractions = gpsService.getAttractions();

		CopyOnWriteArrayList<CompletableFuture> futures = new CopyOnWriteArrayList<CompletableFuture>();

		for(VisitedLocation visitedLocation : userLocations) {
			for (Attraction attr : attractions) {
				futures.add(
						CompletableFuture.runAsync(()-> {
							if(user.getUserRewards().stream().filter(r -> r.attraction.attractionName.equals(attr.attractionName)).count() == 0) {

								if(nearAttraction(visitedLocation, attr)) {
									userService.addUserReward(user.getUserName(), visitedLocation, attr, getRewardPoints(attr, user.getUserId()));
								}
							}
						},executorService)
				);
			}
		}

		futures.forEach((n)-> {
			try {
				n.get();
			} catch (InterruptedException e) {
				e.printStackTrace();
			} catch (ExecutionException e) {
				e.printStackTrace();
			}
		});

		return user.getUserName();
	}
}
