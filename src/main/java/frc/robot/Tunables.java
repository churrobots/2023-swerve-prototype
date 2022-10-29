package frc.robot;

import frc.robot.helpers.Tuner.TunableBoolean;
import frc.robot.helpers.Tuner.TunableDouble;
import frc.robot.helpers.Tuner.TunableInteger;

public final class Tunables {

  public static final TunableDouble armKF = new TunableDouble("armKF", 0.2);
  public static final TunableDouble armKP = new TunableDouble("armKP", 0.2);
  public static final TunableInteger armSmoothingStrength = new TunableInteger("armSmoothingStrength", 3);
  public static final TunableInteger armCruiseVelocityInSensorUnits = new TunableInteger("armCruiseVelocityInSensorUnits", 18000);
  public static final TunableInteger armAccelerationInSensorUnits = new TunableInteger("armAccelerationInSensorUnits", 8000);

  public static final TunableDouble maxDriveVoltage = new TunableDouble("maxDriveVoltage", 0.83);
  public static final TunableDouble maxDriveAcceleration = new TunableDouble("maxDriveAcceleration", 2.4);
  public static final TunableDouble maxDriveMetersPerSecond = new TunableDouble("maxDriveMetersPerSecond", 2.3);

  public static final TunableBoolean driveWithMetersPerSecond = new TunableBoolean("driveWithMetersPerSecond", true);

  public static final TunableDouble ejectionSpeedPercentage = new TunableDouble("ejectionSpeedPercentage", -1.0);  

  public static final TunableInteger armScorePositionSensorCounts = new TunableInteger("armScorePositionSensorCounts", 500);
  public static final TunableInteger armVacuumPositionSensorCounts = new TunableInteger("armVacuumPositionSensorCounts", -43000);
  public static final TunableInteger armDrivingPositionSensorCounts = new TunableInteger("armDrivingPositionSensorCounts", 500);
  public static final TunableInteger armTuckingPositionSensorCounts = new TunableInteger("armTuckingPositionSensorCounts", -17000);

  public static final TunableDouble maxArmCurrent = new TunableDouble("maxArmCurrent", 15.0);
  public static final TunableDouble maxArmSeconds = new TunableDouble("maxArmSeconds", 3.0);

}

