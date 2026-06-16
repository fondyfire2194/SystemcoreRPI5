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

  public int aprilTagPipeline =0;
  public int colorPipeline = 1;
  public int detectorPipeline = 2;

  public String[] pipelineName ={"AprilTag", "Color","Detector"};


  public VisionSubsystem() {
    Cameras frontCam = CameraConstants.frontCamera;
    frontName = frontCam.camname;  
  }

  public int getPipelineNumber(String camname){
    return (int)LimelightHelpers_2027.getCurrentPipelineIndex(camname);
  }

  public String getPipelineType(String camname){
    return LimelightHelpers_2027.getCurrentPipelineType(camname);
  }

  public boolean hasTarget() {
    return LimelightHelpers_2027.getTV(frontName);
  }

  public String getFrontName(){
    return frontName;
  }
}
