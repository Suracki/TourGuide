package tourGuide;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import gpsUtil.GpsUtil;
import rewardCentral.RewardCentral;
import gpsDocker.service.GpsService;
import rewardsDocker.service.RewardsService;
import userDocker.service.UserService;

@Configuration
public class TourGuideModule {
	@Autowired
	UserService userService;

	@Autowired
	GpsService gpsService;
	
	@Bean
	public GpsUtil getGpsUtil() {
		return new GpsUtil();
	}
	
	@Bean
	public RewardsService getRewardsService() {
		return new RewardsService(gpsService, getRewardCentral(), userService);
	}
	
	@Bean
	public RewardCentral getRewardCentral() {
		return new RewardCentral();
	}
	
}
