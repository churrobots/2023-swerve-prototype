/*----------------------------------------------------------------------------*/
/* Copyright (c) 2018-2019 FIRST. All Rights Reserved.                        */
/* Open Source Software - may be modified and shared by FRC teams. The code   */
/* must be accompanied by the FIRST BSD license file in the root directory of */
/* the project.                                                               */
/*----------------------------------------------------------------------------*/

package frc.robot;

/**
 * The Constants class provides a convenient place for teams to hold robot-wide numerical or boolean
 * constants. This class should not be used for any other purpose. Do not put anything functional here.
 */
public final class Constants {

  public static final double joystickDeadband = 0.02;
  public static final int driverGamepadPort = 0;
  public static final int operatorGamepadPort = 1;

  // The Falcon 500s have a Talon FX Integrated sensor, which is rated for 2048 units per rotation:
  // https://docs.ctre-phoenix.com/en/latest/ch14_MCSensor.html#sensor-resolution
  public static final int sensorUnitsPerRevolution = 2048;

}
