package mission.zzdefense.modules

import artemis.Porcelain._
import lib.{Event, Module, Station}

import scala.xml.{Comment, NodeSeq}

class FactoryModule(val station: Station,
                    prefix: String = "module_factory",
                    val maxOre: Long = 500,
                    val maxParts: Long = 100,
                    orePerPart: Long = 5,
                    fullIn: Long = 300
                ) extends Module {

	val varEnabled = s"${prefix}_enable"
	val varOre = s"${prefix}_ore"
	val varParts = s"${prefix}_parts"
	val varTimer = s"${prefix}_timer"

	/* 0 - no current warning
	 * 1 - production has stopped
	 */
	val varPartWarnStep = s"${prefix}_part_warn_step"

	val partsPerTick = maxParts / 100
	val orePerTick = partsPerTick * orePerPart
	val tick: Double = orePerTick * fullIn / maxOre

	private val textCheatPrefix = s"Cheat/${station.id}"

	val textCheatNoOre = s"$textCheatPrefix/No Ore"
	val textCheatAddOre = s"$textCheatPrefix/Add ${maxOre / 5} Ore"
	val textCheatDelOre = s"$textCheatPrefix/Remove ${maxOre / 5} Ore"
	val textCheatFullOre = s"$textCheatPrefix/Full Ore"

	val textCheatNoParts = s"$textCheatPrefix/No Parts"
	val textCheatAddParts = s"$textCheatPrefix/Add ${maxParts / 5} Parts"
	val textCheatDelParts = s"$textCheatPrefix/Remove ${maxParts / 5} Parts"
	val textCheatFullParts = s"$textCheatPrefix/Full Parts"

	override def onLoad: NodeSeq = set(varEnabled, 0) ++ set(varOre, 100) ++ set(varParts, 0) ++ set(varPartWarnStep, 0)

	override def events: NodeSeq = Seq[NodeSeq](
		Comment(
			s"""$getClass ($prefix)
			   |
			   | Simulating the economy of ${station.name}(${station.id}) converting ore into parts.
			   |
			   | Every $tick seconds it converts $orePerTick ore into $partsPerTick parts.
			   | It stores a maximum of $maxOre ore and $maxParts parts.""".stripMargin
		) ++
		Event(
			s"${prefix}_init",
			ifEquals(varEnabled, 1),

			setGmButton(textCheatNoOre),
			setGmButton(textCheatAddOre),
			setGmButton(textCheatDelOre),
			setGmButton(textCheatFullOre),
			setGmButton(textCheatNoParts),
			setGmButton(textCheatAddParts),
			setGmButton(textCheatDelParts),
			setGmButton(textCheatFullParts),

			setTimer(varTimer, tick),
			set(varEnabled, 2)
		) ++
		eventsForTick ++
		eventsCallForHelp ++
		cheats
	)

	def eventsForTick: NodeSeq = Seq[NodeSeq](
		Comment(s"${station.id} converting  $orePerTick ore into $partsPerTick parts"),
		Event(
			s"${prefix}_tick",
			ifGreaterOrEqual(varEnabled, 2),
			ifTimer(varTimer),
			ifProductionPossible,

			set(varParts, s"$varParts + $partsPerTick"),
			set(varOre, s"$varOre - $orePerTick"),
			setTimer(varTimer, tick)
		),
		// just guards for safety with cheats, broken math and stuff
		Event(
			ifSmaller(varOre, 0),
			set(varOre, 0)
		),
		Event(
			ifGreater(varOre, maxOre),
			set(varOre, maxOre)
		),
		Event(
			ifSmaller(varParts, 0),
			set(varParts, 0)
		),
		Event(
			ifGreater(varParts, maxParts),
			set(varParts, maxParts)
		)
	)

	// events that make sure a stream of ore is produced
	def eventsCallForHelp: NodeSeq = Seq[NodeSeq](
		Comment(s"${station.id} informing if production is stopped."),

		Event(
			s"${prefix}_reset",
			ifGreaterOrEqual(varEnabled, 2),
			ifGreaterOrEqual(varPartWarnStep, 1),
			ifProductionPossible,

			sendComms(station.id, s"""We are commencing production""".stripMargin, CommsType.Station),

			set(varPartWarnStep, 0)
		),
		Event(
			s"${prefix}_resource_block",
			ifEquals(varEnabled, 2),
			ifEquals(varPartWarnStep, 0),

			ifSmaller(varOre, orePerTick),
			sendComms(station.id, s"We are ceasing production, because we do not have any ores left.", CommsType.Station),

			set(varPartWarnStep, 1)
		),
		Event(
			s"${prefix}_parts_block",
			ifEquals(varEnabled, 2),
			ifEquals(varPartWarnStep, 0),

			ifGreater(varParts, maxParts - partsPerTick),
			sendComms(station.id, s"We are ceasing production, because our stores are filled up with parts.", CommsType.Station),

			set(varPartWarnStep, 1)
		)
	)

	def cheats: NodeSeq = Seq[NodeSeq](
		Comment(s"${station.id} cheats"),
		Event(
			ifGmButton(textCheatNoOre),
			set(varOre, 0)
		),
		Event(
			ifGmButton(textCheatAddOre),
			set(varOre, s"$varOre + ${maxOre / 5}")
		),
		Event(
			ifGmButton(textCheatDelOre),
			set(varOre, s"$varOre - ${maxOre / 5}")
		),
		Event(
			ifGmButton(textCheatFullOre),
			set(varOre, maxOre)
		),
		Event(
			ifGmButton(textCheatNoParts),
			set(varParts, 0)
		),
		Event(
			ifGmButton(textCheatAddParts),
			set(varParts, s"$varParts + ${maxParts / 5}")
		),
		Event(
			ifGmButton(textCheatDelParts),
			set(varParts, s"$varParts - ${maxParts / 5}")
		),
		Event(
			ifGmButton(textCheatFullParts),
			set(varParts, maxParts)
		)
	)

	def ifProductionPossible = ifSmallerOrEqual(varParts, maxParts - partsPerTick) ++ ifGreaterOrEqual(varOre, orePerTick)
}
