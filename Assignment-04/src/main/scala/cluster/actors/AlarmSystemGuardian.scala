package cluster
package actors

import org.apache.pekko.actor.typed.scaladsl.*
import org.apache.pekko.actor.typed.*
import AlarmSystemZones.*
import org.apache.pekko.actor.typed.receptionist.{Receptionist, ServiceKey}
import scala.concurrent.duration.*

object AlarmSystemGuardian:
  sealed trait Command extends CborSerializable
  object Command:
    // keypad
    case class VerifyPin(pin: String, zones: List[Zones]) extends Command
    // sensors
    case class Detect(sensorId: String, zone: Zones) extends Command
    // private
    case class ReceptionistUpdated(listing: Receptionist.Listing) extends Command
    case class ExitTimeExpired(zones: List[Zones]) extends Command
    case object EntryTimeExpired extends Command

  export Command.*

  val GuardianServiceKey = ServiceKey[AlarmSystemGuardian.Command]("GuardianService")

  def apply(
             secret: String,
             entryDelayTimeout: FiniteDuration,
             exitDelayTimeout: FiniteDuration
           ): Behavior[Command] =
    Behaviors.setup: context =>
      context.system.receptionist ! Receptionist.Register(GuardianServiceKey, context.self)
      context.log.info(s"Guardian registered in Receptionist")

      val receptionistAdapter: ActorRef[Receptionist.Listing] =
        context.messageAdapter(listing => ReceptionistUpdated(listing))
      context.system.receptionist ! Receptionist.Subscribe(SirenActor.SirenServiceKey, receptionistAdapter)
      context.system.receptionist ! Receptionist.Subscribe(SensorsActors.SensorServiceKey, receptionistAdapter)

      var sirens: Set[ActorRef[SirenActor.Command]] = Set.empty
      var sensors: Set[ActorRef[GenericSensor.Command]] = Set.empty

      Behaviors.withTimers: timers =>
        def sensorsUpdateState(setArmed: Boolean, zones: List[Zones]): Unit = zones.foreach(z => sensors.foreach(_ ! GenericSensor.SetArmed(setArmed, z)))
        def sirensUpdateState(setState: Boolean): Unit = sirens.foreach(_ ! SirenActor.SetState(setState))
        def handleCommon(msg: Command): Behavior[Command] = msg match
          case ReceptionistUpdated(listing) =>
            if listing.isForKey(SirenActor.SirenServiceKey) then
              sirens = listing.serviceInstances(SirenActor.SirenServiceKey)
              context.log.info(s"connected sirens: ${sirens.size}")
            else if listing.isForKey(SensorsActors.SensorServiceKey) then
              sensors = listing.serviceInstances(SensorsActors.SensorServiceKey)
              context.log.info(s"connected sensors: ${sensors.size}")
            Behaviors.same
          case Detect(id, zone) =>
            context.log.warn(s"[$id - $zone] trigger ignored")
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
          case Detect(id, zone) =>
            context.log.info(s"System alarming sensor trigger: [$id - $zone], waiting entryDelay: "+ entryDelayTimeout.toString())
            timers.startSingleTimer(EntryTimeExpired, entryDelayTimeout)
            entryDelay()
          case VerifyPin(pin, _) if pin == secret =>
            sensorsUpdateState(false, Zones.values.toList)
            context.log.info(s"System disarmed")
            disarmed()
          case VerifyPin(_, _) =>
            context.log.warn("Incorrect Pin")
            Behaviors.same
          case msg => handleCommon(msg)

        def exitDelay(): Behavior[Command] = Behaviors.receiveMessage:
          case ExitTimeExpired(zones) =>
            if zones.isEmpty then
              sensorsUpdateState(true, Zones.values.toList)
              context.log.info("System armed all Zones")
            else
              sensorsUpdateState(true, zones)
              context.log.info(s"System armed on zones: $zones")
            armed()
          case msg => handleCommon(msg)

        def entryDelay(): Behavior[Command] = Behaviors.receiveMessage:
          case VerifyPin(pin, _) if pin == secret =>
            sensorsUpdateState(false, Zones.values.toList)
            context.log.info("System disarmed")
            disarmed()
          case VerifyPin(_, _) =>
            context.log.warn("Incorrect Pin")
            Behaviors.same
          case EntryTimeExpired =>
            context.log.info("System alarmed")
            sirensUpdateState(true)
            alarm()
          case msg => handleCommon(msg)

        def alarm(): Behavior[Command] = Behaviors.receiveMessage:
            case VerifyPin(pin, _) if pin == secret =>
              context.log.info("System disarmed")
              sirensUpdateState(false)
              sensorsUpdateState(false, Zones.values.toList)
              disarmed()
            case VerifyPin(_, _) =>
              context.log.warn("Incorrect Pin")
              Behaviors.same
            case msg => handleCommon(msg)

        def recovery(): Behavior[Command] = Behaviors.receiveMessage:
          case VerifyPin(pin, _) if pin == secret =>
            context.log.info("System unlocked")
            sirensUpdateState(false)
            sensorsUpdateState(false, Zones.values.toList)
            disarmed()
          case VerifyPin(_, _) =>
            context.log.warn("Incorrect Pin")
            Behaviors.same
          case msg => handleCommon(msg)

        context.log.info("System locked, enter pin to disarm")
        recovery()