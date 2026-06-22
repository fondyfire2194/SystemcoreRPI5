// Copyright (c) FIRST and other WPILib contributors.
// Open Source Software; you can modify and/or share it under the terms of
// the WPILib BSD license file in the root directory of this project.

package first.robot;

import static org.wpilib.units.Units.Seconds;

import org.wpilib.command3.Command;
import org.wpilib.command3.Scheduler;
import org.wpilib.command3.StateMachine;
import org.wpilib.command3.StateMachine.State;
import org.wpilib.command3.Trigger;
import org.wpilib.command3.button.CommandGamepad;
import org.wpilib.driverstation.internal.DriverStationBackend;
import org.wpilib.framework.OpModeRobot;
import org.wpilib.hardware.power.PowerDistribution;
import org.wpilib.hardware.power.PowerDistribution.ModuleType;

import first.robot.subsystems.FeederSubsystem;
import first.robot.subsystems.KrakenSubsystem;
import first.robot.subsystems.ShooterSubsystem;

/**
 * The methods in this class are called automatically as described in the
 * OpModeRobot documentation.
 * OpMode classes anywhere in the package (or sub-packages) where this class is
 * located are
 * automatically registered to display in the Driver Station. If you change the
 * name of this class
 * or the package after creating this project, you must also update the
 * Main.java file in the
 * project.
 */
public class Robot extends OpModeRobot {
  /**
   * This function is run when the robot is first started up and should be used
   * for any
   * initialization code.
   */
  public ShooterSubsystem shooter = new ShooterSubsystem();
  public FeederSubsystem feeder = new FeederSubsystem();
  public PowerDistribution pdh = new PowerDistribution(0, 1, ModuleType.CTRE);
  public KrakenSubsystem kraken = new KrakenSubsystem();
  public CommandGamepad controller = new CommandGamepad(0);

  public Trigger startShooter = new Trigger(() -> DriverStationBackend.isEnabled() && shooter.isRunShooter());
  public Trigger startFeeder = new Trigger(() -> DriverStationBackend.isEnabled() && feeder.isRunFeeder());
  public Trigger startKraken = new Trigger(() -> DriverStationBackend.isEnabled() && kraken.isRunKraken());

  public Robot() {
    startShooter.onTrue(shooter.runShooterAtVelocityCommand());
    startShooter.onFalse(Command
        .noRequirements(coro -> Scheduler.getDefault().cancel(shooter.runShooterAtVelocityCommand()))
        .named("Cancel Run Shooter"));

    startFeeder.onTrue(feeder.runFeederAtVelocityCommand());
    startFeeder.onFalse(Command
        .noRequirements(coro -> Scheduler.getDefault().cancel(feeder.runFeederAtVelocityCommand()))
        .named("Cancel Run Feeder"));

    startKraken.onTrue(kraken.runKrakenAtVelocityCommand());
    startKraken.onFalse(Command
        .noRequirements(coro -> Scheduler.getDefault().cancel(kraken.runKrakenAtVelocityCommand()))
        .named("Cancel Run Kraken"));

  }

  @Override
  public void publishOpModes() {
    // TODO Auto-generated method stub
    super.publishOpModes();
  }

  /** This function is called exactly once when the DS first connects. */
  @Override
  public void driverStationConnected() {
  }

  /**
   * This function is called periodically anytime when no opmode is selected,
   * including when the Driver Station is disconnected.
   */
  @Override
  public void nonePeriodic() {

  }

  public Command shootSequence() {
    return Command.noRequirements(coroutine -> {
      coroutine.await(Command.noRequirements(coro -> shooter.setRunShooter(true)).named("SetRunShooter"));
      // coroutine.fork(shooter.runShooterAtVelocityCommand());
      coroutine.waitUntil(() -> shooter.atSpeed());
      coroutine.await(Command.noRequirements(coro -> feeder.setRunFeeder(true)).named("SetRunFeeder"));
      // coroutine.fork(feeder.runFeederAtVelocityCommand());
      coroutine.wait(Seconds.of(2));
      coroutine.fork(shooter.stopShooterCommand());
      coroutine.fork(feeder.stopFeederCommand());

    }).named("Shooting Sequence");
  }

  public Command shootPositionSequence() {
    return Command.noRequirements(coroutine -> {
      coroutine.await(Command.noRequirements(coro -> shooter.setRunShooter(true)).named("SetRunShooter"));
      // coroutine.fork(shooter.runShooterAtVelocityCommand());
      coroutine.waitUntil(() -> shooter.atSpeed());
      coroutine.await(feeder.feedArtifacts(5, 4));
      coroutine.wait(Seconds.of(1));
      coroutine.await(feeder.positionFeederCommand(0));
      coroutine.wait(Seconds.of(1));
      coroutine.fork(shooter.stopShooterCommand());
    }).named("Shooting Sequence");
  }

  public StateMachine stateMachine = new StateMachine("Shoot Position: ");

  public Command shootPositionState() {

    State reset = stateMachine
        .addState(Command.noRequirements(coro -> feeder.feederMotor.getEncoder().setPosition(0)).named("Reset"));

    State setRunShooter = stateMachine
        .addState(Command.noRequirements(coro -> shooter.setRunShooter(true)).named("SetRunShooter"));

    State positionFeeder = stateMachine.addState(feeder.feedArtifacts(6, 5));

    State stopShooter = stateMachine
        .addState(Command.noRequirements(coro -> shooter.setRunShooter(false)).named("Stop Shooter"));

    // set state switching targets

    stateMachine.setInitialState(reset);

    reset.switchTo(setRunShooter).whenComplete();

    setRunShooter.switchTo(positionFeeder).whenComplete();

    positionFeeder.switchTo(stopShooter).whenComplete();

    return stateMachine;
  }
}