// Copyright (c) FIRST and other WPILib contributors.
// Open Source Software; you can modify and/or share it under the terms of
// the WPILib BSD license file in the root directory of this project.

package frc.robot.subsystems;

import com.ctre.phoenix.motorcontrol.FeedbackDevice;
import com.ctre.phoenix.motorcontrol.can.WPI_TalonFX;
import com.ctre.phoenix.motorcontrol.can.WPI_VictorSPX;

import edu.wpi.first.math.controller.PIDController;
import edu.wpi.first.math.controller.ProfiledPIDController;
import edu.wpi.first.math.geometry.Rotation2d;
import edu.wpi.first.math.kinematics.SwerveModuleState;
import edu.wpi.first.math.trajectory.TrapezoidProfile;
import edu.wpi.first.math.util.Units;
import edu.wpi.first.wpilibj.AnalogEncoder;
import frc.robot.Constants;
import frc.robot.Constants.ModuleConstants;


public class SwerveModule {
  private final WPI_TalonFX driveMotor; 
  private final WPI_VictorSPX m_turningMotor;


  private final AnalogEncoder m_turningEncoder;



  private final PIDController m_drivePIDController =
      new PIDController(ModuleConstants.kPModuleDriveController, 0, 0);

  // Using a TrapezoidProfile PIDController to allow for smooth turning
  private final ProfiledPIDController m_turningPIDController =
      new ProfiledPIDController(
          ModuleConstants.kPModuleTurningController,
          0,
          0,
          new TrapezoidProfile.Constraints(
              ModuleConstants.kMaxModuleAngularSpeedRadiansPerSecond,
              ModuleConstants.kMaxModuleAngularAccelerationRadiansPerSecondSquared));

  /**
   * Constructs a SwerveModule.
   *
   * @param driveMotorChannel The channel of the drive motor.
   * @param turningMotorChannel The channel of the turning motor.
   * @param driveEncoderChannels The channels of the drive encoder.
   * @param turningEncoderChannels The channels of the turning encoder.
   * @param driveEncoderReversed Whether the drive encoder is reversed.
   * @param turningEncoderReversed Whether the turning encoder is reversed.
   */
  public SwerveModule(
      int driveMotorChannel,
      int turningMotorChannel,
      int turningEncoderChannels,
      boolean driveEncoderReversed,
      boolean turningEncoderReversed) {
    
    
    
    driveMotor = new WPI_TalonFX(driveMotorChannel);
    driveMotor.configSelectedFeedbackSensor(FeedbackDevice.IntegratedSensor);

    m_turningMotor = new WPI_VictorSPX(turningMotorChannel);


    m_turningEncoder = new AnalogEncoder(turningEncoderChannels);
    m_turningEncoder.reset();

    // Set the distance per pulse for the drive encoder. We can simply use the
    // distance traveled for one rotation of the wheel divided by the encoder
    // resolution.
    
    //m_driveEncoder.setDistancePerPulse(ModuleConstants.kDriveEncoderDistancePerPulse);

    // Set whether drive encoder should be reversed or not
    
    //m_driveEncoder.setReverseDirection(driveEncoderReversed);

    // Set the distance (in this case, angle) per pulse for the turning encoder.
    // This is the the angle through an entire rotation (2 * pi) divided by the
    // encoder resolution.
    
    //m_turningEncoder.setDistancePerPulse(ModuleConstants.kTurningEncoderDistancePerPulse);

    // Set whether turning encoder should be reversed or not
    
    //m_turningEncoder.setReverseDirection(turningEncoderReversed);

    // Limit the PID Controller's input range between -pi and pi and set the input
    // to be continuous.
    m_turningPIDController.enableContinuousInput(-Math.PI, Math.PI);
  }

  /**
   * Returns the current state of the module.
   *
   * @return The current state of the module.
   */
  public SwerveModuleState getState() {
    return new SwerveModuleState(driveMotor.getSelectedSensorVelocity(), new Rotation2d(m_turningEncoder.get()));
  }
  

  /**
   * Sets the desired state for the module.
   *
   * @param desiredState Desired state with speed and angle.
   */
  private double convertSensorCountsToDistanceInMeters(double sensorCounts) {
    double motorRotations = (double) sensorCounts / Constants.sensorUnitsPerRevolution;
    double wheelRotations = motorRotations / Constants.driveGearRatio;
    double inchesOfRotation = wheelRotations * 2 * Math.PI * Constants.driveWheelRadiusInInches;
    return Units.inchesToMeters(inchesOfRotation);
  }

  public void setDesiredState(SwerveModuleState desiredState) {
    // Optimize the reference state to avoid spinning further than 90 degrees
    SwerveModuleState optimizedDesiredState =
        SwerveModuleState.optimize(desiredState, new Rotation2d(m_turningEncoder.get()));

    // Calculate the drive output from the drive PID controller.
    double actualMetersPerSecond = driveMotor.getSelectedSensorVelocity();
    actualMetersPerSecond = convertSensorCountsToDistanceInMeters(actualMetersPerSecond);
    final double driveOutput =
        m_drivePIDController.calculate(actualMetersPerSecond, optimizedDesiredState.speedMetersPerSecond);

    // Calculate the turning motor output from the turning PID controller.
    final double turnOutput =
        m_turningPIDController.calculate(m_turningEncoder.get(), optimizedDesiredState.angle.getRadians());

    // Calculate the turning motor output from the turning PID controller.
    driveMotor.set(driveOutput);
    m_turningMotor.set(turnOutput);
  }

  /** Zeroes all the SwerveModule encoders. */
  public void resetEncoders() {
    driveMotor.setSelectedSensorPosition(0);
    m_turningEncoder.reset();
  }

  public void setTestOutputs(double driveOutput, double turnOutput) {
    driveMotor.set(driveOutput);
    m_turningMotor.set(turnOutput);
  }
}