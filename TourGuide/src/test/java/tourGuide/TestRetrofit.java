package tourGuide;


import gpsUtil.location.Attraction;
import org.apache.commons.lang3.time.StopWatch;
import org.junit.Test;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;

import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;
import tourGuide.remote.GpsRetro;

import java.util.List;

import static org.junit.Assert.assertTrue;

@ExtendWith(SpringExtension.class)
@SpringBootTest
@AutoConfigureMockMvc
public class TestRetrofit {

    private static final int NUMBER_OF_TEST_USERS = 100;

    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private WebApplicationContext webApplicationContext;

    @BeforeEach
    public void setup() {
        mockMvc = MockMvcBuilders
                .webAppContextSetup(webApplicationContext)
                .build();
    }

    @Test
    public void testGpsRetro() {
        //GpsRemote gpsRemote = new GpsRemote(new GpsService(new GpsUtil()));
        //UserRemote userRemote = new UserRemote(new UserService(gpsRemote));
        //RewardsRemote rewardsRemote = new RewardsRemote(new RewardsService(gpsRemote, new RewardCentral(), userRemote));
        // Users should be incremented up to 100,000, and test finishes within 15 minutes
        //InternalTestHelper.setInternalUserNumber(NUMBER_OF_TEST_USERS);
        //TourGuideService tourGuideService = new TourGuideService(gpsRemote, rewardsRemote, userRemote);

        StopWatch stopWatch = new StopWatch();
        stopWatch.start();
        GpsRetro gpsRetro = new GpsRetro();
        List<Attraction> attractionList = gpsRetro.getAttractions();
        stopWatch.stop();
        //tourGuideService.tracker.stopTracking();

        System.out.println("attractionList.size() = " + attractionList.size());
        assertTrue(attractionList.size() > 0);
    }

}
