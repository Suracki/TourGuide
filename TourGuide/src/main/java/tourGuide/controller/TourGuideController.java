package tourGuide.controller;

import java.util.List;
import java.util.UUID;

import gpsUtil.location.Location;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.jsoniter.output.JsonStream;

import gpsUtil.location.VisitedLocation;
import tourGuide.service.TourGuideService;
import tourGuide.user.User;
import tripPricer.Provider;

/**
 * RestController for TourGuide endpoints
 *
 */
@RestController
public class TourGuideController {

	@Autowired
	TourGuideService tourGuideService;

    /**
     * Mapping for root url
     *
     * Takes no parameters, simply returns welcome message
     *
     * @return welcome message String
     */
    @RequestMapping("/")
    public String index() {
        return "Greetings from TourGuide!";
    }

    /**
     * Mapping for Get
     *
     * Returns:
     * User location if user exists, in format: {"longitude":xxx,"latitude":yyy}
     * "User Not Found [userName]" if user is not in system
     *
     * @param userName name of user
     * @return Json string of user's current location
     */
    @GetMapping("/getLocation")
    public String getLocation(@RequestParam String userName) {
    	VisitedLocation visitedLocation = tourGuideService.getUserLocation(userName);
        if (visitedLocation == null) {
            return JsonStream.serialize("User Not Found [" + userName + "]");
        }
		return JsonStream.serialize(visitedLocation.location);
    }

    /**
     * Mapping for Get
     *
     * Returns:
     * Closest five tourist attractions for the user, regardless of distance.
     * User will always receive atleast five attractions (unless the system contains less than 5 total attractions)
     * Response includes:
     *  -Name of attraction
     *  -Attraction's lat/long
     *  -User's lat/long
     *  -Distance in miles between user's location and attraction
     *  -Reward points value for this attraction/user combination
     *
     * @param userName name of user
     * @return Json string of user's closest five attractions.
     */
    @GetMapping("/getNearbyAttractions")
    public String getNearbyAttractions(@RequestParam String userName) {
    	VisitedLocation visitedLocation = tourGuideService.getUserLocation(userName);
    	return JsonStream.serialize(tourGuideService.getNearByAttractions(visitedLocation));
    }

    /**
     * Mapping for Get
     *
     * Returns:
     * Json object of user's current rewards, in the form of a List of UserReward objects
     *
     * @param userName name of user
     * @return Json string of user's closest five attractions.
     */
    @GetMapping("/getRewards")
    public String getRewards(@RequestParam String userName) {
    	return JsonStream.serialize(tourGuideService.getUserRewards(userName));
    }

    /**
     * Mapping for Get
     *
     * Returns:
     * Json object of all users' current locations, in the format:
     * {"userId":idString,"longitude":xxx,"latitude":yyy},{"userId":idString,"longitude":xxx,"latitude":yyy}]
     * If no users are stored, an empty list is returned
     *
     * @return Json string of all users' current locations.
     */
    @GetMapping("/getAllCurrentLocations")
    public String getAllCurrentLocations() {

    	return JsonStream.serialize(tourGuideService.getAllCurrentLocations());
    }

    /**
     * Mapping for Get
     *
     * Returns:
     * Json object containing 5 trip deals for user. Includes the attraction name, price, and tripId.
     * "User Not Found [userName]" if user is not in system
     *
     * @param userName user's userName
     * @return Json string of all users' current locations.
     */
    @GetMapping("/getTripDeals")
    public String getTripDeals(@RequestParam String userName) {
    	List<Provider> providers = tourGuideService.getTripDeals(userName);
        if (providers == null) {
            return JsonStream.serialize("User Not Found [" + userName + "]");
        }
    	return JsonStream.serialize(providers);
    }
}