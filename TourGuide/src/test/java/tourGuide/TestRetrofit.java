package tourGuide;


import gpsUtil.location.Attraction;
import gpsUtil.location.VisitedLocation;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;

import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import tourGuide.remote.gps.GpsRetro;

import java.util.List;
import java.util.UUID;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

@ExtendWith(SpringExtension.class)
@SpringBootTest
@AutoConfigureMockMvc
public class TestRetrofit {

    @Ignore
    @Test
    public void testGpsRetroGetAttractions() {

        GpsRetro gpsRetro = new GpsRetro();
        List<Attraction> attractionList = gpsRetro.getAttractions();

        System.out.println("attractionList.size() = " + attractionList.size());
        assertTrue(attractionList.size() > 0);
    }

    @Ignore
    @Test
    public void testGpsRetroGetUserLocation() {

        GpsRetro gpsRetro = new GpsRetro();
        VisitedLocation location = gpsRetro.getUserLocation(new UUID(1231,1000));

        System.out.println("location = " + location.location.toString());
        assertNotNull(location);
    }

}
