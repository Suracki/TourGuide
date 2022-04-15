package tourGuide.dockers.gpsDocker.controller;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import tourGuide.dockers.gpsDocker.service.GpsService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import tourGuide.dockers.userDocker.gson.MoneyTypeAdapterFactory;

import java.util.UUID;

@Service
@RestController
public class GpsServiceController {

    private Logger logger = LoggerFactory.getLogger(GpsServiceController.class);

    private GpsService gpsService;

    Gson gson;

    public GpsServiceController(GpsService gpsService){
        this.gpsService = gpsService;
        gson = new GsonBuilder().registerTypeAdapterFactory(new MoneyTypeAdapterFactory()).create();
    }

    @GetMapping("/gps/getUserLocation")
    public String getUserLocation(@RequestParam UUID userId) {
        logger.info("/getUserLocation endpoint called");
        return gson.toJson(gpsService.getUserLocation(userId));
    }

    @GetMapping("/gps/getAttractions")
    public String getAttractions() {
        logger.info("/getAttractions endpoint called");
        return gson.toJson(gpsService.getAttractions());
    }

}
