package mission.defense.whale

import artemis.Porcelain._
import lib._
import mission.defense.whale.WhalerModule.Configuration
import modules.al.Chat.Messages

import scala.xml.NodeSeq

class WhalerModule(conf: Configuration) extends SteppedModule {
	override val name: String = conf.name
	override val prefix: String = conf.prefix
	override protected val trigger: SteppedModule.Trigger = conf.trigger

	val varExtractionX = s"_${prefix}_x"
	val varExtractionZ = s"_${prefix}_z"

	override def onLoad: NodeSeq = super.onLoad

	override def onDestruct: NodeSeq = super.onDestruct ++ destroy(conf.whaler.id)

	override def events: NodeSeq = super.events ++ NodeSeq.fromSeq(Seq[NodeSeq](
		step(100, 101,
			conf.whaler.create(conf.spawnLocation)
		),
		stepWithChat(101, 200, conf.onBeginning.chat,
			conf.whaler.ifCreated,
			conf.whaler.afterCreate(),
			conf.onBeginning.scriptlet,
			setTimer(varTimer, 0)
		),
		// AI just stands still when one target in AI stack does not exist - so we will be very selective... :(
		step(200, 201,
			ifTimer(varTimer),
			setAi(conf.whaler.id)
		),
		conf.whales.map { whale =>
			step(201, 201,
				ifExists(whale.id),
				addAi(conf.whaler.id, aiAttack(whale.id, 1.5))
			)
		},
		step(201, 200,
			addAi(conf.whaler.id,
//				aiChasePlayer(5000, 2500),
				aiChaseAnger(),
				aiAvoidBlackHole(),
				aiUseSpecialPowers()
			),
			setTimer(varTimer, 15.0)
		),
		stepWithChat(200, 9999, conf.onSuccess.chat,
			ifNotExists(conf.whaler.id),
			conf.onSuccess.scriptlet
		),
		stepWithChat(200, 300, conf.onFailure.chat,
			conf.whales.map( whale => ifNotExists(whale.id)),
			setAi(
				conf.whaler.id,
				aiGoto(conf.spawnLocation),
//				aiChasePlayer(7000, 5000),
				aiChaseAnger(),
				aiAvoidBlackHole(),
				aiUseSpecialPowers()
			),
			conf.onFailure.scriptlet
		),
		step(300, 9999,
			ifDistanceSmaller(conf.whaler.id, conf.spawnLocation, 2500),
			destroy(conf.whaler.id)
		),
		step(300, 9999,
			ifNotExists(conf.whaler.id)
		)
	))
}

object WhalerModule {

	import SteppedModule._

	case class Configuration(mayBeName: Option[String] = None,
	                         mayBePrefix: Option[String] = None,
	                         whaler: EnemyShip,
	                         spawnLocation: Location,
	                         whales: Seq[Monster],

	                         trigger: Trigger = AutoTrigger,
	                         onBeginning: ChatAndScriptlet = ChatAndScriptlet(),
	                         onSuccess: ChatAndScriptlet = ChatAndScriptlet(),
	                         onFailure: ChatAndScriptlet = ChatAndScriptlet()
	                        ) {
		def get(implicit idRegistry: IdRegistry) = new WhalerModule(this)

		lazy val prefix: String = mayBePrefix.getOrElse(s"module_defense_whale_$randomId")
		lazy val name: String = mayBeName.getOrElse(s"Whaler Module")

		def withBeginning(chat: Messages = Messages.Empty, scriptlet: NodeSeq = NodeSeq.Empty) = copy(onBeginning = ChatAndScriptlet(chat, scriptlet))
		def withSuccess(chat: Messages = Messages.Empty, scriptlet: NodeSeq = NodeSeq.Empty) = copy(onSuccess = ChatAndScriptlet(chat, scriptlet))
		def withFailure(chat: Messages = Messages.Empty, scriptlet: NodeSeq = NodeSeq.Empty) = copy(onFailure = ChatAndScriptlet(chat, scriptlet))

		def withTrigger(trigger: Trigger): Configuration = this.copy(trigger = trigger)
	}

	def apply(whaler: EnemyShip, whales: Seq[Monster], spawnLocation: Location) = Configuration(whaler = whaler, whales = whales, spawnLocation = spawnLocation)
}
