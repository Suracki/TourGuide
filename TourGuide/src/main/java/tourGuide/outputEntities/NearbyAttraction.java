package tourGuide.outputEntities;

public class NearbyAttraction {
    String attractionName;
    Double attractionLatitute;
    Double attractionLongitude;
    Double userLatitute;
    Double userLongitude;
    Double distance;
    int rewardPoints;

    public NearbyAttraction(String attractionName, Double attractionLatitute, Double attractionLongitude, Double userLatitute, Double userLongitude, Double distance, int rewardPoints) {
        this.attractionName = attractionName;
        this.attractionLatitute = attractionLatitute;
        this.attractionLongitude = attractionLongitude;
        this.userLatitute = userLatitute;
        this.userLongitude = userLongitude;
        this.distance = distance;
        this.rewardPoints = rewardPoints;
    }
}
