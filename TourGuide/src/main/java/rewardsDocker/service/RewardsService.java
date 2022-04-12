package rewardsDocker.service;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.*;

import gpsDocker.service.GpsService;
import org.springframework.stereotype.Service;

import gpsUtil.location.Attraction;
import gpsUtil.location.Location;
import gpsUtil.location.VisitedLocation;
import rewardCentral.RewardCentral;
import tourGuide.remote.GpsRemote;
import tourGuide.remote.UserRemote;
import userDocker.model.User;


@Service
public class RewardsService {
    private static final double STATUTE_MILES_PER_NAUTICAL_MILE = 1.15077945;

	// proximity in miles
    private int defaultProximityBuffer = 100;
	private int proximityBuffer = defaultProximityBuffer;
	private int attractionProximityRange = 200;
	private final GpsRemote gpsRemote;
	private final RewardCentral rewardsCentral;
	private final UserRemote userRemote;
	private ExecutorService executorService = Executors.newFixedThreadPool(10000);

	public RewardsService(GpsRemote gpsRemote, RewardCentral rewardCentral, UserRemote userRemote) {
		this.gpsRemote = gpsRemote;
		this.rewardsCentral = rewardCentral;
		this.userRemote = userRemote;
	}

	public int getRewardValue(UUID attractionId, UUID userid) {
		return rewardsCentral.getAttractionRewardPoints(attractionId, userid);
	}

	public String calculateRewardsByUsername(String userName) {

		List<VisitedLocation> userLocations = userRemote.getVisitedLocationsByUsername(userName);
		List<Attraction> attractions = gpsRemote.getAttractions();
		UUID userID = userRemote.getUserIdByUsername(userName);

		CopyOnWriteArrayList<CompletableFuture> futures = new CopyOnWriteArrayList<>();

		for(VisitedLocation visitedLocation : userLocations) {
			for (Attraction attr : attractions) {
				futures.add(
						CompletableFuture.runAsync(()-> {
							if(userRemote.getUserRewardsByUsername(userName).stream().filter(r -> r.attraction.attractionName.equals(attr.attractionName)).count() == 0) {

								if(nearAttraction(visitedLocation, attr)) {
									userRemote.addUserReward(userName, visitedLocation, attr, getRewardValue(attr.attractionId, userID));
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

		return userName;
	}

	//Used by tests only
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

	private double getDistance(Location loc1, Location loc2) {
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
}
