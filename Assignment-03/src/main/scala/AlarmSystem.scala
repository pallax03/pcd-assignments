package smarthome

import actors.*
import AlarmSystemZones.*
import actors.AlarmSystemGuardian.*

import org.apache.pekko.actor.typed.*
import org.apache.pekko.actor.typed.scaladsl.*
import org.apache.pekko.actor.typed.scaladsl.AskPattern.Askable
import org.apache.pekko.util.Timeout

import scala.concurrent.Await
import scala.concurrent.Future
import scala.concurrent.duration.*
import scala.io.StdIn.readLine

@main
def trySystem(): Unit =
  val pin: String = "1234"
  val entryDelay = 5.seconds
  val exitDelay = 5.seconds
  val system = ActorSystem(AlarmSystemGuardian(pin, entryDelay, exitDelay), "AlarmSystem")

  given Timeout = 5.seconds
  given systemScheduler: org.apache.pekko.actor.typed.Scheduler = system.scheduler

  val futureDevices: Future[TriggerDevices] = system ? (GetTriggerDevices(_))
  val devices = Await.result(futureDevices, 5.seconds)

  // Test
  var flag: Boolean = true
  while(flag) {
    println("Press 1. to interactive keypad")
    println("Press 2. to trigger sensor")
    println("Press 3. to break a sensor")
    println("Press any: to exit")
    readLine() match
      case "1" => 
        val pin = readLine("Enter pin: ")
        val zones: List[Zones] = AlarmSystemZones.requestZones()
        devices.keypad ! KeypadActor.UserFixedInput(pin, zones)
      case "2" => 
        val zones: List[Zones] = AlarmSystemZones.requestZones()
        if zones.isDefinedAt(0) then
          val sensorId: String = readLine("Enter sensor id (0:Motion, 1:Window): ")
          devices.sensors ! SensorsManager.TriggerSensor(zones.head, sensorId.toInt)
        else
          println("zone not selected")
      case "3" => 
        val zones: List[Zones] = AlarmSystemZones.requestZones()
        if zones.isDefinedAt(0) then
          val sensorId: String = readLine("Enter sensor id (0:Motion, 1:Window): ")
          devices.sensors ! SensorsManager.BreakSensor(zones.head, sensorId.toInt)
        else
          println("zone not selected")
      case _ => flag = false
  }
  system.terminate()
