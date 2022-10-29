/*----------------------------------------------------------------------------*/
/* Copyright (c) 2018-2019 FIRST. All Rights Reserved.                        */
/* Open Source Software - may be modified and shared by FRC teams. The code   */
/* must be accompanied by the FIRST BSD license file in the root directory of */
/* the project.                                                               */
/*----------------------------------------------------------------------------*/

package frc.robot;

import frc.robot.commands.HoldArmForDriving;
import frc.robot.commands.LightChaser;
import frc.robot.commands.ScoreBoth;
import frc.robot.commands.ScoreLeft;
import frc.robot.commands.ScoreRight;
import frc.robot.commands.ShowPacMan;
import frc.robot.commands.Tuck;
import frc.robot.commands.UnleashTheUltimateButterDuster;
import frc.robot.commands.Vacuum;
import frc.robot.subsystems.Arm;
import frc.robot.subsystems.ButterDuster;
import frc.robot.subsystems.Climber;
import frc.robot.subsystems.Drivetrain;
import frc.robot.subsystems.IntakeLeft;
import frc.robot.subsystems.IntakeRight;
import frc.robot.subsystems.StephLightShow;
// import frc.robot.subsystems.PacManLights;
import frc.robot.commands.AutoBackAwayFromHubOffTarmac;
import frc.robot.commands.AutoDriveToTheHub;
import frc.robot.commands.AutoDriveWithSensorUnits;
import frc.robot.commands.AutoDump;
import frc.robot.commands.AutoReadyToScore;
import frc.robot.commands.AutoResetEncoders;
import frc.robot.commands.AutoResetOdometry;
import frc.robot.commands.AutoVacuum;
import frc.robot.commands.AutoBackOutOfEdgeOfTarmac;
import frc.robot.commands.Calibrating;
import frc.robot.commands.ClimbDown;
import frc.robot.commands.ClimbUp;
import frc.robot.commands.DriveWithCurvature;
// import frc.robot.commands.ShowPacMan;
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

  // Connect to all the outputs.
  Drivetrain drivetrain = new Drivetrain();
  IntakeLeft polterLeftGust3000 = new IntakeLeft();
  IntakeRight polterRightGust3000 = new IntakeRight();
  Arm muscleArm = new Arm();
  Climber climber = new Climber();
  ButterDuster butterDuster = new ButterDuster();
  //TheShow theShow = new TheShow();
  StephLightShow stephLightShow = new StephLightShow();
  // PacManLights pacManLights = new PacManLights();


  // Create the autonomous chooser.
  SendableChooser<Command> autonomousChooser = new SendableChooser<Command>();

  public RobotContainer() {

    // Enable the camera.
    CameraServer.startAutomaticCapture();

    // Default commands
    drivetrain.setDefaultCommand(new DriveWithCurvature(drivetrain, stephLightShow,driverGamepad.leftYAxis,
        driverGamepad.rightXAxis));
    muscleArm.setDefaultCommand(new HoldArmForDriving(muscleArm));
    stephLightShow.setDefaultCommand(new LightChaser(stephLightShow));

    // Wire up commands to the controllers.
    operatorGamepad.getDualButton(operatorGamepad.startButton, operatorGamepad.backButton)
        .whileHeld(new Calibrating(muscleArm));
    operatorGamepad.povDown.whileHeld(new ClimbDown(climber));
    operatorGamepad.xButton.whileHeld(new Tuck(muscleArm));
    operatorGamepad.povUp.whileHeld(new ClimbUp(climber));
    operatorGamepad.yButton.whileHeld(new Vacuum(muscleArm, polterLeftGust3000, polterRightGust3000));
    operatorGamepad.leftBumper.whileHeld(new ScoreLeft(muscleArm, polterLeftGust3000));
    operatorGamepad.rightBumper.whileHeld(new ScoreRight(muscleArm, polterRightGust3000));
    operatorGamepad.getDualButton(operatorGamepad.leftBumper, operatorGamepad.rightBumper)
        .whileHeld(new ScoreBoth(muscleArm, polterLeftGust3000, polterRightGust3000));

    driverGamepad.rightBumper.whileHeld(new Tuck(muscleArm));
    // TODO: Make sure we raise our arm after climb so we don't hit a partner in front of us

    // Set the options for autonomous.
    Command dump = new SequentialCommandGroup(
      new AutoResetEncoders(drivetrain),
      new AutoDump(muscleArm, polterLeftGust3000, polterRightGust3000)
    );
    Command backAway = new SequentialCommandGroup(
      new AutoResetEncoders(drivetrain),
      new AutoBackAwayFromHubOffTarmac(drivetrain)
    );
    Command dustAndBackAway = new SequentialCommandGroup(
        new UnleashTheUltimateButterDuster(butterDuster), // DUSTED
        new AutoResetEncoders(drivetrain),
        new WaitCommand(2),
        new AutoDriveWithSensorUnits(drivetrain, 47000, 47000) // get off the tarmac
    );

    Command dustAndScore = new SequentialCommandGroup(
        new UnleashTheUltimateButterDuster(butterDuster), // DUSTED
        new AutoResetEncoders(drivetrain),
        new WaitCommand(2),
        new AutoDriveWithSensorUnits(drivetrain, 47000, 47000), // get off the tarmac
        new AutoDriveWithSensorUnits(drivetrain, 0, 0), //go back to tarmac
        new AutoDriveWithSensorUnits(drivetrain, -516 + 200, 15688 + 200), //trun toward ball
        new ParallelCommandGroup(
          new AutoDriveWithSensorUnits(drivetrain, 29346, 46507), //drive to ball
          new AutoVacuum(muscleArm, polterLeftGust3000, polterRightGust3000)//Intake the Ball
        ),
        new AutoReadyToScore(muscleArm, polterLeftGust3000, polterRightGust3000),//Puts arm up
        new AutoDriveWithSensorUnits(drivetrain, 8573, 79277), //turn to Hub
        new AutoDriveWithSensorUnits(drivetrain, 70847 - 1000, 141684 - 2000), //drive to hub
        new AutoDump(muscleArm, polterLeftGust3000, polterRightGust3000)
    );
    Command dumpAndBackAway = new SequentialCommandGroup(
      new AutoResetEncoders(drivetrain),
      new AutoDump(muscleArm, polterLeftGust3000, polterRightGust3000),
      new AutoBackAwayFromHubOffTarmac(drivetrain)
    );
    Command waitForTeammate = new SequentialCommandGroup(
        new AutoResetEncoders(drivetrain),
        new AutoBackOutOfEdgeOfTarmac(drivetrain),
        new WaitCommand(3),
        new ParallelRaceGroup(
          new WaitCommand(4),
          new AutoDriveToTheHub(drivetrain)
        ),
        new AutoDump(muscleArm, polterLeftGust3000, polterRightGust3000)
    );
    Command twoBallAutoWallTarmac = new SequentialCommandGroup(
        new AutoResetEncoders(drivetrain),
        new AutoVacuum(muscleArm, polterLeftGust3000, polterRightGust3000), // drop arm and start intaking
        new AutoDriveWithSensorUnits(drivetrain, 32000, 32000), // drive to pickup
        new AutoReadyToScore(muscleArm, polterLeftGust3000, polterRightGust3000), // pull arm up
        new AutoDriveWithSensorUnits(drivetrain, 40000, 40000), // get off the tarmac
        new AutoDriveWithSensorUnits(drivetrain, 28000, 28000), // move back to prevent bumper crash
        new AutoDriveWithSensorUnits(drivetrain, 1924, 62000), // Sonic spin
        new ParallelRaceGroup(
          new WaitCommand(4),
          new AutoDriveWithSensorUnits(drivetrain, 50000, 118148)
        ),
        new AutoDump(muscleArm, polterLeftGust3000, polterRightGust3000)
    );
    Command twoBallAutoHangerTarmac = new SequentialCommandGroup(
      new AutoResetEncoders(drivetrain),
      new AutoVacuum(muscleArm, polterLeftGust3000, polterRightGust3000), // drop arm and start intaking
      new AutoDriveWithSensorUnits(drivetrain, 32000, 32000), // drive to pickup
      new AutoReadyToScore(muscleArm, polterLeftGust3000, polterRightGust3000), // pull arm up
      new AutoDriveWithSensorUnits(drivetrain, 45000, 45000), // get off the tarmac
      new AutoDriveWithSensorUnits(drivetrain, 28000, 28000), // move back to prevent bumper crash
      new AutoDriveWithSensorUnits(drivetrain, 62000, 1924), // Sonic spin
      new ParallelRaceGroup(
        new WaitCommand(4),
        new AutoDriveWithSensorUnits(drivetrain, 118148, 50000)
      ),
      new AutoDump(muscleArm, polterLeftGust3000, polterRightGust3000)
    );

    Trajectory FirstPart = getTrajectory("pathplanner/generatedJSON/TheBoringOneBallPart1.wpilib.json");
    Trajectory SecondPart = getTrajectory("pathplanner/generatedJSON/TheBoringOneBallPart2.wpilib.json");
    Command testAutoTrajectory = new SequentialCommandGroup(
        new AutoResetOdometry(drivetrain, FirstPart.getInitialPose()),
        new ParallelCommandGroup(
          new AutoVacuum(muscleArm, polterLeftGust3000, polterRightGust3000),  
          drivetrain.getTrajectoryCommand(FirstPart)
        ),
        new AutoReadyToScore(muscleArm, polterLeftGust3000, polterRightGust3000),
        drivetrain.getTrajectoryCommand(SecondPart),
        new AutoDump(muscleArm, polterLeftGust3000, polterRightGust3000)
    );

    Trajectory driveBetweenBalls = getTrajectory("pathplanner/generatedJSON/DriveBetweenBalls.wpilib.json");
    Trajectory grabFirstBall = getTrajectory("pathplanner/generatedJSON/PickupFirstBall.wpilib.json");
    Trajectory grabSecondBall = getTrajectory("pathplanner/generatedJSON/PickupSecondBall.wpilib.json");
    Trajectory scoreSecondBall = getTrajectory("pathplanner/generatedJSON/ScoreTheFreshOnes.wpilib.json");

    Command testTrajectoryCommand2 = new SequentialCommandGroup(
      new AutoResetOdometry(drivetrain, driveBetweenBalls.getInitialPose()),
      new AutoDump(muscleArm, polterLeftGust3000, polterRightGust3000),  
      drivetrain.getTrajectoryCommand(driveBetweenBalls),
        new ParallelCommandGroup(
            drivetrain.getTrajectoryCommand(grabFirstBall),
            new AutoVacuum(muscleArm, polterLeftGust3000, polterRightGust3000)
        ),
        new ParallelCommandGroup(
            drivetrain.getTrajectoryCommand(grabSecondBall),
            new AutoVacuum(muscleArm, polterLeftGust3000, polterRightGust3000)
        ),
        new AutoReadyToScore(muscleArm, polterLeftGust3000, polterRightGust3000),
        drivetrain.getTrajectoryCommand(scoreSecondBall),
        new AutoDump(muscleArm, polterLeftGust3000, polterRightGust3000)
    );

    Trajectory moveToBall = getTrajectory("pathplanner/generatedJSON/AfterTheDust.wpilib.json");
    Trajectory scoreBall = getTrajectory("pathplanner/generatedJSON/ScoreAfterDust.wpilib.json");

    Command scoreAfterDust = new SequentialCommandGroup(
        new AutoResetOdometry(drivetrain, moveToBall.getInitialPose()),
        new UnleashTheUltimateButterDuster(butterDuster),
        new AutoResetEncoders(drivetrain),
        new WaitCommand(1),
        new ParallelCommandGroup(
          drivetrain.getTrajectoryCommand(moveToBall),
          new AutoVacuum(muscleArm, polterLeftGust3000, polterRightGust3000)
        ),
        new AutoReadyToScore(muscleArm, polterLeftGust3000, polterRightGust3000),
        drivetrain.getTrajectoryCommand(scoreBall),
        new AutoDump(muscleArm, polterLeftGust3000, polterRightGust3000)
    );

    Trajectory scorePreLoad = getTrajectory("pathplanner/generatedJSON/ScorePreLoadDust.wpilib.json");
    Command scorePreLoadAfterDust = new SequentialCommandGroup(
      new AutoResetOdometry(drivetrain, scorePreLoad.getInitialPose()),
      new UnleashTheUltimateButterDuster(butterDuster),
      new AutoResetEncoders(drivetrain),
      new WaitCommand(1),
      drivetrain.getTrajectoryCommand(scorePreLoad),
      new AutoDump(muscleArm, polterLeftGust3000, polterRightGust3000)
    );

    Trajectory taxiOut = getTrajectory("pathplanner/generatedJSON/TaxiOut.wpilib.json");
    Trajectory taxiDump = getTrajectory("pathplanner/generatedJSON/TaxiToDump.wpilib.json");

    Command taxiWaitDump = new SequentialCommandGroup(
        new AutoResetOdometry(drivetrain, taxiOut.getInitialPose()),
        drivetrain.getTrajectoryCommand(taxiOut),
        new WaitCommand(6),
        drivetrain.getTrajectoryCommand(taxiDump),
        new AutoDump(muscleArm, polterLeftGust3000, polterRightGust3000)
    );

    Trajectory dumpTax = getTrajectory("pathplanner/generatedJSON/DumpToTaxi.wpilib.json");
    Command dumpTaxi = new SequentialCommandGroup(
        new AutoResetOdometry(drivetrain, dumpTax.getInitialPose()),
        new AutoDump(muscleArm, polterLeftGust3000, polterRightGust3000),
        new WaitCommand(7),
        drivetrain.getTrajectoryCommand(dumpTax)
            );

    // Command showPacManCommand = new ShowPacMan(drivetrain, pacManLights, driverGamepad.leftYAxis,
    //     driverGamepad.rightXAxis);

    autonomousChooser.setDefaultOption("1-ball Auto: Drive, Wait, Dump", waitForTeammate);
    autonomousChooser.addOption("2-ball Auto: From WALL Tarmac", twoBallAutoWallTarmac);
    autonomousChooser.addOption("2-ball Auto: From HANGAR Tarmac", twoBallAutoHangerTarmac);
    autonomousChooser.addOption("Dump and backoff (must start at Hub instead)", dumpAndBackAway);
    autonomousChooser.addOption("Dump only (must start at Hub instead)", dump);
    autonomousChooser.addOption("Backoff only (must start at Hub instead)", backAway);
    autonomousChooser.addOption("DUST YUR BUTTERS", dustAndBackAway);
    // autonomousChooser.addOption("PAC MAN LIGHTS", showPacManCommand);
    // autonomousChooser.addOption("Three Ball auto(Trajectory)", testTrajectoryCommand2);
    autonomousChooser.addOption("Dust and Score Preload (Trajectory)", scorePreLoadAfterDust);
    autonomousChooser.addOption("Dust and Yoink Ball (Trajectory)", scoreAfterDust);
    autonomousChooser.addOption("Taxi, Wait, Dump (Trajectory)", taxiWaitDump);
    autonomousChooser.addOption("Dump, Taxi, Done (Trajectory)", dumpTaxi);




    // autonomousChooser.addOption("TheClassicOneBallRunAndDump", testAutoTrajectory);
    // autonomousChooser.addOption("test trajectory", testTrajectoryCommand2);
    SmartDashboard.putData(autonomousChooser);

    // Show all the subsystems in the smartdashboard.
    SmartDashboard.putData(CommandScheduler.getInstance());
    SmartDashboard.putData((drivetrain));
    SmartDashboard.putData((polterLeftGust3000));
    SmartDashboard.putData((polterRightGust3000));
    SmartDashboard.putData((muscleArm));
  }

  public Command getAutonomousCommand() {
    return autonomousChooser.getSelected();
  }

  public Trajectory getTrajectory(String trajectoryJSON) {

    Path trajectoryPath = Filesystem.getDeployDirectory().toPath().resolve(trajectoryJSON);
    Trajectory trajectory = new Trajectory();
    try {
      trajectory = TrajectoryUtil.fromPathweaverJson(trajectoryPath);
    } catch (IOException err) {
      DriverStation.reportError("broken!!!", err.getStackTrace());
    }
    return trajectory;
  }

}
