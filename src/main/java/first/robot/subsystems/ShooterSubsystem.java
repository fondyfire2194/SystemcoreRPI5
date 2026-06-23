// Copyright (c) FIRST and other WPILib contributors.
// Open Source Software; you can modify and/or share it under the terms of
// the WPILib BSD license file in the root directory of this project.

package first.robot.subsystems;

import static org.wpilib.units.Units.Seconds;

import org.wpilib.command3.Command;
import org.wpilib.command3.Mechanism;
import org.wpilib.hardware.hal.CANBusMap;
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

public class ShooterSubsystem extends Mechanism {
  /** Creates a new shooterSubsystem. */

  public SparkMax shooterMotor = new SparkMax(CANBusMap.CAN_S0, 24, MotorType.kBrushless);

  private SparkClosedLoopController closedLoopController = shooterMotor.getClosedLoopController();

  private RelativeEncoder encoder = shooterMotor.getEncoder();

  private boolean runShooter;

  private int tstctr;

  private int stopctr;

  public boolean isRunShooter() {
    return runShooter;
  }

  public void setRunShooter(boolean runShooter) {
    this.runShooter = runShooter;
  }

  public static final SparkMaxConfig shooterConfig = new SparkMaxConfig();

  public static double positionConversionFactor = 1.;

  public static double targetRPM = 1000.;

  static {
    // // Configure basic settings of the intake motor
    shooterConfig
        .inverted(false)
        .idleMode(IdleMode.kCoast)
        .openLoopRampRate(0.5)
        .closedLoopRampRate(.25)
        .smartCurrentLimit(50);
    shooterConfig.encoder
        .positionConversionFactor(positionConversionFactor)
        .velocityConversionFactor(positionConversionFactor);

    shooterConfig.closedLoop
        .feedbackSensor(FeedbackSensor.kPrimaryEncoder)
        // // Set PID values for speed control. We don't need to pass a closed loop
        // // slot, as it will default to slot 0.
        .p(0.00005)
        .i(0)
        .d(0)
        .outputRange(-1, 1)

            .feedForward
        // // kV is now in Volts, so we multiply by the nominal voltage (12V)
        .kV(12.0 / 5767, ClosedLoopSlot.kSlot0);
  }

  public ShooterSubsystem() {
    shooterMotor.configure(
        shooterConfig,
        ResetMode.kResetSafeParameters,
        PersistMode.kPersistParameters);
    setRunShooter(false);
    // setDefaultCommand(Commands.run(() -> stopshooterMotor()).until(() ->
    // isStopped()));
  }

  public void runShooterMotor(double throttle) {
    shooterMotor.setThrottle(throttle);

  }

  public void runShooterAtVelocity() {
    closedLoopController.setSetpoint(targetRPM, ControlType.kVelocity, ClosedLoopSlot.kSlot0);
  }

  public Command runShooterAtVelocityCommand() {
    // setRunShooter(true);
    return run(coroutine -> {
      while (isRunShooter()) {
        SmartDashboard.putNumber("TSTCTR", tstctr++);
        runShooterAtVelocity();
        coroutine.yield();
      }
      stopShooterMotor();
    }).named("Run Shooter at Velocity ");
  }

  public void stopShooterMotor() {
    SmartDashboard.putNumber("ShooterSTOPCTR", stopctr++);
    shooterMotor.stopMotor();
    setRunShooter(false);
    closedLoopController.setSetpoint(0, ControlType.kVelocity, ClosedLoopSlot.kSlot0);
    // Scheduler.getDefault().cancel(runShooterAtVelocityCommand());
  }

  public Command stopShooterCommand() {
    return run(coroutine -> {
      while (!isStopped()) {
        stopShooterMotor();
        setRunShooter(false);
        coroutine.yield();
      }
      stopShooterMotor();
    }).named("Stop Shooter");
  }

  public Command cycleShooterSpeedUp(double rpmChange, int numberChanges) {
    return run(coroutine -> {
      for (int i = 0; i < numberChanges; i++) {
        runShooterAtVelocity();
        targetRPM += rpmChange;
        coroutine.wait(Seconds.of(.25));
      }
    }).named("Cycle Shooter Speed Up");
  }

  public Command cycleShooterSpeedDown(double rpmChange, int numberChanges) {
    return run(coroutine -> {
      for (int i = 0; i < numberChanges; i++) {
        runShooterAtVelocity();
        targetRPM -= rpmChange;
        coroutine.wait(Seconds.of(.25));
      }
    }).named("Cycle Shooter Speed Down");
  }

  public void clearFaults() {
    shooterMotor.clearFaults();
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

  public void shooterTelemetry() {
    SmartDashboard.putNumber("ShooterPosition", getPosition());
    SmartDashboard.putNumber("ShooterVelocity", getVelocity());
    SmartDashboard.putNumber("ShooterThrottle", shooterMotor.getThrottle());
    SmartDashboard.putNumber("ShooterTemp", shooterMotor.getMotorTemperature().get());
    SmartDashboard.putBoolean("ShooterStopped", isStopped());
    SmartDashboard.putBoolean("ShooterAtSpeed", atSpeed());
    SmartDashboard.putBoolean("RunShooter", isRunShooter());

  }

}
