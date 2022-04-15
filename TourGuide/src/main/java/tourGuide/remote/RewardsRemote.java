package tourGuide.remote;

import com.google.gson.Gson;
import org.springframework.stereotype.Service;
import tourGuide.dockers.rewardsDocker.controller.RewardsServiceController;

import java.util.UUID;

@Service
public class RewardsRemote {

    private RewardsServiceController rewardsServiceController;

    public RewardsRemote(RewardsServiceController rewardsServiceController) {
        this.rewardsServiceController = rewardsServiceController;
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
