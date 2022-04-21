package tourGuide.dockers.rewardsDocker.service;

import java.util.List;
import java.util.UUID;
import java.util.concurrent.*;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import gpsUtil.location.Attraction;
import gpsUtil.location.Location;
import gpsUtil.location.VisitedLocation;
import rewardCentral.RewardCentral;
import tourGuide.dockers.rewardsDocker.controller.RewardsServiceController;
import tourGuide.remote.gps.GpsRetro;
import tourGuide.remote.UserRemote;


@Service
public class RewardsService {
    private static final double STATUTE_MILES_PER_NAUTICAL_MILE = 1.15077945;

	// proximity in miles
    private int defaultProximityBuffer = 100;
	private int proximityBuffer = defaultProximityBuffer;
	private int attractionProximityRange = 200;
	private final GpsRetro gpsRetro;
	private final RewardCentral rewardsCentral;
	private final UserRemote userRemote;
	private ExecutorService executorService = Executors.newFixedThreadPool(10000);

	private Logger logger = LoggerFactory.getLogger(RewardsServiceController.class);

	public RewardsService(GpsRetro gpsRetro, RewardCentral rewardCentral, UserRemote userRemote) {
		this.gpsRetro = gpsRetro;
		this.rewardsCentral = rewardCentral;
		this.userRemote = userRemote;
	}

	public int getRewardValue(UUID attractionId, UUID userid) {
		logger.debug("getRewardValue called");
		return rewardsCentral.getAttractionRewardPoints(attractionId, userid);
	}

	public String calculateRewardsByUsername(String userName) {
		logger.debug("calculateRewardsByUsername called");

		List<VisitedLocation> userLocations = userRemote.getVisitedLocationsByUsername(userName);
		List<Attraction> attractions = gpsRetro.getAttractions();
		UUID userID = userRemote.getUserIdByUsername(userName);

		logger.debug("userLocations found: " + userLocations.size());
		logger.debug("attractions found: " + attractions.size());

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

		logger.debug("futures created: " + futures.size() + ", calling get()...");

		futures.forEach((n)-> {
			try {
				n.get();
			} catch (InterruptedException e) {
				e.printStackTrace();
			} catch (ExecutionException e) {
				e.printStackTrace();
			}
		});

		logger.debug("futures got, returning.");
		return userName;
	}

	//Used by tests only
	public void setProximityBuffer(int proximityBuffer) {
		logger.debug("proximityBuffer updated to " + proximityBuffer);
		this.proximityBuffer = proximityBuffer;
	}
	public void setDefaultProximityBuffer() {
		logger.debug("proximityBuffer set to default value (" + defaultProximityBuffer + ")");
		proximityBuffer = defaultProximityBuffer;
	}
	public boolean isWithinAttractionProximity(Attraction attraction, Location location) {
		logger.debug("isWithinAttractionProximity called");
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
