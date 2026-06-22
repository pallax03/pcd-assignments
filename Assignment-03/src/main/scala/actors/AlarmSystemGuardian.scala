package smarthome
package actors

import org.apache.pekko.actor.typed.scaladsl.*
import org.apache.pekko.actor.typed.*
import AlarmSystemZones.*

import scala.concurrent.duration.*

object AlarmSystemGuardian:

  enum Command:
    // ask for outside tests
    case GetTriggerDevices(replyTo: ActorRef[TriggerDevices])
    // keypad
    case VerifyPin(pin: String, zones: List[Zones])
    // sensors
    case Detect(sensorName: String)
    case SensorOffline(sensorName: String)
    // private
    case ExitTimeExpired(zones: List[Zones])
    case EntryTimeExpired

  export Command.*

  case class TriggerDevices(keypad: ActorRef[KeypadActor.Command], sensors: ActorRef[SensorsManager.Command])

  def apply(secret: String, entryDelayTimeout: FiniteDuration, exitDelayTimeout: FiniteDuration): Behavior[Command] =
    Behaviors.setup: context =>
      val supervisionStrategy = SupervisorStrategy
        .restart
        .withLimit(maxNrOfRetries = 3, withinTimeRange = 5.seconds)

      val keypad = context.spawn(
          Behaviors.supervise(KeypadActor(context.self))
            .onFailure[Exception](supervisionStrategy)
        , "keypad-actor")
      val sensors = context.spawn(SensorsManager(context.self), "sensors-actor")
      val siren = context.spawn(
        Behaviors.supervise(SirenActor(context.self))
          .onFailure[Exception](supervisionStrategy)
        , "siren-actor")

      Behaviors.withTimers: timers =>
        def handleCommon(msg: Command): Behavior[Command] = msg match
          case GetTriggerDevices(replyTo) =>
            replyTo ! TriggerDevices(keypad, sensors)
            Behaviors.same
          case Detect(sensor) =>
            context.log.warn(s"$sensor trigger ignored")
            Behaviors.same
          case SensorOffline(sensor) =>
            context.log.warn(s"$sensor offline")
            Behaviors.same
          case _ => Behaviors.unhandled

        def disarmed(): Behavior[Command] = Behaviors.receiveMessage:
          case VerifyPin(pin, zone) if pin == secret =>
            context.log.info("System arming, waiting exitDelay: "+ exitDelayTimeout.toString())
            timers.startSingleTimer(ExitTimeExpired(zone), exitDelayTimeout)
            exitDelay()
          case VerifyPin(_, _) =>
            context.log.warn("Incorrect Pin")
            Behaviors.same
          case msg => handleCommon(msg)

        def armed(): Behavior[Command] = Behaviors.receiveMessage:
          case Detect(sensor) =>
            context.log.info(s"System alarming ($sensor), waiting entryDelay: "+ entryDelayTimeout.toString())
            timers.startSingleTimer(EntryTimeExpired, entryDelayTimeout)
            entryDelay()
          case SensorOffline(sensor) =>
            context.log.info(s"$sensor offline, trigger")
            context.self ! Detect(sensor)
            Behaviors.same
          case VerifyPin(pin, _) if pin == secret =>
            sensors ! SensorsManager.DisarmAll
            context.log.info(s"System disarmed")
            disarmed()
          case VerifyPin(_, _) =>
            context.log.warn("Incorrect Pin")
            Behaviors.same
          case msg => handleCommon(msg)

        def exitDelay(): Behavior[Command] = Behaviors.receiveMessage:
          case ExitTimeExpired(zones) =>
            if zones.isEmpty then
              sensors ! SensorsManager.ArmAll
              context.log.info("System armed all Zones")
            else
              zones.foreach(sensors ! SensorsManager.ArmZone(_))
              context.log.info(s"System armed on zones: $zones")
            armed()
          case msg => handleCommon(msg)

        def entryDelay(): Behavior[Command] = Behaviors.receiveMessage:
          case VerifyPin(pin, _) if pin == secret =>
            sensors ! SensorsManager.DisarmAll
            context.log.info("System disarmed")
            disarmed()
          case VerifyPin(_, _) =>
            context.log.warn("Incorrect Pin")
            Behaviors.same
          case EntryTimeExpired =>
            context.log.info("System alarmed")
            siren ! SirenActor.SetState(true)
            alarm()
          case msg => handleCommon(msg)

        def alarm(): Behavior[Command] = Behaviors.receiveMessage:
            case VerifyPin(pin, _) if pin == secret =>
              context.log.info("System disarmed")
              siren ! SirenActor.SetState(false)
              disarmed()
            case VerifyPin(_, _) =>
              context.log.warn("Incorrect Pin")
              Behaviors.same
            case msg => handleCommon(msg)

        disarmed()