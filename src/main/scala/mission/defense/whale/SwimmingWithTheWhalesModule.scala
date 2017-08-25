package mission.defense.whale

import artemis.Porcelain._
import lib._
import mission.defense.whale.SwimmingWithTheWhalesModule.Configuration
import modules.al.Chat.Messages

import scala.xml.NodeSeq

class SwimmingWithTheWhalesModule(conf: Configuration) extends SteppedModule {
	override val name: String = conf.name
	override val prefix: String = conf.prefix
	override protected val trigger: SteppedModule.Trigger = conf.trigger

	val varTimerDocked = s"_${prefix}_docked"

	override def onLoad: NodeSeq = super.onLoad

	override def onDestruct: NodeSeq = super.onDestruct ++ clearCommsButton(conf.textBoarding)

	override def events: NodeSeq = super.events ++ NodeSeq.fromSeq(Seq[NodeSeq](
		stepWithChat(100, 200, conf.onBeginning.chat,
			conf.onBeginning.scriptlet
		),
		step(200, 201,
			ifDocked(conf.station.id),
			setCommsButton(conf.textBoarding),

			setTimer(varTimerDocked, 1)
		),
		// these two take care we are detecting when player undocks
		step(201, 201,
			ifTimer(varTimerDocked),
			ifDocked(conf.station.id),
			setTimer(varTimerDocked, 1)
		),
		step(201, 200,
			ifTimer(varTimerDocked),
			clearCommsButton(conf.textBoarding)
		),
		stepWithChat(201, 300, conf.onBoarding.chat,
			ifDocked(conf.station.id),
			ifCommsButton(conf.textBoarding),

			conf.onBoarding.scriptlet,

			clearCommsButton(conf.textBoarding)
		),
		conf.whales.map { whale =>
			step(300, 301,
				ifExists(whale.id),
				ifDistanceSmaller(whale.id, conf.player.id, 7000)
			)
		},
		stepWithChat(301, 400, conf.onApproaching.chat,
			conf.onApproaching.scriptlet
		),
		conf.whales.map { whale =>
			step(400, 401,
				ifExists(whale.id),
				ifDistanceSmaller(whale.id, conf.player.id, 200)
			)
		},
		stepWithChat(401, 500, conf.onContact.chat,
			conf.onContact.scriptlet
		),
		stepWithChat(500, 9999, conf.onSuccess.chat,
			ifDocked(conf.station.id),
			conf.onSuccess.scriptlet
		)
	))
}

object SwimmingWithTheWhalesModule {

	import SteppedModule._

	case class Configuration(mayBeName: Option[String] = None,
	                         mayBePrefix: Option[String] = None,
	                         player: Player,
	                         whales: Seq[Monster],
	                         textBoarding: String,
	                         station: Dockable,

	                         trigger: Trigger = AutoTrigger,
	                         onBeginning: ChatAndScriptlet = ChatAndScriptlet(),
	                         onBoarding: ChatAndScriptlet = ChatAndScriptlet(),
	                         onApproaching: ChatAndScriptlet = ChatAndScriptlet(),
	                         onContact: ChatAndScriptlet = ChatAndScriptlet(),
	                         onSuccess: ChatAndScriptlet = ChatAndScriptlet()) {
		def get(implicit idRegistry: IdRegistry) = new SwimmingWithTheWhalesModule(this)

		lazy val prefix: String = mayBePrefix.getOrElse(s"module_defense_whale_$randomId")
		lazy val name: String = mayBeName.getOrElse(s"Swimming With The Whales Module")

		def withBeginning(chat: Messages = Messages.Empty, scriptlet: NodeSeq = NodeSeq.Empty) = copy(onBeginning = ChatAndScriptlet(chat, scriptlet))
		def withBoarding(chat: Messages = Messages.Empty, scriptlet: NodeSeq = NodeSeq.Empty) = copy(onBoarding = ChatAndScriptlet(chat, scriptlet))
		def withApproaching(chat: Messages = Messages.Empty, scriptlet: NodeSeq = NodeSeq.Empty) = copy(onApproaching = ChatAndScriptlet(chat, scriptlet))
		def withContact(chat: Messages = Messages.Empty, scriptlet: NodeSeq = NodeSeq.Empty) = copy(onContact = ChatAndScriptlet(chat, scriptlet))
		def withSuccess(chat: Messages = Messages.Empty, scriptlet: NodeSeq = NodeSeq.Empty) = copy(onSuccess = ChatAndScriptlet(chat, scriptlet))

		def withTrigger(trigger: Trigger): Configuration = this.copy(trigger = trigger)
	}

	def apply(player: Player, whales: Seq[Monster], textBoarding: String, station: Dockable) = Configuration(player = player, whales = whales, textBoarding = textBoarding, station = station)
}
