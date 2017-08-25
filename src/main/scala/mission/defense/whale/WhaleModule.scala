package mission.defense.whale

import lib._
import mission.defense.whale.WhaleModule.Configuration
import modules.al.Chat.Messages
import artemis.Porcelain._
import modules.alu.BorderLocationModule

import scala.xml.NodeSeq

class WhaleModule(conf: Configuration) extends SteppedModule {
	override val name: String = conf.name
	override val prefix: String = conf.prefix
	override protected val trigger: SteppedModule.Trigger = conf.trigger

	val allWhales = conf.spawnWhales ++ conf.otherWhales

	val varWhalesTotal = s"_${prefix}_whales_total"
	val varWhalesKilled = s"_${prefix}_whales_killed"
	val varWhaleState = allWhales.indices.map(i => s"_${prefix}_whale$i")
	val varRandX = s"_${prefix}_whales_randX"
	val varRandZ = s"_${prefix}_whales_randZ"

	val location = Location(varRandX, "0.0", varRandZ)

	val spawnLocation = Location((conf.area.maxX + conf.area.minX) / 2, 0, (conf.area.maxZ + conf.area.minZ) / 2)

	override def onLoad: NodeSeq = super.onLoad ++ varWhaleState.map(s => set(s, 0)) ++ set(varWhalesTotal, 0) ++ set(varWhalesKilled, 0)

	override def onDestruct: NodeSeq = super.onDestruct ++ allWhales.map { whale => destroy(whale.id) }

	override def events: NodeSeq = super.events ++ NodeSeq.fromSeq(Seq[NodeSeq](
		stepWithChat(100, 200, conf.onBeginning.chat,
			conf.spawnWhales.map(whale => whale.create(spawnLocation.fuzz(100))), // spawn fuzzy to avoid stacking
			varWhaleState.map(s => set(s, 0)),
			set(varWhalesTotal, conf.spawnWhales.size),
			set(varWhalesKilled, 0),
			conf.onBeginning.scriptlet
		),
		step(200, 201,
			randomLocation,
			allWhales.map(gotoAi)
		),
		allWhales.zip(varWhaleState).map{ case (whale, s) =>
			step(201, 200,
				ifExists(whale.id),
				ifDistanceSmaller(whale.id, location, 500.0)
			) ++
			Event(
				ifGreaterOrEqual(varStep, 200),
				ifSmaller(varStep, 300),
				ifEquals(s, 0),
				ifNotExists(whale.id),
				set(varWhalesKilled, s"$varWhalesKilled + 1"),
				set(s, 1)
			)
		},
		step(300, 301, // is set to 300 by calling "leaveSector"
			BorderLocationModule.call(varRandX, varRandZ)
		),
		step(301, 302,
			BorderLocationModule.ifFinished,
			set(varRandX, BorderLocationModule.varX),
			set(varRandZ, BorderLocationModule.varZ),
			allWhales.map(gotoAi)
		),
		allWhales.map{ whale =>
			Event(
				ifEquals(varStep, 302),
				ifDistanceSmaller(whale.id, location, 500),
				destroy(whale.id)
			)
		},
		stepWithChat(200.to(999), 9999, conf.onFailure.chat,
			ifGreaterOrEqual(varWhalesTotal, 1),
			ifEquals(varWhalesKilled, varWhalesTotal),
			conf.onFailure.scriptlet
		),
		stepWithChat(302, 9999, conf.onSuccess.chat,
			ifGreaterOrEqual(varWhalesTotal, 1),
			allWhales.map { whale => ifNotExists(whale.id) },
			conf.onSuccess.scriptlet
		)
	))

	// Here is a possibility to add another whale to the group
	def addWhaleToGroup(whale: Monster): NodeSeq = {
		require(conf.otherWhales.contains(whale), s"The configuration otherWhales needs to contain $whale")

		set(varWhalesTotal, s"$varWhalesTotal + 1") ++
		set(varStep, 200)
	}

	def leaveSector: NodeSeq = set(varStep, 300)

	private def gotoAi(whale: Monster) = setAi(whale.id,
		aiGoto(location),
//		aiMoveWithGroup(),
		aiStayClose()
	)

	private def randomLocation: NodeSeq = {
		val step = 5 // the random number generator only works till 2^15 (~32.000), so lets use smaller steps

		NodeSeq.fromSeq(Seq(
			setRandomInt(varRandX, 0, (conf.area.maxX - conf.area.minX) / step),
			setRandomInt(varRandZ, 0, (conf.area.maxZ - conf.area.minZ) / step),
			set(varRandX, s"$varRandX * $step + ${conf.area.minX}"),
			set(varRandZ, s"$varRandZ * $step + ${conf.area.minZ}")
		))
	}
}

object WhaleModule {

	import SteppedModule._

	case class Configuration(mayBeName: Option[String] = None,
	                         mayBePrefix: Option[String] = None,
	                         spawnWhales: Seq[Monster],
	                         otherWhales: Seq[Monster],
	                         area: Rectangle,

	                         trigger: Trigger = AutoTrigger,
	                         onBeginning: ChatAndScriptlet = ChatAndScriptlet(),
	                         onSuccess: ChatAndScriptlet = ChatAndScriptlet(),
	                         onFailure: ChatAndScriptlet = ChatAndScriptlet()) {
		def get = new WhaleModule(this)

		lazy val prefix: String = mayBePrefix.getOrElse(s"module_defense_whale_$randomId")
		lazy val name: String = mayBeName.getOrElse(s"Whale Module")

		def withBeginning(chat: Messages = Messages.Empty, scriptlet: NodeSeq = NodeSeq.Empty) = copy(onBeginning = ChatAndScriptlet(chat, scriptlet))
		def withSuccess(chat: Messages = Messages.Empty, scriptlet: NodeSeq = NodeSeq.Empty) = copy(onSuccess = ChatAndScriptlet(chat, scriptlet))
		def withFailure(chat: Messages = Messages.Empty, scriptlet: NodeSeq = NodeSeq.Empty) = copy(onFailure = ChatAndScriptlet(chat, scriptlet))

		def withTrigger(trigger: Trigger): Configuration = this.copy(trigger = trigger)
	}

	def apply(spawnWhales: Seq[Monster], otherWhales: Seq[Monster], area: Rectangle) = {
		(spawnWhales ++ otherWhales).foreach { whale =>
			require(whale.monsterType == Monster.Whale, "every monster needs to be a whale")
			require(whale.podNumber.isDefined, "every whale needs to have a podNumber")
		}

		require((spawnWhales ++ otherWhales).map(_.podNumber.get).toSet.size == 1, "all whales need to have the same podNumber")

		Configuration(spawnWhales = spawnWhales, area = area, otherWhales = otherWhales)
	}
}
