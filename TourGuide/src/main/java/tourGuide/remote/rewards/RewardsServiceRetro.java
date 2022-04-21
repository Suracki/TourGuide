package tourGuide.remote.rewards;

import org.springframework.stereotype.Service;
import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.Query;

import java.util.UUID;

@Service
public interface RewardsServiceRetro {

    @GET("/rewards/getRewardValue")
    public Call<Integer> getRewardValue(@Query("attractionId") UUID attractionId,
                                        @Query("userID") UUID userId);

    @POST("/rewards/calculateRewardsByUsername")
    public Call<String> calculateRewardsByUsername(@Query("userName") String userName);

}
