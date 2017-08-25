package modules.al

import artemis.LuxuryLiner
import artemis.Porcelain._
import lib.SteppedModule.{AutoTrigger, Trigger}
import lib._
import modules.al.Chat.Messages
import modules.al.ShuttleModule._

import scala.xml.{Comment, NodeSeq}

@deprecated ("Transport Module is more flexible")
class ShuttleModule(conf: Configuration) extends SteppedModule {

	val prefix = conf.prefix
	val name = conf.name
	val trigger = conf.trigger
	override protected val loopedModule = false

	private val dockTime: Int = conf.loopTime / 10
	private val flyTime: Int = (conf.loopTime - dockTime) / 2

	// using waypoints, so that not every ship takes the exact same route
	private val spawnPoint = conf.from.location.fuzz(500)
	private val waypoint1 = Location.between(spawnPoint, conf.to.location, 0.333).fuzz(5000)
	private val waypoint2 = Location.between(waypoint1, conf.to.location).fuzz(5000)
	private val dockPoint = conf.to.location.atDistance(300)

	private val distance = Calculator.distance(spawnPoint, waypoint1) + Calculator.distance(waypoint1, waypoint2) + Calculator.distance(waypoint2, conf.to.location)
	private val speed = Calculator.speed(distance, flyTime)

	assert(speed < 2.0, s"The ship should not have super human speed. $speed")

	val ship = conf.ship.copy(topSpeed = Some(speed))

	override def onLoad: NodeSeq = super.onLoad ++ set(varEnabled, 0)

	override def events: NodeSeq = super.events ++ Seq[NodeSeq](
		Comment(
			s"""$getClass ($prefix)
			   |
			   | Sending a ${ship.hull.className} from ${conf.from.name} to ${conf.to.id} and back.
			   |
			   | ${conf.loopTime} seconds total loop time
			   |   $flyTime seconds fly time
			   |   $dockTime seconds docked
			   |
			   | Distance between locations is ${distance.formatted("%.0f")}km, so speed is set to ${speed.formatted("%.4f")}.
			 """.stripMargin
		),
		step(100, 101,
			ship.create(spawnPoint)
		),
		stepWithChat(101, 110, conf.comms.onStart,
			ship.afterCreate(),
			gotoAi(waypoint1),
			conf.events.onStart
		),
		step(101.to(110), 120,
			ifDistanceSmaller(ship.id, waypoint1, 500),
			gotoAi(waypoint2)
		),
		step(101.to(120), 130,
			ifDistanceSmaller(ship.id, waypoint2, 500),
			gotoAi(dockPoint)
		),
		stepWithChat(101.to(130), 140, conf.comms.onClosingStation,
			ifDistanceSmaller(ship.id, conf.to.location, 5000),
			gotoAi(dockPoint)
		),
		stepWithChat(101.to(140), 200, conf.comms.onDocking,
			ifDistanceSmaller(ship.id, dockPoint, 500),
			addAi(ship.id,
				aiGuardStation()
			),
			conf.events.onDocking,
			setTimer(varTimer, dockTime)
		),
		stepWithChat(200, 300, conf.comms.onUndocking,
			ifTimer(varTimer),
			conf.events.onUndocking,
			gotoAi(waypoint2)
		),
		step(300, 301,
			ifDistanceSmaller(ship.id, waypoint2, 500),
			gotoAi(waypoint1)
		),
		step(300.to(301), 302,
			ifDistanceSmaller(ship.id, waypoint1, 500),
			gotoAi(conf.from.location)
		),
		stepWithChat(300.to(302), 303, conf.comms.onClosingGate,
			ifDistanceSmaller(ship.id, conf.from.location, 5000),
			gotoAi(conf.from.location)
		),
		stepWithChat(300.to(303), 9999, conf.comms.onLeaving,
			ifDistanceSmaller(ship.id, conf.from.location, 500),
			conf.events.onLeaving,
			destroy(ship.id)
		),
		stepWithChat(102.to(303), 9999, conf.comms.onDestruction,
			ifNotExists(ship.id),
			conf.events.onDestruction
		)
	)

	private def gotoAi(goto: Location) = setAi(ship.id,
		aiGoto(goto),
		aiAvoidBlackHole(),
		aiFollowCommsOrders()
	)
}

object ShuttleModule {

	case class Comms(onStart: Messages = Messages.Empty,
	                 onClosingStation: Messages = Messages.Empty,
	                 onDocking: Messages = Messages.Empty,
	                 onUndocking: Messages = Messages.Empty,
	                 onClosingGate: Messages = Messages.Empty,
	                 onLeaving: Messages = Messages.Empty,
	                 onDestruction: Messages = Messages.Empty)

	case class Events(onStart: NodeSeq = NodeSeq.Empty,
	                  onDocking: NodeSeq = NodeSeq.Empty,
	                  onUndocking: NodeSeq = NodeSeq.Empty,
	                  onLeaving: NodeSeq = NodeSeq.Empty,
	                  onDestruction: NodeSeq = NodeSeq.Empty)

	case class Configuration(mayBeName: Option[String],
	                         mayBePrefix: Option[String],
	                         from: NamedLocation,
	                         to: Dockable,
	                         trigger: Trigger = AutoTrigger,
	                         loopTime: Int = 1200,
	                         mayBeShip: Option[NeutralShip] = None,
	                         comms: Comms = Comms(),
	                         events: Events = Events()) {
		def get(implicit idRegistry: IdRegistry) = new ShuttleModule(copy(
			mayBeShip = mayBeShip.orElse(Some(NeutralShip(
				idRegistry.random("T-"),
				Person(),
				LuxuryLiner
			)))
		))

		lazy val prefix: String = mayBePrefix.getOrElse(s"module_shuttle_${ship.id}")
		lazy val name: String = mayBeName.getOrElse(s"Shuttle Module ${ship.id}")
		lazy val ship: NeutralShip = mayBeShip.get


		def withComms(onStart: Messages = Messages.Empty,
		              onClosingStation: Messages = Messages.Empty,
		              onDocking: Messages = Messages.Empty,
		              onUndocking: Messages = Messages.Empty,
		              onClosingGate: Messages = Messages.Empty,
		              onLeaving: Messages = Messages.Empty,
		              onDestruction: Messages = Messages.Empty): Configuration = this.copy(comms = Comms(
			onStart = onStart,
			onClosingStation = onClosingStation,
			onDocking = onDocking,
			onUndocking = onUndocking,
			onClosingGate = onClosingGate,
			onLeaving = onLeaving,
			onDestruction = onDestruction
		))

		def withEvents(onStart: NodeSeq = NodeSeq.Empty,
		               onDocking: NodeSeq = NodeSeq.Empty,
		               onUndocking: NodeSeq = NodeSeq.Empty,
		               onLeaving: NodeSeq = NodeSeq.Empty,
		               onDestruction: NodeSeq = NodeSeq.Empty): Configuration = this.copy(events = Events(
			onStart = onStart,
			onDocking = onDocking,
			onUndocking = onUndocking,
			onLeaving = onLeaving,
			onDestruction = onDestruction
		))

		def withShip(ship: NeutralShip): Configuration = this.copy(mayBeShip = Some(ship))

		def withTrigger(trigger: Trigger): Configuration = this.copy(trigger = trigger)
	}

	@deprecated ("Transport Module is more flexible")
	def apply(name: Option[String] = None, prefix: Option[String] = None, from: NamedLocation, to: Dockable) = Configuration(name, prefix, from, to)

}
