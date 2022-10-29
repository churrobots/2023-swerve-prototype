// Copyright (c) FIRST and other WPILib contributors.
// Open Source Software; you can modify and/or share it under the terms of
// the WPILib BSD license file in the root directory of this project.

package frc.robot.subsystems;

import com.ctre.phoenix.motorcontrol.can.WPI_VictorSPX;

import edu.wpi.first.wpilibj.RobotState;
import edu.wpi.first.wpilibj2.command.SubsystemBase;
import frc.robot.Constants;
import frc.robot.Tunables;

public class IntakeLeft extends SubsystemBase {
  private final WPI_VictorSPX leftRollerMotor = new WPI_VictorSPX(Constants.leftRollerMotorCAN);
  /** Creates a new Intake. */
  public IntakeLeft() {
    leftRollerMotor.setInverted(Constants.leftRollerMotorIsInverted);
  }
  
  public void collectballs() {
    this.leftRollerMotor.set(Constants.collectionSpeedPercentage);

  }
  
  public void leftejection() {
    this.leftRollerMotor.set(Tunables.ejectionSpeedPercentage.get());
  }

  public void leftstopRollers() {
    this.leftRollerMotor.set(0.0);
  }

  @Override
  public void periodic() {
    // This method will be called once per scheduler run
    if (RobotState.isDisabled()) {
      leftstopRollers();
    }
  }
}
