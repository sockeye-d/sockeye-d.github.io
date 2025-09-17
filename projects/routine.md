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
```
> (11 lines)

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
```
> (39 lines)

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
