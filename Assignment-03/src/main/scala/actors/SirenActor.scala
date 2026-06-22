package smarthome
package actors

import org.apache.pekko.actor.typed.*
import org.apache.pekko.actor.typed.scaladsl.*

object SirenActor:
  enum Command:
    case SetState(active: Boolean)
  export Command.*

  def apply(replyTo: ActorRef[AlarmSystemGuardian.Command], isActive: Boolean = false): Behavior[Command] = Behaviors.receive: (ctx, msg) =>
    msg match
      case SetState(state) =>
        ctx.log.info(s"Siren $state")
        SirenActor(replyTo, state)