package tourGuide.remote;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import gpsUtil.location.Attraction;
import gpsUtil.location.VisitedLocation;
import org.springframework.web.bind.annotation.RequestParam;
import tourGuide.outputEntities.UserLocation;
import userDocker.controller.UserServiceController;
import userDocker.gson.MoneyTypeAdapterFactory;
import userDocker.model.User;
import userDocker.model.UserReward;
import userDocker.service.UserService;

import java.lang.reflect.Type;
import java.util.List;
import java.util.UUID;

public class UserRemote {

    UserServiceController userServiceController;

    public UserRemote(UserService userService) {
        this.userServiceController = new UserServiceController(userService);
    }

    public boolean addUser(@RequestParam User user) {
        return userServiceController.addUser(user);
    }

    public String addToVisitedLocations(@RequestParam VisitedLocation visitedLocation, @RequestParam String userName) {
        return userServiceController.addToVisitedLocations(visitedLocation, userName);
    }

    public List<UserLocation> getAllCurrentLocations() {
        String jsonListString = userServiceController.getAllCurrentLocations();
        Type listType = new TypeToken<List<UserLocation>>(){}.getType();
        List<UserLocation> allCurrentLocations = new Gson().fromJson(jsonListString, listType);
        return allCurrentLocations;
    }

    public void addUserReward(String userName, VisitedLocation visitedLocation,
                              Attraction attraction, int rewardPoints) {
        userServiceController.addUserReward(userName, visitedLocation, attraction, rewardPoints);
    }

    public List<User> getAllUsers(){
        Gson gson = new GsonBuilder().registerTypeAdapterFactory(new MoneyTypeAdapterFactory()).create();
        String json = userServiceController.getAllUsers();
        Type listType = new TypeToken<List<User>>(){}.getType();
        List<User> allUsers = gson.fromJson(json, listType);
        return allUsers;
    }

    public User getUserByUsername(String userName) {
        Gson gson = new GsonBuilder().registerTypeAdapterFactory(new MoneyTypeAdapterFactory()).create();
        String json = userServiceController.getUserByUsername(userName);
        Type userType = new TypeToken<User>(){}.getType();
        User user = gson.fromJson(json, userType);
        return user;
    }

    public VisitedLocation getLastVisitedLocationByName(String userName) {
        System.out.println("getLastVisitedLocationByName CALL");
        String json = userServiceController.getLastVisitedLocationByName(userName);
        Type type = new TypeToken<VisitedLocation>(){}.getType();
        VisitedLocation visitedLocation = new Gson().fromJson(json, type);
        System.out.println("getLastVisitedLocationByName RETURN");
        return visitedLocation;
    }

    //Works
    public List<UserReward> getUserRewardsByUsername(String userName) {
        String json = userServiceController.getUserRewardsByUsername(userName);
        Type userType = new TypeToken<List<UserReward>>(){}.getType();
        List<UserReward> userRewards = new Gson().fromJson(json, userType);
        return userRewards;
    }

    public List<VisitedLocation> getVisitedLocationsByUsername(String userName) {
        String json = userServiceController.getVisitedLocationsByUsername(userName);
        Type userType = new TypeToken<List<VisitedLocation>>(){}.getType();
        List<VisitedLocation> visitedLocations = new Gson().fromJson(json, userType);
        return visitedLocations;
    }

    //Works
    public UUID getUserIdByUsername(String userName) {
        return new Gson().fromJson(userServiceController.getUserIdByUsername(userName), UUID.class);
        //return userServiceController.getUserIdByUsername(userName);
    }

    public void trackAllUserLocations() {
        System.out.println("trackAllUserLocations remote call");
        userServiceController.trackAllUserLocations();
    }

    public int getUserCount() {
        String json = userServiceController.getUserCount();
        int userCount = new Gson().fromJson(json, int.class);
        return userCount;
    }

    public List<String> getAllUserNames() {
        String json = userServiceController.getAllUserNames();
        Type listType = new TypeToken<List<String>>(){}.getType();
        List<String> userNames = new Gson().fromJson(json, listType);
        return userNames;
    }
}
