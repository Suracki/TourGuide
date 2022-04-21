package tourGuide.remote.user;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import gpsUtil.location.Attraction;
import gpsUtil.location.VisitedLocation;
import okhttp3.OkHttpClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import retrofit2.Call;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;
import tourGuide.dockers.userDocker.gson.MoneyTypeAdapterFactory;
import tourGuide.dockers.userDocker.model.User;
import tourGuide.dockers.userDocker.model.UserReward;
import tourGuide.outputEntities.UserAndReward;
import tourGuide.outputEntities.UserLocation;

import java.lang.reflect.Type;
import java.util.List;
import java.util.UUID;

@Service
public class UserRetro {

    @Value("${docker.user.ip}")
    private String ip = "127.0.0.1";

    @Value("${docker.user.port}")
    private String port = "8083";

    private Logger logger = LoggerFactory.getLogger(UserRetro.class);

    private Gson gson = new GsonBuilder().registerTypeAdapterFactory(new MoneyTypeAdapterFactory()).setDateFormat("EEE, dd MMM yyyy HH:mm:ss zzz").setLenient().create();

    public boolean addUser(User user) {
        logger.info("addUser called");

        String json = gson.toJson(user);
        System.out.println(json);

        OkHttpClient.Builder httpClient = new OkHttpClient.Builder();
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl("http://" + ip + ":" + port +"/")
                .addConverterFactory(GsonConverterFactory.create(gson))
                .client(httpClient.build())
                .build();


        UserServiceRetro userService = retrofit.create(UserServiceRetro.class);

        Call<Boolean> callSync = userService.addUser(json);

        try {
            System.out.println("Call: " + callSync.toString());
            Response<Boolean> response = callSync.execute();
            boolean value = response.body();
            logger.debug("addUser external call completed");
            return value;
        }
        catch (Exception e){
            logger.error("addUser external call failed: " + e);
            return false;
        }
    }

    //TODO: update to return bool?
    public String addToVisitedLocations(VisitedLocation visitedLocation, String userName) {
        logger.info("addToVisitedLocations called");

        String json = gson.toJson(visitedLocation);

        OkHttpClient.Builder httpClient = new OkHttpClient.Builder();
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl("http://" + ip + ":" + port + "/")
                .addConverterFactory(GsonConverterFactory.create(gson))
                .client(httpClient.build())
                .build();

        UserServiceRetro userService = retrofit.create(UserServiceRetro.class);

        Call<String> callSync = userService.addToVisitedLocations(json, userName);

        try {
            Response<String> response = callSync.execute();
            String value = response.body();
            logger.debug("addToVisitedLocations external call completed");
            return value;
        } catch (Exception e) {
            logger.error("addToVisitedLocations external call failed: " + e);
            return "err";
        }
    }

    public List<UserLocation> getAllCurrentLocations() {
        logger.info("getAllCurrentLocations called");

        OkHttpClient.Builder httpClient = new OkHttpClient.Builder();
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl("http://" + ip + ":" + port + "/")
                .addConverterFactory(GsonConverterFactory.create())
                .client(httpClient.build())
                .build();

        UserServiceRetro userService = retrofit.create(UserServiceRetro.class);

        Call<List<UserLocation>> callSync = userService.getAllCurrentLocations();

        try {
            Response<List<UserLocation>> response = callSync.execute();
            List<UserLocation>  value = response.body();
            logger.debug("getAllCurrentLocations external call completed");
            return value;
        } catch (Exception e) {
            logger.error("getAllCurrentLocations external call failed: " + e);
            return null;
        }
    }

    public boolean addUserReward(String userName, VisitedLocation visitedLocation,
                                 Attraction attraction, int rewardPoints) {
        logger.info("addUserReward called");

        OkHttpClient.Builder httpClient = new OkHttpClient.Builder();
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl("http://" + ip + ":" + port + "/")
                .addConverterFactory(GsonConverterFactory.create())
                .client(httpClient.build())
                .build();

        UserServiceRetro userService = retrofit.create(UserServiceRetro.class);

        UserAndReward userAndReward = new UserAndReward(userName, visitedLocation, attraction, rewardPoints);


        Call<Boolean> callSync = userService.addUserReward(gson.toJson(userAndReward));

        try {
            Response<Boolean> response = callSync.execute();
            Boolean value = response.body();
            logger.debug("addUserReward external call completed");
            return true;
        } catch (Exception e) {
            logger.error("addUserReward external call failed: " + e);
            return false;
        }
    }

    public List<User> getAllUsers() {
        logger.info("getAllUsers called");

        OkHttpClient.Builder httpClient = new OkHttpClient.Builder();
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl("http://" + ip + ":" + port + "/")
                .addConverterFactory(GsonConverterFactory.create(gson))
                .client(httpClient.build())
                .build();

        UserServiceRetro userService = retrofit.create(UserServiceRetro.class);

        Call<List<User>> callSync = userService.getAllUsers();

        try {
            Response<List<User>> response = callSync.execute();
            List<User> value = response.body();
            logger.debug("getAllUsers external call completed");
            System.out.println(value.size());
            return value;
        } catch (Exception e) {
            logger.error("getAllUsers external call failed: " + e);
            return null;
        }
    }

    public User getUserByUsername(String userName) {
        logger.info("getUserByUsername called");

        OkHttpClient.Builder httpClient = new OkHttpClient.Builder();
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl("http://" + ip + ":" + port + "/")
                .addConverterFactory(GsonConverterFactory.create(gson))
                .client(httpClient.build())
                .build();

        UserServiceRetro userService = retrofit.create(UserServiceRetro.class);

        Call<User> callSync = userService.getUserByUsername(userName);

        try {
            Response<User> response = callSync.execute();
            User value = response.body();
            logger.debug("getUserByUsername external call completed");
            return value;
        } catch (Exception e) {
            logger.error("getUserByUsername external call failed: " + e);
            return null;
        }
    }

