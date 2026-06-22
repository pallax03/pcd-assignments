package smarthome
package actors

import AlarmSystemZones.*

import org.apache.pekko.actor.typed.*
import org.apache.pekko.actor.typed.scaladsl.*

object SensorsActors:

  object GenericSensor:
    enum Command:
      case Trigger
      case SetArmed(isArmed: Boolean)
      case SimulateException
    export Command.*

    def baseBehavior(
                    name: String,
                    guardian: ActorRef[AlarmSystemGuardian.Command],
                    isArmed: Boolean = false
                    ): Behavior[Command] = Behaviors.receive: (ctx, msg) =>
      msg match
        case Trigger =>
          if isArmed then
            guardian ! AlarmSystemGuardian.Detect(name)
          else
            ctx.log.debug(s"[$name] Trigger ignored")
          Behaviors.same
        case SetArmed(armed) =>
          ctx.log.info(s"[$name] sensor armed: $armed")
          baseBehavior(name, guardian, armed)
        case SimulateException =>
          throw new RuntimeException(s"[$name] got exception")

  object MotionSensor:
    export GenericSensor.Command
    export GenericSensor.Command.*

    def apply(
               zone: Zones,
               guardian: ActorRef[AlarmSystemGuardian.Command]
             ): Behavior[Command] = Behaviors.setup: context =>
      GenericSensor.baseBehavior(s"MotionSensor $zone", guardian)

  object WindowSensor:
    export GenericSensor.Command
    export GenericSensor.Command.*

    def apply(
               zone: Zones,
               guardian: ActorRef[AlarmSystemGuardian.Command]
             ): Behavior[Command] = Behaviors.setup: context =>
      GenericSensor.baseBehavior(s"WindowSensor $zone", guardian)

export SensorsActors.*