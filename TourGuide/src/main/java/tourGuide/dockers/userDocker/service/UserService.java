package tourGuide.dockers.userDocker.service;

import gpsUtil.location.Attraction;
import gpsUtil.location.VisitedLocation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import tourGuide.outputEntities.UserLocation;
import tourGuide.dockers.userDocker.model.User;
import tourGuide.dockers.userDocker.model.UserReward;
import tourGuide.remote.gps.GpsRetro;

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
    private final GpsRetro gpsRetro;

    public UserService(GpsRetro gpsRetro) {
        this.gpsRetro = gpsRetro;
        final int CAPACITY = 100;
        usersByName = new ConcurrentHashMap<String, User>(CAPACITY);
    }

//    public int addUsers(List<User> startingUsers) {
//        for (User user : startingUsers){
//            if(!usersByName.containsKey(user.getUserName())) {
//                usersByName.put(user.getUserName(), user);
//            }
//        }
//        return usersByName.size();
//    }

    public boolean addUser(User user){
        logger.debug("addUser called");
        if(!usersByName.containsKey(user.getUserName())) {
            logger.debug("userName not already in map, adding user");
            usersByName.put(user.getUserName(), user);
            return true;
        }
        logger.debug("userName is already in map, failed to add user");
        return false;
    }

    //TODO: add fail case for user not found
    public String addToVisitedLocations(VisitedLocation visitedLocation, String userName) {
        logger.debug("addToVisitedLocations called");
        usersByName.get(userName).addToVisitedLocations(visitedLocation);
        return userName;
    }

    public List<UserLocation> getAllCurrentLocations() {
        logger.debug("getAllCurrentLocations called");
        List<UserLocation> userLocations = new ArrayList<>();
        usersByName.forEach((k,v)-> {
            userLocations.add(new UserLocation(v.getUserId(), v.getLastVisitedLocation()));
        });
        logger.debug("returning " + userLocations.size() + " UserLocations");
        return userLocations;
    }

    //TODO: add fail case for user not found
    public boolean addUserReward(String userName, VisitedLocation visitedLocation, Attraction attraction, int rewardPoints) {
        logger.debug("addUserReward called");
        User user = getUserByUsername(userName);
        user.addUserReward(new UserReward(visitedLocation, attraction, rewardPoints));
        return true;
    }

    public List<User> getAllUsers() {
        logger.debug("getAllUsers called");
        logger.debug("returning " + usersByName.size() + " Users");
        return usersByName.values().stream().collect(Collectors.toList());
    }

    //TODO: add fail case for user not found
    public User getUserByUsername(String userName) {
        logger.debug("getUserByUsername called");
        return usersByName.get(userName);
    }

    //TODO: add fail case for user not found
    public VisitedLocation getLastVisitedLocationByName(String userName) {
        logger.debug("getLastVisitedLocationByName called");
        return getUserByUsername(userName).getLastVisitedLocation();
    }

    //TODO: add fail case for user not found
    public List<VisitedLocation> getVisitedLocationsByUsername(String userName) {
        logger.debug("getVisitedLocationsByUsername called");
        return getUserByUsername(userName).getVisitedLocations();
    }

    //TODO: add fail case for user not found
    public List<UserReward> getUserRewardsByUsername(String userName){
        logger.debug("getUserRewardsByUsername called");
        return getUserByUsername(userName).getUserRewards();
    }

    //TODO: add fail case for user not found
    public UUID getUserIdByUsername(String userName) {
        logger.debug("getUserIdByUsername called");
        return getUserByUsername(userName).getUserId();
    }

    //TODO: add fail case
    public boolean trackAllUserLocations() {
        logger.debug("trackAllUserLocations called");

        List<User> allUsers = getAllUsers();
        ArrayList<Thread> threads = new ArrayList<>();

        logger.debug("Creating threads for " + allUsers.size() + " user(s)");

        allUsers.forEach((n)-> {
            threads.add(
                    new Thread( ()-> {
                        addToVisitedLocations(gpsRetro.getUserLocation(n.getUserId()), n.getUserName());
                    })
            );
        });

        logger.debug("Threads created: " + threads.size() + ", calling start()...");
        threads.forEach((n)->n.start());
        threads.forEach((n)-> {
            try {
                n.join();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        });
        logger.debug("Threads join()ed, returning.");
        return true;
    }

    public int getUserCount() {
        logger.debug("getUserCount called");
        return usersByName.size();
    }

    public List<String> getAllUserNames() {
        logger.debug("getAllUserNames called");
        logger.debug("returning " + usersByName.size() + " UserNames");
        return usersByName.values().stream().map(User::getUserName).collect(Collectors.toList());
    }
}
