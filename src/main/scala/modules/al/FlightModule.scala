package modules.al

import artemis.LuxuryLiner
import artemis.Porcelain.{addAi, _}
import lib.SteppedModule.{AutoTrigger, ChatAndScriptlet, Trigger}
import lib._
import modules.al.Chat.Messages
import modules.al.FlightModule._

import scala.xml.{Comment, NodeSeq}

// Spawn a ship that flies to a few waypoints and then disappears.
// This one is your swiss army knife when it comes to letting civil vessels fly.
class FlightModule(conf: Configuration) extends SteppedModule {

	val prefix = conf.prefix
	val name = conf.name
	val trigger = conf.trigger

	require(conf.targets.nonEmpty, "There should at least be one target")

	override def onLoad: NodeSeq = super.onLoad ++ set(varEnabled, 0)

	override def events: NodeSeq = {

		val maxRegularStep = (1 + conf.targets.size) * 100

		super.events ++ Seq[NodeSeq](
			moduleComment,
			step(100, 101,
				conf.ship.create(conf.spawnPoint)
			),
			stepWithChat(101, 200, conf.onBeginning.chat,
				conf.ship.ifCreated,
				conf.ship.afterCreate(),
				conf.onBeginning.scriptlet
			),
			conf.targets.sliding(2).toList.zipWithIndex.map { case (w, idx) =>
				val originTarget = w.head
				val destinationTarget = w.tail.head
				val minStep = (2 + idx) * 100

				destinationTarget match {
					case wayPoint: LocationTarget => flyToTarget(wayPoint, originTarget.location, minStep)
					case wayPoint: StationTarget => flyToTarget(wayPoint, originTarget.location, minStep, addAi(conf.ship.id,
						aiGuardStation()
					))
				}
			},
			stepWithChat(maxRegularStep, 9999, conf.onSuccess.chat,
				destroy(conf.ship.id),
				conf.onSuccess.scriptlet
			),

			stepWithChat(200.until(maxRegularStep), 9999, conf.onDestruction.chat,
				ifNotExists(conf.ship.id),
				conf.onDestruction.scriptlet
			)
		)
	}

	private def flyToTarget(target: Target, origin: Location, minStep: Int, onArrival: NodeSeq = noop): NodeSeq = {
		val wayPoints = if(target.fuzzPath) {
			Location.fuzzyPath(
				one = origin,
				other = target.location,
				minDistance = Math.max(10000, 2 * target.approachDistance),
				maxSegments = 3
			)
		} else {
			Seq(origin, target.location)
		}

		// @TODO: disable the loop if the module is disabled
		val lastStep = target.andThenLoopTo.map(i => (i + 2) * 100).getOrElse(minStep + 100)

		NodeSeq.fromSeq(Seq[NodeSeq](
			wayPoints.sliding(2).toList.zipWithIndex.map { case (org :: wayPoint :: Nil, idx) =>
				val isFirst = idx == 0

				val thisStep = minStep + idx * 10
				val nextStep = thisStep + 10

				if(isFirst) {
					stepWithChat(minStep.to(thisStep), nextStep, target.onDeparture.chat,
						gotoAi(wayPoint),
						target.onDeparture.scriptlet
					)
				} else {
					step(minStep.to(thisStep), nextStep,
						ifDistanceSmaller(conf.ship.id, org, 500),
						gotoAi(wayPoint)
					)
				}
			},
			if (target.onApproach.nonEmpty) {
				stepWithChat(minStep.to(minStep + 70), minStep + 80, target.onApproach.chat,
					ifDistanceSmaller(conf.ship.id, target.location, target.approachDistance),
					target.onApproach.scriptlet
				)
			} else noop,
			stepWithChat(minStep.to(minStep + 80), if(target.hasDelay) minStep + 90 else lastStep, target.onArrival.chat,
				ifDistanceSmaller(conf.ship.id, target.location, 500),
				onArrival,
				target.onArrival.scriptlet,
				if(target.hasDelay) {
					setTimer(varTimer, target.delay)
				} else noop
			),
			if (target.hasDelay) {
				step(minStep + 90, lastStep,
					ifTimer(varTimer)
				)
			} else noop
		))
	}

	private def gotoAi(goto: Location) = setAi(conf.ship.id,
		aiGoto(goto),
		aiAvoidBlackHole(),
		aiFollowCommsOrders()
	)

	private def moduleComment = {
		val totalDistance = conf.targets.sliding(2).foldLeft(0.0) { case (total, from :: to :: Nil) =>
			total + Calculator.distance(from.location, to.location)
		}
		val totalDuration = conf.ship.topSpeed.map { topSpeed =>
			conf.targets.sliding(2).foldLeft(0.0) { case (total, from :: to :: Nil) =>
				total + Calculator.duration(from.location, to.location, topSpeed)
			}
		}

		Comment(
			s"""$getClass ($prefix)
			   |
			   | A ${conf.ship.hull.className} flying around with the following stop points:
			   |
			   |${conf.targets.sliding(2).zipWithIndex.map { case (from :: to :: Nil, idx) =>

				val label = to match {
					case l: LocationTarget => l.location.toString
					case s: StationTarget => s.station.id
				}
				val distance = Calculator.distance(from.location, to.location)
				val duration = conf.ship.topSpeed.map(d => Calculator.speed(distance, d))

				s"  ${idx+1}. $label (${distance.formatted("%.0f")}km" +
				duration.map(d => s", ${d.formatted("%.0f")}s").getOrElse("") + ")" +
				to.andThenLoopTo.map(i => s" -> goto ${i + 1}").getOrElse("")
			}.mkString("\n")}
               |
			   | This adds up to a total distance of ${totalDistance.formatted("%.0f")}km. ${totalDuration.map(d => s"Total flight duration is ${d.formatted("%.0f")}s.").getOrElse("")}
			 """.stripMargin
		)
	}
}

