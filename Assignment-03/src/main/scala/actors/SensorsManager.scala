package smarthome
package actors

import AlarmSystemZones.Zones

import org.apache.pekko.actor.typed.*
import org.apache.pekko.actor.typed.scaladsl.*

object SensorsManager:
  enum Command:
    case ArmAll
    case DisarmAll
    case ArmZone(zone: Zones)
    case TriggerSensor(zone: Zones, sensorId: Int)
    case BreakSensor(zone: Zones, sensorId: Int)
  export Command.*

  def apply(
             guardian: ActorRef[AlarmSystemGuardian.Command]
           ): Behavior[Command] = Behaviors.setup: context =>
    val allZones: Map[Zones, ActorRef[SensorsGroup.Command]] = Zones.values.map { zone =>
      val factories = List[ActorRef[AlarmSystemGuardian.Command] => Behavior[SensorsActors.GenericSensor.Command]](
        g => SensorsActors.MotionSensor(zone, g),
        g => SensorsActors.WindowSensor(zone, g)
      )

      val group = context.spawn(SensorsGroup(zone, guardian, factories),s"group-$zone")
      zone -> group
    }.toMap

    Behaviors.receiveMessage:
        case ArmAll =>
          allZones.values.foreach(_ ! SensorsGroup.SetArmedZone(true))
          Behaviors.same
        case DisarmAll =>
          allZones.values.foreach(_ ! SensorsGroup.SetArmedZone(false))
          Behaviors.same
        case ArmZone(zone) =>
          allZones.get(zone).foreach(_ ! SensorsGroup.SetArmedZone(true))
          Behaviors.same
        case TriggerSensor(zone, id) =>
          allZones.get(zone).foreach(_ ! SensorsGroup.TriggerSensor(id))
          Behaviors.same
        case BreakSensor(zone, id) =>
          allZones.get(zone).foreach(_ ! SensorsGroup.BreakSensor(id))
          Behaviors.same