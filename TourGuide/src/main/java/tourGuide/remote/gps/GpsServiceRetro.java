package tourGuide.remote.gps;

import gpsUtil.location.Attraction;
import gpsUtil.location.VisitedLocation;
import org.springframework.stereotype.Service;
import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Query;

import java.util.List;
import java.util.UUID;

@Service
public interface GpsServiceRetro {

    @GET("/gps/getUserLocation")
    public Call<VisitedLocation> getUserLocation(@Query("userId") UUID userId);

    @GET("/gps/getAttractions")
    public Call<List<Attraction>> getAttractions();

}
