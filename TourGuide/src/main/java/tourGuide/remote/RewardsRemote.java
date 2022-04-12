package tourGuide.remote;

import com.google.gson.Gson;
import rewardsDocker.controller.RewardsServiceController;
import rewardsDocker.service.RewardsService;

import java.util.UUID;

public class RewardsRemote {

    private RewardsServiceController rewardsServiceController;

    public RewardsRemote(RewardsService rewardsService) {
        this.rewardsServiceController = new RewardsServiceController(rewardsService);
    }

    public int getRewardValue(UUID attractionId, UUID userid) {
        System.out.println("RewardsRemote getRewardValue");
        String json = rewardsServiceController.getRewardValue(attractionId, userid);
        int rewardValue = new Gson().fromJson(json, int.class);
        return rewardValue;
    }

    public String calculateRewardsByUsername(String userName) {
        System.out.println("RewardsRemote getRewardValue");
        String json = rewardsServiceController.calculateRewardsByUsername(userName);
        String result = new Gson().fromJson(json, String.class);
        return result;
    }

}
