package org.firstinspires.ftc.teamcode;

/** Donanimdan bagimsiz, bloklamayan sut sirasi. Tum zamanlar saniye. */
public final class ShotController {
    public enum State { IDLE, SPINUP, WAITING, GATE_OPENING, FEEDING }
    public State state = State.IDLE;
    public boolean intakeOn = false;
    public String message = "Hazir";
    private boolean previousA, previousRb;
    private double startedAt, openedAt, reachedAt = Double.NaN;

    public void update(double now, double rpm, boolean a, boolean rb, boolean cancel) {
        boolean aPressed = a && !previousA;
        boolean rbPressed = rb && !previousRb;
        previousA = a;
        previousRb = rb;
        if (cancel) { reset("Iptal edildi"); return; }
        if (state == State.IDLE) {
            if (aPressed) {
                intakeOn = false;
                state = State.SPINUP;
                startedAt = now;
                reachedAt = Double.NaN;
                message = "3300 RPM bekleniyor";
            } else if (rbPressed) {
                intakeOn = !intakeOn;
            }
        }
        if (state == State.SPINUP) {
            if (now - startedAt >= RobotSettings.SPINUP_TIMEOUT_SECONDS) {
                reset("RPM zaman asimi: encoder, yon ve PIDF kontrol edin");
                return;
            }
            if (Double.isFinite(rpm) && Math.abs(rpm - RobotSettings.SHOT_RPM) <= RobotSettings.RPM_TOLERANCE) {
                reachedAt = now;
                state = State.WAITING;
                message = "Hedefe ulasildi: 0.5 saniye bekleniyor";
            }
        }
        // Hedefe ilk ulasildiktan sonra RPM dusse de sayac sifirlanmaz.
        if (state == State.WAITING && now - reachedAt >= RobotSettings.RPM_REACHED_WAIT_SECONDS) {
            state = State.GATE_OPENING;
            openedAt = now;
            message = "Kapak aciliyor";
        }
        if (state == State.GATE_OPENING || state == State.FEEDING) {
            if (now - openedAt >= RobotSettings.GATE_OPEN_SECONDS) {
                reset("Sut tamamlandi");
            } else if (now - openedAt >= RobotSettings.GATE_SETTLE_SECONDS) {
                state = State.FEEDING;
                message = "Sut: RPM dusse de intake devam eder";
            }
        }
    }
    private void reset(String text) {
        state = State.IDLE;
        intakeOn = false;
        reachedAt = Double.NaN;
        message = text;
    }
    public double targetRpm() { return state == State.IDLE ? RobotSettings.IDLE_RPM : RobotSettings.SHOT_RPM; }
    public double intakeTargetRpm() {
        if (state == State.FEEDING) return RobotSettings.SHOT_INTAKE_RPM;
        return state == State.IDLE && intakeOn ? RobotSettings.INTAKE_RPM : 0;
    }
    public boolean gateOpen() { return state == State.GATE_OPENING || state == State.FEEDING; }
}