    public VisitedLocation getLastVisitedLocationByName(String userName) {
        logger.info("getLastVisitedLocationByName called");

        OkHttpClient.Builder httpClient = new OkHttpClient.Builder();
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl("http://" + ip + ":" + port + "/")
                .addConverterFactory(GsonConverterFactory.create(gson))
                .client(httpClient.build())
                .build();

        UserServiceRetro userService = retrofit.create(UserServiceRetro.class);

        Call<VisitedLocation> callSync = userService.getLastVisitedLocationByName(userName);

        try {
            Response<VisitedLocation> response = callSync.execute();
            VisitedLocation value = response.body();
            logger.debug("getLastVisitedLocationByName external call completed");
            return value;
        } catch (Exception e) {
            logger.error("getLastVisitedLocationByName external call failed: " + e);
            return null;
        }
    }

    public List<UserReward> getUserRewardsByUsername(String userName) {
        logger.info("getUserRewardsByUsername called");

        OkHttpClient.Builder httpClient = new OkHttpClient.Builder();
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl("http://" + ip + ":" + port + "/")
                .addConverterFactory(GsonConverterFactory.create(gson))
                .client(httpClient.build())
                .build();

        UserServiceRetro userService = retrofit.create(UserServiceRetro.class);

        Call<List<UserReward>> callSync = userService.getUserRewardsByUsername(userName);

        try {
            Response<List<UserReward>> response = callSync.execute();
            List<UserReward> value = response.body();
            logger.debug("getUserRewardsByUsername external call completed");
            return value;
        } catch (Exception e) {
            logger.error("getUserRewardsByUsername external call failed: " + e);
            return null;
        }
    }

    public List<VisitedLocation> getVisitedLocationsByUsername(String userName) {
        logger.info("getVisitedLocationsByUsername called");

        OkHttpClient.Builder httpClient = new OkHttpClient.Builder();
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl("http://" + ip + ":" + port + "/")
                .addConverterFactory(GsonConverterFactory.create(gson))
                .client(httpClient.build())
                .build();

        UserServiceRetro userService = retrofit.create(UserServiceRetro.class);

        Call<List<VisitedLocation>> callSync = userService.getVisitedLocationsByUsername(userName);

        try {
            Response<List<VisitedLocation>> response = callSync.execute();
            List<VisitedLocation> value = response.body();
            return value;
        } catch (Exception e) {
            logger.error("getVisitedLocationsByUsername external call failed: " + e);
            return null;
        }
    }

    public UUID getUserIdByUsername(String userName) {
        logger.info("getUserIdByUsername called");

        OkHttpClient.Builder httpClient = new OkHttpClient.Builder();
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl("http://" + ip + ":" + port + "/")
                .addConverterFactory(GsonConverterFactory.create())
                .client(httpClient.build())
                .build();

        UserServiceRetro userService = retrofit.create(UserServiceRetro.class);

        Call<UUID> callSync = userService.getUserIdByUsername(userName);

        try {
            Response<UUID > response = callSync.execute();
            UUID value = response.body();
            logger.debug("getUserIdByUsername external call completed");
            return value;
        } catch (Exception e) {
            logger.error("getUserIdByUsername external call failed: " + e);
            return null;
        }
    }

    public boolean trackAllUserLocations() {
        logger.info("trackAllUserLocations called");

        OkHttpClient.Builder httpClient = new OkHttpClient.Builder();
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl("http://" + ip + ":" + port + "/")
                .addConverterFactory(GsonConverterFactory.create())
                .client(httpClient.build())
                .build();

        UserServiceRetro userService = retrofit.create(UserServiceRetro.class);

        Call<Boolean> callSync = userService.trackAllUserLocations();

        try {
            Response<Boolean > response = callSync.execute();
            Boolean value = response.body();
            logger.debug("trackAllUserLocations external call completed");
            return value;
        } catch (Exception e) {
            logger.error("trackAllUserLocations external call failed: " + e);
            return false;
        }
    }

    public int getUserCount() {
        logger.info("getUserCount called");

        OkHttpClient.Builder httpClient = new OkHttpClient.Builder();
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl("http://" + ip + ":" + port + "/")
                .addConverterFactory(GsonConverterFactory.create())
                .client(httpClient.build())
                .build();

        UserServiceRetro userService = retrofit.create(UserServiceRetro.class);

        Call<Integer> callSync = userService.getUserCount();

        try {
            Response<Integer> response = callSync.execute();
            int value = response.body();
            logger.debug("getUserCount external call completed");
            return value;
        } catch (Exception e) {
            logger.error("getUserCount external call failed: " + e);
            return 0;
        }
    }

    public List<String> getAllUserNames() {
        logger.info("getAllUserNames called");

        OkHttpClient.Builder httpClient = new OkHttpClient.Builder();
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl("http://" + ip + ":" + port + "/")
                .addConverterFactory(GsonConverterFactory.create())
                .client(httpClient.build())
                .build();

        UserServiceRetro userService = retrofit.create(UserServiceRetro.class);

        Call<List<String>> callSync = userService.getAllUserNames();

        try {
            Response<List<String>> response = callSync.execute();
            List<String> value = response.body();
            logger.debug("getAllUserNames external call completed");
            return value;
        } catch (Exception e) {
            logger.error("getAllUserNames external call failed: " + e);
            return null;
        }
    }

}
