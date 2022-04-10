package userDocker.service;

import gpsUtil.location.Attraction;
import gpsUtil.location.VisitedLocation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import tourGuide.outputEntities.UserLocation;
import userDocker.model.User;
import userDocker.model.UserReward;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.stream.Collectors;

@Service
public class UserService {
    private Logger logger = LoggerFactory.getLogger(UserService.class);
    private ConcurrentMap<String, User> usersByName;

    public UserService() {
        final int CAPACITY = 100;
        usersByName = new ConcurrentHashMap<String, User>(CAPACITY);
    }

    public UserService(List<User> startingUsers) {
        for (User user : startingUsers){
            usersByName.put(user.getUserName(), user);
        }
    }

    public int addUsers(List<User> startingUsers) {
        for (User user : startingUsers){
            if(!usersByName.containsKey(user.getUserName())) {
                usersByName.put(user.getUserName(), user);
            }
        }
        return usersByName.size();
    }

    public boolean addUser(User user){
        if(!usersByName.containsKey(user.getUserName())) {
            usersByName.put(user.getUserName(), user);
            System.out.println(usersByName.size());
            return true;
        }
        return false;
    }

    public String addToVisitedLocations(VisitedLocation visitedLocation, String userName) {
        usersByName.get(userName).addToVisitedLocations(visitedLocation);
        return userName;
    }

    public List<UserLocation> getAllCurrentLocations() {
        List<UserLocation> userLocations = new ArrayList<>();
        usersByName.forEach((k,v)-> {
            userLocations.add(new UserLocation(v.getUserId(), v.getLastVisitedLocation()));
        });
        return userLocations;
    }

    public void addUserReward(String userName, VisitedLocation visitedLocation, Attraction attraction, int rewardPoints) {
        User user = getUserByUsername(userName);
        user.addUserReward(new UserReward(visitedLocation, attraction, rewardPoints));
    }

    public List<User> getAllUsers() {
        return usersByName.values().stream().collect(Collectors.toList());
    }

    public User getUserByUsername(String userName) {
        return usersByName.get(userName);
    }

    public VisitedLocation getLastVisitedLocationByName(String userName) {
        return getUserByUsername(userName).getLastVisitedLocation();
    }

    public List<UserReward> getUserRewardsByUsername(String userName){
        return getUserByUsername(userName).getUserRewards();
    }

}
