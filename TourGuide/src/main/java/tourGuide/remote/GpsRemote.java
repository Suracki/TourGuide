package tourGuide.remote;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import gpsDocker.controller.GpsServiceController;
import gpsDocker.service.GpsService;
import gpsUtil.location.Attraction;
import gpsUtil.location.VisitedLocation;

import java.lang.reflect.Type;
import java.util.List;
import java.util.UUID;

public class GpsRemote {

    private GpsServiceController gpsServiceController;

    public GpsRemote(GpsService gpsService) {
        this.gpsServiceController = new GpsServiceController(gpsService);
    }

    public VisitedLocation getUserLocation(UUID userId) {
        System.out.println("GpsRemote getUserLocation");
        String json = gpsServiceController.getUserLocation(userId);
        Type listType = new TypeToken<VisitedLocation>(){}.getType();
        VisitedLocation visitedLocation = new Gson().fromJson(json, listType);
        return visitedLocation;
    }

    public List<Attraction> getAttractions() {
        System.out.println("GpsRemote getAttractions");
        String jsonListString = gpsServiceController.getAttractions();
        Type listType = new TypeToken<List<Attraction>>(){}.getType();
        List<Attraction> attractions = new Gson().fromJson(jsonListString, listType);
        return attractions;
    }

}
