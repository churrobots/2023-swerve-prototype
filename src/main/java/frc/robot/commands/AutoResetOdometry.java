// Copyright (c) FIRST and other WPILib contributors.
// Open Source Software; you can modify and/or share it under the terms of
// the WPILib BSD license file in the root directory of this project.

package frc.robot.commands;

import edu.wpi.first.math.geometry.Pose2d;
import edu.wpi.first.wpilibj.Timer;
import edu.wpi.first.wpilibj2.command.CommandBase;
import frc.robot.subsystems.Drivetrain;

public class AutoResetOdometry extends CommandBase {
  /** Creates a new AutoResetEncoders. */
  Drivetrain drivetrain;
  Pose2d initialPose;
  Timer timer = new Timer();

  public AutoResetOdometry(Drivetrain drivetrain, Pose2d initialPose) {
    this.drivetrain = drivetrain;
    this.initialPose = initialPose;
    this.addRequirements(drivetrain);
    // Use addRequirements() here to declare subsystem dependencies.
  }

  // Called when the command is initially scheduled.
  @Override
  public void initialize() {
    this.timer.reset();
    this.timer.start();
    this.drivetrain.resetOdometry(initialPose);
  }

  // Called every time the scheduler runs while the command is scheduled.
  @Override
  public void execute() {}

  // Called once the command ends or is interrupted.
  @Override
  public void end(boolean interrupted) {
    this.timer.stop();
  }

  // Returns true when the command should end.
  @Override
  public boolean isFinished() {
    return this.timer.get() > 0.5;
  }
}
