package smarthome
package actors

import org.apache.pekko.actor.typed.*
import org.apache.pekko.actor.typed.scaladsl.*
import AlarmSystemZones.Zones

object SensorsGroup:
  enum Command:
    case SetArmedZone(armed: Boolean)
    case TriggerSensor(id: Int)
    case BreakSensor(id: Int)
  export Command.*

  def apply(
           zone: Zones,
           guardian: ActorRef[AlarmSystemGuardian.Command],
           sensorFactories: List[ActorRef[AlarmSystemGuardian.Command] => Behavior[GenericSensor.Command]]
           ): Behavior[Command] = Behaviors.setup: context =>
    context.log.info(s"$zone init")
    val sensors = sensorFactories.zipWithIndex.map {
      case (factory, id) =>
        val sensor = context.spawn(factory(guardian), s"$zone-sensor-$id")
        context.watch(sensor)
        sensor
    }
    Behaviors.receiveMessage[Command]:
      case SetArmedZone(armed) =>
        sensors.foreach(_ ! GenericSensor.SetArmed(armed))
        Behaviors.same
      case TriggerSensor(id) if sensors.isDefinedAt(id) =>
        sensors(id) ! GenericSensor.Trigger
        Behaviors.same
      case TriggerSensor(id) =>
        context.log.warn(s"sensor($id) not found in $zone")
        Behaviors.same
      case BreakSensor(id) if sensors.isDefinedAt(id) =>
        sensors(id) ! GenericSensor.SimulateException
        Behaviors.same
      case BreakSensor(id) =>
        context.log.warn(s"sensor($id) not found in $zone")
        Behaviors.same
    .receiveSignal:
      case (context, Terminated(sensor)) =>
        guardian ! AlarmSystemGuardian.SensorOffline(sensor.path.name)
        Behaviors.same