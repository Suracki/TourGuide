package tourGuide.service;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Stream;

import com.google.common.eventbus.Subscribe;
import org.springframework.stereotype.Service;

import gpsUtil.GpsUtil;
import gpsUtil.location.Attraction;
import gpsUtil.location.Location;
import gpsUtil.location.VisitedLocation;
import rewardCentral.RewardCentral;
import tourGuide.user.User;
import tourGuide.user.UserReward;

import org.reactivestreams.Subscriber;
import org.reactivestreams.Subscription;
import io.reactivex.Flowable;

@Service
public class RewardsService {
    private static final double STATUTE_MILES_PER_NAUTICAL_MILE = 1.15077945;

	// proximity in miles
    private int defaultProximityBuffer = 10;
	private int proximityBuffer = defaultProximityBuffer;
	private int attractionProximityRange = 200;
	private final GpsUtil gpsUtil;
	private final RewardCentral rewardsCentral;
	private final UserService userService;

	//new
	private CopyOnWriteArrayList<PendingReward> pendingRewards;

	public RewardsService(GpsUtil gpsUtil, RewardCentral rewardCentral, UserService userService) {
		this.gpsUtil = gpsUtil;
		this.rewardsCentral = rewardCentral;
		this.userService = userService;
		pendingRewards = new CopyOnWriteArrayList<>();
	}
	
	public void setProximityBuffer(int proximityBuffer) {
		this.proximityBuffer = proximityBuffer;
	}
	
	public void setDefaultProximityBuffer() {
		proximityBuffer = defaultProximityBuffer;
	}
	
	public void calculateRewardsOld(User user) {

		List<VisitedLocation> userLocations = user.getVisitedLocations();
		List<Attraction> attractions = gpsUtil.getAttractions();
		
		for(VisitedLocation visitedLocation : userLocations) {
			for(Attraction attraction : attractions) {
				if(user.getUserRewards().stream().filter(r -> r.attraction.attractionName.equals(attraction.attractionName)).count() == 0) {
					if(nearAttraction(visitedLocation, attraction)) {
						user.addUserReward(new UserReward(visitedLocation, attraction, getRewardPoints(attraction, user)));
					}
				}
			}
		}
	}
	
	public boolean isWithinAttractionProximity(Attraction attraction, Location location) {
		return getDistance(attraction, location) > attractionProximityRange ? false : true;
	}
	
	private boolean nearAttraction(VisitedLocation visitedLocation, Attraction attraction) {
		return getDistance(attraction, visitedLocation.location) > proximityBuffer ? false : true;
	}
	
	private int getRewardPoints(Attraction attraction, User user) {
		return rewardsCentral.getAttractionRewardPoints(attraction.attractionId, user.getUserId());
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

	// Concurrency Testing

	public void calculateRewardsOldest(User user) {
		List<VisitedLocation> userLocations = user.getVisitedLocations();
		List<Attraction> attractions = gpsUtil.getAttractions();


		ExecutorService locationExecutorService = Executors.newFixedThreadPool(userLocations.size());


			for(VisitedLocation visitedLocation : userLocations) {
				for (Attraction attraction : attractions) {
					if(user.getUserRewards().stream().filter(r -> r.attraction.attractionName.equals(attraction.attractionName)).count() == 0) {

						CompletableFuture.supplyAsync( () -> {
							return nearAttraction(visitedLocation, attraction);}, locationExecutorService)
								.thenAcceptAsync(a -> {
									if (a) {
										user.addUserReward(new UserReward(visitedLocation, attraction, getRewardPoints(attraction, user)));
									}
						});

					}
				}
			}

	}

	public void calculateRewards(User user) {
		List<VisitedLocation> userLocations = user.getVisitedLocations();
		List<Attraction> attractions = gpsUtil.getAttractions();


		ExecutorService locationExecutorService = Executors.newFixedThreadPool(userLocations.size());

		Stream<UserReward> userRewardStream = user.getUserRewards().stream();


		for(VisitedLocation visitedLocation : userLocations) {
			for (Attraction attraction : attractions) {
				RewardSubscriber sub = new RewardSubscriber();
				sub.setup(userService, user, visitedLocation, attraction, pendingRewards);
				Flowable<UserReward> flowbleRewards = Flowable.fromIterable(userRewardStream::iterator).
						onBackpressureDrop();
				flowbleRewards.subscribe(sub);
			}
		}

	}

	public void processPendingRewards() {
		if (pendingRewards.size() != 0) {
			int processed = 0;
			int toProcess = pendingRewards.size();
			CopyOnWriteArrayList<PendingReward> processingRewards = new CopyOnWriteArrayList<>(pendingRewards);
			pendingRewards = new CopyOnWriteArrayList<>();

			List<CompletableFuture<Void>> completableFutures = new ArrayList<>();
			for (PendingReward pendingReward : processingRewards) {
				completableFutures.add(CompletableFuture.runAsync(
						()->
							pendingReward.getUser().addUserReward(
								new UserReward(pendingReward.getVisitedLocation(),
										pendingReward.getAttraction(),
										getRewardPoints(pendingReward.getAttraction(), pendingReward.getUser())))

				));
			}
			CompletableFuture<Void> runFutures = CompletableFuture.allOf(
					completableFutures.toArray(new CompletableFuture[]{}));
			//runFutures.get();
		}
	}
}

class RewardSubscriber implements Subscriber<UserReward> {
	private UserService userService = null;
	private VisitedLocation visitedLocation = null;
	private User user = null;
	private Attraction attraction = null;
	private CopyOnWriteArrayList<PendingReward> rewards = null;
	private Subscription subscription;
	private boolean found = false;



	public void setup(UserService userService, User user, VisitedLocation visitedLocation, Attraction attraction, CopyOnWriteArrayList<PendingReward> rewards) {
		this.userService = userService;
		this.user = user;
		this.visitedLocation = visitedLocation;
		this.attraction = attraction;
		this.rewards = rewards;
	}

	@Override
	public void onSubscribe(Subscription subscription) {
		this.subscription = subscription;
		subscription.request(1);
	}

	@Override
	public void onNext(UserReward userReward) {

		if (userReward.attraction.attractionName.equals(attraction.attractionName)) {
			found = true;
		}
	}


	@Override
	public void onError(Throwable t) {

	}

	@Override
	public void onComplete() {
		if (!found) {
			rewards.add(new PendingReward(user, visitedLocation, attraction));
		}
	}
}

class PendingReward {
	private User user;
	private VisitedLocation visitedLocation;
	private Attraction attraction;

	public PendingReward(User user, VisitedLocation visitedLocation, Attraction attraction){
		this.user = user;
		this.visitedLocation = visitedLocation;
		this.attraction = attraction;
	}

	public User getUser() {
		return user;
	}

	public void setUser(User user) {
		this.user = user;
	}

	public VisitedLocation getVisitedLocation() {
		return visitedLocation;
	}

	public void setVisitedLocation(VisitedLocation visitedLocation) {
		this.visitedLocation = visitedLocation;
	}

	public Attraction getAttraction() {
		return attraction;
	}

	public void setAttraction(Attraction attraction) {
		this.attraction = attraction;
	}
}
