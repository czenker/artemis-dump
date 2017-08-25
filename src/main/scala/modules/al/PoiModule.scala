package modules.al
import PoiModule._
import lib._
import modules.al.Chat.{Message, Messages}
import artemis.Porcelain._

import scala.xml.NodeSeq


class PoiModule(conf: Configuration) extends SteppedModule {

	override val name: String = conf.name
	override val prefix: String = conf.prefix
	override protected val trigger: SteppedModule.Trigger = conf.trigger

	val pois = conf.pois.toList

	val varDiscoveries = pois.indices.map(i => s"_${prefix}_poi$i")
	val varTotalDiscoveries = s"_${prefix}_total"

	override def onLoad: NodeSeq = super.onLoad ++ set(varTotalDiscoveries, 0) ++ varDiscoveries.map(s => set(s, 0))

	override def events: NodeSeq = super.events ++
	stepWithChat(100, 101, conf.onBeginning.chat,
		set(varTotalDiscoveries, 0),
		varDiscoveries.map(s => set(s, 0)),
		conf.onBeginning.scriptlet
	) ++
	pois.zip(varDiscoveries).map { case (poi, variable) =>
		val condition: NodeSeq = poi match {
			case SpherePoi(location, radius, _, _) => ifInsideSphere(conf.player.id, location, radius)
			case RectanglePoi(minX, maxX, minZ, maxZ, _, _) => ifInsideBox(conf.player.id, minX, maxX, minZ, maxZ)
			case StationPoi(dockable, _, _) => ifDocked(dockable.id)
		}

		Event(
			ifEquals(variable, 0),
			condition,
			poi.message.map(msg => sendComms(msg.from, msg.message, msg.commsType)).getOrElse(noop),
			poi.scriptlet,
			set(varTotalDiscoveries, s"$varTotalDiscoveries + 1"),
			set(variable, 1)
		).toNodeSeq
	} ++
	stepWithChat(101, 9999, conf.onSuccess.chat,
		ifEquals(varTotalDiscoveries, conf.pois.size),
		conf.onSuccess.scriptlet
	)
}

object PoiModule {
	import lib.SteppedModule._

	sealed trait Poi {
		def message: Option[Message]
		def scriptlet: NodeSeq
	}
	case class SpherePoi(location: Location, radius: Double, message: Option[Message], scriptlet: NodeSeq) extends Poi
	case class RectanglePoi(minX: Double, maxX: Double, minZ: Double, maxZ: Double, message: Option[Message], scriptlet: NodeSeq) extends Poi
	case class StationPoi(dockable: Dockable, message: Option[Message], scriptlet: NodeSeq) extends Poi

	case class Configuration(mayBeName: Option[String] = None,
	                         mayBePrefix: Option[String] = None,
	                         player: Player,
	                         pois: Set[Poi] = Set.empty,

	                         trigger: Trigger = AutoTrigger,
	                         onBeginning: ChatAndScriptlet = ChatAndScriptlet(),
	                         onSuccess: ChatAndScriptlet = ChatAndScriptlet()) {
		def get = new PoiModule(this)

		lazy val prefix: String = mayBePrefix.getOrElse(s"module_poi_$randomId")
		lazy val name: String = mayBeName.getOrElse(s"Poi Module")

		def withBeginning(chat: Messages = Messages.Empty, scriptlet: NodeSeq = NodeSeq.Empty) = copy(onBeginning = ChatAndScriptlet(chat, scriptlet))
		def withSuccess(chat: Messages = Messages.Empty, scriptlet: NodeSeq = NodeSeq.Empty) = copy(onSuccess = ChatAndScriptlet(chat, scriptlet))
		def addSpherePoi(location: Location, radius: Double, chat: Option[Message], scriptlet: NodeSeq = NodeSeq.Empty) = addPoi(SpherePoi(
			location, radius, chat, scriptlet
		))
		def addRectanglePoi(minX: Double, maxX: Double, minZ: Double, maxZ: Double, chat: Option[Message], scriptlet: NodeSeq = NodeSeq.Empty) = addPoi(RectanglePoi(
			minX, maxX, minZ, maxZ, chat, scriptlet
		))
		def addStationPoi(dockable: Dockable, chat: Option[Message], scriptlet: NodeSeq = NodeSeq.Empty) = addPoi(StationPoi(
			dockable, chat, scriptlet
		))

		private def addPoi(poi: Poi) = copy(pois = pois + poi)

		def withTrigger(trigger: Trigger): Configuration = this.copy(trigger = trigger)
	}

	def apply(player: Player) = Configuration(player = player)
}
