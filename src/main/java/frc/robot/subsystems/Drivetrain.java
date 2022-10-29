/*----------------------------------------------------------------------------*/
/* Copyright (c) 2018-2019 FIRST. All Rights Reserved.                        */
/* Open Source Software - may be modified and shared by FRC teams. The code   */
/* must be accompanied by the FIRST BSD license file in the root directory of */
/* the project.                                                               */
/*----------------------------------------------------------------------------*/

package frc.robot.subsystems;

import edu.wpi.first.math.MathUtil;
import edu.wpi.first.math.controller.PIDController;
import edu.wpi.first.math.controller.RamseteController;
import edu.wpi.first.math.controller.SimpleMotorFeedforward;
import edu.wpi.first.math.filter.SlewRateLimiter;
import edu.wpi.first.math.geometry.Pose2d;
import edu.wpi.first.math.geometry.Rotation2d;
import edu.wpi.first.math.kinematics.DifferentialDriveKinematics;
import edu.wpi.first.math.kinematics.DifferentialDriveOdometry;
import edu.wpi.first.math.kinematics.DifferentialDriveWheelSpeeds;
import edu.wpi.first.math.trajectory.Trajectory;
import edu.wpi.first.math.util.Units;
import edu.wpi.first.wpilibj.RobotState;
import edu.wpi.first.wpilibj.drive.DifferentialDrive;
import edu.wpi.first.wpilibj.drive.DifferentialDrive.WheelSpeeds;
import edu.wpi.first.wpilibj.smartdashboard.Field2d;
import edu.wpi.first.wpilibj.smartdashboard.SmartDashboard;
import edu.wpi.first.wpilibj2.command.Command;
import edu.wpi.first.wpilibj2.command.RamseteCommand;
import edu.wpi.first.wpilibj2.command.SubsystemBase;

import com.ctre.phoenix.motorcontrol.FeedbackDevice;
import com.ctre.phoenix.motorcontrol.InvertType;
import com.ctre.phoenix.motorcontrol.NeutralMode;
import com.ctre.phoenix.motorcontrol.StatusFrameEnhanced;
import com.ctre.phoenix.motorcontrol.TalonFXControlMode;
import com.ctre.phoenix.motorcontrol.can.WPI_TalonFX;
import com.ctre.phoenix.sensors.WPI_Pigeon2;

import frc.robot.Constants;
import frc.robot.Tunables;
import frc.robot.helpers.SubsystemInspector;

public class Drivetrain extends SubsystemBase {

  private final SubsystemInspector inspector = new SubsystemInspector("Drivetrain");

  private final WPI_TalonFX leftFollower = new WPI_TalonFX(Constants.falconRearLeftCAN);
  private final WPI_TalonFX leftLeader = new WPI_TalonFX(Constants.falconFrontLeftCAN);

  private final WPI_TalonFX rightFollower = new WPI_TalonFX(Constants.falconRearRightCAN);
  private final WPI_TalonFX rightLeader = new WPI_TalonFX(Constants.falconFrontRightCAN);

  private final WPI_Pigeon2 pigeonGyro = new WPI_Pigeon2(Constants.pigeonCAN);

  private SlewRateLimiter curvatureThrottleFilter = new SlewRateLimiter(Tunables.maxDriveAcceleration.get());

  private final PIDController leftPIDController = new PIDController(Constants.kP, 0, Constants.kD);
  private final PIDController rightPIDController = new PIDController(Constants.kP, 0, Constants.kD);
  private final SimpleMotorFeedforward feedforward = new SimpleMotorFeedforward(Constants.kS, Constants.kV, Constants.kA);
  private final DifferentialDriveKinematics kinematics = new DifferentialDriveKinematics(
      Units.inchesToMeters(Constants.trackWidthInInches));

  private final DifferentialDriveOdometry odometry;
  private final Field2d field = new Field2d();

  private final DifferentialDrive differentialDrive = new DifferentialDrive(leftLeader, rightLeader);
  private double mostRecentLeftSensorCountTarget = 0;
  private double mostRecentRightSensorCountTarget = 0;

  public Drivetrain() {

    SmartDashboard.putData("Field", field);

    leftLeader.configFactoryDefault();
    leftLeader.setInverted(Constants.leftFalconsAreInverted);
    leftLeader.configSelectedFeedbackSensor(FeedbackDevice.IntegratedSensor);
    leftLeader.configNeutralDeadband(Constants.drivetrainNeutralDeadbandPercentage);

    rightLeader.configFactoryDefault();
    rightLeader.setInverted(Constants.rightFalconsAreInverted);
    rightLeader.configSelectedFeedbackSensor(FeedbackDevice.IntegratedSensor);
    rightLeader.configNeutralDeadband(Constants.drivetrainNeutralDeadbandPercentage);

    leftFollower.configFactoryDefault();
    leftFollower.follow(leftLeader);
    leftFollower.setInverted(InvertType.FollowMaster);

    rightFollower.configFactoryDefault();
    rightFollower.follow(rightLeader);
    rightFollower.setInverted(InvertType.FollowMaster);

    configureMotionMagic(leftLeader);
    configureMotionMagic(rightLeader);

    pigeonGyro.configFactoryDefault();
    pigeonGyro.reset();

    Pose2d initialPosition = new Pose2d(0.0, 0.0, getHeadingInRotation2d());
    odometry = new DifferentialDriveOdometry(getHeadingInRotation2d());
    resetOdometry(initialPosition);
  }
  
