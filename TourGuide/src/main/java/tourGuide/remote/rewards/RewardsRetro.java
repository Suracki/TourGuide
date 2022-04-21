package tourGuide.remote.rewards;

import gpsUtil.location.VisitedLocation;
import okhttp3.OkHttpClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import retrofit2.Call;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;
import tourGuide.remote.gps.GpsServiceRetro;

import java.util.UUID;

@Service
public class RewardsRetro {

    @Value("${docker.rewards.ip}")
    private String ip = "127.0.0.1";

    @Value("${docker.rewards.port}")
    private String port = "8082";

    private Logger logger = LoggerFactory.getLogger(RewardsRetro.class);

    public int getRewardValue(UUID attractionID, UUID userId) {
        logger.info("getRewardValue called");

        OkHttpClient.Builder httpClient = new OkHttpClient.Builder();
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl("http://" + ip + ":" + port +"/")
                .addConverterFactory(GsonConverterFactory.create())
                .client(httpClient.build())
                .build();

        RewardsServiceRetro rewardsService = retrofit.create(RewardsServiceRetro.class);

        Call<Integer> callSync = rewardsService.getRewardValue(attractionID, userId);

        try {
            Response<Integer> response = callSync.execute();
            int value = response.body();
            logger.debug("getRewardValue external call completed");
            return value;
        }
        catch (Exception e){
            logger.error("getRewardValue external call failed: " + e);
            return 0;
        }

    }

    public String calculateRewardsByUsername(String userName) {
        logger.info("calculateRewardsByUsername called");

        OkHttpClient.Builder httpClient = new OkHttpClient.Builder();
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl("http://" + ip + ":" + port +"/")
                .addConverterFactory(GsonConverterFactory.create())
                .client(httpClient.build())
                .build();

        RewardsServiceRetro rewardsService = retrofit.create(RewardsServiceRetro.class);

        Call<String> callSync = rewardsService.calculateRewardsByUsername(userName);

        try {
            Response<String> response = callSync.execute();
            String value = response.body();
            logger.debug("calculateRewardsByUsername external call completed");
            return value;
        }
        catch (Exception e){
            logger.error("calculateRewardsByUsername external call failed: " + e);
            return "";
        }

    }

}
