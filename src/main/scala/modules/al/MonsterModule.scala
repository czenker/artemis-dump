package mission.al

import artemis.Porcelain._
import lib.SteppedModule.GmTrigger
import lib._
import mission.al.MonsterModule.Configuration
import modules.al.Chat.Messages
import modules.al.TransportModule
import modules.alu.RandomLocationModule

import scala.xml.{Comment, Node, NodeSeq}

// creates a monster that terrorizes the sector
// The player needs to hunt it down
class MonsterModule(conf: Configuration) extends SteppedModule {

	val prefix = conf.prefix
	val name = conf.name
	val trigger = conf.trigger

	override def onDestruct = super.onDestruct ++ conf.monsters.map(m => destroy(m.id))

	override def events: NodeSeq = super.events ++ Seq[NodeSeq](
		Comment(
			s"""$getClass ($prefix)
			   |
			   | The players should hunt ${conf.monsters.size} monsters.""".stripMargin
		),
		step(100, 101,
			conf.monsters.toList.zipWithIndex.map {case (monster, idx) =>
				monster.create(conf.spawnLocation.atDistance(100, idx * 2 * Math.PI / conf.monsters.size))
			}
		),
		stepWithChat(101, 200, conf.onBeginning.chat,
			conf.monsters.map(_.ifCreated),
			conf.monsters.map(_.afterCreate()),
			conf.monsters.map(m => havocAi(m.id)),
			conf.onBeginning.scriptlet
		),
		stepWithChat(200, 9999, conf.onSuccess.chat,
			conf.monsters.map(m => ifNotExists(m.id)).toSeq,
			conf.onSuccess.scriptlet
		)
	)

	private def havocAi(id: String) = setAi(id,
		Seq(aiGoto(conf.spawnLocation)) ++ conf.ai.commands: _*
	)

}


object MonsterModule {
	import SteppedModule._

	trait AI {
		def commands: Seq[(String) => Node]
	}
	object DefaultAI extends AI {
		val commands = Seq(
			aiChaseAiShip(50000, 5000),
			aiChasePlayer(7000, 3000),
			aiChaseAnger()
		)
	}
	case class FreeAI(commands: Seq[(String) => Node]) extends AI

	case class Configuration(mayBeName: Option[String],
	                         mayBePrefix: Option[String],
	                         monsters: Set[Monster],
	                         spawnLocation: Location,
	                         ai: AI = DefaultAI,

	                         trigger: Trigger = AutoTrigger,
	                         onBeginning: ChatAndScriptlet = ChatAndScriptlet(),
	                         onSuccess: ChatAndScriptlet = ChatAndScriptlet()) {
		def get = new MonsterModule(this)

		lazy val prefix: String = mayBePrefix.getOrElse(s"module_monster_$randomId")
		lazy val name: String = mayBeName.getOrElse(s"Monster Module")

		def withBeginning(chat: Messages = Messages.Empty, scriptlet: NodeSeq = NodeSeq.Empty) = copy(onBeginning = ChatAndScriptlet(chat, scriptlet))
		def withSuccess(chat: Messages = Messages.Empty, scriptlet: NodeSeq = NodeSeq.Empty) = copy(onSuccess = ChatAndScriptlet(chat, scriptlet))
		def withTrigger(trigger: Trigger): Configuration = this.copy(trigger = trigger)
		def withAI(commands: ((String) => Node)*) = copy(ai = FreeAI(commands))
	}

	def apply(name: Option[String] = None, prefix: Option[String] = None, monsters: Set[Monster], spawnLocation: Location) = Configuration(name, prefix, monsters, spawnLocation)
}
