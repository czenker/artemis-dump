package mission.defense.shipyard

import lib._
import mission.defense.shipyard.RaceAgainstTheClock.Configuration
import modules.al.Chat.Messages
import artemis.Porcelain._

import scala.util.Random
import scala.xml.NodeSeq

class RaceAgainstTheClock(conf: Configuration) extends SteppedModule {

	override val name: String = conf.name
	override val prefix: String = conf.prefix
	override protected val trigger: SteppedModule.Trigger = conf.trigger

	val varTimerDocked = s"_${prefix}_docked"
	val varDestructionTimer = s"_${prefix}_destruct_timer"
	val varDestruction = s"_${prefix}_destruct"

	val destructions: Seq[NodeSeq] = Seq(
		damagePlayerBackShield(conf.player.id) ++ damagePlayerTurning(conf.player.id, systems = Seq(0)),
		damagePlayerImpulse(conf.player.id, systems = Seq(0)),
		damagePlayerFrontShield(conf.player.id) ++ damagePlayerTurning(conf.player.id, systems = Seq(1)),
		damagePlayerImpulse(conf.player.id, systems = Seq(1)),
		damagePlayerTactical(conf.player.id, systems = Seq(0, 1)),
		damagePlayerImpulse(conf.player.id, systems = Seq(2)),
		damagePlayerBeam(conf.player.id) ++ damagePlayerTurning(conf.player.id, systems = Seq(2)),
		damagePlayerImpulse(conf.player.id, systems = Seq(3)),
		damagePlayerTorpedo(conf.player.id) ++ damagePlayerTurning(conf.player.id, systems = Seq(3)),
		damagePlayerImpulse(conf.player.id, systems = Seq(4)),
		damagePlayerTactical(conf.player.id, systems = Seq(2, 3)),
		damagePlayerImpulse(conf.player.id, systems = Seq(5))
	)

	override def onLoad: NodeSeq = super.onLoad ++ set(varDestruction, 0)

	override def onDestruct: NodeSeq = super.onDestruct ++ clearCommsButton(conf.textAccept) ++ clearCommsButton(conf.textReady) ++ conf.waypoints.map { waypoint => destroy(waypoint.id) }

	override def events: NodeSeq = {
		val stepForReturn = 200 + conf.waypoints.size * 100

		super.events ++ NodeSeq.fromSeq(Seq[NodeSeq](
			stepWithChat(100, 120, conf.onBeginning.chat,
				set(varDestruction, 0),
				conf.onBeginning.scriptlet
			),
			step(120, 121,
				ifDocked(conf.station.id),
				setCommsButton(conf.textAccept),

				setTimer(varTimerDocked, 1)
			),
			// these two take care we are detecting when player undocks
			step(121, 121,
				ifTimer(varTimerDocked),
				ifDocked(conf.station.id),
				setTimer(varTimerDocked, 1)
			),
			step(121, 120,
				ifTimer(varTimerDocked),
				clearCommsButton(conf.textAccept)
			),
			stepWithChat(120.to(121), 140, conf.onAccept.chat,
				ifDocked(conf.station.id),
				ifCommsButton(conf.textAccept),

				conf.onAccept.scriptlet,

				clearCommsButton(conf.textAccept)
			),
			step(140, 141,
				ifDocked(conf.station.id),
				setCommsButton(conf.textReady),

				setTimer(varTimerDocked, 1)
			),
			// these two take care we are detecting when player undocks
			step(141, 141,
				ifTimer(varTimerDocked),
				ifDocked(conf.station.id),
				setTimer(varTimerDocked, 1)
			),
			step(141, 140,
				ifTimer(varTimerDocked),
				clearCommsButton(conf.textReady)
			),
			stepWithChat(140.to(141), 199, conf.onReady.chat,
				ifDocked(conf.station.id),
				ifCommsButton(conf.textReady),

				conf.onReady.scriptlet,

				clearCommsButton(conf.textReady)
			),
			step(199, 200,
				// @TODO: set timer
				setTimer(varDestructionTimer, 20.0)
			),
			conf.waypoints.zipWithIndex.map { case (waypoint, idx) =>
				val minStep = 200 + idx * 100
				val endStep = minStep + 100

				stepWithChat(minStep, minStep + 10, waypoint.onStartChat,
					createMesh(waypoint.id, waypoint.location, meshFileName = "dat/mine.dxs", textureFileName="dat/mine1.png", color = RGB(255, 255, 255)),
					waypoint.onStartScriptlet
				) ++
				stepWithChat(minStep + 10, minStep + 20, waypoint.onApproachingChat,
					ifDistanceSmaller(conf.player.id, waypoint.location, 7000)
				) ++
				step(minStep + 20, endStep,
					ifDistanceSmaller(conf.player.id, waypoint.location, 300),
					waypoint.onArrivalScriptlet,
					destroy(waypoint.id)
				)
			},
			stepWithChat(stepForReturn, stepForReturn + 50, conf.onReturn.chat,
				conf.onReturn.scriptlet
			),
			stepWithChat(stepForReturn + 50, 9999, conf.onSuccess.chat,
				ifDocked(conf.station.id),
				conf.onSuccess.scriptlet
			),
			destructions.zipWithIndex.map { case(destruction, idx) =>
				val isLast = idx == destructions.length - 1
				Event(
					ifGreaterOrEqual(varStep, 200),
					ifTimer(varDestructionTimer),
					ifEquals(varDestruction, idx),
					new Random().nextInt(4) match {
						case 0 => setProperty(conf.player.id, "pitch", 0.5)
						case 1 => setProperty(conf.player.id, "pitch", -0.5)
						case 2 => setProperty(conf.player.id, "roll ", 0.5)
						case _ => setProperty(conf.player.id, "roll ", -0.5)
					},
					destruction,

					set(varDestruction, if(isLast) 0 else idx + 1),
					setTimer(varDestructionTimer, 10.0)
				)
			}

		))
	}
}

