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
    private ConcurrentMap<UUID, User> usersByUuid;

    public UserService() {
        final int CAPACITY = 100;
        usersByUuid = new ConcurrentHashMap<UUID, User>(CAPACITY);
    }

    public UserService(List<User> startingUsers) {
        for (User user : startingUsers){
            usersByUuid.put(user.getUserId(), user);
        }
    }

    public int addUsers(List<User> startingUsers) {
        for (User user : startingUsers){
            if(!usersByUuid.containsKey(user.getUserId())) {
                usersByUuid.put(user.getUserId(), user);
            }
        }
        return usersByUuid.size();
    }

    public void addUser(User user){
        if(!usersByUuid.containsKey(user.getUserId())) {
            usersByUuid.put(user.getUserId(), user);
        }
    }

    public User getUserByUuid(UUID uuid) {
        return usersByUuid.get(uuid);
    }

    public UUID addToVisitedLocations(VisitedLocation visitedLocation, UUID userId) {
        new Thread( ()-> {
            usersByUuid.get(userId).addToVisitedLocations(visitedLocation);
        }).start();
        return userId;
    }

    public List<UserLocation> getAllCurrentLocations() {
        List<UserLocation> userLocations = new ArrayList<>();
        usersByUuid.forEach((k,v)-> {
            userLocations.add(new UserLocation(k, v.getLastVisitedLocation()));
        });
        return userLocations;
    }

    public void addUserReward(UUID userId, VisitedLocation visitedLocation, Attraction attraction) {
        System.out.println("ADDING REWARD?");
        //user.addUserReward(new UserReward(visitedLocation, attraction, getRewardPoints(attraction, user)));
    }

    public List<User> getAllUsers() {
        return usersByUuid.values().stream().collect(Collectors.toList());
    }

    public User getUserByUsername(String userName) {
        User[] user = new User[1];
        usersByUuid.forEach((k,v)-> {
            if (v.getUserName().equals(userName)) {
                user[0] = v;
            }
        });
        if (user.length == 0) {
            return null;
        }
        else {
            return user[0];
        }
    }
}
