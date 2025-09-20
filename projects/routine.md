```yaml
title: routine
```

# routine

Kotlin is easier to read and write, and often results in shorter code.

## Basic drivetrain movement

```kotlin
routine {
    drivetrain.lock()
    restart = true
    ready()
    forever {
        drivetrain.drive(
            driver[VectorInput.STICK_LEFT].yx.rotated(-pinpoint.pose.h).halfLinearHalfCubic(),
            -driver[AnalogInput.STICK_X_RIGHT].symmetricSqrt().radians
        )
    }
}.run()

/* ... */

class Drivetrain(map: HardwareMapEx) : Subsystem() {
    val flMotor by map.deferred<DcMotor>("frontLeft") { direction = REVERSE }
    val frMotor by map.deferred<DcMotor>("frontRight")
    val blMotor by map.deferred<DcMotor>("backLeft") { direction = REVERSE }
    val brMotor by map.deferred<DcMotor>("backRight")

    fun drive(power: Pose2) {
        val (x, y, h) = power.xyh
        flMotor.power = x.inches - y.inches - h.radians
        frMotor.power = x.inches + y.inches + h.radians
        blMotor.power = x.inches + y.inches - h.radians
        brMotor.power = x.inches - y.inches + h.radians
    }

    fun drive(translationPower: Vector2, headingPower: Radians) = drive(Pose2(translationPower, headingPower))
}
```
> (30 lines)

Equivalent Java code:

```java
public class DefaultDriveCommand extends CommandBase {
    MecanumDriveSubsystem drive;
    DoubleSupplier x, y, rx, heading;

    public DefaultDriveCommand(
            MecanumDriveSubsystem driveSubsystem,
            DoubleSupplier inputX,
            DoubleSupplier inputY,
            DoubleSupplier inputRx,
            DoubleSupplier robotHeading) {
        this.drive = driveSubsystem;
        this.x = inputX;
        this.y = inputY;
        this.rx = inputRx;
        this.heading = robotHeading;
        addRequirements(drive);
    }

    @Override
    public void execute() {
        drive.driveFieldCentric(
                -x.getAsDouble() + getXModPower(),
                y.getAsDouble() + getYModPower(),
                rx.getAsDouble() + getRModPower(),
                heading.getAsDouble());
    }

    public double getXModPower() {
        return 0.0;
    }

    public double getYModPower() {
        return 0.0;
    }

    public double getRModPower() {
        return 0.0;
    }
}

/* ... */

public class MecanumDriveSubsystem extends SubsystemBase {
    DcMotorEx fr, fl, br, bl;
    Localizer odo;

    public MecanumDriveSubsystem(
            DcMotor fr,
            DcMotor fl,
            DcMotor br,
            DcMotor bl,
            Localizer localizer) {
        this.fr = (DcMotorEx) fr;
        this.fl = (DcMotorEx) fl;
        this.br = (DcMotorEx) br;
        this.bl = (DcMotorEx) bl;
        br.setDirection(DcMotorSimple.Direction.REVERSE);
        fr.setDirection(DcMotorSimple.Direction.REVERSE);
        this.odo = localizer;
    }

    /**
     * @param x positive drives forward
     * @param y positive drives left
     * @param rx positive turns clockwise
     * @param heading in degrees
     */
    public void driveFieldCentric(double x, double y, double rx, double heading) {
        rx = -rx;
        
        double headingRads = -Math.toRadians(heading);
        double rotX = y * Math.cos(headingRads) + x * Math.sin(headingRads);
        double rotY = y * Math.sin(headingRads) - x * Math.cos(headingRads);

        fl.setPower(rotY + rotX + rx);
        bl.setPower(rotY - rotX + rx);
        fr.setPower(rotY - rotX - rx);
        br.setPower(rotY + rotX - rx);
    }
}
```
> (80 lines)

## Binding reset yaw button

```kotlin
driver[ButtonInput.OPTIONS and ButtonInput.SHARE].onceOnTrue { pinpoint.resetYaw() }
```

vs

```java
new Trigger(() -> gamepad1.options && gamepad1.share).whileActiveOnce(new InstantCommand(pinpoint::resetYaw));
```

## Getting hardware map objects

```kotlin
private val motor0 by map.deferred<DcMotor>("slide0") {
    direction = REVERSE
    zeroPowerBehavior = FLOAT
}
```

vs

