// Copyright (c) FIRST and other WPILib contributors.
// Open Source Software; you can modify and/or share it under the terms of
// the WPILib BSD license file in the root directory of this project.

package first.robot.opmode.Teleop;

import org.wpilib.command3.Command;
import org.wpilib.command3.Scheduler;
import org.wpilib.opmode.PeriodicOpMode;
import org.wpilib.opmode.Teleop;

import first.robot.Robot;

@Teleop
public class TeleopRunKraken extends PeriodicOpMode {
  private final Robot robot;

  /** The Robot instance is passed into the opmode via the constructor. */
  public TeleopRunKraken(Robot robot) {
    this.robot = robot;

    robot.controller.leftBumper().onTrue(
        Command.noRequirements(coro -> robot.kraken.x60Motor.setThrottle(.25)).named("Jog Kraken plus"))
        .onFalse(Command.noRequirements(coro -> robot.kraken.x60Motor.setThrottle(0.)).named("Jog Kraken stop"));

    robot.controller.rightBumper().onTrue(
         Command.noRequirements(coro -> robot.kraken.x60Motor.setThrottle(-.25)).named("Jog Kraken minus"))
        .onFalse(Command.noRequirements(coro -> robot.kraken.x60Motor.setThrottle(0.)).named("Jog Kraken stop"));

    robot.controller.northFace()
        .onTrue(Command.noRequirements(coro -> robot.feeder.setRunFeeder(true)).named("SetRunFeeder"));
    // .onFalse(robot.feeder.runFeederAtVelocityCommand());

    robot.controller.southFace()
        .onTrue(Command.noRequirements(coro -> robot.feeder.setRunFeeder(false)).named("Stop Feeder"));
    robot.controller.eastFace().onTrue(
        Command.noRequirements(coro -> robot.feeder.setTargetPosition(robot.feeder.getTargetPosition() + 20))
            .named("Position Feeder +20"))
        .onFalse(robot.feeder.positionFeederCommand(20));

    robot.controller.westFace().onTrue(
        Command.noRequirements(coro -> robot.feeder.setTargetPosition(0)).named("Position Feeder 0"))
        .onFalse(robot.feeder.positionFeederCommand(0));

    robot.feeder.setRunFeeder(false);
    robot.shooter.setRunShooter(false);
  }

  @Override
  public void disabledPeriodic() {
    /* Called periodically (on every DS packet) while the robot is disabled. */
    Scheduler.getDefault().run();
    robot.feeder.feederTelemetry();

  }

  @Override
  public void start() {
    /* Called once when the robot is enabled. */
    robot.feeder.setRunFeeder(false);
  }

  @Override
  public void periodic() {
    Scheduler.getDefault().run();
    robot.feeder.feederTelemetry();
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
    robot.feeder.setRunFeeder(false);
    robot.close();
    Scheduler.getDefault().cancelAll();
  }

  @Override
  public void close() {
    robot.close();
    robot.feeder.setRunFeeder(false);
    Scheduler.getDefault().cancelAll();
    /*
     * Called when the opmode is de-selected / no additional methods will be called.
     */
  }
}
