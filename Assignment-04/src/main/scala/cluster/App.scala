package cluster

import AlarmSystemRoles.Roles
import AlarmSystemRoles.Roles.*
import AlarmSystemZones.Zones
import cluster.AlarmSystemRoles.SensorType.{Motion, Window}
import cluster.actors.*
import com.typesafe.config.ConfigFactory
import org.apache.pekko.actor.typed.*
import org.apache.pekko.actor.typed.scaladsl.*

import scala.concurrent.duration.DurationInt
import scala.io.StdIn.readLine


object App:
  enum Command:
    // command for tests
    case InteractiveKeypad(pin: String, zones: List[Zones])
  export Command.*

  def apply(role: Roles): Behavior[Command] = Behaviors.setup: context =>
    role match
      case Guardian =>
        val pin: String = "1234"
        val entryDelay = 5.seconds
        val exitDelay = 5.seconds
        context.log.info(s"Starting Guardian: pin=$pin, entryTimeout=$entryDelay, exitTimeout=$exitDelay.")
        context.spawn(AlarmSystemGuardian(pin, entryDelay, exitDelay), "guardian")
        Behaviors.empty
      case Keypad =>
        context.log.info(s"Starting Keypad")
        val keypad = context.spawn(KeypadActor(), "keypad")

        Behaviors.receiveMessage:
          case InteractiveKeypad(pin, zones) =>
            context.log.info("received")
            keypad ! KeypadActor.UserFixedInput(pin, zones)
            Behaviors.same
      case Siren =>
        context.log.info(s"Starting Siren")
        context.spawn(SirenActor(), "siren")
        Behaviors.empty
      case Sensor(kind) =>
        val id = sys.props.getOrElse("SENSOR_ID", "unknown-sensor")
        val zoneString = sys.props.getOrElse("SENSOR_ZONE", Zones.values.head.toString)
        val zone = Zones.valueOf(zoneString)
        var sensor: ActorRef[GenericSensor.Command] = null
        kind match
          case Motion =>
            context.log.info("Starting Motion Sensor")
            sensor = context.spawn(MotionSensor(id, zone), "motion-sensor")
          case Window =>
            context.log.info("Starting Window Sensor")
            sensor = context.spawn(WindowSensor(id, zone), "window-sensor")
        Behaviors.empty

  def main(args: Array[String]): Unit =
    val role = sys.props.getOrElse("NODE_ROLE", "guardian")
    val port = sys.props.getOrElse("PORT", "2551")

    val config = ConfigFactory.parseString(
      s"""
          pekko.remote.artery.canonical.port = $port
          pekko.cluster.roles = [$role]
          """).withFallback(ConfigFactory.load())

    val mappedRole = Roles.fromString(role)
    val system = ActorSystem[Command](App(mappedRole), "ClusterSystem", config)
    if mappedRole == Keypad then
      Thread.sleep(2.seconds.toMillis)
      var flag: Boolean = true
      while(flag)
        val pin = readLine("Enter pin (enter to exit): ")
        if pin != null && pin.trim.nonEmpty then
          val zones: List[Zones] = AlarmSystemZones.requestZones()
          system ! InteractiveKeypad(pin.trim, zones)
        else
          flag = false
          system.terminate()