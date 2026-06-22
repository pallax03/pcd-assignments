package cluster
package actors

import org.apache.pekko.actor.typed.*
import org.apache.pekko.actor.typed.receptionist.{Receptionist, ServiceKey}
import org.apache.pekko.actor.typed.scaladsl.*

object SirenActor:
  sealed trait Command extends CborSerializable
  object Command:
    case class SetState(active: Boolean) extends Command
  export Command.*

  val SirenServiceKey = ServiceKey[SirenActor.Command]("SirenService")

  def apply(isActive: Boolean = false): Behavior[Command] = Behaviors.setup: context =>
    context.system.receptionist ! Receptionist.Register(SirenActor.SirenServiceKey, context.self)
    context.log.info(s"Siren registered in Receptionist")
    Behaviors.receiveMessage:
      case SetState(state) =>
        context.log.info(s"Siren $state")
        SirenActor(state)