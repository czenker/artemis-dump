package mission.zzdefense.modules

import artemis.Porcelain._
import lib.{Event, Module, Station}

import scala.xml.{Comment, NodeSeq}

class MineModule(val station: Station,
                 prefix: String = "module_mine",
                 val maxOre: Long = 500,
                 fullIn: Long = 300
                ) extends Module {

	val varEnabled = s"${prefix}_enable"
	val varAmount = s"${prefix}_ore"
	val varTimer = s"${prefix}_timer"
	/* 0 - no current warning
	 * 1 - player has been warned that stores are filled by 75%
	 * 2 - player has been warned that stores are full
	 */
	val varWarningStep = s"${prefix}_warning_step"

	val orePerTick: Long = maxOre / 100 // producing 1% per tick
	val tick: Double = orePerTick * fullIn / maxOre

	private val textCheatPrefix = s"Cheat/${station.id}"
	val textCheatNoOre = s"$textCheatPrefix/No Ore"
	val textCheatAddOre = s"$textCheatPrefix/Add ${maxOre / 5} Ore"
	val textCheatDelOre = s"$textCheatPrefix/Remove ${maxOre / 5} Ore"
	val textCheatFullOre = s"$textCheatPrefix/Full Ore"

	override def onLoad: NodeSeq = set(varEnabled, 0) ++
		set(varAmount, 0) ++ set(varWarningStep, 0)

	override def events: NodeSeq = Seq[NodeSeq](
		Comment(
			s"""$getClass ($prefix)
			   |
			   | Simulating the economy of ${station.name}(${station.id}) producing ore.
			   |
			   | Producing $orePerTick ore per $tick seconds up to a limit of $maxOre.""".stripMargin
		) ++
		Event(
			s"${prefix}_init",
			ifEquals(varEnabled, 1),

			setGmButton(textCheatNoOre),
			setGmButton(textCheatAddOre),
			setGmButton(textCheatDelOre),
			setGmButton(textCheatFullOre),

			setTimer(varTimer, tick),
			set(varEnabled, 2)
		) ++
		eventsForTick ++
		eventsCallForHelp ++
		cheats
	)

	// events that make sure a stream of ore is produced
	def eventsForTick: NodeSeq = Seq[NodeSeq](
		Comment(s"${station.id} producing ore"),
		Event(
			s"${prefix}_tick",
			ifGreaterOrEqual(varEnabled, 2),
			ifTimer(varTimer),
			set(varAmount, s"$varAmount + $orePerTick"),
			setTimer(varTimer, tick)
		),
		Event(
			s"${prefix}_upper",
			ifGreaterOrEqual(varEnabled, 2),
			ifGreater(varAmount, maxOre),
			set(varAmount, maxOre)
		)
	)

	// events that make sure a stream of ore is produced
	def eventsCallForHelp: NodeSeq = Seq[NodeSeq](
		Comment(s"${station.id} calling for help if too much ore builds up"),
		Event(
			s"${prefix}_fill_reset",
			ifGreaterOrEqual(varEnabled, 2),
			ifGreater(varWarningStep, 1),
			ifSmaller(varAmount, maxOre),

			sendComms(station.id, s"""We are commencing production of ore.""".stripMargin, CommsType.Station),

			set(varWarningStep, 1)
		),
		Event(
			s"${prefix}_fill_reset2",
			ifGreaterOrEqual(varEnabled, 2),
			ifGreater(varWarningStep, 0),
			ifSmaller(varAmount, maxOre * 3 / 4),

			set(varWarningStep, 0)
		),
		Event(
			s"${prefix}_fill_escalate",
			ifGreaterOrEqual(varEnabled, 2),
			ifSmallerOrEqual(varWarningStep, 0),
			ifGreaterOrEqual(varAmount, maxOre * 3 / 4),

			sendComms(station.id, s"""Our stores are filling up with ore. Please help us.""".stripMargin, CommsType.Station),

			set(varWarningStep, 1)
		),
		Event(
			s"${prefix}_fill_escalate2",
			ifGreaterOrEqual(varEnabled, 2),
			ifSmallerOrEqual(varWarningStep, 1),
			ifGreaterOrEqual(varAmount, maxOre),

			sendComms(station.id, s"""We are ceasing production, because our stores are filled up with ore.""".stripMargin, CommsType.Station),

			set(varWarningStep, 2)
		)
	)

	def cheats: NodeSeq = Seq[NodeSeq](
		Comment(s"${station.id} cheats"),
		Event(
			ifGmButton(textCheatNoOre),
			set(varAmount, 0)
		),
		Event(
			ifGmButton(textCheatAddOre),
			set(varAmount, s"$varAmount + ${maxOre / 5}")
		),
		Event(
			ifGmButton(textCheatDelOre),
			set(varAmount, s"$varAmount - ${maxOre / 5}")
		),
		Event(
			ifGmButton(textCheatFullOre),
			set(varAmount, maxOre)
		)
	)
}
