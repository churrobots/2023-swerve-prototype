
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
import frc.robot.Drawings;

public class PacManLights extends SubsystemBase {

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

  public PacManLights() {
    leds.setLength(PIXELS);
    leds.start();
    t.start();
  }

  public int getPixelCount() {
    return PIXELS;
  }

  public void clearPixels() {
    fill(0, 0, 0, Drawings.empty);
  }

  public void fill(int red, int green, int blue, int [][] array) {
    int index = 0;
    for (int row = 0; row < array.length; row++) {
      if (row % 2 == 0) {
        for (int col = array[row].length - 1; col >= 0; col--) {
          pixels.setRGB(index, red, green, blue);
          index++;
        }
      } else {
        for (int col = 0; col < array[row].length; col++) {
          pixels.setRGB(index, red, green, blue);
          index++;
        }

      }
    }
    if (index < PIXELS) {
      for (;index < PIXELS; index++){
        pixels.setRGB(index, 0, 0, 0);
     }
   }
    leds.setData(pixels);
  }

  public void setPixels(AddressableLEDBuffer buffer) {
    boolean isError = buffer.getLength() != PIXELS;
    if (isError) {
      fill(255, 0, 0, Drawings.empty);
      return;
    }
    // if (!priorBuffer.equals(buffer)) {
    leds.setData(buffer);
    // }
  }
  
  public void setSpeed(double drivetrainSpeed) {
    if (Math.abs(drivetrainSpeed) < 0.1) {
      fill(100, 70, 0, Drawings.circle);
    }
   
    else if (Math.abs(drivetrainSpeed) < 0.8) {
      fill(100, 70, 0, Drawings.halfMouth);

    }
    else if (Math.abs(drivetrainSpeed) < 1.2) {
      fill(100, 70, 0, Drawings.fullMouth);
    }
    else {
      fill(20, 5, 5, Drawings.empty);
    }
    }
  @Override
  public void periodic() {
    if (RobotState.isDisabled()) {
      runDefaultLights();
    }
      


    // }
  }

  private void runDefaultLights() {
    this.fill(10, 10, 10, Drawings.circle);
  }
}