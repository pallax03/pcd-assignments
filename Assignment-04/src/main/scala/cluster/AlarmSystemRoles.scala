package cluster

object AlarmSystemRoles:
  enum SensorType:
    case Motion
    case Window

  enum Roles:
    case Guardian
    case Keypad
    case Siren
    case Sensor(kind: SensorType)

  object Roles:
    def fromString(s: String): Roles = s.toLowerCase match
      case "guardian" => Guardian
      case "keypad" => Keypad
      case "siren" => Siren
      case "sensor-motion" => Sensor(Motion)
      case "sensor-window" => Sensor(Window)
      case _ => throw new IllegalArgumentException(s"role: $s not valid")
  export Roles.*
  export SensorType.*