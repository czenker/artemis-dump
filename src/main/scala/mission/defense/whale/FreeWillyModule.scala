package mission.defense.whale

import lib._
import modules.al.Chat.Messages
import artemis.Porcelain._
import mission.defense.whale.FreeWillyModule.Configuration

import scala.xml.NodeSeq

class FreeWillyModule(conf: Configuration) extends SteppedModule {
	override val name: String = conf.name
	override val prefix: String = conf.prefix
	override protected val trigger: SteppedModule.Trigger = conf.trigger

	val varTimerDocked = s"_${prefix}_docked"

	override def onLoad: NodeSeq = super.onLoad

	override def onDestruct: NodeSeq = super.onDestruct ++ clearCommsButton(conf.textDrop) ++ clearCommsButton(conf.textBoarding)

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
				ifDistanceSmaller(whale.id, conf.player.id, 200),
				setCommsButton(conf.textDrop)
			)
		},
		step(401, 400,
			conf.whales.map( whale => ifDistanceGreater(whale.id, conf.player.id, 500)),
			clearCommsButton(conf.textDrop)
		),
		step(401, 402,
			ifCommsButton(conf.textDrop),
			clearCommsButton(conf.textDrop),

			conf.willy.create()
		),
		stepWithChat(402, 9999, conf.onSuccess.chat,
			ifExists(conf.willy.id),
			setRelativePosition(conf.willy.id, conf.player.id, 20, 150),

			conf.onSuccess.scriptlet
		)

	))

}

object FreeWillyModule {

	import SteppedModule._

	case class Configuration(mayBeName: Option[String] = None,
	                         mayBePrefix: Option[String] = None,
	                         player: Player,
	                         whales: Seq[Monster],
	                         willy: Monster,
	                         station: Dockable,
	                         textBoarding: String,
	                         textDrop: String,

	                         trigger: Trigger = AutoTrigger,
	                         onBeginning: ChatAndScriptlet = ChatAndScriptlet(),
	                         onBoarding: ChatAndScriptlet = ChatAndScriptlet(),
	                         onApproaching: ChatAndScriptlet = ChatAndScriptlet(),
	                         onSuccess: ChatAndScriptlet = ChatAndScriptlet()) {
		def get = new FreeWillyModule(this)

		lazy val prefix: String = mayBePrefix.getOrElse(s"module_defense_whale_$randomId")
		lazy val name: String = mayBeName.getOrElse(s"Free Willy Module")

		def withBeginning(chat: Messages = Messages.Empty, scriptlet: NodeSeq = NodeSeq.Empty) = copy(onBeginning = ChatAndScriptlet(chat, scriptlet))
		def withBoarding(chat: Messages = Messages.Empty, scriptlet: NodeSeq = NodeSeq.Empty) = copy(onBoarding = ChatAndScriptlet(chat, scriptlet))
		def withApproaching(chat: Messages = Messages.Empty, scriptlet: NodeSeq = NodeSeq.Empty) = copy(onApproaching = ChatAndScriptlet(chat, scriptlet))
		def withSuccess(chat: Messages = Messages.Empty, scriptlet: NodeSeq = NodeSeq.Empty) = copy(onSuccess = ChatAndScriptlet(chat, scriptlet))

		def withTrigger(trigger: Trigger): Configuration = this.copy(trigger = trigger)
	}

	def apply(player: Player, whales: Seq[Monster], willy: Monster, station: Dockable, textBoarding: String, textDrop: String) = {
		require(willy.podNumber.isDefined, "Willy should have the pod number of all his fellow whales set.")
		Configuration(player = player, whales = whales, willy = willy, station = station, textBoarding = textBoarding, textDrop = textDrop)
	}
}
