package userDocker.controller;

import gpsUtil.location.Attraction;
import gpsUtil.location.VisitedLocation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import tourGuide.outputEntities.UserLocation;
import userDocker.model.User;
import userDocker.model.UserReward;
import userDocker.service.UserService;

import java.util.List;

@RestController
public class UserServiceController {
    @Autowired
    UserService userService;

    @PostMapping("/addUser")
    public boolean addUser(@RequestParam User user) {
        return userService.addUser(user);
    }

    @PostMapping("/addToVisitedLocations")
    public String addToVisitedLocations(@RequestParam VisitedLocation visitedLocation, @RequestParam String userName) {
        return userService.addToVisitedLocations(visitedLocation, userName);
    }

    @GetMapping("/getAllCurrentLocations")
    public List<UserLocation> getAllCurrentLocations() {
        return userService.getAllCurrentLocations();
    }

    @PostMapping("/addUserReward")
    public void addUserReward(@RequestParam String userName, @RequestParam VisitedLocation visitedLocation,
                              @RequestParam Attraction attraction, @RequestParam int rewardPoints) {
        userService.addUserReward(userName, visitedLocation, attraction, rewardPoints);
    }

    @RequestMapping("/getAllUsers")
    public List<User> getAllUsers(){
        return userService.getAllUsers();
    }

    @GetMapping("/getUser")
    public User getUserByUsername(String userName) {
        return userService.getUserByUsername(userName);
    }

    @GetMapping("/getLastVisitedLocationByName")
    public VisitedLocation getLastVisitedLocationByName(String userName) {
        return userService.getLastVisitedLocationByName(userName);
    }

    @GetMapping("getUserRewardsByUsername")
    public List<UserReward> getUserRewardsByUsername(String userName) {
        return userService.getUserRewardsByUsername(userName);
    }

}
