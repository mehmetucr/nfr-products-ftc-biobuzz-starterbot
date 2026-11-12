package org.firstinspires.ftc.teamcode;
public class ShooterVelocityControllerTest {
 static int n;
 static void eq(double a,double b) {n++; if(Math.abs(a-b)>1e-8) throw new AssertionError(a+" != "+b);}
 public static void main(String[] args) {
  ShooterVelocityController c=new ShooterVelocityController(.05,.0001,.0002,0,0);
  eq(c.update(2000,1500,0,1),.35);
  eq(c.update(3300,0,.1,1),1);
  eq(c.update(2000,6000,.2,1),0);
  eq(c.update(0,2000,.3,1),0);
  eq(c.update(2000,Double.NaN,.4,1),0);
  c=new ShooterVelocityController(0,0,0,0,.001);
  eq(c.update(2000,1000,0,1),0);
  eq(c.update(3300,1000,.1,1),0); // no setpoint kick
  eq(c.update(3300,990,.2,1),.1);
  eq(c.update(3300,980,2,1),0); // stale dt resets
  c=new ShooterVelocityController(0,.0002,0,.001,0);
  eq(c.update(2000,0,0,1),.4);
  for(int i=1;i<=20;i++) c.update(2000,0,i*.1,1);
  eq(c.update(2000,2000,2.1,1),1); // integral capped at .6
  eq(c.update(3300,3300,2.2,1),.66); // target resets integral
  c.reset(); eq(c.update(2000,2000,3,1),.4);
  c=new ShooterVelocityController(RobotSettings.kS,RobotSettings.kV,RobotSettings.kP,RobotSettings.kI,RobotSettings.kD);
  eq(c.update(3300,3300,0,12),10.4698/12);
  eq(c.update(2000,2000,.1,12),7.3407/12);
  eq(c.update(3300,3300,.2,13),10.4698/13);
  eq(c.update(3300,0,.3,12),1);
  eq(c.update(2000,4000,.4,12),0);
  eq(c.update(3300,3300,.5,Double.NaN),0);
  eq(c.update(3300,3300,.6,0),0);
  // Same speed error, different battery voltage: keep the requested voltage constant.
  c=new ShooterVelocityController(1,.004,.01,0,0);
  eq(c.update(1500,1300,0,12),.75);
  eq(c.requestedVoltage(),9);
  eq(c.update(1500,1300,.02,10),.9);
  eq(c.requestedVoltage(),9);
  eq(c.update(1500,1300,.04,8),1);
  eq(c.requestedVoltage(),9);
  eq(c.update(1500,1300,.06,Double.NaN),0);
  eq(c.requestedVoltage(),0);
  System.out.println(n+" velocity controller checks passed");
 }
}
