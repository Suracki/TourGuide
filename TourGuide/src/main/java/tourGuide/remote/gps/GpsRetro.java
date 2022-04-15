package tourGuide.remote.gps;

import gpsUtil.location.Attraction;
import gpsUtil.location.VisitedLocation;
import okhttp3.OkHttpClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import retrofit2.Call;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

import java.util.List;
import java.util.UUID;

@Service
public class GpsRetro {

    @Value("${docker.gps.ip}")
    private String ip = "127.0.0.1";

    @Value("${docker.gps.port}")
    private String port = "8081";

    private Logger logger = LoggerFactory.getLogger(GpsRetro.class);

    public GpsRetro() {}

    public VisitedLocation  getUserLocation(UUID userId) {
        logger.info("getUserLocation called");

        OkHttpClient.Builder httpClient = new OkHttpClient.Builder();
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl("http://" + ip + ":" + port +"/")
                .addConverterFactory(GsonConverterFactory.create())
                .client(httpClient.build())
                .build();

        GpsServiceRetro gpsService = retrofit.create(GpsServiceRetro.class);

        Call<VisitedLocation> callSync = gpsService.getUserLocation(userId);

        try {
            Response<VisitedLocation> response = callSync.execute();
            VisitedLocation userLocation = response.body();
            logger.debug("getUserLocation external call completed");
            return userLocation;
        }
        catch (Exception e){
            logger.error("getUserLocation external call failed: " + e);
            return null;
        }

    }



    public List<Attraction>  getAttractions() {
        logger.info("getAttractions called");

        OkHttpClient.Builder httpClient = new OkHttpClient.Builder();
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl("http://" + ip + ":" + port +"/")
                .addConverterFactory(GsonConverterFactory.create())
                .client(httpClient.build())
                .build();

        GpsServiceRetro gpsService = retrofit.create(GpsServiceRetro.class);
        Call<List<Attraction>> callSync = gpsService.getAttractions();

        try {
            Response<List<Attraction>> response = callSync.execute();
            List<Attraction> attractions = response.body();
            logger.debug("getAttractions external call completed");
            return attractions;
        }
        catch (Exception e){
            logger.error("getAttractions external call failed: " + e);
            return null;
        }

    }




}