  private DifferentialDriveWheelSpeeds getWheelSpeeds() {
    double leftSensorUnitsPer100ms = leftLeader.getSelectedSensorVelocity();
    double rightSensorUnitsPer100ms = rightLeader.getSelectedSensorVelocity();
    double leftMetersPer100ms = convertSensorCountsToDistanceInMeters(leftSensorUnitsPer100ms);
    double rightMetersPer100ms = convertSensorCountsToDistanceInMeters(rightSensorUnitsPer100ms);
    double leftMetersPerSecond = leftMetersPer100ms * 10;
    double rightMetersPerSecond = rightMetersPer100ms * 10;
    return new DifferentialDriveWheelSpeeds(leftMetersPerSecond, rightMetersPerSecond);
  }

  public void resetEncoders() {
    leftLeader.setSelectedSensorPosition(0);
    rightLeader.setSelectedSensorPosition(0);
    leftLeader.stopMotor();
    rightLeader.stopMotor();
  }

  public void resetOdometry(Pose2d pose) {
    resetEncoders();
    odometry.resetPosition(pose, getHeadingInRotation2d());
  }

  public double getPitch() {
    double pitch = pigeonGyro.getPitch();
    return pitch;

  }

  public void driveWithMotionMagic(double leftSensorCounts, double rightSensorCounts) {
    mostRecentLeftSensorCountTarget = leftSensorCounts;
    mostRecentRightSensorCountTarget = rightSensorCounts;
    leftLeader.set(TalonFXControlMode.MotionMagic, leftSensorCounts);
    rightLeader.set(TalonFXControlMode.MotionMagic, rightSensorCounts);
  }

  public boolean isDoneWithMotionMagic() {
    var deltaLeft = Math.abs(leftLeader.getSelectedSensorPosition() - mostRecentLeftSensorCountTarget);
    var deltaRight = Math.abs(rightLeader.getSelectedSensorPosition() - mostRecentRightSensorCountTarget);
    var isFinished = deltaLeft < 918 && deltaRight < 918;
    return isFinished;
  }

  public void driveWithCurvature(double throttlePercent, double curvaturePercentage, boolean allowSpinning) {
    var smoothedThrottlePercent = curvatureThrottleFilter.calculate(throttlePercent);
    if (Tunables.driveWithMetersPerSecond.get() == true) {
      smoothedThrottlePercent = MathUtil.applyDeadband(smoothedThrottlePercent, Constants.joystickDeadband);
      curvaturePercentage = MathUtil.applyDeadband(curvaturePercentage, Constants.joystickDeadband);
      WheelSpeeds speeds = DifferentialDrive.curvatureDriveIK(smoothedThrottlePercent, curvaturePercentage,
          allowSpinning);
      var leftMetersPerSecond = speeds.left * Tunables.maxDriveMetersPerSecond.get();
      var rightMetersPerSecond = speeds.right * Tunables.maxDriveMetersPerSecond.get();
      this.driveWithMetersPerSecond(leftMetersPerSecond, rightMetersPerSecond);
    } else {
      differentialDrive.curvatureDrive(smoothedThrottlePercent, curvaturePercentage, allowSpinning);
    }
  }

  @Override
  public void periodic() {
    differentialDrive.feed();

    // Allow tuning
    if (Tunables.maxDriveAcceleration.didChange()) {
      curvatureThrottleFilter = new SlewRateLimiter(Tunables.maxDriveAcceleration.get());
    }

    var maxVoltagePercentage = Tunables.maxDriveVoltage.get();
    differentialDrive.setMaxOutput(Tunables.maxDriveVoltage.get());
    leftLeader.configPeakOutputForward(maxVoltagePercentage);
    leftLeader.configPeakOutputReverse(-maxVoltagePercentage);
    rightLeader.configPeakOutputForward(maxVoltagePercentage);
    rightLeader.configPeakOutputReverse(-maxVoltagePercentage);

    // Coast when disabled
    if (RobotState.isDisabled()) {
      leftLeader.setNeutralMode(NeutralMode.Coast);
      rightLeader.setNeutralMode(NeutralMode.Coast);
    } else {
      leftLeader.setNeutralMode(NeutralMode.Brake);
      rightLeader.setNeutralMode(NeutralMode.Brake);
    }

    // Update odometry
    Rotation2d heading = getHeadingInRotation2d();
    double leftDistanceInMeters = convertSensorCountsToDistanceInMeters(leftLeader.getSelectedSensorPosition());
    double rightDistanceInMeters = convertSensorCountsToDistanceInMeters(rightLeader.getSelectedSensorPosition());
    odometry.update(heading, leftDistanceInMeters, rightDistanceInMeters);

    // Show debug information in NetworkTables
    Pose2d pose = odometry.getPoseMeters();
    field.setRobotPose(pose);
    inspector.set("rotation", pose.getRotation().getDegrees());
    inspector.set("x", pose.getX());
    inspector.set("y", pose.getY());
    inspector.set("roll", pigeonGyro.getRoll());
    inspector.set("pitch", pigeonGyro.getPitch());
    inspector.set("leftVoltage", leftLeader.getMotorOutputVoltage());
    inspector.set("rightVoltage", rightLeader.getMotorOutputVoltage());
    inspector.set("leftSensor", leftLeader.getSelectedSensorPosition());
    inspector.set("rightSensor", rightLeader.getSelectedSensorPosition());
    inspector.set("isDoneWithMotionMagic", isDoneWithMotionMagic());
  }

