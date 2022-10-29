// Copyright (c) FIRST and other WPILib contributors.
// Open Source Software; you can modify and/or share it under the terms of
// the WPILib BSD license file in the root directory of this project.

package frc.robot.commands;


import edu.wpi.first.wpilibj2.command.CommandBase;
import frc.robot.subsystems.Drivetrain;

public class AutoDriveWithSensorUnits extends CommandBase {
  /** Creates a new AutoDriveWithSensorUnits. */
  protected final Drivetrain drivetrain;
  protected final double rightSensorCounts;
  protected final double leftSensorCounts;

  public AutoDriveWithSensorUnits(Drivetrain drivetrain, double leftSensorCounts, double rightSensorCounts) {
    this.drivetrain = drivetrain;
    this.leftSensorCounts = leftSensorCounts;
    this.rightSensorCounts = rightSensorCounts;
    // Use addRequirements() here to declare subsystem dependencies.
    this.addRequirements(drivetrain);
  }

  // Called when the command is initially scheduled.
  @Override
  public void initialize() {}

  // Called every time the scheduler runs while the command is scheduled.
  @Override
  public void execute() {
    this.drivetrain.driveWithMotionMagic(leftSensorCounts, rightSensorCounts);

  }

  // Called once the command ends or is interrupted.
  @Override
  public void end(boolean interrupted) {}

  // Returns true when the command should end.
  @Override
  public boolean isFinished() {
    if (this.drivetrain.isDoneWithMotionMagic()) {
      return true;
    }
    else {
      return false;
    }
  }
}
