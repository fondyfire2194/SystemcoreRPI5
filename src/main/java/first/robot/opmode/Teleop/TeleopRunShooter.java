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

@Teleop
public class TeleopRunShooter extends PeriodicOpMode {
  private final Robot robot;

  CommandGamepad controller;

  /** The Robot instance is passed into the opmode via the constructor. */
  public TeleopRunShooter(Robot robot) {
    this.robot = robot;
    controller = new CommandGamepad(0);

    robot.controller.leftBumper().onTrue(
        Command.noRequirements(coro -> robot.shooter.shooterMotor.setThrottle(.25)).named("Jog Shooter plus"))
        .onFalse(Command.noRequirements(coro -> robot.shooter.shooterMotor.setThrottle(0.)).named("Jog Shooter stop"));

    robot.controller.rightBumper().onTrue(
        Command.noRequirements(coro -> robot.shooter.shooterMotor.setThrottle(-.25)).named("Jog Shooter minus"))
        .onFalse(Command.noRequirements(coro -> robot.shooter.shooterMotor.setThrottle(0.)).named("Jog Shooter stop"));

    robot.controller.northFace()
        .onTrue(Command.noRequirements(coro -> robot.shooter.setRunShooter(true)).named("SetRunShooter"));
      //  .onFalse(robot.shooter.runShooterAtVelocityCommand());

    robot.controller.southFace()
        .onTrue(Command.noRequirements(coro -> robot.shooter.setRunShooter(false)).named("Stop Shooter"));

        robot.shooter.setRunShooter(false);
        robot.feeder.setRunFeeder(false);
  }

  @Override
  public void disabledPeriodic() {
    /* Called periodically (on every DS packet) while the robot is disabled. */
    // Scheduler.getDefault().run();
    // robot.shooter.shooterTelemetry();
  }

  @Override
  public void start() {
    /* Called once when the robot is enabled. */
     robot.shooter.setRunShooter(false);

  }

  @Override
  public void periodic() {

    Scheduler.getDefault().run();
    robot.shooter.shooterTelemetry();

    SmartDashboard.putNumber("PDH Volts", robot.pdh.getVoltage());
    /*
     * Called periodically
     * 
     * 
     * (set time interval) while the robot is enabled.
     */
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
