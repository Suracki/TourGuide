package tourGuide.service;

import gpsUtil.location.Attraction;
import gpsUtil.location.VisitedLocation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import tourGuide.outputEntities.UserLocation;
import tourGuide.user.User;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
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

    public void addUser(User user){
        if(!usersByName.containsKey(user.getUserName())) {
            usersByName.put(user.getUserName(), user);
        }
    }

    public String addToVisitedLocationsThread(VisitedLocation visitedLocation, String userName) {
        new Thread( ()-> {
            usersByName.get(userName).addToVisitedLocations(visitedLocation);
        }).start();
        return userName;
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

    public void addUserReward(String userName, VisitedLocation visitedLocation, Attraction attraction) {
        System.out.println("ADDING REWARD?");
        //user.addUserReward(new UserReward(visitedLocation, attraction, getRewardPoints(attraction, user)));
    }

    public List<User> getAllUsers() {
        return usersByName.values().stream().collect(Collectors.toList());
    }

    public User getUserByUsername(String userName) {
        return usersByName.get(userName);
    }
}