object FlightModule {
	sealed trait Target {
		def onDeparture: ChatAndScriptlet
		def onApproach: ChatAndScriptlet
		def approachDistance: Double
		def onArrival: ChatAndScriptlet
		def delay: Double
		def fuzzPath: Boolean
		def location: Location
		def andThenLoopTo: Option[Int]

		def hasDelay: Boolean = delay > 0.0
	}
	case class LocationTarget(location: Location,
	                          onDeparture: ChatAndScriptlet = ChatAndScriptlet(),
	                          onApproach: ChatAndScriptlet = ChatAndScriptlet(),
	                          approachDistance: Double = 2500,
	                          onArrival: ChatAndScriptlet = ChatAndScriptlet(),
	                          delay: Double = 0.0,
	                          fuzzPath: Boolean = false,
	                          andThenLoopTo: Option[Int] = None
	                           ) extends Target
	case class StationTarget(station: Dockable,
	                         onDeparture: ChatAndScriptlet = ChatAndScriptlet(),
	                         onApproach: ChatAndScriptlet = ChatAndScriptlet(),
	                         approachDistance: Double = 2500,
	                         onArrival: ChatAndScriptlet = ChatAndScriptlet(),
	                         delay: Double = 60.0,
	                         fuzzPath: Boolean = false,
	                         andThenLoopTo: Option[Int] = None
	                          ) extends Target {
		def location = station.location
	}

	case class Configuration(mayBeName: Option[String],
	                         mayBePrefix: Option[String],
	                         spawnPoint: Location,
	                         targets: Seq[Target] = Seq.empty,
	                         trigger: Trigger = AutoTrigger,
	                         mayBeShip: Option[NeutralShip] = None,
	                         onBeginning: ChatAndScriptlet = ChatAndScriptlet(),
	                         onSuccess: ChatAndScriptlet = ChatAndScriptlet(),
	                         onDestruction: ChatAndScriptlet = ChatAndScriptlet()) {
		def get(implicit idRegistry: IdRegistry) = new FlightModule(copy(
			mayBeShip = mayBeShip.orElse(Some(NeutralShip(
				idRegistry.random("T-"),
				Person(),
				LuxuryLiner
			)))
		))

		lazy val prefix: String = mayBePrefix.getOrElse(s"module_flight_${ship.id}")
		lazy val name: String = mayBeName.getOrElse(s"Flight Module ${ship.id}")
		lazy val ship: NeutralShip = mayBeShip.get

		def addLocationTarget(location: Location,
		                      onDeparture: ChatAndScriptlet = ChatAndScriptlet(),
		                      onApproach: ChatAndScriptlet = ChatAndScriptlet(),
		                      approachDistance: Double = 2500,
		                      onArrival: ChatAndScriptlet = ChatAndScriptlet(),
		                      delay: Double = 0.0,
		                      fuzzPath: Boolean = false,
		                      andThenLoopTo: Option[Int] = None): Configuration = addTarget(LocationTarget(location, onDeparture, onApproach, approachDistance, onArrival, delay, fuzzPath, andThenLoopTo))
		def addStationTarget(station: Dockable,
		                     onDeparture: ChatAndScriptlet = ChatAndScriptlet(),
		                     onApproach: ChatAndScriptlet = ChatAndScriptlet(),
		                     approachDistance: Double = 2500,
		                     onArrival: ChatAndScriptlet = ChatAndScriptlet(),
		                     delay: Double = 60.0,
		                     fuzzPath: Boolean = false,
		                     andThenLoopTo: Option[Int] = None): Configuration = addTarget(StationTarget(station, onDeparture, onApproach, approachDistance, onArrival, delay, fuzzPath, andThenLoopTo))
		private def addTarget(wayPoint: Target): Configuration = copy(targets = targets :+ wayPoint)
		def withShip(ship: NeutralShip): Configuration = this.copy(mayBeShip = Some(ship))

		def withBeginning(chat: Messages = Messages.Empty, scriptlet: NodeSeq = NodeSeq.Empty) = copy(onBeginning = ChatAndScriptlet(chat, scriptlet))
		def withSuccess(chat: Messages = Messages.Empty, scriptlet: NodeSeq = NodeSeq.Empty) = copy(onSuccess = ChatAndScriptlet(chat, scriptlet))
		def withDestruction(chat: Messages = Messages.Empty, scriptlet: NodeSeq = NodeSeq.Empty) = copy(onDestruction = ChatAndScriptlet(chat, scriptlet))

		def withTrigger(trigger: Trigger): Configuration = this.copy(trigger = trigger)
	}

	def apply(name: Option[String] = None, prefix: Option[String] = None, spawnPoint: Location) = Configuration(name, prefix, spawnPoint, targets = Seq(LocationTarget(spawnPoint)))

}
