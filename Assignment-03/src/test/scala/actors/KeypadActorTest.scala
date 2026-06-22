package smarthome
package actors

import org.apache.pekko.actor.testkit.typed.scaladsl.ScalaTestWithActorTestKit
import org.scalatest.wordspec.AnyWordSpecLike

import scala.concurrent.duration.*
import AlarmSystemZones.*

import org.scalatest.BeforeAndAfterEach

class KeypadActorTest extends ScalaTestWithActorTestKit with AnyWordSpecLike with BeforeAndAfterEach:

  override def afterEach(): Unit =
    Thread.sleep(5.seconds.toMillis)

  "A KeypadActor" should:
    val randomPin = "1224"
    val noZones = List.empty
    val zones = List(LivingRoom, Kitchen)
    val guardianProbe = testKit.createTestProbe[AlarmSystemGuardian.Command]()
    val keypad = spawn(KeypadActor(guardianProbe.ref))

    "Insert a pin with no Zones" in:
      keypad ! KeypadActor.Command.UserFixedInput(randomPin, noZones)

    "Insert a pin with Zones" in:
      keypad ! KeypadActor.Command.UserFixedInput(randomPin, zones)