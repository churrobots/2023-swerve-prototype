
// Copyright (c) FIRST and other WPILib contributors.
// Open Source Software; you can modify and/or share it under the terms of
// the WPILib BSD license file in the root directory of this project.

package frc.robot.subsystems;

import edu.wpi.first.wpilibj.AddressableLED;
import edu.wpi.first.wpilibj.AddressableLEDBuffer;
import edu.wpi.first.wpilibj.RobotState;
import edu.wpi.first.wpilibj.Timer;
import edu.wpi.first.wpilibj2.command.SubsystemBase;
import frc.robot.Constants;

public class StephLightShow extends SubsystemBase {

  // NOTE: you can only allocate ONE strip of LEDs (in series). This is a
  // limitation
  // of how WPILib implements the addressable LED strips.
  // https://www.chiefdelphi.com/t/can-not-allocate-a-second-addressableled-pwm-port/376859

  // Our lights are set up as two 8x32 grids in series.
  AddressableLED leds = new AddressableLED(Constants.lightsPWM);
  final int ROWS = 8;
  final int COLS = 32;
  final int STRIPS = 2;
  final int PIXELS = (ROWS * COLS) * STRIPS;
  AddressableLEDBuffer pixels = new AddressableLEDBuffer(PIXELS);
  Timer t = new Timer();
  double waitTime = 0.0;

  public StephLightShow() {
    leds.setLength(PIXELS);
    leds.start();
    t.start();
  }

  public int getPixelCount() {
    return PIXELS;
  }

  public void clearPixels() {
    fill(0, 0, 0);
  }

  public void fill(int red, int green, int blue) {
    for (int i = 0; i < PIXELS; i++) {
      pixels.setRGB(i, red, green, blue);
    }
    leds.setData(pixels);
  }

  public void fillPercentage(double redPercent, double greenPercent, double bluePercent) {
    int red = (int) Math.floor(redPercent * 255);
    int green = (int) Math.floor(greenPercent * 255);
    int blue = (int) Math.floor(bluePercent * 255);
    for (int i = 0; i < PIXELS; i++) {
      pixels.setRGB(i, red, green, blue);
    }
    leds.setData(pixels);
  }

  public void setPixels(AddressableLEDBuffer buffer) {
    boolean isError = buffer.getLength() != PIXELS;
    if (isError) {
      fill(255, 0, 0);
      return;
    }
    // if (!priorBuffer.equals(buffer)) {
    leds.setData(buffer);
    // }
  }


  
  public void setSpeed(double drivetrainSpeedPercentage) {
    if (Math.abs(drivetrainSpeedPercentage) < 0.1) {
      runDefaultLights(1, 0, 0);
    }
    else if (Math.abs(drivetrainSpeedPercentage) < 0.8) {
      runDefaultLights(1, 1, 0);
    } else if (Math.abs(drivetrainSpeedPercentage) < 1.2) {
      runDefaultLights(0, 1, 1);
    }
  }
  
  @Override
  public void periodic() {
    if (RobotState.isDisabled() && RobotState.isTeleop()) {
      runDefaultLights(1,1,1);
    }
    else if (RobotState.isAutonomous() && RobotState.isDisabled())
    {
      runDefaultLights(1, 0,1 );
    }
    else if (RobotState.isAutonomous() && RobotState.isEnabled()) {
      runDefaultLights(1, 0, 0);
    }
      


    // }
  }

  private void runDefaultLights(int r, int g, int b) {
    this.fill(r, g, b);
  }
}