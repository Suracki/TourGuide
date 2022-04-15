package tourGuide.remote;

import gpsUtil.location.Attraction;
import gpsUtil.location.VisitedLocation;
import okhttp3.OkHttpClient;
import org.springframework.stereotype.Service;
import retrofit2.Call;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

import java.util.List;
import java.util.UUID;

@Service
public class GpsRetro {


    public VisitedLocation  getUserLocation(UUID userId) {
        OkHttpClient.Builder httpClient = new OkHttpClient.Builder();
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl("http://127.0.0.1:8080/")
                .addConverterFactory(GsonConverterFactory.create())
                .client(httpClient.build())
                .build();

        GpsServiceRetro gpsService = retrofit.create(GpsServiceRetro.class);
        Call<VisitedLocation> callSync = gpsService.getUserLocation(userId);

        try {
            Response<VisitedLocation> response = callSync.execute();
            VisitedLocation userLocation = response.body();
            return userLocation;
        }
        catch (Exception e){
            System.out.println("Error: " + e);
            return null;
        }

    }



    public List<Attraction>  getAttractions() {
        OkHttpClient.Builder httpClient = new OkHttpClient.Builder();
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl("http://127.0.0.1:8081/")
                .addConverterFactory(GsonConverterFactory.create())
                .client(httpClient.build())
                .build();

        GpsServiceRetro gpsService = retrofit.create(GpsServiceRetro.class);
        Call<List<Attraction>> callSync = gpsService.getAttractions();

        try {
            Response<List<Attraction>> response = callSync.execute();
            List<Attraction> attractions = response.body();
            return attractions;
        }
        catch (Exception e){
            System.out.println("Error: " + e);
            return null;
        }

    }




}