object RaceAgainstTheClock {
	import SteppedModule._
	case class Waypoint(location: Location,
	                    id: String,
	                    onStartChat: Messages = Messages.Empty,
	                    onStartScriptlet: NodeSeq = NodeSeq.Empty,
	                    onApproachingChat: Messages = Messages.Empty,
	                    onArrivalScriptlet: NodeSeq = NodeSeq.Empty
	                   )


	case class Configuration(mayBeName: Option[String] = None,
	                         mayBePrefix: Option[String] = None,
	                         player: Player,
	                         station: Dockable,
	                         textAccept: String,
	                         textReady: String,
	                         waypoints: Seq[Waypoint] = Seq.empty,

	                         trigger: Trigger = AutoTrigger,
	                         onBeginning: ChatAndScriptlet = ChatAndScriptlet(),
	                         onAccept: ChatAndScriptlet = ChatAndScriptlet(),
	                         onReady: ChatAndScriptlet = ChatAndScriptlet(),
	                         onReturn: ChatAndScriptlet = ChatAndScriptlet(),
	                         onSuccess: ChatAndScriptlet = ChatAndScriptlet()) {
		def get = new RaceAgainstTheClock(this)

		lazy val prefix: String = mayBePrefix.getOrElse(s"module_defense_race_$randomId")
		lazy val name: String = mayBeName.getOrElse(s"Race Against the Clock Module")

		def addWaypoint(id: String,
		                location: Location,
		                onStartChat: Messages = Messages.Empty,
		                onStartScriptlet: NodeSeq = NodeSeq.Empty,
		                onApproachingChat: Messages = Messages.Empty,
		                onArrivalScriptlet: NodeSeq = NodeSeq.Empty) = copy(waypoints = waypoints ++ Seq(Waypoint(location, id, onStartChat, onStartScriptlet, onApproachingChat, onArrivalScriptlet)))
		def withBeginning(chat: Messages = Messages.Empty, scriptlet: NodeSeq = NodeSeq.Empty) = copy(onBeginning = ChatAndScriptlet(chat, scriptlet))
		def withAccept(chat: Messages = Messages.Empty, scriptlet: NodeSeq = NodeSeq.Empty) = copy(onAccept = ChatAndScriptlet(chat, scriptlet))
		def withReady(chat: Messages = Messages.Empty, scriptlet: NodeSeq = NodeSeq.Empty) = copy(onReady = ChatAndScriptlet(chat, scriptlet))
		def withReturn(chat: Messages = Messages.Empty, scriptlet: NodeSeq = NodeSeq.Empty) = copy(onReturn = ChatAndScriptlet(chat, scriptlet))
		def withSuccess(chat: Messages = Messages.Empty, scriptlet: NodeSeq = NodeSeq.Empty) = copy(onSuccess = ChatAndScriptlet(chat, scriptlet))

		def withTrigger(trigger: Trigger): Configuration = this.copy(trigger = trigger)
	}

	def apply(player: Player, station: Dockable, textAccept: String, textReady: String) = {
		Configuration(player = player, station = station, textAccept = textAccept, textReady = textReady)
	}
}