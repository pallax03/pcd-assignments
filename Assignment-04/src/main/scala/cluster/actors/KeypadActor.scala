package cluster
package actors

import org.apache.pekko.actor.typed.scaladsl.*
import org.apache.pekko.actor.typed.*
import AlarmSystemZones.Zones
import org.apache.pekko.actor.typed.receptionist.Receptionist

object KeypadActor:
  sealed trait Command extends CborSerializable
  object Command:
    case class UserFixedInput(pin: String, zone: List[Zones]) extends Command
    case class UpdatedGuardian(listing: Receptionist.Listing) extends Command
  export Command.*

  def apply(): Behavior[Command] = Behaviors.setup: context =>
    val receptionistAdapter: ActorRef[Receptionist.Listing] = context.messageAdapter(listing => UpdatedGuardian(listing))
    context.system.receptionist ! Receptionist.Subscribe(AlarmSystemGuardian.GuardianServiceKey, receptionistAdapter)
    def waitingForGuardian: Behavior[Command] = Behaviors.receiveMessage:
      case UpdatedGuardian(listing) =>
        val guardians = listing.serviceInstances(AlarmSystemGuardian.GuardianServiceKey)
        if guardians.nonEmpty then
          context.log.info("Keypad ready for Guardian")
          ready(guardians.head)
        else
          Behaviors.same
      case UserFixedInput(_, _) =>
        context.log.warn("No Guardian Registered")
        Behaviors.same
    def ready(guardian: ActorRef[AlarmSystemGuardian.Command]): Behavior[Command] = Behaviors.receiveMessage:
      case UserFixedInput(pin, zones) =>
        context.log.info(s"Verify the pin: $pin ${if zones.isEmpty then "for all Zones" else s"for $zones"}")
        guardian ! AlarmSystemGuardian.VerifyPin(pin, zones)
        Behaviors.same
      case UpdatedGuardian(listing) =>
        val guardians = listing.serviceInstances(AlarmSystemGuardian.GuardianServiceKey)
        if guardians.nonEmpty then
          context.log.info("New Guardian hot-reload")
          ready(guardians.head)
        else
          context.log.warn("Guardian disconnected")
          waitingForGuardian
    waitingForGuardian