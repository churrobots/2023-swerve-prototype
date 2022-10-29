## SVR TODO

1. Characterize Beefcake and update constants
2. Write subsystem + commands for Butter Duster, and make a single Auto option for it
3. Revert to normal arm controls from SAC + add "tuck" mode
4. 

## STEP 1: Install WPILib on your computer
1. Download the MacOS zip from https://github.com/wpilibsuite/allwpilib/releases/tag/v2022.2.1
2. Run the installer
3. Choose "Everything" for install mode
4. Choose "Download for this computer only (fastest)"

## STEP 2: Install Phoenix in your WPILib
1. Download the MacOS zip from https://store.ctr-electronics.com/software/
2. Copy the `vendordeps` and `maven` directories into your WPILib home (should be in `~/wpilib/YEAR/*`)

## TROUBLESHOOTING

* If build fails, delete the .gradle folder in the project directory