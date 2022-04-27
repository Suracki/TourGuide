package tourGuide.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import tourGuide.user.User;
import tripPricer.Provider;
import tripPricer.TripPricer;

import java.util.List;

@Service
public class TripService {

    private Logger logger = LoggerFactory.getLogger(TripService.class);
    private final TripPricer tripPricer;

    @Value("${tripPricer.api.key}")
    private static final String tripPricerApiKey = "test-server-api-key";

    public TripService(TripPricer tripPricer) {
        this.tripPricer = tripPricer;
    }

    public List<Provider> getTripDeals(User user) {
        int cumulatativeRewardPoints = user.getUserRewards().stream().mapToInt(i -> i.getRewardPoints()).sum();
        List<Provider> providers = tripPricer.getPrice(tripPricerApiKey, user.getUserId(), user.getUserPreferences().getNumberOfAdults(),
                user.getUserPreferences().getNumberOfChildren(), user.getUserPreferences().getTripDuration(), cumulatativeRewardPoints);
        user.setTripDeals(providers);
        return providers;
    }

}
