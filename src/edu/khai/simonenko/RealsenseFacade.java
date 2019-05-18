package edu.khai.simonenko;

import edu.khai.simonenko.domain.DrawFrame;
import edu.khai.simonenko.domain.HeadPosition;
import edu.khai.simonenko.domain.Landmark;
import edu.khai.simonenko.domain.Listener;
import edu.khai.simonenko.domain.ProgramMode;
import edu.khai.simonenko.util.DrowsinessCheckerUtil;
import edu.khai.simonenko.util.SettingsUtil;
import edu.khai.simonenko.util.SoundUtil;
import edu.khai.simonenko.util.SwingComponentUtils;
import intel.rssdk.PXCMCapture;
import intel.rssdk.PXCMFaceConfiguration;
import intel.rssdk.PXCMFaceData;
import intel.rssdk.PXCMFaceModule;
import intel.rssdk.PXCMImage;
import intel.rssdk.PXCMRectI32;
import intel.rssdk.PXCMSenseManager;
import intel.rssdk.pxcmStatus;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.image.BufferedImage;
import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;

public class RealsenseFacade {

    private final static String NOT_FOUND_SOUND = "resources/notfound.wav";
    private final static String BEEP_SOUND = "resources/beep.wav";
    private final static String DROWSINESS_SOUND = "resources/retro_sfx_loop_1.wav";

    private final static int C_WIDTH = 640;
    private final static int C_HEIGHT = 480;

    private static int dWidth, dHeight;
    private static DrawFrame c_df;
    private static DrawFrame d_df;
    private static JFrame frame;
    private static JLabel areEyesClosedLabel;
    private static JLabel isRollCriticalLabel;
    private static JLabel isYawCriticalLabel;
    private static JLabel isPitchCriticalLabel;
    private static JLabel isHeadStableLabel;
    private static JLabel isPupilsStableLabel;

    private static PXCMSenseManager senseMgr;
    private static pxcmStatus sts;
    private static PXCMFaceData faceData;
    private static Listener listener;

    public static void run(ProgramMode mode, String fileName) {
        configure(mode, fileName);
        createWindows();
        runStream();
    }

    private static void configure(ProgramMode mode, String fileName) {
        senseMgr = PXCMSenseManager.CreateInstance();
        System.out.println(senseMgr != null ? "SenseManager created" : "SenseManager is null");

        sts = senseMgr.EnableStream(PXCMCapture.StreamType.STREAM_TYPE_COLOR, C_WIDTH, C_HEIGHT);
        System.out.println(!sts.isError() ? "Color stream enabled" : "Color stream isn't enabled because " + sts);

        sts = senseMgr.EnableStream(PXCMCapture.StreamType.STREAM_TYPE_DEPTH);
        System.out.println(!sts.isError() ? "Depth stream enabled" : "Depth stream isn't enabled because " + sts);

        sts = senseMgr.EnableFace(null);
        System.out.println(!sts.isError() ? "Face is fine" : "Face is not fine because " + sts);

        PXCMFaceModule faceModule = senseMgr.QueryFace();
        PXCMFaceConfiguration faceConf = faceModule.CreateActiveConfiguration();
        faceConf.SetTrackingMode(PXCMFaceConfiguration.TrackingModeType.FACE_MODE_COLOR_PLUS_DEPTH);

        switch (mode) {
            case PLAYBACK:
                senseMgr.QueryCaptureManager().SetFileName(fileName, false);
                break;
            case RECORD:
                senseMgr.QueryCaptureManager().SetFileName(fileName, true);
                break;
        }

        sts = senseMgr.Init();
        faceData = faceModule.CreateOutput();

        PXCMCapture.Device device = senseMgr.QueryCaptureManager().QueryDevice();
        PXCMCapture.Device.StreamProfileSet profiles = new PXCMCapture.Device.StreamProfileSet();
        device.QueryStreamProfileSet(profiles);

        dWidth = profiles.depth.imageInfo.width;
        dHeight = profiles.depth.imageInfo.height;
    }

