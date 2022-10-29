// Copyright (c) FIRST and other WPILib contributors.
// Open Source Software; you can modify and/or share it under the terms of
// the WPILib BSD license file in the root directory of this project.

package frc.robot.commands;

import edu.wpi.first.wpilibj.Timer;
import edu.wpi.first.wpilibj2.command.CommandBase;
import frc.robot.Tunables;
import frc.robot.subsystems.Arm;
import frc.robot.subsystems.IntakeLeft;
import frc.robot.subsystems.IntakeRight;


public class AutoDump extends CommandBase {

  protected final IntakeRight rightWay;
  protected final IntakeLeft leftway;
  protected final Arm armSubsystem;
  protected final Timer timer = new Timer();

  public AutoDump(Arm armSubsystem, IntakeLeft lefteject, IntakeRight righteject) {
    this.armSubsystem = armSubsystem;
    this.leftway = lefteject;
    this.rightWay = righteject;

    // Use addRequirements() here to declare subsystem dependencies.
    this.addRequirements(armSubsystem);
    this.addRequirements(lefteject);
    this.addRequirements(righteject);
  }

  // Called when the command is initially scheduled.
  @Override
  public void initialize() {
    timer.reset();
    timer.start();
  }

  // Called every time the scheduler runs while the command is scheduled.
  @Override
  public void execute() {
    armSubsystem.moveToPositionWithMotionMagic(Tunables.armScorePositionSensorCounts.get());
      leftway.leftejection();
      rightWay.rightejection();
  }
    
  // Called once the command ends or is interrupted.
  @Override
  public void end(boolean interrupted) {
    leftway.leftstopRollers();
    rightWay.rightstopRollers();
  }

  // Returns true when the command should end.
  @Override
  public boolean isFinished() {
    if (timer.get() > 1.6) {
      return true;
    }
    else {
      return false;
    }
  }
}
