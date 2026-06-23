// Copyright (c) FIRST and other WPILib contributors.
// Open Source Software; you can modify and/or share it under the terms of
// the WPILib BSD license file in the root directory of this project.

package first.robot.opmode.Auto;

import org.wpilib.command3.Scheduler;
import org.wpilib.opmode.PeriodicOpMode;
import org.wpilib.opmode.Utility;

import first.robot.Robot;

@Utility(name = "Shooter Speed Auto", group = "Group 1")
public class ShooterSpeedAuto extends PeriodicOpMode {
  private final Robot robot;

  /** The Robot instance is passed into the opmode via the constructor. */
  public ShooterSpeedAuto(Robot robot) {
    this.robot = robot;
  }

  @Override
  public void start() {
    /* Called once when the robot is enabled. */
    Scheduler.getDefault().run();
    Scheduler.getDefault().schedule(robot.shooterSpeedSequence(100, 25));

  }

  /*
   * This method runs periodically, using the same period as the Robot instance.
   *
   * Additional periodic methods may be configured with addPeriodic(),
   * which can have periods that differ from the main Robot instance.
   */
  @Override
  public void periodic() {
    // Put custom auto code here
    Scheduler.getDefault().run();
    robot.kraken.krakenTelemetry();

  }

  @Override
  public void end() {
    /* Called when the robot is disabled (after previously being enabled). */
    robot.close();
    Scheduler.getDefault().cancelAll();
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
