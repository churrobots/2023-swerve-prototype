/*----------------------------------------------------------------------------*/
/* Copyright (c) 2018-2019 FIRST. All Rights Reserved.                        */
/* Open Source Software - may be modified and shared by FRC teams. The code   */
/* must be accompanied by the FIRST BSD license file in the root directory of */
/* the project.                                                               */
/*----------------------------------------------------------------------------*/

package frc.robot;
import frc.robot.helpers.Gamepad;

import java.io.IOException;
import java.nio.file.Path;

import edu.wpi.first.cameraserver.CameraServer;
import edu.wpi.first.math.trajectory.Trajectory;
import edu.wpi.first.math.trajectory.TrajectoryUtil;
import edu.wpi.first.wpilibj.DriverStation;
import edu.wpi.first.wpilibj.Filesystem;
import edu.wpi.first.wpilibj.smartdashboard.SendableChooser;
import edu.wpi.first.wpilibj.smartdashboard.SmartDashboard;
import edu.wpi.first.wpilibj2.command.Command;
import edu.wpi.first.wpilibj2.command.CommandScheduler;
import edu.wpi.first.wpilibj2.command.ParallelCommandGroup;
import edu.wpi.first.wpilibj2.command.ParallelRaceGroup;
import edu.wpi.first.wpilibj2.command.SequentialCommandGroup;
import edu.wpi.first.wpilibj2.command.WaitCommand;

/**
 * This class is where the bulk of the robot should be declared. Since
 * Command-based is a "declarative" paradigm, very little robot logic should
 * actually be handled in the {@link Robot} periodic methods (other than the
 * scheduler calls). Instead, the structure of the robot (including subsystems,
 * commands, and button mappings) should be declared here.
 */
public class RobotContainer {

  // Connect to all the inputs (gamepads and shuffleboard).
  Gamepad driverGamepad = new Gamepad(Constants.driverGamepadPort); 
  Gamepad operatorGamepad = new Gamepad(Constants.operatorGamepadPort);

  // Create the autonomous chooser.
  SendableChooser<Command> autonomousChooser = new SendableChooser<Command>();

  public RobotContainer() {

    // Enable the camera.
    CameraServer.startAutomaticCapture();

    // autonomousChooser.setDefaultOption("1-ball Auto: Drive, Wait, Dump", waitForTeammate);
    // autonomousChooser.addOption("2-ball Auto: From WALL Tarmac", twoBallAutoWallTarmac);
    SmartDashboard.putData(autonomousChooser);

    // Show all the subsystems in the smartdashboard.
    SmartDashboard.putData(CommandScheduler.getInstance());
  }

  public Command getAutonomousCommand() {
    return autonomousChooser.getSelected();
  }

  public Trajectory getTrajectory(String trajectoryName) {

    Path trajectoryPath = Filesystem.getDeployDirectory().toPath().resolve(
      "pathplanner/generatedJSON/" + trajectoryName + ".wpilib.json");

    try {
      var preprogrammedTrajectory = TrajectoryUtil.fromPathweaverJson(trajectoryPath);
      return preprogrammedTrajectory;
    } catch (IOException err) {
      DriverStation.reportError("broken!!!", err.getStackTrace());
      var emptyTrajectory = new Trajectory();
      return emptyTrajectory;
    }

  }

}
