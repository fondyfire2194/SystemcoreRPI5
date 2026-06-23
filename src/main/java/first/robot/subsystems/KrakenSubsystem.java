// Copyright (c) FIRST and other WPILib contributors.
// Open Source Software; you can modify and/or share it under the terms of
// the WPILib BSD license file in the root directory of this project.

package first.robot.subsystems;

import static org.wpilib.units.Units.Amps;
import static org.wpilib.units.Units.Seconds;
import static org.wpilib.units.Units.Volts;

import org.wpilib.command3.Command;
import org.wpilib.command3.Mechanism;
import org.wpilib.math.filter.Debouncer;
import org.wpilib.math.filter.Debouncer.DebounceType;
import org.wpilib.smartdashboard.SmartDashboard;

import com.ctre.phoenix6.CANBus;
import com.ctre.phoenix6.configs.CurrentLimitsConfigs;
import com.ctre.phoenix6.configs.TalonFXConfiguration;
import com.ctre.phoenix6.controls.PositionVoltage;
import com.ctre.phoenix6.hardware.TalonFX;
import com.ctre.phoenix6.signals.InvertedValue;

public class KrakenSubsystem extends Mechanism {
  /** Creates a new Kraken Subsystem. */

  public TalonFX x60Motor = new TalonFX(10, CANBus.systemcore(0));
  private TalonFXConfiguration x60Config = new TalonFXConfiguration();
  private final CurrentLimitsConfigs m_currentLimits = new CurrentLimitsConfigs();
  /* Start at position 0, use slot 0 */
  private final PositionVoltage m_positionVoltage = new PositionVoltage(0).withSlot(0);

  private Debouncer inPositionDebouncer = new Debouncer(.1, DebounceType.kRising);

  private double targetRPM = 1000;

  public double getTargetRPM() {
    return targetRPM;
  }

  public void setTargetRPM(double targetRPM) {
    this.targetRPM = targetRPM;
  }

  private double targetPosition = 0;

  public double getTargetPosition() {
    return targetPosition;
  }

  public void setTargetPosition(double targetPosition) {
    this.targetPosition = targetPosition;
  }

  public static double positionConversionFactor = 1;

  private boolean runKraken;

  public int artifactsDone;

  public boolean isRunKraken() {
    return runKraken;
  }

  public void setRunKraken(boolean runKraken) {
    this.runKraken = runKraken;
  }

  public KrakenSubsystem() {
    x60Config.MotorOutput.Inverted = InvertedValue.CounterClockwise_Positive;
    x60Config.Slot0.kP = 2.4; // An error of 1 rotation results in 2.4 V output
    x60Config.Slot0.kI = 0; // No output for integrated error
    x60Config.Slot0.kD = 0.1; // A velocity of 1 rps results in 0.1 V output
    // Peak output of 8 V
    x60Config.Voltage.withPeakForwardVoltage(Volts.of(8))
        .withPeakReverseVoltage(Volts.of(-8));

    x60Config.Slot1.kP = 60; // An error of 1 rotation results in 60 A output
    x60Config.Slot1.kI = 0; // No output for integrated error
    x60Config.Slot1.kD = 6; // A velocity of 1 rps results in 6 A output

    m_currentLimits.withSupplyCurrentLowerLimit(Amps.of(20)) // Default limit of 70 A
        .withSupplyCurrentLimit(Amps.of(150)) // Reduce the limit to 40 A if we've limited to 70 A...
        .withSupplyCurrentLowerTime(Seconds.of(1.0)) // ...for at least 1 second
        .withSupplyCurrentLimitEnable(true); // And enable it

    m_currentLimits.withStatorCurrentLimit(Amps.of(40)) // Limit stator current to 120 A
        .withStatorCurrentLimitEnable(true); // And enable it

    x60Config.CurrentLimits = m_currentLimits;

    x60Motor.getConfigurator().apply(x60Config);

  }

  public void runKrakenAtVelocity() {
    x60Motor.setControl(m_positionVoltage.withVelocity(targetRPM));
  }

  public Command runKrakenAtVelocityCommand() {
    // setRunFeeder(true);
    return run(coroutine -> {
      while (runKraken) {
        runKrakenAtVelocity();
        coroutine.yield();
      }
      stopKrakenMotor();
    }).named("Run Feeder at Velocity ");
  }

  public void positionKraken() {
    x60Motor.setControl(m_positionVoltage.withPosition(targetPosition));
  }

  public Command positionKrakenCommand(double value) {
    targetPosition = value;
    return run(coroutine -> {
      while (!inPosition()) {
        positionKraken();
        coroutine.yield();
      }
      stopKrakenMotor();
    }).named("Position Feeder ");
  }

  public void stopKrakenMotor() {
    x60Motor.stopMotor();
  }

  public Command stopKrakenCommand() {
    return run(coroutine -> {
      while (!isStopped()) {
        stopKrakenMotor();
        coroutine.yield();
      }
      stopKrakenMotor();
    }).named("Stop Kraken");
  }

  public Command cycleKrakenSpeedUp(double rpmChange, int numberChanges) {
    return run(coroutine -> {
      for (int i = 0; i < numberChanges; i++) {
        runKrakenAtVelocity();
        targetRPM += rpmChange;
        coroutine.wait(Seconds.of(.25));
      }
    }).named("Cycle Kraken Speed Up");
  }
  
public Command cycleKrakenSpeedDown(double rpmChange, int numberChanges) {
    return run(coroutine -> {
      for (int i = 0; i < numberChanges; i++) {
        runKrakenAtVelocity();
        targetRPM -= rpmChange;
        coroutine.wait(Seconds.of(.25));
      }
    }).named("Cycle Kraken Speed Down");
  }



  public void clearFaults() {
    x60Motor.clearStickyFaults();
  }

  public double getPosition() {
    return x60Motor.getPosition().getValueAsDouble();
  }

  public double getVelocity() {
    return x60Motor.getVelocity().getValueAsDouble();
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

  public void krakenTelemetry() {
    SmartDashboard.putNumber("KrakenPosition", x60Motor.getPosition().getValueAsDouble());
    SmartDashboard.putNumber("KrakenVelocity", x60Motor.getVelocity().getValueAsDouble());

    SmartDashboard.putNumber("Kraken Throttle", x60Motor.getThrottle());
    SmartDashboard.putNumber("KrakenTemp", x60Motor.getDeviceTemp().getValueAsDouble());
    SmartDashboard.putNumber("KrakenTargetRPM", targetRPM);

    SmartDashboard.putBoolean("FeederStopped", isStopped());
    SmartDashboard.putBoolean("RunKraken", isRunKraken());
    SmartDashboard.putBoolean("KrakenInPosition", inPosition());

  }

}