    private static void createWindows() {
        listener = new Listener();

        frame = new JFrame("Intel(R) RealSense(TM) SDK");
        frame.addWindowListener(listener);
        frame.setLayout(new GridBagLayout());
        GridBagConstraints gridBagConstraints = new GridBagConstraints();

        d_df = new DrawFrame(dWidth, dHeight);
        d_df.setVisible(!Settings.isColourStreamEnabled);
        gridBagConstraints.weighty = 0;
        gridBagConstraints.gridheight = 26;
        gridBagConstraints.gridwidth = 1;
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        frame.add(d_df, gridBagConstraints);

        c_df = new DrawFrame(C_WIDTH, C_HEIGHT);
        c_df.setVisible(Settings.isColourStreamEnabled);
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 0;
        frame.add(c_df, gridBagConstraints);

        gridBagConstraints.gridheight = 1;

        ItemListener colourStreamItemListener = e -> {
            Settings.isColourStreamEnabled = e.getStateChange() == ItemEvent.SELECTED;
            d_df.setVisible(!Settings.isColourStreamEnabled);
            c_df.setVisible(Settings.isColourStreamEnabled);
        };
        SwingComponentUtils
            .createCheckBox("Is colour stream enabled", Settings.isColourStreamEnabled,
                            "isColourStreamEnabled", gridBagConstraints, 2, 0, frame, colourStreamItemListener);

        SwingComponentUtils
            .createCheckBox("Is face detection printed", Settings.isFaceDetectionPrinted,
                            "isFaceDetectionPrinted", gridBagConstraints, 2, 1, frame);

        SwingComponentUtils
            .createCheckBox("Is landmarks displayed", Settings.isLandmarksDisplayed,
                            "isLandmarksDisplayed", gridBagConstraints, 2, 2, frame);

        SwingComponentUtils
            .createCheckBox("Is sound activated when no face found", Settings.isSoundActivatedWhenNoFaceFound,
                            "isSoundActivatedWhenNoFaceFound", gridBagConstraints, 2, 3, frame);

        SwingComponentUtils
            .createCheckBox("Should drowsiness be determined by the eyes", Settings.shouldDrowsinessBeDeterminedByTheEyes,
                            "shouldDrowsinessBeDeterminedByTheEyes", gridBagConstraints, 2, 4, frame);

        SwingComponentUtils
            .createTextFieldPanel("Max eyes closed time, ms", Settings.maxEyesClosedTime.toString(), "maxEyesClosedTime",
                                  gridBagConstraints, 2, 5, frame);

        SwingComponentUtils
            .createTextFieldPanel("Min eyes closed coefficient", Settings.minEyesClosedCoefficient.toString(), "minEyesClosedCoefficient",
                                  gridBagConstraints, 2, 6, frame);

        SwingComponentUtils
            .createTextFieldPanel("Max head turned time, ms", Settings.maxHeadTurnedTime.toString(), "maxHeadTurnedTime",
                                  gridBagConstraints, 2, 7, frame);

        SwingComponentUtils
            .createCheckBox("Should drowsiness be determined by the roll", Settings.shouldDrowsinessBeDeterminedByTheRoll,
                            "shouldDrowsinessBeDeterminedByTheRoll", gridBagConstraints, 2, 8, frame);

        SwingComponentUtils
            .createTextFieldPanel("Max roll coefficient", Settings.maxRollCoefficient.toString(), "maxRollCoefficient",
                                  gridBagConstraints, 2, 9, frame);

        SwingComponentUtils
            .createCheckBox("Should drowsiness be determined by the yaw", Settings.shouldDrowsinessBeDeterminedByTheYaw,
                            "shouldDrowsinessBeDeterminedByTheYaw", gridBagConstraints, 2, 10, frame);

        SwingComponentUtils
            .createTextFieldPanel("Max yaw coefficient", Settings.maxYawCoefficient.toString(), "maxYawCoefficient",
                                  gridBagConstraints, 2, 11, frame);

        SwingComponentUtils
            .createCheckBox("Should drowsiness be determined by the pitch", Settings.shouldDrowsinessBeDeterminedByThePitch,
                            "shouldDrowsinessBeDeterminedByThePitch", gridBagConstraints, 2, 12, frame);

        SwingComponentUtils
            .createTextFieldPanel("Max pitch coefficient", Settings.maxPitchCoefficient.toString(), "maxPitchCoefficient",
                                  gridBagConstraints, 2, 13, frame);

        SwingComponentUtils
            .createCheckBox("Should drowsiness be determined by head stable", Settings.shouldDrowsinessBeDeterminedByHeadStable,
                            "shouldDrowsinessBeDeterminedByHeadStable", gridBagConstraints, 2, 14, frame);

        SwingComponentUtils
            .createTextFieldPanel("Max head stable time, ms", Settings.maxHeadStableTime.toString(), "maxHeadStableTime",
                                  gridBagConstraints, 2, 15, frame);

        SwingComponentUtils
            .createTextFieldPanel("Max head's move delta", Settings.maxHeadMoveDelta.toString(), "maxHeadMoveDelta",
                                  gridBagConstraints, 2, 16, frame);

        SwingComponentUtils
            .createCheckBox("Should drowsiness be determined by pupils stable", Settings.shouldDrowsinessBeDeterminedByPupilsStable,
                            "shouldDrowsinessBeDeterminedByPupilsStable", gridBagConstraints, 2, 17, frame);

        SwingComponentUtils
            .createTextFieldPanel("Max pupils stable time, ms", Settings.maxPupilsStableTime.toString(), "maxPupilsStableTime",
                                  gridBagConstraints, 2, 18, frame);

        SwingComponentUtils
            .createTextFieldPanel("Max pupils' move delta", Settings.maxPupilsMoveDelta.toString(), "maxPupilsMoveDelta",
                                  gridBagConstraints, 2, 19, frame);

        SwingComponentUtils
            .createCheckBox("Should drowsiness be checked in a time period", Settings.shouldDrowsinessByCheckedInATimePeriod,
                            "shouldDrowsinessByCheckedInATimePeriod", gridBagConstraints, 2, 20, frame);

        SwingComponentUtils
            .createTextFieldPanel("Time period of checking for drowsiness, ms", Settings.timePeriodOfCheckingForDrowsiness.toString(),
                                  "timePeriodOfCheckingForDrowsiness",
                                  gridBagConstraints, 2, 21, frame);

        SwingComponentUtils
            .createTextFieldPanel("Percent of time with closed eyes to play sound", Settings.percentOfTimeWithClosedEyesToPlaySound.toString(),
                                  "percentOfTimeWithClosedEyesToPlaySound",
                                  gridBagConstraints, 2, 22, frame);

        SwingComponentUtils
            .createComboBoxPanel("Settings file profile", Arrays.stream(new File("settings").list()).map(el -> el.replace(".xml", "")).toArray(),
                                 gridBagConstraints, 3, 0, frame);

        JPanel saveSettingsPanel = new JPanel();
        JLabel saveSettingsText = new JLabel("Name of profile");
        JTextField saveSettingsValue = new JTextField("", 10);
        JButton saveSettingsButton = new JButton("Save");
        saveSettingsButton.addActionListener(e -> {
            SettingsUtil.saveSettings(new File("settings/" + saveSettingsValue.getText() + ".xml"));
        });
        saveSettingsButton.setFont(new Font("Dialog", Font.PLAIN, 8));
        saveSettingsButton.setPreferredSize(new Dimension(40, 18));
        saveSettingsButton.setMargin(new Insets(0, 0, 0, 0));

        saveSettingsPanel.add(saveSettingsText);
        saveSettingsPanel.add(saveSettingsValue);
        saveSettingsPanel.add(saveSettingsButton);
        gridBagConstraints.gridx = 3;
        gridBagConstraints.gridy = 1;
        frame.add(saveSettingsPanel, gridBagConstraints);

        JPanel areEyesClosedPanel = new JPanel();
        areEyesClosedLabel = new JLabel();
        gridBagConstraints.gridx = 3;
        gridBagConstraints.gridy = 2;
        areEyesClosedPanel.add(areEyesClosedLabel);
        frame.add(areEyesClosedPanel, gridBagConstraints);

        JPanel isRollCriticalPanel = new JPanel();
        isRollCriticalLabel = new JLabel();
        gridBagConstraints.gridx = 3;
        gridBagConstraints.gridy = 3;
        isRollCriticalPanel.add(isRollCriticalLabel);
        frame.add(isRollCriticalPanel, gridBagConstraints);

        JPanel isYawCriticalPanel = new JPanel();
        isYawCriticalLabel = new JLabel();
        gridBagConstraints.gridx = 3;
        gridBagConstraints.gridy = 4;
        isYawCriticalPanel.add(isYawCriticalLabel);
        frame.add(isYawCriticalPanel, gridBagConstraints);

        JPanel isPitchCriticalPanel = new JPanel();
        isPitchCriticalLabel = new JLabel();
        gridBagConstraints.gridx = 3;
        gridBagConstraints.gridy = 5;
        isPitchCriticalPanel.add(isPitchCriticalLabel);
        frame.add(isPitchCriticalPanel, gridBagConstraints);

        JPanel isHeadStablePanel = new JPanel();
        isHeadStableLabel = new JLabel();
        gridBagConstraints.gridx = 3;
        gridBagConstraints.gridy = 6;
        isHeadStablePanel.add(isHeadStableLabel);
        frame.add(isHeadStablePanel, gridBagConstraints);

        JPanel isPupilsStablePanel = new JPanel();
        isPupilsStableLabel = new JLabel();
        gridBagConstraints.gridx = 3;
        gridBagConstraints.gridy = 7;
        isPupilsStablePanel.add(isPupilsStableLabel);
        frame.add(isPupilsStablePanel, gridBagConstraints);

        frame.setMinimumSize(new Dimension(0, 700));
        frame.pack();
        frame.setVisible(true);
    }

