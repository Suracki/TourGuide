package tourGuide.service;

import gpsUtil.location.Attraction;
import gpsUtil.location.VisitedLocation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import tourGuide.outputEntities.UserLocation;
import tourGuide.user.User;
import tourGuide.user.UserReward;

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
            return true;
        }
        return false;
    }

    public boolean addToVisitedLocations(VisitedLocation visitedLocation, String userName) {
        User user = usersByName.get(userName);
        if (user != null) {
            user.addToVisitedLocations(visitedLocation);
            return true;
        }
        return false;
    }

    public List<UserLocation> getAllCurrentLocations() {
        List<UserLocation> userLocations = new ArrayList<>();
        usersByName.forEach((k,v)-> {
            userLocations.add(new UserLocation(v.getUserId(), v.getLastVisitedLocation()));
        });
        return userLocations;
    }

    public boolean addUserReward(String userName, VisitedLocation visitedLocation, Attraction attraction, int rewardPoints) {
        System.out.println("adding reward");
        User user = getUserByUsername(userName);
        if (user != null) {
            user.addUserReward(new UserReward(visitedLocation, attraction, rewardPoints));
            return true;
        }
        return false;
    }

    public List<User> getAllUsers() {
        return usersByName.values().stream().collect(Collectors.toList());
    }

    public User getUserByUsername(String userName) {
        return usersByName.get(userName);
    }

    public int getUserCount() {
        return usersByName.size();
    }
}
