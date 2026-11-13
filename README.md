# NFR Products FTC-Biobuzz StarterBot

An **FTC SDK 12.0 Java project for Android Studio** with two-motor tank drive, ball collection, and an automatic shot sequence. The robot uses one Control Hub, four motors, and one gate servo. The shooter and intake use encoder feedback to regulate their speed.

## 1. Download and install on the Control Hub

1. Open the [GitHub repository](https://github.com/mehmetucr/nfr-products-ftc-biobuzz-starterbot), select **Code → Download ZIP**, and extract the archive. If you use Git, run `git clone https://github.com/mehmetucr/nfr-products-ftc-biobuzz-starterbot.git`. [GitHub download instructions](https://docs.github.com/en/repositories/working-with-files/using-files/downloading-source-code-archives)
2. In Android Studio, select **Open** and choose the project folder containing `settings.gradle`, `TeamCode`, and `FtcRobotController`. Wait for Gradle Sync to finish. Install **Android SDK Platform 30** and **Android SDK Platform-Tools** from SDK Manager if prompted. The first sync needs internet access to download dependencies. [FTC SDK 12.0 release notes](https://github.com/FIRST-Tech-Challenge/FtcRobotController/releases/tag/v12.0)
3. Power the Control Hub from the robot battery. Connect your computer to the Hub's **USB-C port** with a data cable. In Android Studio, select the **TeamCode** run configuration and the **Control Hub** as the target device. Click **Run ▶** to build and install the Robot Controller app. [FIRST installation guide](https://ftc-docs.firstinspires.org/en/latest/programming_resources/tutorial_specific/android_studio/creating_op_modes/Creating-and-Running-an-Op-Mode-%28Android-Studio%29.html)
4. Connect the Driver Station to the Control Hub and activate the hardware configuration described below. Connect the controller to the Driver Station and verify that it is assigned to **Gamepad 1**. Select **TeleOp → NFR Products FTC-Biobuzz StarterBot**, press **INIT**, and check the displayed ports. Place the robot in a safe test position before pressing **START**. The shooter starts spinning toward its idle RPM as soon as you press START.

**Install over Wi-Fi.** Connect your computer to the Control Hub's Wi-Fi network and confirm that [Program & Manage](http://192.168.43.1:8080) opens. Run `adb connect 192.168.43.1:5555` using `adb` from Android SDK Platform-Tools. If `adb` is missing from your PATH, the default executable path is `~/Library/Android/sdk/platform-tools/adb` on macOS and `%LOCALAPPDATA%\Android\Sdk\platform-tools\adb.exe` on Windows. When `adb devices` lists the Hub as `device`, select it in Android Studio. [FIRST wireless ADB instructions](https://ftc-docs.firstinspires.org/en/latest/programming_resources/shared/managing_control_hub/Managing-a-Control-Hub.html#connecting-to-the-control-hub-using-wireless-adb)

## 2. Wire the robot and configure the Driver Station

| Control Hub connection | Port | Component | Driver Station configuration name |
|---|---:|---|---|
| Motor | 0 | Right drive motor | `sag_drive` |
| Motor | 1 | Left drive motor | `sol_drive` |
| Motor | 2 | Intake motor | `intake` |
| Motor | 3 | Shooter motor | `shooter` |
| Servo | 0 | Ball gate, positional servo | `kapak` |
| Encoder | 2 | Intake encoder | Uses the matching motor configuration |
| Encoder | 3 | Shooter encoder | Uses the matching motor configuration |

In the Driver Station app, open **⋮ → Configure Robot**. Edit an existing configuration or use **New → Scan** to create one. Enter the names exactly as shown in the table, choose the types that match your hardware, and select **Servo** for `kapak`. Save and activate the configuration. The code looks up devices by name. Plug each encoder into the same Hub and port number as its motor power cable. The drive motors run without encoder feedback. [FIRST hardware configuration guide](https://ftc-docs.firstinspires.org/en/latest/hardware_and_software_configuration/configuring/getting_started/getting-started.html)

## 3. Gamepad controls and shot sequence

| Gamepad 1 | Action |
|---|---|
| Left stick Y | Drive forward or backward |
| Right stick X | Turn left or right |
| Right bumper | Toggle ball collection |
| A | Start the automatic shot sequence |
| X | Cancel the shot, close the gate, and stop the intake |

After pressing START, leave both sticks centered for about **0.25 seconds** to enable driving. Between shots, the shooter targets `IDLE_RPM`.

1. Press **A** to stop collection and bring the shooter up to `SHOT_RPM`.
2. The first time the measured speed comes within **±100 RPM** of the target, a **0.5-second** timer starts. The timer continues if the speed leaves that range. If the shooter fails to enter the range within **5 seconds**, the shot is canceled.
3. The servo opens the gate. **0.20 seconds** after the open command, the intake starts at the feed speed. Feeding continues if shooter RPM drops during the shot.
4. **2 seconds** after the open command, the gate closes, the intake stops, and the shooter returns to its idle target. Additional **A** and **right bumper** presses are ignored during the sequence. Press **X** to cancel the shot or **STOP** on the Driver Station to end the OpMode.

Adjust the tolerance and timing with `RPM_TOLERANCE`, `RPM_REACHED_WAIT_SECONDS`, `SPINUP_TIMEOUT_SECONDS`, `GATE_SETTLE_SECONDS`, and `GATE_OPEN_SECONDS`. Gate timing is measured from the servo open command.

## 4. Set shooter, intake, and feeder speeds

Edit [RobotSettings.java](TeamCode/src/main/java/org/firstinspires/ftc/teamcode/RobotSettings.java). RPM means revolutions per minute. Enter positive speed targets and use the `*_REVERSED` settings to choose motor direction. Click **Run** in Android Studio after each change to install the updated code.

| Setting | Default | When it is used |
|---|---:|---|
| `IDLE_RPM` | 2000 | Shooter wheel speed between shots |
| `SHOT_RPM` | 3175 | Shooter wheel speed for a shot started with A |
| `INTAKE_RPM` | 500 | Intake wheel speed while collecting balls with the right bumper |
| `SHOT_INTAKE_RPM` | 500 | Intake wheel speed while feeding balls after the gate opens |

The **feeder** is the intake motor running in feed mode during a shot. Collection and feed speeds can be set independently.

The conversion is `wheel RPM = ticks per second × 60 / (ticks per motor revolution × total gear ratio)`. The ratio is the number of motor turns per wheel turn. The code assumes **28 ticks per motor revolution and 1:1 gearing** for the shooter, and **28 ticks per motor revolution and 68:13 gearing** for the intake. The actual ratio of the REV UltraPlanetary **5:1** cartridge is 68:13. With the intake wheel directly on the cartridge output, 500 wheel RPM is approximately **1,220 ticks per second**. Update the encoder counts and total ratio in `RobotSettings.java` if your robot uses a different motor or additional belt or gear reduction. [REV encoder calculations](https://docs.revrobotics.com/duo-control/hello-robot-java/part-3/autonomous-navigation-onbot/converting-encoder-ticks-to-a-distance) · [REV cartridge ratios](https://docs.revrobotics.com/rev-crossover-products/ultraplanetary/cartridge-details)

The Driver Station displays measured and target speed on the **Shooter RPM** and **Intake teker RPM (olculen / hedef)** lines. For example, `483 / 500` means the intake wheel is turning at 483 RPM with a target of 500 RPM.

### Enter your motor and gearbox ratios

Open [RobotSettings.java](TeamCode/src/main/java/org/firstinspires/ftc/teamcode/RobotSettings.java) and edit the constants for the mechanism you changed.

| Mechanism | Total ratio (motor turns per wheel turn) | Encoder ticks per motor shaft turn |
|---|---|---|
| Shooter | `MOTOR_REVS_PER_WHEEL_REV` | `ENCODER_TICKS_PER_MOTOR_REV` |
| Intake and feeder | `INTAKE_MOTOR_REVS_PER_WHEEL_REV` | `INTAKE_ENCODER_TICKS_PER_MOTOR_REV` |

1. Find the actual gearbox ratio in the manufacturer's specifications. Enter the number of **motor shaft turns for one wheel turn**. A direct drive is `1.0`. A 20:1 gearbox driving the wheel directly is `20.0`. For the REV UltraPlanetary cartridge labeled 5:1, enter `68.0 / 13.0`. Use decimal literals so Java keeps the fractional part. [REV cartridge ratios](https://docs.revrobotics.com/rev-crossover-products/ultraplanetary/cartridge-details)
2. Multiply the gearbox ratio by any external gear or belt ratio. For a toothed drive, divide the driven gear or pulley tooth count by the driving tooth count. For example, if the 68:13 gearbox drives a 36-tooth pulley from an 18-tooth pulley, use `(68.0 / 13.0) * (36.0 / 18.0)` for `INTAKE_MOTOR_REVS_PER_WHEEL_REV`. [REV encoder and gearing guide](https://docs.revrobotics.com/duo-control/hello-robot-java/part-3/autonomous-navigation-onbot/converting-encoder-ticks-to-a-distance)
3. Enter encoder ticks per **motor shaft revolution before reduction** in the matching tick constant. For a REV HD Hex motor, use `28`. If your datasheet gives ticks per gearbox output revolution, divide that count by the gearbox ratio to get ticks per motor shaft revolution. This keeps the gearbox reduction from being counted twice.
4. Reinstall with **Run** in Android Studio. Test without balls and check the wheel RPM on the Driver Station. The RPM targets in the table above refer to the wheel after all reductions.

## 5. Correct motor directions and calibrate the gate

Raise the robot so the drive wheels are off the ground. Test without balls, keep hands clear of moving parts, and use brief commands. Both wheels should drive the robot forward when you push the left stick forward. The intake should pull balls in, and the shooter should send them toward the exit. If a component moves the wrong way, switch its setting from `true` to `false` or from `false` to `true`, then reinstall the code.

| Component | `RobotSettings.java` setting |
|---|---|
| Left drive | `LEFT_REVERSED` |
| Right drive | `RIGHT_REVERSED` |
| Intake and feeder | `INTAKE_REVERSED` |
| Shooter | `SHOOTER_REVERSED` |
| Gate servo | `GATE_REVERSED` |

`GATE_CLOSED_POSITION` and `GATE_OPEN_POSITION` range from **0 to 1**. The gate moves to its closed position on INIT. Adjust the endpoints in small steps while watching the linkage. The code assumes a **180°** servo range for its angle conversion. Verify that the gate opens and closes freely before testing with balls.

## 6. How the PIDF controller works

The shooter and intake each use a separate instance of [ShooterVelocityController.java](TeamCode/src/main/java/org/firstinspires/ftc/teamcode/ShooterVelocityController.java). Each controller reads the encoder and calculates the voltage needed to correct the **error = target RPM − measured RPM**.

```text
requested voltage = kS + kV × target RPM + kP × error
                    + kI × accumulated error − kD × rate of change of measured RPM
motor power = requested voltage / measured battery voltage, clamped to 0–1
```

| Term | Shooter | Intake | What it does |
|---|---|---|---|
| Static feedforward | `kS` | `INTAKE_KS` | Adds a constant voltage to help overcome friction |
| Velocity feedforward | `kV` | `INTAKE_KV` | Estimates the voltage needed to maintain the target speed |
| Proportional | `kP` | `INTAKE_KP` | Responds to the current speed error |
| Integral | `kI` | `INTAKE_KI` | Corrects persistent error by accumulating it over time |
| Derivative | `kD` | `INTAKE_KD` | Opposes changes in measured speed to help damp oscillation |

The gains use **volts and wheel RPM**. For example, `kV` and `kP` are in V/RPM. The Java control loop reads encoder speed in `RUN_WITHOUT_ENCODER` mode and commands the motor with `setPower()`. It limits integral buildup when the output is saturated and resets the integral when the target changes. A zero target commands zero power. [WPILib flywheel tuning guide](https://docs.wpilib.org/en/stable/docs/software/advanced-controls/introduction/tuning-flywheel.html)

### Tune the controller on your robot

1. **Verify the measurement.** Start without balls and check the motor direction, encoder connection, and gear ratio. Measured RPM should be positive when the motor turns in its intended direction. Watch the target and measured speed, motor power, and **Batarya V** on the Driver Station.
2. **Tune feedforward first.** Work on one mechanism at a time. Temporarily set its `kP`, `kI`, and `kD` to zero. Pick a fixed, achievable `IDLE_RPM` for the shooter or `INTAKE_RPM` for the intake. After the speed settles, increase `kV` in small steps if the measured speed stays below target, or reduce it if the speed stays above target. You can tune `kS` separately if friction makes the motor slow to start at low speeds.
3. **Use `kP` to improve recovery.** Start with a small `kP` and change one gain at a time. Increase it slightly if the speed returns to target slowly. Reduce it if the speed keeps oscillating around the target or overshoot increases noticeably. Compare the RPM and motor power after each change.
4. **Add `kI` or `kD` as needed.** Try a small `kI` if a steady error remains after tuning feedforward and `kP`. Leave `kD` at zero if the speed measurement is noisy. If the system needs more damping, try a small `kD` and observe the response. The starting gains will behave differently on different robots.
5. **Test the final settings with balls.** Restore any temporary speed targets. Use similar balls, battery voltage, and shooting distance across tests. Compare the minimum RPM during the shot, recovery toward the target, and accuracy. If the motor command stays at `1.000` while the speed is low, check whether the target is achievable with the current battery, feed speed, and mechanical friction.

**Battery voltage compensation.** The controller uses the gains in `RobotSettings.java` and divides its voltage request by the measured battery voltage on each loop. For a 9 V request, the power command is 0.75 with a 12 V battery and 0.90 with a 10 V battery. **Shooter V (istenen / tahmini)** shows the requested voltage and the estimated applied voltage, calculated as `power × battery voltage`. If the request exceeds the measured battery voltage, the output is capped at full power and the Driver Station displays a message. Check the mechanical load, feed speed, and battery if the shooter cannot reach its target.

## 7. First test and troubleshooting

| Symptom | What to check |
|---|---|
| `Unable to find a hardware device` during INIT | Check the exact device names in the active robot configuration. Verify each motor or servo type and port. |
| The RPM target is set, but the reading is zero or implausible | Press STOP. Check the intake encoder on port 2 or the shooter encoder on port 3, then verify the encoder cable and gear ratio. |
| The shot times out | Compare measured and target speed on the **Shooter RPM** line. Check the encoder, direction, target speed, and motor command. |
| The intake collects balls, but feeding is inconsistent | Check that the gate opens and that the intake turns in the right direction. Adjust `SHOT_INTAKE_RPM` in small steps. |
| Android Studio cannot see the Hub | Power on the Hub and use a USB-C data cable. For Wi-Fi, check that Program & Manage opens, then check `adb devices`. |
| The TeleOp does not appear | Verify that you installed **TeamCode** on the intended Control Hub and that the Driver Station is connected to it. Reopen the TeleOp list. |

### Ball stacking and single-ball feeding

- **Balls ride up over one another during collection.** Check the gate's closed position first. If the gate sits too far open, adjust `GATE_CLOSED_POSITION` to close it a little farther. If it already closes far enough, reduce `INTAKE_RPM` slightly and test again with several balls.
- **A single ball fails to reach the shooter during a shot.** Adjust `GATE_CLOSED_POSITION` toward a slightly more closed position in steps of about **2°**, testing with one ball each time. For example, try `102.0 / SERVO_TOTAL_TRAVEL_DEG` instead of `100.0 / SERVO_TOTAL_TRAVEL_DEG` if increasing the value closes the gate farther on your robot.
- **Consecutive shots are inconsistent after tuning the shooter PIDF.** Reduce `SHOT_INTAKE_RPM` in **50 RPM steps**, for example from 500 to 450, then to 400 if needed. Use the same number of balls for each test. Compare how consistently they feed and how closely the shooter returns to target between balls.

Change one setting per test and reinstall the updated code with **Run** in Android Studio.

## 8. Where to start in the code

| File | What it contains |
|---|---|
| [RobotSettings.java](TeamCode/src/main/java/org/firstinspires/ftc/teamcode/RobotSettings.java) | Speed targets, PIDF gains, gear ratios, directions, and timing |
| [TankShooterTeleOp.java](TeamCode/src/main/java/org/firstinspires/ftc/teamcode/TankShooterTeleOp.java) | Hardware mapping, gamepad input, motor commands, and Driver Station telemetry |
| [ShotController.java](TeamCode/src/main/java/org/firstinspires/ftc/teamcode/ShotController.java) | Shot sequence, including waiting, gate movement, and feeding |
| [ShooterVelocityController.java](TeamCode/src/main/java/org/firstinspires/ftc/teamcode/ShooterVelocityController.java) | PIDF calculation and battery voltage compensation for both mechanisms |

[FTC SDK 12.0 source project](https://github.com/FIRST-Tech-Challenge/FtcRobotController/tree/v12.0) · [License](LICENSE)
