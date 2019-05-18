package edu.khai.simonenko.util;

import edu.khai.simonenko.Settings;
import edu.khai.simonenko.domain.HeadPosition;
import edu.khai.simonenko.domain.Landmark;
import java.util.Map;

public final class DrowsinessCheckerUtil {

    private static double distanceBetweenLandmarks(Landmark first, Landmark second) {
        return Math.sqrt(Math.pow(second.getX() - first.getX(), 2)
                             + Math.pow(second.getY() - first.getY(), 2));
    }

    public static double getDrowsinessScore(Map<Integer, Landmark> landmarks) {
        double rightEyeDrowsiness =
            (distanceBetweenLandmarks(landmarks.get(13), landmarks.get(15)) + distanceBetweenLandmarks(landmarks.get(12), landmarks.get(16)) +
                distanceBetweenLandmarks(landmarks.get(11), landmarks.get(17))) /
                (3 * distanceBetweenLandmarks(landmarks.get(14), landmarks.get(10)));
        double leftEyeDrowsiness =
            (distanceBetweenLandmarks(landmarks.get(19), landmarks.get(25)) + distanceBetweenLandmarks(landmarks.get(20), landmarks.get(24)) +
                distanceBetweenLandmarks(landmarks.get(21), landmarks.get(23))) /
                (3 * distanceBetweenLandmarks(landmarks.get(18), landmarks.get(22)));
        return (rightEyeDrowsiness + leftEyeDrowsiness) / 2;
    }

    public static boolean areEyesClosed(Map<Integer, Landmark> landmarks) {
        return Settings.shouldDrowsinessBeDeterminedByTheEyes && getDrowsinessScore(landmarks) < Settings.minEyesClosedCoefficient;
    }

    public static boolean isRollCritical(float roll) {
        return Settings.shouldDrowsinessBeDeterminedByTheRoll && Math.abs(roll) > Settings.maxRollCoefficient;
    }

    public static boolean isYawCritical(float yaw) {
        return Settings.shouldDrowsinessBeDeterminedByTheYaw && Math.abs(yaw) > Settings.maxYawCoefficient;
    }

    public static boolean isPitchCritical(float pitch) {
        return Settings.shouldDrowsinessBeDeterminedByThePitch && Math.abs(pitch) > Settings.maxPitchCoefficient;
    }

    public static boolean isHeadTurned(float roll, float yaw, float pitch) {
        return isRollCritical(roll) || isYawCritical(yaw) || isPitchCritical(pitch);
    }

    public static boolean isHeadStable(HeadPosition previousHeadPosition, HeadPosition currentHeadPosition) {
        return Settings.shouldDrowsinessBeDeterminedByHeadStable &&
            Math.abs(currentHeadPosition.getPitch() - previousHeadPosition.getPitch()) < Settings.maxHeadMoveDelta &&
            Math.abs(currentHeadPosition.getRoll() - previousHeadPosition.getRoll()) < Settings.maxHeadMoveDelta &&
            Math.abs(currentHeadPosition.getYaw() - previousHeadPosition.getYaw()) < Settings.maxHeadMoveDelta;

    }

    public static boolean isPupilsStable(Map<Integer, Landmark> previousLandmarks, Map<Integer, Landmark> currentLandmarks) {
        return Settings.shouldDrowsinessBeDeterminedByPupilsStable &&
            Math.abs(currentLandmarks.get(76).getX() - previousLandmarks.get(76).getX()) < Settings.maxPupilsMoveDelta &&
            Math.abs(currentLandmarks.get(76).getY() - previousLandmarks.get(76).getY()) < Settings.maxPupilsMoveDelta &&
            Math.abs(currentLandmarks.get(77).getX() - previousLandmarks.get(77).getX()) < Settings.maxPupilsMoveDelta &&
            Math.abs(currentLandmarks.get(77).getY() - previousLandmarks.get(77).getY()) < Settings.maxPupilsMoveDelta;
    }
}