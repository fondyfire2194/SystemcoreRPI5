// Copyright (c) FIRST and other WPILib contributors.
// Open Source Software; you can modify and/or share it under the terms of
// the WPILib BSD license file in the root directory of this project.

package first.robot;

import org.wpilib.math.geometry.Pose3d;
import org.wpilib.math.geometry.Rotation3d;
import org.wpilib.math.util.Units;
import org.wpilib.networktables.NetworkTableInstance;
import org.wpilib.networktables.StructArrayPublisher;

/**
 * The Constants class provides a convenient place for teams to hold robot-wide
 * numerical or boolean
 * constants. This class should not be used for any other purpose. All constants
 * should be declared
 * globally (i.e. public static). Do not put anything functional in this class.
 *
 * <p>
 * It is advised to statically import this class (or one of its inner classes)
 * wherever the
 * constants are needed, to reduce verbosity.
 */
public final class Constants {
  public static final class CameraConstants {

    public static class Cameras {
      public String camname = "name";
      public String ipaddress = "ip";
      public boolean isLL4;
      public Pose3d camPose;
      public double hfov;
      public double vfov;
      public int horpixels;
      public int vertpixels;
      public boolean isUsed;

      public Cameras(
          final String camname,
          final String ipaddress,
          final boolean isLL4,
          final Pose3d camPose,
          final double hfov,
          final double vfov,
          final int horpixels,
          final int vertpixels,
          final boolean isUsed) {
        this.camname = camname;
        this.ipaddress = ipaddress;
        this.isLL4 = isLL4;
        this.camPose = camPose;
        this.hfov = hfov;
        this.vfov = vfov;
        this.horpixels = horpixels;
        this.vertpixels = vertpixels;
        this.isUsed = isUsed;

      }
    }

    /**
     * //https://youtu.be/unX1PsPi0VA?si=D1i4hf6OA0_LXidt
     * Pose3d rotation Parameters:
     * Roll is CCW angle around X in radians (normally 0()
     * Pitch is CCW angle around Y in radians (0 is parallel to ground)
     * Yaw is CCW angle around Z axis in radians 90 is pointing left
     * 
     */
    static Pose3d frontCamPose = new Pose3d(
        Units.inchesToMeters(12), // front of robot
        Units.inchesToMeters(0), // on LR center
        Units.inchesToMeters(23.), // high
        new Rotation3d(
            Units.degreesToRadians(0), // no roll
            Units.degreesToRadians(26), // angled up
            Units.degreesToRadians(0)));// facing forward

    public static Cameras frontCamera = new Cameras(
        "limelight",
        "10.21.94.15",
        false,
        frontCamPose,
        63.3,
        49.7,
        1280,
        960,
        true);

  
    public static StructArrayPublisher<Pose3d> arrayPublisher = NetworkTableInstance.getDefault()
        .getStructArrayTopic("Camposes", Pose3d.struct).publish();


    public static int apriltagPipeline = 0;
    public static int fuelDetectorPipeline = 1;
    public static int viewFinderPipeline = 5;

  }

  public static final class OIConstants {
    public static final int kDriverControllerPort = 0;
  }
}
