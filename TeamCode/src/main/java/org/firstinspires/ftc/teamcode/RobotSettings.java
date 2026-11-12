package org.firstinspires.ftc.teamcode;

/** Robot ayarlari: degistirdikten sonra Android Studio Run ile yeniden yukleyin. */
public final class RobotSettings {
    private RobotSettings() { }
    // Intake tekerinin RPM'i. Atis beslemesi ayri ayarlanabilir.
    public static final double INTAKE_RPM = 500;
    public static final double SHOT_INTAKE_RPM = 500;
    public static final double INTAKE_ENCODER_TICKS_PER_MOTOR_REV = 28;
    // REV-41-1603: 68:13. Teker gearbox cikisina dogrudan bagli kabul edilir.
    public static final double INTAKE_MOTOR_REVS_PER_WHEEL_REV = 68.0 / 13.0;

    // Intake PIDF baslangic tahminleri; teker RPM'i ve volt cinsinden.
    public static final double INTAKE_KS = 0.30;   // V
    public static final double INTAKE_KV = 0.0105; // V / RPM
    public static final double INTAKE_KP = 0.010;  // V / RPM
    public static final double INTAKE_KI = 0.012;  // V / (RPM * saniye)
    public static final double INTAKE_KD = 0.0;    // V * saniye / RPM
    public static final double IDLE_RPM = 2000;
    public static final double SHOT_RPM = 3175;
    // Reduksiyonsuz REV HD Hex: 28 tick/motor turu. Motor turu / teker turu.
    public static final double ENCODER_TICKS_PER_MOTOR_REV = 28;
    public static final double MOTOR_REVS_PER_WHEEL_REV = 1.0;

    // Fotografla verilen katsayilar. Kaynak kontrolcu bulunamadi:
    // VOLT / RPM birimleri varsayiliyor; orijinal tuner ile dogrulanmali.
    // Hesaplanan voltaj, olculen batarya voltajina bolunerek guce cevrilir.
    public static final double kS = 2.5267;   // V
    public static final double kV = 0.002407; // V / RPM
    public static final double kP = 0.0275; // V / RPM
    public static final double kI = 0.0;      // V / (RPM * saniye)
    public static final double kD = 0.0;      // V * saniye / RPM

    public static final double RPM_TOLERANCE = 100;
    public static final double RPM_REACHED_WAIT_SECONDS = 0.5;
    public static final double SPINUP_TIMEOUT_SECONDS = 5.0;
    // Sure, servo ac komutu verildiginde baslar. Fiziksel konum sensoru yoktur.
    public static final double GATE_OPEN_SECONDS = 2.0;
    public static final double GATE_SETTLE_SECONDS = 0.20;
    // Servo modeli bilinmiyor: 180 derece varsayimi; fiziksel olarak kalibre edin.
    public static final double SERVO_TOTAL_TRAVEL_DEG = 180;
    public static final double GATE_CLOSED_POSITION = 100.0 / SERVO_TOTAL_TRAVEL_DEG;
    public static final double GATE_OPEN_POSITION = 0.0 / SERVO_TOTAL_TRAVEL_DEG;

    // Montaja gore degistirin; eski robotun yonleri bu robota aktarilmadi.
    public static final boolean LEFT_REVERSED = true;
    public static final boolean RIGHT_REVERSED = false;
    // Eski negatif intake gucunun fiziksel yonunu korur.
    public static final boolean INTAKE_REVERSED = true;
    public static final boolean SHOOTER_REVERSED = false;
    public static final boolean GATE_REVERSED = false;
    public static double intakeTicksToWheelRpm(double ticksPerSecond) {
        return ticksPerSecond * 60.0 / (INTAKE_ENCODER_TICKS_PER_MOTOR_REV * INTAKE_MOTOR_REVS_PER_WHEEL_REV);
    }

    public static double rpmToTicks(double rpm) {
        return rpm * ENCODER_TICKS_PER_MOTOR_REV * MOTOR_REVS_PER_WHEEL_REV / 60.0;
    }
    public static double ticksToRpm(double ticks) {
        return ticks * 60.0 / (ENCODER_TICKS_PER_MOTOR_REV * MOTOR_REVS_PER_WHEEL_REV);
    }
}
