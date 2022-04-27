package tourGuide.module;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import gpsUtil.GpsUtil;
import rewardCentral.RewardCentral;
import tourGuide.service.GpsService;
import tourGuide.service.RewardsService;
import tourGuide.service.UserService;
import tourGuide.user.User;

@Configuration
public class TourGuideModule {

	@Bean
	public GpsUtil getGpsUtil() {
		return new GpsUtil();
	}

	@Bean
	public RewardCentral getRewardCentral() {
		return new RewardCentral();
	}
	
}
