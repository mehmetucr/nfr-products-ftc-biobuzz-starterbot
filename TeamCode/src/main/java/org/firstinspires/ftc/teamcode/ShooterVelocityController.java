package org.firstinspires.ftc.teamcode;

/** Volt/RPM tabanli PID + statik/hiz feedforward. Cikis: 0..1 motor gucu. */
public final class ShooterVelocityController {
    private double kS, kV, kP, kI, kD;
    private double integral, previousRpm, previousTime, previousTarget;
    private boolean initialized;
    private double requestedVoltage;
    public ShooterVelocityController(double kS, double kV, double kP, double kI, double kD) {
        setGains(kS, kV, kP, kI, kD);
    }
    public void setGains(double kS, double kV, double kP, double kI, double kD) {
        for (double gain : new double[]{kS, kV, kP, kI, kD}) {
            if (!Double.isFinite(gain) || gain < 0) throw new IllegalArgumentException("Katsayilar sonlu ve >= 0 olmali");
        }
        reset();
        this.kS=kS; this.kV=kV; this.kP=kP; this.kI=kI; this.kD=kD;
    }
    public void reset() {
        integral = 0;
        initialized = false;
        requestedVoltage = 0;
    }

    public double requestedVoltage() { return requestedVoltage; }
    public double update(double targetRpm, double measuredRpm, double nowSeconds, double batteryVoltage) {
        if (!Double.isFinite(targetRpm) || !Double.isFinite(measuredRpm)
                || !Double.isFinite(nowSeconds) || !Double.isFinite(batteryVoltage)
                || batteryVoltage <= 0 || targetRpm <= 0) {
            reset(); return 0;
        }
        double dt = nowSeconds - previousTime;
        boolean validStep = initialized && dt >= 0.0001 && dt <= 0.25;
        if (!validStep || targetRpm != previousTarget) integral=0;
        double error = targetRpm - measuredRpm;
        // Olcum turevi: hedef 2000->3300 degisiminde derivative kick olusmaz.
        double derivative = validStep ? -(measuredRpm - previousRpm) / dt : 0;
        double base = kS + kV * targetRpm + kP * error + kD * derivative;
        double candidate = kI > 0 && validStep ? integral + error * dt : integral;
        double candidateVolts = base + kI * candidate;
        // Doygunluk yonunde integral birikmesini engelle; ters hata ile cozulmesine izin ver.
        if ((candidateVolts >= 0 && candidateVolts <= batteryVoltage)
                || (candidateVolts > batteryVoltage && error < 0) || (candidateVolts < 0 && error > 0)) {
            integral = candidate;
        }
        previousRpm=measuredRpm; previousTime=nowSeconds; previousTarget=targetRpm; initialized=true;
        requestedVoltage = Math.max(0, base + kI * integral);
        // PIDF katsayilari volt tabanli. Her dongude olculen bataryaya gore PWM ayarlanir.
        return Math.min(1, requestedVoltage / batteryVoltage);
    }
}
