package rewardsDocker.controller;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;
import rewardsDocker.service.RewardsService;
import userDocker.gson.MoneyTypeAdapterFactory;

import java.util.UUID;

@RestController
public class RewardsServiceController {

    private RewardsService rewardsService;

    Gson gson = new Gson();

    public RewardsServiceController(RewardsService rewardsService){
        this.rewardsService = rewardsService;
        gson = new GsonBuilder().registerTypeAdapterFactory(new MoneyTypeAdapterFactory()).create();
    }

    @GetMapping("/getRewardValue")
    public String getRewardValue(UUID attractionId, UUID userid) {
        return gson.toJson(rewardsService.getRewardValue(attractionId, userid));
    }

    @PostMapping("/calculateRewardsByUsername")
    public String calculateRewardsByUsername(String userName) {
        return gson.toJson(rewardsService.calculateRewardsByUsername(userName));
    }

}
