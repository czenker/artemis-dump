package modules.al

import artemis.Porcelain._
import lib._
import modules.al.Chat.Messages
import modules.al.TransportModule.Configuration

import scala.xml.NodeSeq

// pick something up at Station A, bring to Station B. That's it.
class TransportModule(conf: Configuration) extends SteppedModule {

	override val name: String = conf.name
	override val prefix: String = conf.prefix
	override protected val trigger: SteppedModule.Trigger = conf.trigger

	lazy val varTimerDocked = s"_${prefix}_docked"

	override def onDestruct = super.onDestruct ++ clearCommsButton(conf.textBoarding)

	override def events: NodeSeq = super.events ++ Seq[NodeSeq](
		stepWithChat(100, 200, conf.onBeginning.chat,
			conf.onBeginning.scriptlet
		),
		step(200, 201,
			ifDocked(conf.from.id),
			setCommsButton(conf.textBoarding),

			setTimer(varTimerDocked, 1)
		),
		// these two take care we are detecting when player undocks
		step(201, 201,
			ifTimer(varTimerDocked),
			ifDocked(conf.from.id),
			setTimer(varTimerDocked, 1)
		),
		step(201, 200,
			ifTimer(varTimerDocked),
			clearCommsButton(conf.textBoarding)
		),
		stepWithChat(201, 300, conf.onBoarding.chat,
			ifDocked(conf.from.id),
			ifCommsButton(conf.textBoarding),

			conf.onBoarding.scriptlet,

			clearCommsButton(conf.textBoarding)
		),
		stepWithChat(300, 9999, conf.onSuccess.chat,
			ifDocked(conf.to.id),

			conf.onSuccess.scriptlet
		)
	)
}

object TransportModule {
	import SteppedModule._

	case class Configuration(mayBeName: Option[String],
	                         mayBePrefix: Option[String],
	                         from: Dockable,
	                         to: Dockable,
	                         textBoarding: String,

	                         trigger: Trigger = AutoTrigger,
	                         onBeginning: ChatAndScriptlet = ChatAndScriptlet(),
	                         onBoarding: ChatAndScriptlet = ChatAndScriptlet(),
	                         onSuccess: ChatAndScriptlet = ChatAndScriptlet()) {
		def get = new TransportModule(this)

		lazy val prefix: String = mayBePrefix.getOrElse(s"module_transport_$randomId")
		lazy val name: String = mayBeName.getOrElse(s"Shuttle Module")

		def withBeginning(chat: Messages = Messages.Empty, scriptlet: NodeSeq = NodeSeq.Empty) = copy(onBeginning = ChatAndScriptlet(chat, scriptlet))
		def withBoarding(chat: Messages = Messages.Empty, scriptlet: NodeSeq = NodeSeq.Empty) = copy(onBoarding = ChatAndScriptlet(chat, scriptlet))
		def withSuccess(chat: Messages = Messages.Empty, scriptlet: NodeSeq = NodeSeq.Empty) = copy(onSuccess = ChatAndScriptlet(chat, scriptlet))
		def withTrigger(trigger: Trigger): Configuration = this.copy(trigger = trigger)
	}

	def apply(name: Option[String] = None, prefix: Option[String] = None, textBoarding: String, from: Dockable, to: Dockable) = Configuration(name, prefix, from, to, textBoarding)

}
