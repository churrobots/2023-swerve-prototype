// Copyright (c) FIRST and other WPILib contributors.
// Open Source Software; you can modify and/or share it under the terms of
// the WPILib BSD license file in the root directory of this project.

package frc.robot.commands;

import edu.wpi.first.wpilibj2.command.CommandBase;
import frc.robot.Tunables;
import frc.robot.subsystems.Arm;
import frc.robot.subsystems.IntakeRight;

public class ScoreRight extends CommandBase {
  Arm arm;
  IntakeRight intakeRight;
  /** Creates a new Score. */
  public ScoreRight(Arm arm, IntakeRight intakeRight) {
    // Use addRequirements() here to declare subsystem dependencies.
    this.arm = arm;
    this.intakeRight = intakeRight;
    addRequirements(arm);
    addRequirements(intakeRight);
  }

  // Called when the command is initially scheduled.
  @Override
  public void initialize() {
    arm.moveToPositionWithMotionMagic(Tunables.armScorePositionSensorCounts.get());
  }

  // Called every time the scheduler runs while the command is scheduled.
  @Override
  public void execute() {
    if (arm.isDoneWithMotionMagic()) {
      intakeRight.rightejection();
    }
  }

  // Called once the command ends or is interrupted.
  @Override
  public void end(boolean interrupted) {
    intakeRight.rightstopRollers();
  }

  // Returns true when the command should end.
  @Override
  public boolean isFinished() {
    return false;
  }
}
