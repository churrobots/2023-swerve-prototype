// Copyright (c) FIRST and other WPILib contributors.
// Open Source Software; you can modify and/or share it under the terms of
// the WPILib BSD license file in the root directory of this project.

package frc.robot.subsystems;

import com.ctre.phoenix.motorcontrol.FeedbackDevice;
import com.ctre.phoenix.motorcontrol.NeutralMode;
import com.ctre.phoenix.motorcontrol.StatusFrameEnhanced;
import com.ctre.phoenix.motorcontrol.TalonFXControlMode;
import com.ctre.phoenix.motorcontrol.can.WPI_TalonFX;

import edu.wpi.first.wpilibj.DigitalInput;
import edu.wpi.first.wpilibj.RobotState;
import edu.wpi.first.wpilibj.Timer;
import edu.wpi.first.wpilibj2.command.SubsystemBase;
import frc.robot.Constants;
import frc.robot.Tunables;
import frc.robot.helpers.SubsystemInspector;

public class Arm extends SubsystemBase {
  
  private final WPI_TalonFX armMotor = new WPI_TalonFX(Constants.falconArmCAN);
  private final DigitalInput armSensor = new DigitalInput(Constants.armSensorDIO);
  private boolean isCalibrating = true;
  private Timer timer = new Timer();
  private boolean isEstop = false;

  private final SubsystemInspector inspector = new SubsystemInspector("Arm");
  private double mostRecentArmSensorCountTarget = 0;

  /** Creates a new Arm. */
  public Arm() {
    armMotor.configFactoryDefault();
    armMotor.configSelectedFeedbackSensor(FeedbackDevice.IntegratedSensor);
    configureMotionMagic();
  }

  private void configureMotionMagic() {
    // This sets a lot of the defaults that the example code seems to require
    // for full functioning of the Falcon500s. Cargo culting FTW.
    // https://github.com/CrossTheRoadElec/Phoenix-Examples-Languages/blob/master/Java%20Talon%20FX%20(Falcon%20500)/MotionMagic/src/main/java/frc/robot/Robot.java
    // TODO: calculate or characterize these values? why would you ever not use the 0th slots?
    int fakeSlot = 0;
    int fakePIDSlot = 0;
    int fakeTimeoutMilliseconds = 30;
    double fakeKP = Tunables.armKP.get();
    double fakeKI = 0.0;
    double fakeKD = 0.0;
    double fakeKF = Tunables.armKF.get();

    armMotor.configNeutralDeadband(0.001); // really low deadzone

    armMotor.setStatusFramePeriod(StatusFrameEnhanced.Status_13_Base_PIDF0, 10, fakeTimeoutMilliseconds);
		armMotor.setStatusFramePeriod(StatusFrameEnhanced.Status_10_MotionMagic, 10, fakeTimeoutMilliseconds);

		armMotor.selectProfileSlot(fakeSlot, fakePIDSlot);
		armMotor.config_kF(fakeSlot, fakeKF, fakeTimeoutMilliseconds);
		armMotor.config_kP(fakeSlot, fakeKP, fakeTimeoutMilliseconds);
		armMotor.config_kI(fakeSlot, fakeKI, fakeTimeoutMilliseconds);
		armMotor.config_kD(fakeSlot, fakeKD, fakeTimeoutMilliseconds);

		armMotor.configMotionCruiseVelocity(Tunables.armCruiseVelocityInSensorUnits.get(), fakeTimeoutMilliseconds);
		armMotor.configMotionAcceleration(Tunables.armAccelerationInSensorUnits.get(), fakeTimeoutMilliseconds);
    armMotor.configMotionSCurveStrength(Tunables.armSmoothingStrength.get());
    
    armMotor.configPeakOutputForward(0.4);
    armMotor.configPeakOutputReverse(-0.4);


  }

  public void beginCalibration() {
    isCalibrating = true;
    isEstop = false;
  }

  public void finishCalibration() {
    armMotor.set(TalonFXControlMode.PercentOutput, 0);
    armMotor.setSelectedSensorPosition(0);
    isCalibrating = false; 
  }

  public double getCurrentPosition() {
    return armMotor.getSelectedSensorPosition();
  }

  public void moveToPositionWithMotionMagic(int sensorCountsFromUp) {
    if (!isCalibrating && !isEstop){
      this.armMotor.set(TalonFXControlMode.MotionMagic, sensorCountsFromUp);
      inspector.set("moveToPosition", sensorCountsFromUp);
      mostRecentArmSensorCountTarget = sensorCountsFromUp;
    }
  }
 
  @Override
  public void periodic() {
    calibrateIfNeeded();
    if (Tunables.armKF.didChange()
        || Tunables.armKP.didChange()
        || Tunables.armSmoothingStrength.didChange()
        || Tunables.armAccelerationInSensorUnits.didChange()
        || Tunables.armCruiseVelocityInSensorUnits.didChange()
    ) {
      configureMotionMagic();
    }
    
    makeItSafePlease(); //no -Maynor

    // Coast when disabled, and also make sure arm freshly moves to the upward position upon enabling
    if (RobotState.isDisabled()) {
      armMotor.setNeutralMode(NeutralMode.Brake);
      moveToPositionWithMotionMagic(Tunables.armScorePositionSensorCounts.get());
    } else {
      armMotor.setNeutralMode(NeutralMode.Brake);
    }
    inspector.set("armSensor", armSensor.get());
    inspector.set("isCalibrating", isCalibrating);
    inspector.set("sensorCount", this.armMotor.getSelectedSensorPosition());
    inspector.set("statorCurrent", armMotor.getStatorCurrent());
    inspector.set("Emergency stop", isEstop);
  }

  private void makeItSafePlease() {
    double current = armMotor.getStatorCurrent();
    if (current > Tunables.maxArmCurrent.get()) {
      timer.start();
      if (timer.get() > Tunables.maxArmSeconds.get()) {
        isEstop = true;
      }
    }
    else {
      timer.reset();
    }
    if (isEstop) {
      armMotor.stopMotor();
    }
  }

  public boolean isDoneWithMotionMagic() {
    var deltaArm = Math.abs(armMotor.getSelectedSensorPosition() - mostRecentArmSensorCountTarget);
    var isFinished = deltaArm < 2000;
    return isFinished;
  }

  private void calibrateIfNeeded() {
    final boolean isArmUp = armSensor.get() == true;
    if (isCalibrating) {
      if (isArmUp) {
        armMotor.set(TalonFXControlMode.PercentOutput, 0);
        armMotor.setSelectedSensorPosition(0);
        isCalibrating = false; 
      }
      else {
        armMotor.set(TalonFXControlMode.PercentOutput, Constants.armCalibrationSpeedPercentage);
      }
    }
  }

}
