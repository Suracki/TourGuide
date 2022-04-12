package gpsDocker.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import gpsDocker.service.GpsService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import userDocker.gson.MoneyTypeAdapterFactory;

import java.util.UUID;

@RestController
public class GpsServiceController {

    private GpsService gpsService;

    Gson gson = new Gson();
    ObjectMapper objectMapper;

    public GpsServiceController(GpsService gpsService){
        this.gpsService = gpsService;
        gson = new GsonBuilder().registerTypeAdapterFactory(new MoneyTypeAdapterFactory()).create();
        objectMapper = new ObjectMapper();
    }

    @GetMapping("/getUserLocation")
    public String getUserLocation(@RequestParam UUID userId) {
        return gson.toJson(gpsService.getUserLocation(userId));
    }

    @GetMapping("/getAttractions")
    public String getAttractions() {
        return gson.toJson(gpsService.getAttractions());
    }

}
