package edu.khai.simonenko;

public class Settings {

    public static Boolean isColourStreamEnabled;
    public static Boolean isFaceDetectionPrinted;
    public static Boolean isLandmarksDisplayed;
    public static Boolean isSoundActivatedWhenNoFaceFound;
    public static Boolean shouldDrowsinessBeDeterminedByTheEyes;
    public static Integer maxEyesClosedTime;
    public static Double minEyesClosedCoefficient;
    public static Integer maxHeadTurnedTime;
    public static Boolean shouldDrowsinessBeDeterminedByTheRoll;
    public static Double maxRollCoefficient;
    public static Boolean shouldDrowsinessBeDeterminedByTheYaw;
    public static Double maxYawCoefficient;
    public static Boolean shouldDrowsinessBeDeterminedByThePitch;
    public static Double maxPitchCoefficient;
    public static Boolean shouldDrowsinessBeDeterminedByHeadStable;
    public static Integer maxHeadStableTime;
    public static Double maxHeadMoveDelta;
    public static Boolean shouldDrowsinessBeDeterminedByPupilsStable;
    public static Integer maxPupilsStableTime;
    public static Double maxPupilsMoveDelta;
    public static Boolean shouldDrowsinessByCheckedInATimePeriod;
    public static Long timePeriodOfCheckingForDrowsiness;
    public static Double percentOfTimeWithClosedEyesToPlaySound;
}
