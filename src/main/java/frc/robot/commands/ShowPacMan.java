/*----------------------------------------------------------------------------*/
/* Copyright (c) 2018-2019 FIRST. All Rights Reserved.                        */
/* Open Source Software - may be modified and shared by FRC teams. The code   */
/* must be accompanied by the FIRST BSD license file in the root directory of */
/* the project.                                                               */
/*----------------------------------------------------------------------------*/

package frc.robot.commands;

import frc.robot.helpers.Gamepad.Axis;
import frc.robot.subsystems.Drivetrain;
import frc.robot.subsystems.PacManLights;
import frc.robot.subsystems.StephLightShow;
import edu.wpi.first.wpilibj2.command.CommandBase;

/**
 * An example command that uses an example subsystem.
 */
public class ShowPacMan extends CommandBase {

  protected final Drivetrain drivetrainSubsystem;
  protected final PacManLights pacManLights;
  protected final Axis leftAxis;
  protected final Axis rightHorizontalAxis;

  public ShowPacMan(Drivetrain drivetrainSubsystem, PacManLights pacManLights ,Axis leftAxis, Axis rightHorizontalAxis) {
    this.drivetrainSubsystem = drivetrainSubsystem;
    this.pacManLights = pacManLights;
    this.leftAxis = leftAxis;
    this.rightHorizontalAxis = rightHorizontalAxis;
    this.addRequirements(this.pacManLights);
    this.addRequirements(this.drivetrainSubsystem);
  }

  @Override
  public void initialize() {
  }

  @Override
  public void execute() {
    double throttlePercentage = -1 * leftAxis.get() * Math.abs(leftAxis.get());
    double curvaturePercentage = rightHorizontalAxis.get() * Math.abs(rightHorizontalAxis.get());;
    boolean allowSpinning = true;
    this.pacManLights.setSpeed(throttlePercentage);
    this.drivetrainSubsystem.driveWithCurvature(throttlePercentage, curvaturePercentage, allowSpinning);
  }

  @Override
  public void end(boolean interrupted) {
    this.drivetrainSubsystem.stopDriving();
  }

  @Override
  public boolean isFinished() {
    return false;
  }
}