  public Pose2d getPose() {
    return odometry.getPoseMeters();
  }

  public void stopDriving() {
    driveWithCurvature(0, 0, false);
  }
  private void tankDriveVolts(double leftVolts, double rightVolts) {
    leftLeader.setVoltage(leftVolts);
    rightLeader.setVoltage(rightVolts);
  }
  
  public double getHeading() {
    return pigeonGyro.getRotation2d().getDegrees();
  }

  private Rotation2d getHeadingInRotation2d() {
    return pigeonGyro.getRotation2d();
  }

  private double convertSensorCountsToDistanceInMeters(double sensorCounts) {
    double motorRotations = (double) sensorCounts / Constants.sensorUnitsPerRevolution;
    double wheelRotations = motorRotations / Constants.driveGearRatio;
    double inchesOfRotation = wheelRotations * 2 * Math.PI * Constants.driveWheelRadiusInInches;
    return Units.inchesToMeters(inchesOfRotation);
  }

  public Command getTrajectoryCommand(Trajectory trajectory) {

    RamseteCommand ramseteCommand = new RamseteCommand(
        trajectory,
        this::getPose,
        new RamseteController(),
        feedforward,
        kinematics,
        this::getWheelSpeeds,
        leftPIDController,
        rightPIDController,
        // RamseteCommand passes volts to the callback
        this::tankDriveVolts,
        this);


    // Run path following command, then stop at the end.
    return ramseteCommand.andThen(() -> this.tankDriveVolts(0, 0));
  }

  private void configureMotionMagic(WPI_TalonFX motor) {
    // This sets a lot of the defaults that the example code seems to require
    // for full functioning of the Falcon500s. Cargo culting FTW.
    // https://github.com/CrossTheRoadElec/Phoenix-Examples-Languages/blob/master/Java%20Talon%20FX%20(Falcon%20500)/MotionMagic/src/main/java/frc/robot/Robot.java
    // TODO: calculate or characterize these values? why would you ever not use the 0th slots?
    int fakeSlot = 0;
    int fakePIDSlot = 0;
    int timeoutMilliseconds = 30;

    motor.configNeutralDeadband(0.001); // really low deadzone

    motor.setStatusFramePeriod(StatusFrameEnhanced.Status_13_Base_PIDF0, 10, timeoutMilliseconds);
		motor.setStatusFramePeriod(StatusFrameEnhanced.Status_10_MotionMagic, 10, timeoutMilliseconds);

		motor.selectProfileSlot(fakeSlot, fakePIDSlot);
		motor.config_kF(fakeSlot, Tunables.armKF.get(), timeoutMilliseconds);
		motor.config_kP(fakeSlot, Tunables.armKP.get(), timeoutMilliseconds);
		motor.config_kI(fakeSlot, 0.0, timeoutMilliseconds);
		motor.config_kD(fakeSlot, 0.0, timeoutMilliseconds);

		motor.configMotionCruiseVelocity(12000, timeoutMilliseconds);
		motor.configMotionAcceleration(3000, timeoutMilliseconds);
    motor.configMotionSCurveStrength(4);
  }

  private void driveWithMetersPerSecond(double leftTargetMetersPerSecond, double rightTargetMetersPerSecond) {

    double leftFeedforward = feedforward.calculate(leftTargetMetersPerSecond);
    double leftActualSensorCountsPerSecond = leftLeader.getSelectedSensorVelocity();
    double leftActualMetersPerSecond = convertSensorCountsToDistanceInMeters(leftActualSensorCountsPerSecond);
    double leftFeedback = leftPIDController.calculate(leftActualMetersPerSecond, leftTargetMetersPerSecond);
    double leftVoltage = leftFeedback + leftFeedforward;
    leftLeader.setVoltage(leftVoltage);

    double rightFeedforward = feedforward.calculate(rightTargetMetersPerSecond);
    double rightActualSensorCountsPerSecond = rightLeader.getSelectedSensorVelocity();
    double rightActualMetersPerSecond = convertSensorCountsToDistanceInMeters(rightActualSensorCountsPerSecond);
    double rightFeedback = rightPIDController.calculate(rightActualMetersPerSecond, rightTargetMetersPerSecond);
    double rightVoltage = rightFeedback + rightFeedforward;
    rightLeader.setVoltage(rightVoltage);
  }

}
