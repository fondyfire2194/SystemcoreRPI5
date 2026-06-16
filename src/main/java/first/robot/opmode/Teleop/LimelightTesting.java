// Copyright (c) FIRST and other WPILib contributors.
// Open Source Software; you can modify and/or share it under the terms of
// the WPILib BSD license file in the root directory of this project.

package first.robot.opmode.Teleop;

import org.wpilib.command3.Command;
import org.wpilib.command3.Scheduler;
import org.wpilib.command3.button.CommandGamepad;
import org.wpilib.opmode.PeriodicOpMode;
import org.wpilib.opmode.Teleop;
import org.wpilib.smartdashboard.SmartDashboard;

import first.robot.Robot;
import first.robot.subsystems.VisionSubsystem;
import first.robot.utils.LimelightHelpers_2027;
import first.robot.utils.LimelightHelpers_2027.RawDetection;
import first.robot.utils.LimelightHelpers_2027.RawFiducial;

@Teleop
public class LimelightTesting extends PeriodicOpMode {
  private final Robot robot;

  CommandGamepad controller;
  VisionSubsystem vision;
  String camName = "";

  /** The Robot instance is passed into the opmode via the constructor. */
  public LimelightTesting(Robot robot) {
    this.robot = robot;
    controller = new CommandGamepad(0);
    vision = new VisionSubsystem();
    camName = vision.frontName;
    SmartDashboard.putString("Camname", camName);
    LimelightHelpers_2027.setPipelineIndex(camName, vision.aprilTagPipeline);
    controller.eastFace()
        .onTrue(Command.noRequirements(coro -> LimelightHelpers_2027.setPipelineIndex(camName, vision.aprilTagPipeline))
            .named("AprilTag"));

    controller.westFace()
        .onTrue(Command.noRequirements(coro -> LimelightHelpers_2027.setPipelineIndex(camName, vision.colorPipeline))
            .named("Color"));
    controller.southFace()
        .onTrue(Command.noRequirements(coro -> LimelightHelpers_2027.setPipelineIndex(camName, vision.detectorPipeline))
            .named("Detector"));
  }

  @Override
  public void disabledPeriodic() {
    /* Called periodically (on every DS packet) while the robot is disabled. */
    // Scheduler.getDefault().run();

  }

  @Override
  public void start() {
    /* Called once when the robot is enabled. */

  }

  @Override
  public void periodic() {

    Scheduler.getDefault().run();
    SmartDashboard.putString("PipelineType", vision.getPipelineType(camName));

    SmartDashboard.putNumber("PDH Volts", robot.pdh.getVoltage());
    /*
     * Called periodically
     * 
     * 
     * (set time interval) while the robot is enabled.
     */

    switch (vision.getPipelineNumber(camName)) {
      case 0:
        // Get raw AprilTag/Fiducial data
        RawFiducial[] fiducials = LimelightHelpers_2027.getRawFiducials(camName);
        for (RawFiducial fiducial : fiducials) {
          int id = fiducial.id; // Tag ID
          double txnc = fiducial.txnc; // X offset (no crosshair)
          double tync = fiducial.tync; // Y offset (no crosshair)
          double ta = fiducial.ta; // Target area
          double distToCamera = fiducial.distToCamera; // Distance to camera
          double distToRobot = fiducial.distToRobot; // Distance to robot
          double ambiguity = fiducial.ambiguity; // Tag pose ambiguity
        }
        break;

      case 1:

        break;

      case 2:
        // Get raw neural detector results
        RawDetection[] detections = LimelightHelpers_2027.getRawDetections(camName);
        for (RawDetection detection : detections) {
          int classID = detection.classId;
          double txnc = detection.txnc;
          double tync = detection.tync;
          double ta = detection.ta;

        }
        break;

      default:
        break;

    }

  }

  @Override
  public void end() {
    robot.close();
    Scheduler.getDefault().cancelAll();
    robot.shooter.setRunShooter(false);
    /* Called when the robot is disabled (after previously being enabled). */
  }

  @Override
  public void close() {
    robot.close();
    Scheduler.getDefault().cancelAll();
    /*
     * Called when the opmode is de-selected / no additional methods will be called.
     */
  }
}
