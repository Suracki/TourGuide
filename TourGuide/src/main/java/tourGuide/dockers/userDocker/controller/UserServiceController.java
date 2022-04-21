package tourGuide.dockers.userDocker.controller;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import gpsUtil.location.Attraction;
import gpsUtil.location.VisitedLocation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.*;
import tourGuide.dockers.userDocker.gson.MoneyTypeAdapterFactory;
import tourGuide.dockers.userDocker.model.User;
import tourGuide.dockers.userDocker.service.UserService;


@RestController
public class UserServiceController {

    private Logger logger = LoggerFactory.getLogger(UserServiceController.class);

    UserService userService;

    Gson gson;

    public UserServiceController(UserService userService){
        this.userService = userService;
        gson = new GsonBuilder().registerTypeAdapterFactory(new MoneyTypeAdapterFactory()).create();
    }

    @PostMapping("/user/addUser")
    public boolean addUser(@RequestParam User user) {
        logger.info("/addUser endpoint called");
        return userService.addUser(user);
    }

    @PostMapping("user/addToVisitedLocations")
    public String addToVisitedLocations(@RequestParam VisitedLocation visitedLocation, @RequestParam String userName) {
        logger.info("/addToVisitedLocations endpoint called");
        return userService.addToVisitedLocations(visitedLocation, userName);
    }

    @GetMapping("user/getAllCurrentLocations")
    public String getAllCurrentLocations() {
        logger.info("/getAllCurrentLocations endpoint called");
        //List<UserLocation>
        return gson.toJson(userService.getAllCurrentLocations());
    }

    @PostMapping("user/addUserReward")
    public boolean addUserReward(@RequestParam String userName, @RequestParam VisitedLocation visitedLocation,
                              @RequestParam Attraction attraction, @RequestParam int rewardPoints) {
        logger.info("/addUserReward endpoint called");
        return userService.addUserReward(userName, visitedLocation, attraction, rewardPoints);
    }

    @GetMapping("user/getAllUsers")
    public String getAllUsers() {
        logger.info("/getAllUsers endpoint called");
        //List<User>
        return gson.toJson(userService.getAllUsers());
    }

    @GetMapping("/getUserByUsername")
    public String getUserByUsername(String userName) {
        logger.info("/getUser endpoint called");
        //User
        return gson.toJson(userService.getUserByUsername(userName));
    }

    @GetMapping("user/getLastVisitedLocationByName")
    public String getLastVisitedLocationByName(String userName) {
        logger.info("/getLastVisitedLocationByName endpoint called");
        //VisitedLocation
        return gson.toJson(userService.getLastVisitedLocationByName(userName));
    }

    @GetMapping("user/getUserRewardsByUsername")
    public String getUserRewardsByUsername(String userName) {
        logger.info("/getUserRewardsByUsername endpoint called");
        //List<UserReward>
        return gson.toJson(userService.getUserRewardsByUsername(userName));
    }

    @GetMapping("user/getVisitedLocationsByUsername")
    public String getVisitedLocationsByUsername(String userName) {
        logger.info("/getVisitedLocationsByUsername endpoint called");
        //List<VisitedLocation
        return gson.toJson(userService.getVisitedLocationsByUsername(userName));
    }

    @GetMapping("user/getUserIdByUsername")
    public String getUserIdByUsername(String userName) {
        logger.info("/getUserIdByUsername endpoint called");
        //UUID
        return gson.toJson(userService.getUserIdByUsername(userName));
    }

    @PostMapping("user/trackAllUserLocations")
    public boolean trackAllUserLocations() {
        logger.info("/trackAllUserLocations endpoint called");
        return userService.trackAllUserLocations();
    }

    @GetMapping("user/getUserCount")
    public String getUserCount() {
        logger.info("/getUserCount endpoint called");
        //int
        return gson.toJson(userService.getUserCount());
    }

    @GetMapping("user/getAllUserNames")
    public String getAllUserNames() {
        logger.info("/getAllUserNames endpoint called");
        //List<String>
        return gson.toJson(userService.getAllUserNames());
    }
}