    private static void runStream() {
        List<PXCMRectI32> lastFaceRect = new ArrayList<>();
        Map<Integer, Landmark> landmarks = new HashMap<>();
        Map<Integer, Landmark> previousLandmarks = new HashMap<>();
        HeadPosition previousHeadPosition = null;
        int currentFrame = 0;
        long eyesClosedTime = 0;
        long headTurnedTime = 0;
        long stableHeadTime = 0;
        long stablePupilsTime = 0;
        boolean areEyesClosed = false;
        boolean isHeadTurned = false;
        boolean isHeadStable = false;
        boolean arePupilsStable = false;
        long checkingDrowsinessStartTime = System.currentTimeMillis();
        long lastCheck = System.currentTimeMillis();
        long timeAsleep = 0;

        if (sts == pxcmStatus.PXCM_STATUS_NO_ERROR) {
            while (!listener.exit) {
                ++currentFrame;
                lastFaceRect.clear();
                landmarks.clear();

                sts = senseMgr.AcquireFrame(true);

                if (sts == pxcmStatus.PXCM_STATUS_NO_ERROR) {
                    PXCMCapture.Sample sample = senseMgr.QuerySample();
                    senseMgr.QueryFace();
                    faceData.Update();

                    for (int faceId = 0; ; faceId++) {
                        PXCMFaceData.Face face = faceData.QueryFaceByIndex(faceId);
                        if (face == null) {
                            if (faceId == 0 && Settings.isSoundActivatedWhenNoFaceFound) {
                                SoundUtil.playSound(NOT_FOUND_SOUND);
                            }
                            break;
                        }

                        PXCMFaceData.DetectionData detectData = face.QueryDetection();

                        if (detectData != null) {
                            PXCMRectI32 faceRect = new PXCMRectI32();
                            boolean success = detectData.QueryBoundingRect(faceRect);

                            if (success) {
                                System.out.println();
                                System.out.println("Detection Rectangle at frame #" + currentFrame);
                                System.out.println("Top Left corner: (" + faceRect.x + "," + faceRect.y + ")");
                                System.out.println("Height: " + faceRect.h + " Width: " + faceRect.w);
                            }
                            lastFaceRect.add(faceRect);
                        }

                        PXCMFaceData.PoseData poseData = face.QueryPose();
                        PXCMFaceData.PoseEulerAngles pea = null;
                        if (poseData != null) {
                            pea = new PXCMFaceData.PoseEulerAngles();
                            poseData.QueryPoseAngles(pea);
                            System.out.println("Pose Data:");
                            System.out.println("(Roll, Yaw, Pitch) = (" + pea.roll + "," + pea.yaw + "," + pea.pitch + ")");
                        }

                        PXCMFaceData.LandmarksData landmarkData = face.QueryLandmarks();
                        if (landmarkData != null) {
                            int numPointsLeftEye = landmarkData.QueryNumPointsByGroup(PXCMFaceData.LandmarksGroupType.LANDMARK_GROUP_LEFT_EYE);
                            PXCMFaceData.LandmarkPoint[] landmarkPointsLeftEye = new PXCMFaceData.LandmarkPoint[numPointsLeftEye];

                            for (int i = 0; i < landmarkPointsLeftEye.length; i++) {
                                landmarkPointsLeftEye[i] = new PXCMFaceData.LandmarkPoint();
                            }

                            if (landmarkData.QueryPointsByGroup(
                                PXCMFaceData.LandmarksGroupType.LANDMARK_GROUP_LEFT_EYE, landmarkPointsLeftEye)) {
                                Arrays.asList(landmarkPointsLeftEye).forEach(landmarkPoint -> {
                                    if (landmarkPoint != null) {
                                        landmarks.put(landmarkPoint.source.index, new Landmark(landmarkPoint.image.x, landmarkPoint.image.y));
                                    }
                                });
                            }

                            int numPointsRightEye = landmarkData.QueryNumPointsByGroup(PXCMFaceData.LandmarksGroupType.LANDMARK_GROUP_RIGHT_EYE);
                            PXCMFaceData.LandmarkPoint[] landmarkPointsRightEye = new PXCMFaceData.LandmarkPoint[numPointsRightEye];

                            for (int i = 0; i < landmarkPointsRightEye.length; i++) {
                                landmarkPointsRightEye[i] = new PXCMFaceData.LandmarkPoint();
                            }

                            if (landmarkData.QueryPointsByGroup(
                                PXCMFaceData.LandmarksGroupType.LANDMARK_GROUP_RIGHT_EYE, landmarkPointsRightEye)) {
                                Arrays.asList(landmarkPointsRightEye).forEach(landmarkPoint -> {
                                    if (landmarkPoint != null) {
                                        landmarks.put(landmarkPoint.source.index, new Landmark(landmarkPoint.image.x, landmarkPoint.image.y));
                                    }
                                });
                            }
                            if (pea != null) {
                                HeadPosition currentHeadPosition = new HeadPosition(pea.roll, pea.yaw, pea.pitch);
                                if (previousHeadPosition != null) {
                                    if (DrowsinessCheckerUtil.isHeadStable(previousHeadPosition, currentHeadPosition)) {
                                        System.out.println("Head stable");
                                        isHeadStableLabel.setForeground(Color.RED);
                                        isHeadStableLabel.setText("Head is stable");
                                        if (!isHeadStable) {
                                            isHeadStable = true;
                                            stableHeadTime = System.currentTimeMillis();
                                        }
                                        if (System.currentTimeMillis() - stableHeadTime > Settings.maxHeadStableTime) {
                                            SoundUtil.playSound(BEEP_SOUND);
                                        }
                                    } else {
                                        isHeadStableLabel.setForeground(Color.GREEN);
                                        isHeadStableLabel.setText("Head is not stable");
                                        isHeadStable = false;
                                        stableHeadTime = 0;
                                    }
                                }
                                previousHeadPosition = currentHeadPosition;

                                if (!previousLandmarks.isEmpty()) {
                                    if (DrowsinessCheckerUtil.isPupilsStable(previousLandmarks, landmarks)) {
                                        System.out.println("Pupils stable");
                                        isPupilsStableLabel.setForeground(Color.RED);
                                        isPupilsStableLabel.setText("Pupils are stable");
                                        if (!arePupilsStable) {
                                            arePupilsStable = true;
                                            stablePupilsTime = System.currentTimeMillis();
                                        }
                                        if (System.currentTimeMillis() - stablePupilsTime > Settings.maxPupilsStableTime) {
                                            SoundUtil.playSound(BEEP_SOUND);
                                        }
                                    } else {
                                        isPupilsStableLabel.setForeground(Color.GREEN);
                                        isPupilsStableLabel.setText("Pupils are not stable");
                                        arePupilsStable = false;
                                        stablePupilsTime = 0;
                                    }
                                }
                                previousLandmarks.putAll(landmarks);

                                System.out.println("Drowsiness coeff: " + DrowsinessCheckerUtil.getDrowsinessScore(landmarks));
                                if (DrowsinessCheckerUtil.areEyesClosed(landmarks)) {
                                    System.out.println("Eyes are closed");
                                    areEyesClosedLabel.setForeground(Color.RED);
                                    areEyesClosedLabel.setText("Eyes are closed");
                                    if (!areEyesClosed) {
                                        areEyesClosed = true;
                                        eyesClosedTime = System.currentTimeMillis();
                                    }
                                    if (System.currentTimeMillis() - eyesClosedTime > Settings.maxEyesClosedTime) {
                                        SoundUtil.playSound(BEEP_SOUND);
                                    }
                                } else {
                                    areEyesClosedLabel.setForeground(Color.GREEN);
                                    areEyesClosedLabel.setText("Eyes are not closed");
                                    areEyesClosed = false;
                                    eyesClosedTime = 0;
                                }
                            }

                            if (DrowsinessCheckerUtil.isHeadTurned(pea.roll, pea.yaw, pea.pitch)) {
                                System.out.println("Eyes are closed");
                                if (!isHeadTurned) {
                                    isHeadTurned = true;
                                    headTurnedTime = System.currentTimeMillis();
                                }
                                if (System.currentTimeMillis() - headTurnedTime > Settings.maxHeadTurnedTime) {
                                    SoundUtil.playSound(BEEP_SOUND);
                                }
                            } else {
                                isHeadTurned = false;
                                headTurnedTime = 0;
                            }

                            if (Settings.shouldDrowsinessByCheckedInATimePeriod) {
                                if (System.currentTimeMillis() - checkingDrowsinessStartTime < Settings.timePeriodOfCheckingForDrowsiness) {
                                    if (DrowsinessCheckerUtil.areEyesClosed(landmarks)) {
                                        timeAsleep += System.currentTimeMillis() - lastCheck;
                                    }
                                    lastCheck = System.currentTimeMillis();

                                } else {
                                    if ((double) timeAsleep / Settings.timePeriodOfCheckingForDrowsiness >
                                        Settings.percentOfTimeWithClosedEyesToPlaySound) {
                                        SoundUtil.playSound(DROWSINESS_SOUND);
                                    }
                                    checkingDrowsinessStartTime = System.currentTimeMillis();
                                    lastCheck = checkingDrowsinessStartTime;
                                    timeAsleep = 0;
                                }
                            }
                            isRollCriticalLabel.setText(DrowsinessCheckerUtil.isRollCritical(pea.roll) ? "Roll is critical" : "Roll is not critical");
                            isRollCriticalLabel.setForeground(DrowsinessCheckerUtil.isRollCritical(pea.roll) ? Color.RED : Color.GREEN);
                            isYawCriticalLabel.setText(DrowsinessCheckerUtil.isYawCritical(pea.yaw) ? "Yaw is critical" : "Yaw is not critical");
                            isYawCriticalLabel.setForeground(DrowsinessCheckerUtil.isYawCritical(pea.yaw) ? Color.RED : Color.GREEN);
                            isPitchCriticalLabel
                                .setText(DrowsinessCheckerUtil.isPitchCritical(pea.pitch) ? "Pitch is critical" : "Pitch is not critical");
                            isPitchCriticalLabel.setForeground(DrowsinessCheckerUtil.isPitchCritical(pea.pitch) ? Color.RED : Color.GREEN);
                        }
                    }

                    if (sample.color != null) {
                        PXCMImage.ImageData cData = new PXCMImage.ImageData();
                        sts = sample.color.AcquireAccess(PXCMImage.Access.ACCESS_READ, PXCMImage.PixelFormat.PIXEL_FORMAT_RGB32, cData);
                        if (sts.compareTo(pxcmStatus.PXCM_STATUS_NO_ERROR) < 0) {
                            System.out.println("Failed to Acquire Access of color image data " + sts);
                            System.exit(3);
                        }

                        int cBuff[] = new int[cData.pitches[0] / 4 * C_HEIGHT];

                        cData.ToIntArray(0, cBuff);
                        c_df.image.setRGB(0, 0, C_WIDTH, C_HEIGHT, cBuff, 0, cData.pitches[0] / 4);

                        if (Settings.isFaceDetectionPrinted && lastFaceRect != null) {
                            drawFaces(lastFaceRect, c_df.image);
                        }
                        if (Settings.isLandmarksDisplayed) {
                            drawLandmarks(landmarks, c_df.image);
                        }

                        c_df.repaint();
                        sts = sample.color.ReleaseAccess(cData);

                        if (sts.compareTo(pxcmStatus.PXCM_STATUS_NO_ERROR) < 0) {
                            System.out.println("Failed to Release Access of color image data");
                            System.exit(3);
                        }
                    }

                    if (sample.depth != null) {
                        PXCMImage.ImageData dData = new PXCMImage.ImageData();
                        sample.depth.AcquireAccess(PXCMImage.Access.ACCESS_READ, PXCMImage.PixelFormat.PIXEL_FORMAT_RGB32, dData);
                        if (sts.compareTo(pxcmStatus.PXCM_STATUS_NO_ERROR) < 0) {
                            System.out.println("Failed to Acquire Access of depth image data");
                            System.exit(3);
                        }

                        int dBuff[] = new int[dData.pitches[0] / 4 * dHeight];
                        dData.ToIntArray(0, dBuff);
                        d_df.image.setRGB(0, 0, dWidth, dHeight, dBuff, 0, dData.pitches[0] / 4);
                        d_df.repaint();
                        sts = sample.depth.ReleaseAccess(dData);
                        if (sts.compareTo(pxcmStatus.PXCM_STATUS_NO_ERROR) < 0) {
                            System.out.println("Failed to Release Access of depth image data");
                            System.exit(3);
                        }
                    }
                } else {
                    if (sts == pxcmStatus.PXCM_STATUS_ITEM_UNAVAILABLE) {
                        listener.exit = true;
                    }
                    System.out.println("Failed to acquire frame: " + sts);
                }
                senseMgr.ReleaseFrame();
            }
            senseMgr.Close();
            System.out.println("Done streaming");
        } else {
            System.out.println("Failed to initialize");
        }
        frame.dispose();
    }

    private static void drawLandmarks(Map<Integer, Landmark> landmarks, BufferedImage image) {
        landmarks.forEach((k, v) -> {
            int x = (int) v.getX();
            int y = (int) v.getY();
            Graphics2D g = image.createGraphics();
            g.setColor(new Color(255, 0, 0));
            g.fillOval(x, y, 3, 3);
        });
    }

    private static void drawFaces(List<PXCMRectI32> faces, BufferedImage image) {
        faces.forEach(rect -> {
                          int color = new Color(0, 255, 0).getRGB();
                          int x = rect.x;
                          int y = rect.y;
                          if (C_HEIGHT > y + rect.w && C_WIDTH > x + rect.h) {
                              for (int h = 0; h < rect.h; h++) {
                                  image.setRGB(x + h, y, color);
                                  image.setRGB(x + h, y + rect.w, color);
                              }
                              for (int w = 0; w < rect.w; w++) {
                                  image.setRGB(x, y + w, color);
                                  image.setRGB(x + rect.h, y + w, color);
                              }
                          }
                      }
        );
    }
}