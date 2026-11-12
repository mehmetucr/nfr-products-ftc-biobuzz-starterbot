package org.firstinspires.ftc.teamcode;

import com.qualcomm.robotcore.eventloop.opmode.LinearOpMode;
import com.qualcomm.robotcore.eventloop.opmode.TeleOp;
import com.qualcomm.robotcore.hardware.DcMotor;
import com.qualcomm.robotcore.hardware.DcMotorEx;
import com.qualcomm.robotcore.hardware.DcMotorSimple;
import com.qualcomm.robotcore.hardware.Servo;
import com.qualcomm.robotcore.hardware.VoltageSensor;
import com.qualcomm.robotcore.util.ElapsedTime;

@TeleOp(name = "NFR Products FTC-Biobuzz StarterBot", group = "NFR")
public class TankShooterTeleOp extends LinearOpMode {
    private static DcMotorSimple.Direction direction(boolean reversed) {
        return reversed ? DcMotorSimple.Direction.REVERSE : DcMotorSimple.Direction.FORWARD;
    }
    private static double deadband(double value) { return Math.abs(value) < 0.05 ? 0 : value; }
    private double batteryVoltage() {
        double voltage = Double.POSITIVE_INFINITY;
        for (VoltageSensor sensor : hardwareMap.voltageSensor) {
            double reading = sensor.getVoltage();
            if (Double.isFinite(reading) && reading > 0) voltage = Math.min(voltage, reading);
        }
        return Double.isFinite(voltage) ? voltage : Double.NaN;
    }
    @Override public void runOpMode() throws InterruptedException {
        DcMotor left = hardwareMap.get(DcMotor.class, "sol_drive");
        DcMotor right = hardwareMap.get(DcMotor.class, "sag_drive");
        DcMotorEx intake = hardwareMap.get(DcMotorEx.class, "intake");
        DcMotorEx shooter = hardwareMap.get(DcMotorEx.class, "shooter");
        Servo gate = hardwareMap.get(Servo.class, "kapak");
        // Intake ve shooter encoderlari yazilimdaki hiz kontrolune veri saglar.
        // Robot Configuration: motor 0=sag_drive, 1=sol_drive, 2=intake, 3=shooter.
        left.setDirection(direction(RobotSettings.LEFT_REVERSED));
        right.setDirection(direction(RobotSettings.RIGHT_REVERSED));
        intake.setDirection(direction(RobotSettings.INTAKE_REVERSED));
        shooter.setDirection(direction(RobotSettings.SHOOTER_REVERSED));
        gate.setDirection(RobotSettings.GATE_REVERSED ? Servo.Direction.REVERSE : Servo.Direction.FORWARD);
        if (RobotSettings.GATE_OPEN_POSITION < 0 || RobotSettings.GATE_OPEN_POSITION > 1
                || RobotSettings.GATE_CLOSED_POSITION < 0 || RobotSettings.GATE_CLOSED_POSITION > 1) {
            throw new IllegalArgumentException("Servo konumlari 0..1 araliginda olmali");
        }
        for (DcMotor motor : new DcMotor[]{left, right, intake}) {
            motor.setPower(0);
            motor.setMode(DcMotor.RunMode.RUN_WITHOUT_ENCODER);
            motor.setZeroPowerBehavior(DcMotor.ZeroPowerBehavior.BRAKE);
        }
        shooter.setPower(0);
        shooter.setZeroPowerBehavior(DcMotor.ZeroPowerBehavior.FLOAT);
        // Encoder okunur; Hub hiz PID'si kapali, kontrolu biz setPower ile yapiyoruz.
        shooter.setMode(DcMotor.RunMode.RUN_WITHOUT_ENCODER);
        ShooterVelocityController velocity = new ShooterVelocityController(
                RobotSettings.kS, RobotSettings.kV, RobotSettings.kP, RobotSettings.kI, RobotSettings.kD);
        ShooterVelocityController intakeVelocity = new ShooterVelocityController(
                RobotSettings.INTAKE_KS, RobotSettings.INTAKE_KV, RobotSettings.INTAKE_KP,
                RobotSettings.INTAKE_KI, RobotSettings.INTAKE_KD);
        gate.setPosition(RobotSettings.GATE_CLOSED_POSITION);
        ShotController shot = new ShotController();
        ElapsedTime clock = new ElapsedTime();
        telemetry.addData("Sag drive baglanti (motor 0 olmali)", right.getConnectionInfo());
        telemetry.addData("Sol drive baglanti (motor 1 olmali)", left.getConnectionInfo());
        telemetry.addData("Intake baglanti (motor 2 olmali)", intake.getConnectionInfo());
        telemetry.addData("Shooter baglanti (motor 3 olmali)", shooter.getConnectionInfo());
        telemetry.addLine("RB: intake ac/kapa | A: sut | X: sutu iptal et");
        telemetry.addLine("START sonrasi shooter surekli 2000 RPM hedefler.");
        telemetry.addLine("Servo 180 derece varsayimi: RobotSettings dosyasinda kalibre edin.");
        telemetry.update();
        try {
            while (opModeInInit()) {
                left.setPower(0);
                right.setPower(0);
                intake.setPower(0);
                shooter.setPower(0);
                idle();
            }
            waitForStart();
            clock.reset();
            boolean driveEnabled = false;
            double neutralSince = Double.NaN;
            while (opModeIsActive()) {
                double forward = deadband(-gamepad1.left_stick_y);
                double turn = deadband(gamepad1.right_stick_x);
                double driveNow = clock.seconds();
                if (!driveEnabled) {
                    if (forward == 0 && turn == 0) {
                        if (Double.isNaN(neutralSince)) neutralSince = driveNow;
                        if (driveNow - neutralSince >= 0.25) driveEnabled = true;
                    } else neutralSince = Double.NaN;
                }
                double denominator = Math.max(1, Math.abs(forward) + Math.abs(turn));
                double leftPower = driveEnabled ? (forward + turn) / denominator : 0;
                double rightPower = driveEnabled ? (forward - turn) / denominator : 0;
                left.setPower(leftPower);
                right.setPower(rightPower);
                double rpm = RobotSettings.ticksToRpm(shooter.getVelocity());
                double now = clock.seconds();
                shot.update(now, rpm, gamepad1.a, gamepad1.right_bumper, gamepad1.x);
                double batteryVolts = batteryVoltage();
                double shooterPower = velocity.update(shot.targetRpm(), rpm, now, batteryVolts);
                shooter.setPower(shooterPower);
                gate.setPosition(shot.gateOpen() ? RobotSettings.GATE_OPEN_POSITION : RobotSettings.GATE_CLOSED_POSITION);
                double intakeTargetRpm = shot.intakeTargetRpm();
                double intakeRpm = RobotSettings.intakeTicksToWheelRpm(intake.getVelocity());
                double intakePower = intakeVelocity.update(intakeTargetRpm, intakeRpm, now, batteryVolts);
                intake.setPower(intakePower);
                telemetry.addData("Surus", driveEnabled ? "Aktif" : "Joystickleri merkeze birak");
                telemetry.addData("Stick Y / X", "%.3f / %.3f", gamepad1.left_stick_y, gamepad1.right_stick_x);
                telemetry.addData("Sol / sag guc", "%.3f / %.3f", leftPower, rightPower);
                telemetry.addData("Sag motor portu", right.getConnectionInfo());
                telemetry.addData("Shooter motor portu", shooter.getConnectionInfo());
                telemetry.addData("Durum", shot.state);
                telemetry.addData("Mesaj", shot.message);
                telemetry.addData("Shooter RPM", "%.0f / %.0f", rpm, shot.targetRpm());
                telemetry.addData("Intake teker RPM (olculen / hedef)", "%.0f / %.0f", intakeRpm, intakeTargetRpm);
                telemetry.addData("Intake guc", "%.3f", intakePower);
                telemetry.addData("Kapak", shot.gateOpen() ? "ACIK" : "KAPALI");
                telemetry.addData("Batarya V", batteryVolts);
                if (!Double.isFinite(batteryVolts)) telemetry.addLine("Voltaj okunamadi: shooter ve intake durduruldu");
                telemetry.addData("Shooter guc", "%.3f", shooterPower);
                telemetry.addData("Shooter V (istenen / tahmini)", "%.2f / %.2f",
                        velocity.requestedVoltage(), Double.isFinite(batteryVolts) ? shooterPower * batteryVolts : 0);
                if (Double.isFinite(batteryVolts) && velocity.requestedVoltage() > batteryVolts) {
                    telemetry.addLine("Shooter tam guc: istenen voltaj bataryadan yuksek");
                }
                telemetry.update();
                idle();
            }
        } finally {
            left.setPower(0);
            right.setPower(0);
            intake.setPower(0);
            velocity.reset();
            shooter.setPower(0);
            gate.setPosition(RobotSettings.GATE_CLOSED_POSITION);
        }
    }
}
