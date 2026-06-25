// Copyright (c) FIRST and other WPILib contributors.
// Open Source Software; you can modify and/or share it under the terms of
// the WPILib BSD license file in the root directory of this project.

package first.robot.opmode.Utility;

import org.wpilib.command3.Scheduler;
import org.wpilib.opmode.PeriodicOpMode;
import org.wpilib.opmode.Utility;

import dev.doglog.DogLog;
import first.robot.Robot;

@Utility(name = "Utility Tune Kraken", group = "Group 2")
public class TuneKrakenPID extends PeriodicOpMode {
  private final Robot robot;

  /** The Robot instance is passed into the opmode via the constructor. */
  public TuneKrakenPID(Robot robot) {
    this.robot = robot;
    // DogLog.tunable("" + "/kP", robot.kraken.x60Config.Slot0.kP,
    //     newP -> robot.kraken.x60Motor.getConfigurator().apply(robot.kraken.x60Config.Slot0.withKP(newP)));

  }

  @Override
  public void disabledPeriodic() {
    /* Called periodically (on every DS packet) while the robot is disabled. */
    Scheduler.getDefault().run();
    robot.kraken.krakenTelemetry();

  }

  @Override
  public void start() {
    /* Called once when the robot is enabled. */
    robot.kraken.setRunKraken(false);
  }

  @Override
  public void periodic() {
    Scheduler.getDefault().run();

    robot.kraken.krakenTelemetry();
    /*
     * Called periodically
     * 
     * 
     * (set time interval) while the robot is enabled.
     */
  }

  @Override
  public void end() {
    /* Called when the robot is disabled (after previously being enabled). */
    robot.kraken.setRunKraken(false);
    robot.close();
    Scheduler.getDefault().cancelAll();
  }

  @Override
  public void close() {
    robot.close();
    robot.kraken.setRunKraken(false);
    Scheduler.getDefault().cancelAll();
    /*
     * Called when the opmode is de-selected / no additional methods will be called.
     */
  }
}