```java
private final DcMotorEx motor1;

/* ... */

public ExtensionSubsystem(HardwareMap hMap, PivotSubsystem pivotSubsystem, CachingVoltageSensor voltage) {
    /* ... */
    motor0 = (DcMotorEx) hMap.dcMotor.get("slide0");
    motor0.setDirection(DcMotorSimple.Direction.REVERSE);
    motor0.setZeroPowerBehavior(DcMotor.ZeroPowerBehavior.FLOAT);
    /* ... */
}
```

## Basic button binds

```kotlin
driver[ButtonInput.A].onceOnTrue { retract().start() }
```

vs

```java
driver.getGamepadButton(GamepadKeys.Button.A).whenPressed(retract());
```

Less code means fewer places to create bugs, better developer experience, and faster iteration.

Routines are also created on the fly instead of being reused, hugely reducing the number of strange state bugs having to be worked around with excessive use of value suppliers.

## Composing routines

Composing routines is much less verbose compared to FTCLib thanks to free functions:

```kotlin
fun retract() = serial(
    slides.extendTo(SlideConstants.minExtension.inches),
    pivot.rotateTo(PivotConstants.bottomLimit.degrees),
    //   trailing comma makes it easier to duplicate ⤴
    //   lines, reduces noise in diffs, and makes
    //   syntax easier to visually parse
)
```
> (4 lines)

```java
public class RetractCommand extends SequentialCommandGroup {
    public RetractCommand(
            PivotSubsystem pivot,
            ExtensionSubsystem extend
        ) {
        addCommands(
            new ExtendCommand(extend, SlideConstants.minExtension),
            new PivotCommand(pivot, PivotConstants.bottomLimit)
            //                              no trailing comma ⤴
        );
    }
}

/* ... */

public Command retract() {
    return new RetractCommand(pivot, extension);
}
```
> (14 lines)

## Simple subsystems

```kotlin
class Wrist(map: HardwareMapEx) : Subsystem() {
    private val wrist by map.deferred<ServoImplEx>("wrist") { pwmRange = PwmControl.PwmRange(500.0, 2500.0) }

    var position = 0.0
        set(value) {
            field = value + IntakeConstants.wristOffset
            wrist.position = field
        }

    var pwmDisabled = false
        set(disabled) {
            field = disabled
            if (disabled) wrist.setPwmDisable() else wrist.setPwmEnable()
        }

    fun rotateTo(target: Double) = routine(name = "WristTo$target") {
        ready()
        val rotationTime = IntakeConstants.timeMultiplier.seconds * abs(position - target)
        position = target
        wait(rotationTime)
    }
}
```

> (24 lines)

vs

```java
@Config
public class WristSubsystem extends SubsystemBase {
    private ServoImplEx wrist;
    private double rotation = IntakeConstants.groundPos;
    public static double debug = 0;
    private boolean pwmDisabled = false;

    public WristSubsystem(HardwareMap hardwareMap) {
        wrist = (ServoImplEx) hardwareMap.get(Servo.class, "wrist");
        wrist.setPwmRange(new PwmControl.PwmRange(500, 2500));
    }

    public void setWrist(double rotation) {
        wrist.setPosition(rotation + IntakeConstants.wristOffset);
        this.rotation = rotation + IntakeConstants.wristOffset;
    }

    public double getPosition() {
        return wrist.getPosition();
    }

    public boolean isClose(double target) {
        return Util.inRange(target, getPosition(), SlideConstants.tolerance);
    }

    public boolean isPwmDisabled() {
        return pwmDisabled;
    }

    public void setPwmDisabled(boolean disabled) {
        this.pwmDisabled = disabled;
        if (disabled) {
            wrist.setPwmDisable();
        } else {
            wrist.setPwmEnable();
        }
    }
}
```

> (38 lines)

```kotlin
class Intake(map: HardwareMapEx) : Subsystem() {
    private val resetTimer = Timer()

    private val intake by map.deferred<CRServo>("intake")
    private val clawer by map.deferred<Servo>("clawer") { direction = REVERSE }
    private val prox by map.deferred<AnalogInput>("clawer")

    /**
     * Speeds from 1.0 to -1.0. Positive is outtake Negative is intake
     */
    var intakeSpeed = 0.0
    var clawPos = 0.0
        set(value) {
            field = value
            clawer.position = field + IntakeConstants.clawOffset
        }

    val proximity get() = prox.voltage
    val isProximityClose get() = proximity < IntakeConstants.intakeSensorVoltageThres

    override fun tick() {
        // reset intake servos every 25 seconds so they don't suddenly stop every 30 seconds
        if (resetTimer.elapsed > 25.seconds) {
            intake.power = 0.0
            resetTimer.reset()
        } else intake.power = intakeSpeed
    }
}
```

