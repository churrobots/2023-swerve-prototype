// Copyright (c) FIRST and other WPILib contributors.
// Copyright (c) FIRST and other WPILib contributors.
// Open Source Software; you can modify and/or share it under the terms of
// the WPILib BSD license file in the root directory of this project.

package frc.robot.subsystems;
import com.ctre.phoenix.motorcontrol.ControlMode;
import com.ctre.phoenix.motorcontrol.can.WPI_VictorSPX;

import edu.wpi.first.wpilibj2.command.SubsystemBase;
import frc.robot.Constants;
import frc.robot.helpers.SubsystemInspector;

public class ButterDuster extends SubsystemBase {
  
  private final WPI_VictorSPX butterDusterMotor = new WPI_VictorSPX(Constants.butterDusterCAN);

  private final SubsystemInspector inspector = new SubsystemInspector("ButterDuster");
  

  public ButterDuster() {

  }

  @Override
  public void periodic() {
    butterDusterMotor.feed();
    inspector.set("currentCommand", this.getCurrentCommand());
  }

  public void unleash() {
    butterDusterMotor.set(ControlMode.PercentOutput, 0.60);
  }

  public void contain() {
    butterDusterMotor.set(ControlMode.PercentOutput, -0.30);
  }

  public void stop() {
    butterDusterMotor.set(ControlMode.PercentOutput, 0.00);
  }
}
