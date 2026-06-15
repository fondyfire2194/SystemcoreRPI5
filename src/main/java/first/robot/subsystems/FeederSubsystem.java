// Copyright (c) FIRST and other WPILib contributors.
// Open Source Software; you can modify and/or share it under the terms of
// the WPILib BSD license file in the root directory of this project.

package first.robot.subsystems;

import static org.wpilib.units.Units.Seconds;

import java.util.InputMismatchException;

import org.wpilib.command3.Command;
import org.wpilib.command3.Mechanism;
import org.wpilib.hardware.hal.CANBusMap;
import org.wpilib.math.filter.Debouncer;
import org.wpilib.math.filter.Debouncer.DebounceType;
import org.wpilib.smartdashboard.SmartDashboard;

import com.revrobotics.PersistMode;
import com.revrobotics.RelativeEncoder;
import com.revrobotics.ResetMode;
import com.revrobotics.spark.ClosedLoopSlot;
import com.revrobotics.spark.FeedbackSensor;
import com.revrobotics.spark.SparkBase.ControlType;
import com.revrobotics.spark.SparkClosedLoopController;
import com.revrobotics.spark.SparkLowLevel.MotorType;
import com.revrobotics.spark.SparkMax;
import com.revrobotics.spark.config.SparkBaseConfig.IdleMode;
import com.revrobotics.spark.config.SparkMaxConfig;

public class FeederSubsystem extends Mechanism {
  /** Creates a new feederSubsystem. */

  public SparkMax feederMotor = new SparkMax(CANBusMap.CAN_S0, 20, MotorType.kBrushless);

  private SparkClosedLoopController closedLoopController = feederMotor.getClosedLoopController();

  private RelativeEncoder encoder = feederMotor.getEncoder();

  private Debouncer inPositionDebouncer = new Debouncer(.1, DebounceType.kRising);

  private double targetRPM = 1000;

  private double targetPosition = 0;

  public double getTargetPosition() {
    return targetPosition;
  }

  public void setTargetPosition(double targetPosition) {
    this.targetPosition = targetPosition;
  }

  public static final SparkMaxConfig feederConfig = new SparkMaxConfig();

  public static double positionConversionFactor = 1;

  private boolean runFeeder;

  public int artifactsDone;

  public boolean isRunFeeder() {
    return runFeeder;
  }

  public void setRunFeeder(boolean runFeeder) {
    this.runFeeder = runFeeder;
  }

  static {
    // // Configure basic settings of the intake motor
    feederConfig
        .inverted(false)
        .idleMode(IdleMode.kCoast)
        .openLoopRampRate(0.5)
        .closedLoopRampRate(.25)
        .smartCurrentLimit(50);
    feederConfig.encoder
        .positionConversionFactor(positionConversionFactor)
        .velocityConversionFactor(positionConversionFactor);

    feederConfig.closedLoop
        .feedbackSensor(FeedbackSensor.kPrimaryEncoder)
        // // Set PID values for speed control. We don't need to pass a closed loop
        // // slot, as it will default to slot 0.
        .p(0.00005)
        .i(0)
        .d(0)
        .outputRange(-1, 1)
        .p(0.072, ClosedLoopSlot.kSlot1)
        .i(0, ClosedLoopSlot.kSlot1)
        .d(0, ClosedLoopSlot.kSlot1)
        .outputRange(-1, 1, ClosedLoopSlot.kSlot1).feedForward
        // // kV is now in Volts, so we multiply by the nominal voltage (12V)
        .kV(12.0 / 5767, ClosedLoopSlot.kSlot0);

  }

  public FeederSubsystem() {
    feederMotor.configure(
        feederConfig,
        ResetMode.kResetSafeParameters,
        PersistMode.kPersistParameters);
    setRunFeeder(false);
    encoder.setPosition(0);
  }

  public void runFeederMotor(double throttle) {
    feederMotor.setThrottle(throttle);
  }

  public void runFeederAtVelocity() {
    closedLoopController.setSetpoint(targetRPM, ControlType.kVelocity, ClosedLoopSlot.kSlot0);
  }

  public Command runFeederAtVelocityCommand() {
    // setRunFeeder(true);
    return run(coroutine -> {
      while (runFeeder) {
        runFeederAtVelocity();
        coroutine.yield();
      }
      stopFeederMotor();
    }).named("Run Feeder at Velocity ");
  }

  public void positionFeeder() {
    closedLoopController.setSetpoint(targetPosition, ControlType.kPosition, ClosedLoopSlot.kSlot1);
  }

  public Command positionFeederCommand(double value) {
    targetPosition = value;
    return run(coroutine -> {
      while (!inPosition()) {
        positionFeeder();
        coroutine.yield();
      }
      stopFeederMotor();
    }).named("Position Feeder ");
  }

  public Command feedArtifacts(int quantity, double distance) {
    return run(coroutine -> {
      for (int i = 0; i < quantity; i++) {
        coroutine.await(
            positionFeederCommand(distance));
        coroutine.wait(Seconds.of(.25));
        coroutine.await(positionFeederCommand(0));
        coroutine.wait(Seconds.of(.25));
        artifactsDone++;
        SmartDashboard.putNumber("ArtDone", artifactsDone);
      }
    }).named("Feed Artifacts");
  }

  

  public void stopFeederMotor() {
    feederMotor.stopMotor();
    setRunFeeder(false);
    closedLoopController.setSetpoint(0, ControlType.kVelocity, ClosedLoopSlot.kSlot0);
  }

  public Command stopFeederCommand() {
    return run(coroutine -> {
      while (!isStopped()) {
        stopFeederMotor();
        setRunFeeder(false);
        coroutine.yield();
      }
      stopFeederMotor();
    }).named("Stop s=Shooter");
  }

  public void clearFaults() {
    feederMotor.clearFaults();
  }

  public double getPosition() {
    return encoder.getPosition().get();
  }

  public double getVelocity() {
    return encoder.getVelocity().get();
  }

  public boolean isStopped() {
    return Math.abs(getVelocity()) < .005;
  }

  public boolean atSpeed() {
    return Math.abs(targetRPM - getVelocity()) < 100;
  }

  public boolean inPosition() {
    return inPositionDebouncer.calculate(Math.abs(targetPosition - getPosition()) < .2);
  }

  public void feederTelemetry() {
    SmartDashboard.putNumber("FeederPosition", getPosition());
    SmartDashboard.putNumber("FeederVelocity", getVelocity());
    SmartDashboard.putNumber("FeederThrottle", feederMotor.getThrottle());
    SmartDashboard.putNumber("FeederTemp", feederMotor.getMotorTemperature().get());
    SmartDashboard.putNumber("FeederTarget", targetPosition);

    SmartDashboard.putBoolean("FeederStopped", isStopped());
    SmartDashboard.putBoolean("RunFeeder", isRunFeeder());
    SmartDashboard.putBoolean("FeederInPosition", inPosition());

  }

}
