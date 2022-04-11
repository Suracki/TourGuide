package userDocker.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import gpsUtil.location.Attraction;
import gpsUtil.location.VisitedLocation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import tourGuide.outputEntities.UserLocation;
import userDocker.gson.MoneyTypeAdapterFactory;
import userDocker.model.User;
import userDocker.model.UserReward;
import userDocker.service.UserService;

import java.io.IOException;
import java.util.List;

@RestController
public class UserServiceController {
    UserService userService;

    Gson gson = new Gson();
    ObjectMapper objectMapper;

    public UserServiceController(UserService userService){
        this.userService = userService;
        gson = new GsonBuilder().registerTypeAdapterFactory(new MoneyTypeAdapterFactory()).create();
        objectMapper = new ObjectMapper();
    }

    @PostMapping("/addUser")
    public boolean addUser(@RequestParam User user) {
        return userService.addUser(user);
    }

    @PostMapping("/addToVisitedLocations")
    public String addToVisitedLocations(@RequestParam VisitedLocation visitedLocation, @RequestParam String userName) {
        return userService.addToVisitedLocations(visitedLocation, userName);
    }

    @GetMapping("/getAllCurrentLocations")
    public String getAllCurrentLocations() {
        //List<UserLocation>
        return gson.toJson(userService.getAllCurrentLocations());
    }

    @PostMapping("/addUserReward")
    public void addUserReward(@RequestParam String userName, @RequestParam VisitedLocation visitedLocation,
                              @RequestParam Attraction attraction, @RequestParam int rewardPoints) {
        userService.addUserReward(userName, visitedLocation, attraction, rewardPoints);
    }

    @RequestMapping("/getAllUsers")
    public String getAllUsers() {
        //List<User>
        try {
            String json = objectMapper.writeValueAsString(userService.getAllUsers());
            System.out.println(json);
            return json;
        }
        catch (Exception e) {
            System.out.println("Exception: " + e);
            return null;
        }

        //return gson.toJson(userService.getAllUsers());
    }

    @GetMapping("/getUser")
    public String getUserByUsername(String userName) {
        //User
        return gson.toJson(userService.getUserByUsername(userName));
    }

    @GetMapping("/getLastVisitedLocationByName")
    public String getLastVisitedLocationByName(String userName) {
        //VisitedLocation
        return gson.toJson(userService.getLastVisitedLocationByName(userName));
    }

    @GetMapping("getUserRewardsByUsername")
    public String getUserRewardsByUsername(String userName) {
        //List<UserReward>
        return gson.toJson(userService.getUserRewardsByUsername(userName));
    }

    @GetMapping("getVisitedLocationsByUsername")
    public String getVisitedLocationsByUsername(String userName) {
        //List<VisitedLocation
        return gson.toJson(userService.getVisitedLocationsByUsername(userName));
    }

    @GetMapping("getUserIdByUsername")
    public String getUserIdByUsername(String userName) {
        //List<VisitedLocation
        return gson.toJson(userService.getUserIdByUsername(userName));
    }

    @GetMapping("trackAllUserLocations")
    public void trackAllUserLocations() {
        System.out.println("trackAllUserLocations controller call");
        userService.trackAllUserLocations();
    }
}
