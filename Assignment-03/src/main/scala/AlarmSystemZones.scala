package smarthome

import scala.io.StdIn.readLine
import scala.util.matching.Regex

object AlarmSystemZones:
  enum Zones:
    case LivingRoom
    case SleepingRoom
    case Kitchen
    case BathRoom

  def requestZones(): List[Zones] =
    val allZones = Zones.values.toList
    println("Enter zones (es. '0 2' or '0, 2'):")
    allZones.zipWithIndex.foreach { case (zone, idx) =>
      println(s"[$idx] $zone")
    }
    println(s"[Enter] allZones")

    val input = readLine()
    if input.trim.isEmpty then
      List.empty
    else
      val digitRegex: Regex = "\\d+".r
      digitRegex.findAllIn(input)
        .flatMap(s => allZones.lift(s.toInt))
        .toList
  export Zones.*