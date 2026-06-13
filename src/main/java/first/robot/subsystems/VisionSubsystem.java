// Copyright (c) FIRST and other WPILib contributors.
// Open Source Software; you can modify and/or share it under the terms of
// the WPILib BSD license file in the root directory of this project.

package first.robot.subsystems;



import org.wpilib.command3.Mechanism;

import first.robot.Constants.CameraConstants;
import first.robot.Constants.CameraConstants.Cameras;
import first.robot.utils.LimelightHelpers_2027;

public class VisionSubsystem extends Mechanism {
  /** Creates a new VisionSubsystem. */

  public int frontCam = 0;

  public String frontName;

  public boolean frontConnected;

  public double lastFrontHeartbeat;

  public VisionSubsystem() {
    Cameras frontCam = CameraConstants.frontCamera;
    frontName = frontCam.camname;
  }

  public boolean hasTarget() {
    return LimelightHelpers_2027.getTV(frontName);
  }

  public String getFrontName(){
    return frontName;
  }
}
