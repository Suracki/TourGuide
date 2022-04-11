package tourGuide.remote;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import gpsUtil.location.Attraction;
import gpsUtil.location.VisitedLocation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestParam;
import tourGuide.outputEntities.UserLocation;
import userDocker.controller.UserServiceController;
import userDocker.model.User;
import userDocker.model.UserReward;
import userDocker.service.UserService;

import java.lang.reflect.Type;
import java.util.List;
import java.util.UUID;

public class UserRemote {

    UserService userService;

    UserServiceController userServiceController;

    public UserRemote(UserService userService) {
        this.userService = userService;
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

    public void addUserReward(@RequestParam String userName, @RequestParam VisitedLocation visitedLocation,
                              @RequestParam Attraction attraction, @RequestParam int rewardPoints) {
        userServiceController.addUserReward(userName, visitedLocation, attraction, rewardPoints);
    }

    public List<User> getAllUsers(){
//        String jsonListString = userServiceController.getAllUsers();
//        Type listType = new TypeToken<List<User>>(){}.getType();
//        List<User> allUsers = new Gson().fromJson(jsonListString, listType);
//        return allUsers;
        return userService.getAllUsers();
    }

    public User getUserByUsername(String userName) {
//        String json = userServiceController.getUserByUsername(userName);
//        Type userType = new TypeToken<User>(){}.getType();
//        User user = new Gson().fromJson(json, userType);
//        return user;
        return userService.getUserByUsername(userName);
    }

    public VisitedLocation getLastVisitedLocationByName(String userName) {
        System.out.println("TESTED");
        String json = userServiceController.getLastVisitedLocationByName(userName);
        Type type = new TypeToken<VisitedLocation>(){}.getType();
        VisitedLocation visitedLocation = new Gson().fromJson(json, type);
        return visitedLocation;
        //return userService.getLastVisitedLocationByName(userName);
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

}
