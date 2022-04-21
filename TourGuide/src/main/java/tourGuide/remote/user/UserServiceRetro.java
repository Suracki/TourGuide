package tourGuide.remote.user;

import gpsUtil.location.Attraction;
import gpsUtil.location.VisitedLocation;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.Query;
import tourGuide.dockers.userDocker.model.User;
import tourGuide.dockers.userDocker.model.UserReward;
import tourGuide.outputEntities.UserAndReward;
import tourGuide.outputEntities.UserLocation;

import java.util.List;
import java.util.UUID;

@Service
public interface UserServiceRetro {

    @POST("/user/addUser")
    public Call<Boolean> addUser(@Body String user);

//    @POST("user/addToVisitedLocations")
//    public Call<String> addToVisitedLocations(@Query("visitedLocation")VisitedLocation visitedLocation,
//                                              @Query("userName") String userName);

    @POST("user/addToVisitedLocations")
    public Call<String> addToVisitedLocations(@Body String visitedLocation,
                                                              @Query("userName") String userName);

    @GET("user/getAllCurrentLocations")
    public Call<List<UserLocation>> getAllCurrentLocations();

//    @POST("user/addUserReward")
//    public Call<Boolean> addUserReward(@Query("userName") String userName,
//                                    @Query("visitedLocation") String visitedLocation,
//                                    @Query("attraction")String attraction,
//                                    @Query("rewardPoints") int rewardPoints);

    @POST("user/addUserReward")
    public Call<Boolean> addUserReward(@Body String userAndRewardJson);

    @GET("user/getAllUsers")
    public Call<List<User>> getAllUsers();

    @GET("user/getUserByUsername")
    public Call<User> getUserByUsername(@Query("userName") String userName);

    @GET("user/getLastVisitedLocationByName")
    public Call<VisitedLocation> getLastVisitedLocationByName(@Query("userName") String userName);

    @GET("user/getUserRewardsByUsername")
    public Call<List<UserReward>> getUserRewardsByUsername (@Query("userName") String userName);

    @GET("user/getVisitedLocationsByUsername")
    public Call<List<VisitedLocation>> getVisitedLocationsByUsername (@Query("userName") String userName);

    @GET("user/getUserIdByUsername")
    public Call<UUID> getUserIdByUsername (@Query("userName") String userName);

    @POST("user/trackAllUserLocations")
    public Call<Boolean> trackAllUserLocations();

    @GET("user/getUserCount")
    public Call<Integer> getUserCount();

    @GET("user/getAllUserNames")
    public Call<List<String>> getAllUserNames();

}
