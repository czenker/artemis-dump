package mission.zzdefense.modules

import artemis.LuxuryLiner
import artemis.Porcelain._
import lib._

import scala.xml.{Comment, NodeSeq}

class BlackHoleShuttleModule(idRegistry: IdRegistry,
                             station: Station,
                             blackHole: BlackHole,
                             prefix: String = "module_bhshuttle",
                             creditsPerLoop: Long = 5000,
                             loopTime: Long = 900
                ) extends Module {

	val varEnabled = s"${prefix}_enable"
	val varStep = s"_${prefix}_step"
	val varTimer = s"_${prefix}_timer"
	val varTourists = s"_${prefix}_tourists"
	val varCharge = s"_${prefix}_charge"
	val varChargeTotal = s"_${prefix}_charge_total"

	private val dockTime: Long = loopTime / 6
	private val flyTime: Long = loopTime / 3

	val minDistance: Long = 4000

	private val distance = Calculator.distance(station.location, blackHole.location) - 4000
	private val speed = Calculator.speed(distance, flyTime)

	private val minTourists: Long = 10
	private val maxTourists: Long = 30
	private val minCharge: Long = Math.round(creditsPerLoop / (maxTourists + minTourists) * 2 * 0.7 * 2/3)
	private val maxCharge: Long = Math.round(creditsPerLoop / (maxTourists + minTourists) * 2 * 1.3 * 2/3)

	private val id = idRegistry.random("T-")
	private val description = "An unarmed vehicle for comfortable travelling."

	assert(speed > 1.0, s"If the ship is too slow it can't escape gravity. $speed")
	assert(speed < 2.0, s"The ship should not have super human speed. $speed")

	override def onLoad: NodeSeq = set(varEnabled, 0) ++ set(varStep, 0)


	override def events: NodeSeq = Seq[NodeSeq](
		Comment(
			s"""$getClass ($prefix)
			   |
			   | A transport starting at ${station.name} and taking a tour to ${blackHole.name}.
			   |
			   | $minTourists - $maxTourists tourists per ship
			   | $minCharge - $maxCharge credits per tourist
			   |
			   | $loopTime seconds total loop time
			   |   $flyTime seconds fly time
			   |   $dockTime seconds docked
			   |
			   | Distance between locations is ${distance.formatted("%.0f")}km, so speed is set to ${speed.formatted("%.4f")}.
			   |""".stripMargin
		),
		Event(
			s"${prefix}_init",
			ifEquals(varEnabled, 1),

			setTimer(varTimer, 1),
			setStep(1),

			set(varEnabled, 2)
		),
		Event(
			s"${prefix}_spawn",
			ifStep(1),
			ifTimer(varTimer),

			createNeutral(id, LuxuryLiner, station.location),

			setStep(2)
		),
		Event(
			s"${prefix}_waitspawn",
			ifStep(2),
			ifExists(id),

			setSpecial(id, -1, -1),
			setProperty(id, "topSpeed", speed.toString),
			setProperty(id, "turnRate", "0.006"),

			set(varStep, 100)
		),

		// loop
		Event(
			s"${prefix}_100",
			ifStep(100),
			setAi(id,
				aiGoto(station.location),
				aiGuardStation()
			),
			setShipText(id,
				hailtext = s"We are currently boarding passengers for a flight to ${blackHole.name}.",
				desc = description + " The captain is currently boarding passengers."
			),
			setTimer(varTimer, dockTime / 2),
			setStep(200)
		),
		Event(
			ifStep(200),
			ifTimer(varTimer),

			setRandomInt(varTourists, minTourists, maxTourists),
			setRandomInt(varCharge, minCharge, maxCharge),
			setInt(varChargeTotal, s"$varTourists * $varCharge"),

			sendComms(id, s"Boarding |$varTourists| passengers on board of $id is finished. We are ready for take off.", CommsType.Side),

			setStep(201),
			setTimer(varTimer, 10)
		),
		Event(
			ifStep(201),
			ifTimer(varTimer),

			sendComms(station.id, s"You are free to go, $id. May the gravitational force not be with you.", CommsType.Station),

			setStep(202),
			setTimer(varTimer, 7)
		),
		Event(
			ifStep(202),
			ifTimer(varTimer),

			setAi(id,
				aiGoto(blackHole.location),
				aiAvoidBlackHole(minDistance),
				aiFollowCommsOrders()
			),

			sendComms(id, s"Let's get going then. Hopefully everything turns out fine.", CommsType.Side),

			setShipText(id,
				hailtext = s"We are on our way to ${blackHole.name}.",
				desc = description + s" |$varTourists| tourists on their way to ${blackHole.name}."
			),

			setStep(300)
		),
		Event(
			ifStep(300),
			ifDistanceSmaller(blackHole.id, id, minDistance + 1000),
			sendComms(id,
				s"Brace yourself. We are entering the gravitational field of the black hole.",
				CommsType.Side
			),
			setShipText(id,
				hailtext = s"Hah, time runs slower for us, you fools.",
				desc = description + s" It's inside the gravitation of ${blackHole.name}. If the signal gets lost it means the ship crossed the event horizon and the crew is most likely dead."
			),
			setStep(301)
		),
		Event(
			ifStep(301),
			ifDistanceSmaller(blackHole.id, id, minDistance + 1000),
			setTimer(varTimer, dockTime),
			setStep(302)
		),
		Event(
			ifStep(302),
			ifDistanceGreater(blackHole.id, id, minDistance + 1000),
			setStep(301)
		),
		Event(
			ifStep(302),
			ifTimer(varTimer),

			setAi(id,
				aiGoto(station.location),
				aiAvoidBlackHole(minDistance),
				aiFollowCommsOrders()
			),

			sendComms(id,
				s"That was fun, wasn't it? We are returning to ${station.name}.",
				CommsType.Side
			),
			setShipText(id,
				hailtext = s"I always feel younger after flying close to a black hole",
				desc = description + s" The ship is flying back to ${station.name} with |$varTourists|."
			),

			setStep(400)
		),
		Event(
			ifStep(400),
			ifDistanceSmaller(station.id, id, 5000),
			sendComms(station.id,
				s"Hey, look. $id is still alive. Should we reserve you a docking bay, you youngsters?",
				CommsType.Station
			),
			setTimer(varTimer, 5),
			setStep(401)
		),
		Event(
			ifStep(401),
			ifTimer(varTimer),
			sendComms(id,
				s"Yay, come on. That would be fly.",
				CommsType.Side),
			setTimer(varTimer, 5),
			setStep(402)
		),
		Event(
			ifStep(402),
			ifTimer(varTimer),
			sendComms(station.id,
				s"Stop talking as if you got younger. You just aged slower than me.",
				CommsType.Station),
			setStep(403)
		),
		Event(
			ifStep(403),
			ifDistanceSmaller(station.id, id, 800),

			sendComms(id,
				s"Let's see if a still remember how to dock on at ${station.name}.",
			CommsType.Station),
			addAi(id, aiGuardStation()),

			setTimer(varTimer, 10),
			setStep(404)
		),
		Event(
			ifStep(404),
			ifTimer(varTimer),

			setInt(EconomyModule.varCredits, s"${EconomyModule.varCredits} + $varChargeTotal"),

			setStep(100)
		),

		// if ship was destroyed
		Event(
			s"${prefix}_respawn_timer",
			ifEnabled,
			ifGreaterOrEqual(varStep, 100),
			ifNotExists(id),

			sendComms(station.id, s"Wow! $id got lost. Have they really entered the event horizon of the black hole?", CommsType.Station),

			setTimer(varTimer, 2 * loopTime),
			set(varStep, 1)
		)
	)

	private def ifEnabled = ifEquals(varEnabled, 2)
	private def ifStep(step: Long) = ifEquals(varStep, step)
	private def setStep(step: Long) = set(varStep, step)
}