> (32 lines)

Note the much cleaner hardware map syntax compared to Java (combined declaration and assignment, easier inline property modification):

```java
@Config
public class IntakeSubsystem extends SubsystemBase {
    private CRServo intake;
    private Servo clawer;
    private AnalogInput prox, imu;
    private double speed = 0;
    private double clawPos = IntakeConstants.closedPos;

    private ElapsedTime lastResetTime;

    public IntakeSubsystem(HardwareMap hardwareMap) {
        intake = hardwareMap.crservo.get("intake");
        clawer = hardwareMap.servo.get("clawer");
        prox = hardwareMap.analogInput.get("clawProx");
        imu = hardwareMap.analogInput.get("clawImu");
        clawer.setDirection(Servo.Direction.REVERSE);
        lastResetTime = new ElapsedTime();
    }

    /**
     * Speeds from 1.0 to -1.0 Positive is outtake Negative is intake
     *
     * @param speed
     */
    public void setIntakeSpeed(double speed) {
        this.speed = speed;
        intake.setPower(speed);
    }

    public void setClawer(double value) {
        clawPos = value;
        clawer.setPosition(clawPos + IntakeConstants.clawOffset);
    }

    public double getFrontV() {
        return prox.getVoltage();
    }
    public boolean proxClose() {
        return prox.getVoltage() < IntakeConstants.intakeSensorVoltageThres;
    }

    @Override
    public void periodic() {
        // reset intake servos every 30 seconds so they don't suddently stop
        if (lastResetTime.seconds() > 25) {
            intake.setPower(0);
            lastResetTime.reset();
        } else {
            intake.setPower(speed);
        }

        clawer.setPosition(clawPos + IntakeConstants.clawOffset);
    }
}
```

> (54 lines)

## Inverse kinematics commands

```kotlin
private fun getTargetExtension(x: Inches, y: Inches): Inches {
    return max(sqrt(x.inches.pow(2.0) + (y - IVKConstants.pivotPointHeightOffset).inches.pow(2.0)), 0.0).inches
}

private fun getTargetAngleDegrees(x: Inches, y: Inches): Radians {
    return atan2(y - IVKConstants.pivotPointHeightOffset, x)
}

fun Robot.ivk(x: Inches, y: Inches) = parallel(
    slides.extendTo(getTargetExtension(x + IVKConstants.ivkCenterOffset, y)),
    pivot.rotateTo(getTargetAngleDegrees(x + IVKConstants.ivkCenterOffset, y))
)
```

vs

```java
@Config
public class IVKCommand extends ParallelCommandGroup {
    /**
     * Height is from the tile to the claw
     * Distance is from the front of the robot, to a point forwards from the bot
     * <p>
     * Units are in inches
     * <p>
     * Schedule intake position command before this, to prevent samples from getting hit
     */
    public IVKCommand(double x, double y, ExtensionSubsystem extensionSubsystem, PivotSubsystem pivotSubsystem) {
        addCommands(
                extensionSubsystem.getExtendCommand(getTargetExtension(x, y)),
                pivotSubsystem.getPivotCommand(getTargetAngleDegrees(x, y))
        );
    }
    
    private double getTargetExtension(double x, double y) {
        return Math.max(Math.sqrt(Math.pow(x, 2) + Math.pow(y - IVKConstants.pivotPointHeightOffset, 2)), 0);
    }

    private double getTargetAngleDegrees(double x, double y) {
        return Math.toDegrees(Math.atan2(y - IVKConstants.pivotPointHeightOffset, x));
    }
}
```

Thanks to type-safe value class wrappers, the Kotlin code is significantly safer because there's no chance of mixing up radians and degrees, or inches and millimeters.
The domain-specific types render the comments specifying units useless because the code itself documents what units it expects.

## English-like code

Routines often sound like conversational English:

```kotlin
fun retract() = serial(
    wrist.rotateTo(IntakeConstants.foldedPos),
    slides.extendTo(SlideConstants.minExtension.inches),
    pivot.rotateTo(PivotConstants.bottomLimit.degrees),
    changeTo(State.READY),
)
```

The more something sounds like English, the easier it is to understand to us English-speaking citizens.
This does not mean, however, that we program in English, as it is far too imprecise for accurate fulfillment of requirements.
