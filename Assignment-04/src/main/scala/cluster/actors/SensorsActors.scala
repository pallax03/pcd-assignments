package cluster
package actors

import AlarmSystemZones.*
import org.apache.pekko.actor.typed.*
import org.apache.pekko.actor.typed.receptionist.{Receptionist, ServiceKey}
import org.apache.pekko.actor.typed.scaladsl.*

import scala.concurrent.duration.DurationInt
import scala.util.Random

object SensorsActors:
  val SensorServiceKey = ServiceKey[GenericSensor.Command]("SensorService")

  object GenericSensor:
    sealed trait Command extends CborSerializable
    object Command:
      case object Trigger extends Command
      case class SetArmed(isArmed: Boolean, zone: Zones) extends Command
      case class UpdatedGuardian(listing: Receptionist.Listing) extends Command
    export Command.*
    def apply(
               id: String,
              zone: Zones
             ): Behavior[Command] = Behaviors.setup: context =>
      context.system.receptionist ! Receptionist.Register(SensorsActors.SensorServiceKey, context.self)
      context.log.info(s"$id registered in Receptionist")

      val receptionistAdapter: ActorRef[Receptionist.Listing] = context.messageAdapter(UpdatedGuardian(_))
      context.system.receptionist ! Receptionist.Subscribe(AlarmSystemGuardian.GuardianServiceKey, receptionistAdapter)
      var guardian: Option[ActorRef[AlarmSystemGuardian.Command]] = Option.empty

      Behaviors.withTimers: timer =>
        timer.startTimerAtFixedRate(Trigger, Random.between(10, 30).seconds)
        def baseBehavior(isArmed: Boolean): Behavior[Command] = Behaviors.receiveMessage:
            case SetArmed(isArmed, newZone) if zone == newZone =>
              context.log.info(s"[$id - $zone] sensor armed: $isArmed")
              baseBehavior(isArmed)
            case SetArmed(isArmed, newZone) =>
              context.log.debug(s"[$id - $zone] ignored new armed for zone: $zone")
              Behaviors.same
            case UpdatedGuardian(listing) =>
              val guardians = listing.serviceInstances(AlarmSystemGuardian.GuardianServiceKey)
              if guardians.nonEmpty then
                context.log.info(s"[$id - $zone] Guardian reference updated")
                guardian = Option(guardians.head)
              else
                context.log.warn(s"[$id - $zone] Guardian disconnected")
                guardian = Option.empty
              baseBehavior(isArmed)
            case Trigger =>
              if isArmed then
                guardian match
                  case Some(value) =>
                    context.log.info(s"[$id - $zone] trigger")
                    value ! AlarmSystemGuardian.Detect(id, zone)
                  case None =>
                    context.log.debug(s"[$id - $zone] trigger ignored: sensor armed, but no guardian")
              else
                context.log.debug(s"[$id - $zone] trigger ignored: no armed")
              Behaviors.same
        baseBehavior(false)


  object MotionSensor:
    export GenericSensor.Command
    export GenericSensor.Command.*

    def apply(
               id: String,
               zone: Zones
             ): Behavior[Command] = Behaviors.setup: context =>
      GenericSensor(id, zone)

  object WindowSensor:
    export GenericSensor.Command
    export GenericSensor.Command.*

    def apply(
               id: String,
               zone: Zones
             ): Behavior[Command] = Behaviors.setup: context =>
      GenericSensor(id, zone)

export SensorsActors.*