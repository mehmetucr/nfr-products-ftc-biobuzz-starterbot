package org.firstinspires.ftc.teamcode;
public class ShotControllerTest {
 static int checks;
 static void check(boolean ok, String name) { checks++; if (!ok) throw new AssertionError(name); }
 public static void main(String[] args) {
  ShotController c = new ShotController();
  c.update(0,2000,false,true,false); check(c.intakeTargetRpm()==RobotSettings.INTAKE_RPM,"RB on");
  c.update(.1,2000,false,true,false); check(c.intakeTargetRpm()==RobotSettings.INTAKE_RPM,"held RB");
  c.update(.2,2000,false,false,false); c.update(.3,2000,false,true,false);
  check(c.intakeTargetRpm()==0,"RB off");
  c.update(.4,2000,false,false,false); c.update(.5,2000,false,true,false);
  c.update(1,2000,true,false,false); check(c.intakeTargetRpm()==0 && c.targetRpm()==RobotSettings.SHOT_RPM && !c.gateOpen(),"A overrides intake");
  c.update(1.1,RobotSettings.SHOT_RPM,true,false,false);
  c.update(1.2,0,true,false,false); check(!c.gateOpen(),"RPM drop during latched wait");
  c.update(1.59,0,true,false,false); check(!c.gateOpen() && c.intakeTargetRpm()==0,"wait full 0.5 seconds");
  c.update(1.61,0,true,false,false); check(c.gateOpen() && c.intakeTargetRpm()==0,"opens despite RPM drop");
  c.update(1.82,0,true,true,false); check(c.intakeTargetRpm()==RobotSettings.SHOT_INTAKE_RPM,"RPM drop and RB ignored");
  c.update(3.60,0,true,false,false); check(c.gateOpen() && c.intakeTargetRpm()==RobotSettings.SHOT_INTAKE_RPM,"feed until deadline");
  c.update(3.62,0,true,false,false); check(!c.gateOpen() && c.intakeTargetRpm()==0 && c.targetRpm()==RobotSettings.IDLE_RPM,"finish");
  c.update(4,2000,true,false,false); check(c.state==ShotController.State.IDLE,"held A no retrigger");
  c.update(4.1,2000,false,false,false); c.update(4.2,0,true,false,false);
  c.update(9.3,0,false,false,false); check(!c.gateOpen() && c.state==ShotController.State.IDLE,"encoder timeout");
  c.update(10,2000,true,false,false); c.update(10.1,3300,false,false,false); c.update(10.61,3300,false,false,false);
  c.update(10.9,3300,false,false,true); check(!c.gateOpen() && c.intakeTargetRpm()==0,"cancel");
  check(Math.abs(RobotSettings.rpmToTicks(3300)-1540)<1e-9,"rpm conversion");
  check(Math.abs(RobotSettings.ticksToRpm(RobotSettings.rpmToTicks(2000))-2000)<1e-9,"rpm roundtrip");
  System.out.println(checks+" checks passed");
 }
}
