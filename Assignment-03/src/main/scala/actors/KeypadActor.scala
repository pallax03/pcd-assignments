package smarthome
package actors

import org.apache.pekko.actor.typed.scaladsl.*
import org.apache.pekko.actor.typed.*
import AlarmSystemZones.{Zones, requestZones}

object KeypadActor:
  enum Command:
    case UserFixedInput(pin: String, zone: List[Zones])
  export Command.*

  def apply(replyTo: ActorRef[AlarmSystemGuardian.Command]): Behavior[Command] = Behaviors.receive: (ctx, msg) =>
    msg match
      case UserFixedInput(pin, zones) =>
        ctx.log.info(s"Verify the pin: $pin ${if zones.isEmpty then "for all Zones" else s"for $zones"}")
        replyTo ! AlarmSystemGuardian.VerifyPin(pin, zones)
        Behaviors.same
      case _ => Behaviors.unhandled