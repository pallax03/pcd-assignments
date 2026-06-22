package smarthome
package actors

import org.apache.pekko.actor.testkit.typed.scaladsl.ScalaTestWithActorTestKit
import org.scalatest.wordspec.AnyWordSpecLike

import scala.concurrent.duration.*
import AlarmSystemZones.*

import org.scalatest.BeforeAndAfterEach

class AlarmSystemGuardianTest extends ScalaTestWithActorTestKit with AnyWordSpecLike with BeforeAndAfterEach:

  override def afterEach(): Unit =
    Thread.sleep(5.seconds.toMillis)

  "An AlarmSystemGuardian" should:
    val exitDelay = 50.milliseconds
    val entryDelay = 100.milliseconds
    val secretPin = "1234"
    val guardian = testKit.spawn(AlarmSystemGuardian(secretPin, entryDelay, exitDelay))
    val probe = testKit.createTestProbe[AlarmSystemGuardian.TriggerDevices]()
    guardian ! AlarmSystemGuardian.Command.GetTriggerDevices(probe.ref)
    val devices = probe.receiveMessage()

    "Disarmed: Correct Pin -> Armed" in:
      devices.keypad ! KeypadActor.Command.UserFixedInput(secretPin, List.empty)

    "Disarmed: Wrong Pin" in:
      devices.keypad ! KeypadActor.Command.UserFixedInput("9999", List.empty)

    val validSensorId = 0
    val activeZone = LivingRoom
    "Disarmed -> Armed: Trigger ignored (under exitDelay)" in :
      devices.keypad ! KeypadActor.Command.UserFixedInput(secretPin, List.empty)
      devices.sensors ! SensorsManager.Command.TriggerSensor(activeZone, validSensorId)

    val notActiveZone = SleepingRoom
    "Disarmed -> Armed: Trigger ignored (another zone)" in :
      devices.keypad ! KeypadActor.Command.UserFixedInput(secretPin, List(activeZone))
      Thread.sleep(exitDelay.toMillis * 2)
      devices.sensors ! SensorsManager.Command.TriggerSensor(notActiveZone, validSensorId)

    "Disarmed -> Armed: Trigger -> Alarmed: Siren" in:
      devices.keypad ! KeypadActor.Command.UserFixedInput(secretPin, List.empty)
      Thread.sleep(entryDelay.toMillis*2)
      devices.sensors ! SensorsManager.Command.TriggerSensor(activeZone, validSensorId)

    "Disarmed -> Armed -> Trigger: Correct Pin (under entryDelay) -> Disarm" in:
      devices.keypad ! KeypadActor.Command.UserFixedInput(secretPin, List.empty)
      devices.sensors ! SensorsManager.Command.TriggerSensor(notActiveZone, validSensorId)
      devices.keypad ! KeypadActor.Command.UserFixedInput(secretPin, List.empty)

    "Disarmed: BreakSensor" in:
      devices.sensors ! SensorsManager.BreakSensor(activeZone, validSensorId)

    "Armed: BreakSensor -> Alarmed: Siren" in:
      devices.keypad ! KeypadActor.UserFixedInput(secretPin, List(activeZone))
      Thread.sleep(exitDelay.toMillis*2)
      devices.sensors ! SensorsManager.BreakSensor(activeZone, validSensorId)