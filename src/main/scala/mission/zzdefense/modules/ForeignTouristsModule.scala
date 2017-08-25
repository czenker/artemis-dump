package mission.zzdefense.modules

import artemis.LuxuryLiner
import lib._

import scala.xml.{Comment, NodeSeq}
import artemis.Porcelain._

// foreign tourists flying from gate to station and back
class ForeignTouristsModule(idRegistry: IdRegistry,
                            prefix: String = "module_tourists",
                            creditsPerShuttle: Long = 1500,
                            secondsPerShuttle: Long = 360,
                            numberOfShuttles: Int = 4,
                            gate: Gate,
                            station: Station) extends Module {

	val varEnabled = s"${prefix}_enable"

	private val minTourists: Long = 10
	private val maxTourists: Long = 30
	private val minCharge: Long = Math.round(creditsPerShuttle / (maxTourists + minTourists) * 2 * 0.7 * 2/3)
	private val maxCharge: Long = Math.round(creditsPerShuttle / (maxTourists + minTourists) * 2 * 1.3 * 2/3)

	// statistically speaking how often a shuttle should dock
	private val loopTime: Long = secondsPerShuttle * numberOfShuttles

	private val dockTime: Long = loopTime / 10
	private val flyTime: Long = loopTime / 3
	private val remoteTime: Long = loopTime - 2 * flyTime - dockTime

	private val distance = Calculator.distance(gate.location, station.location)
	private val speed = Calculator.speed(gate.location, station.location, flyTime)

	assert(speed < 2.0, s"The ship should not have super human speed. $speed")

	override def onLoad: NodeSeq = set(varEnabled, 0) ++ Range(0, numberOfShuttles).flatMap(startForShuttle)

	override def events: NodeSeq = Comment(
		s"""$getClass ($prefix)
		   |
		   | Sending $numberOfShuttles shuttles from ${gate.name}(${gate.id}) to ${station.name}(${station.id}) and back.
		   |
		   | $minTourists - $maxTourists tourists per ship
		   | $minCharge - $maxCharge credits per tourist
		   |
		   | $loopTime seconds total loop time
		   |   $flyTime seconds fly time
		   |   $dockTime seconds docked
		   |   $remoteTime seconds in gate
		   |
		   | Distance between locations is ${distance.formatted("%.0f")}km, so speed is set to ${speed.formatted("%.4f")}.
		   |
		 """.stripMargin) ++ Range(0, numberOfShuttles).flatMap(eventsForShuttle)

	def startForShuttle(shuttleNumber: Int): NodeSeq = {
		val prefix = s"${this.prefix}_$shuttleNumber"
		val varStep = s"_${prefix}_step"

		set(varStep, 0)
	}

	def eventsForShuttle(shuttleNumber: Int): NodeSeq = {
		val id = idRegistry.random("T-")

		val prefix = s"${this.prefix}_$shuttleNumber"
		val varStep = s"_${prefix}_step"
		val varTimer = s"_${prefix}_timer"
		val varDelay = s"_${prefix}_delay"
		val varTourists = s"_${prefix}_tourists"
		val varCharge = s"_${prefix}_charge"
		val varChargeTotal = s"_${prefix}_charge_total"

		Seq[NodeSeq](
			Comment(s"$prefix: Shuttle $id"),
			Event(
				s"Initial delay for shuttle $shuttleNumber",
				ifEquals(varEnabled, 1),
				ifEquals(varStep, 0),
				setTimer(varTimer, shuttleNumber * secondsPerShuttle),
				set(varStep, 99)
			),
			Event(
				s"Looping delay for shuttle $shuttleNumber",
				ifEquals(varEnabled, 1),
				ifEquals(varStep, 1),
				setTimer(varTimer, varDelay),
				set(varStep, 99)
			),
			Event(
				s"spawn shuttle $shuttleNumber",
				ifEquals(varStep, 99),
				ifTimer(varTimer),

				setRandomInt(varTourists, minTourists, maxTourists),
				setRandomInt(varCharge, minCharge, maxCharge),
				setInt(varChargeTotal, s"$varTourists * $varCharge"),

				createNeutral(id, LuxuryLiner, gate.location),

				set(varStep, 100)
			),
			Event(
				s"shuttle $shuttleNumber trip to target",
				ifEquals(varStep, 100),
				ifExists(id),

				setSpecial(id, -1, -1),
				setProperty(id, "topSpeed", speed.toString),
				setProperty(id, "turnRate", "0.004"),
				setShipText(id,
					desc = s"A shuttle that brings |$varTourists| tourists to HQ.",
					hailtext = s"We are bringing |$varTourists| tourists to HQ. Is our course correct?"
				),

				setAi(id,
					aiGoto(station.location),
					aiAvoidBlackHole(),
					aiFollowCommsOrders()
				),

				set(varStep, 101)
			),
			Event(
				ifEquals(varStep, 101),
				ifDistanceSmaller("HQ", id, 5000),
				sendComms(id,
					s"""This is $id requesting docking permissions to HQ. We carry |$varTourists| tourists
					   |who came here to buy our finest goods, like carpets and ugly cats.""".stripMargin,
					CommsType.Side
				),
				setTimer(varTimer, 5),
				set(varStep, 200)
			),
			Event(
				ifEquals(varStep, 200),
				ifTimer(varTimer),
				sendComms(
					"HQ",
					s"""$id, nice to have you back at our station. We are preparing Docking Bay 12 for your arrival.""",
					CommsType.Station),
				set(varStep, 201)
			),
			Event(
				ifEquals(varStep, 201),
				ifDistanceSmaller("HQ", id, 800),
				sendComms(id, "Initiating docking maneuver.", CommsType.Side),
				addAi(id,
					aiGuardStation()
				),

				setTimer(varTimer, 5),
				set(varStep, 300)
			),
			Event(
				ifEquals(varStep, 300),
				ifTimer(varTimer),
				sendComms("HQ",
					s"""Welcome onboard HQ, $id. Have a nice stay.""",
					CommsType.Station),

				setShipText(id,
					desc = s"A shuttle waiting at HQ to carry pessengers back to Sirius Alpha.",
					hailtext = s"We are boarding passengers for a flight to Sirius Alpha."
				),
				set(EconomyModule.varCredits, s"${EconomyModule.varCredits} + $varChargeTotal"),

				setTimer(varTimer, dockTime),
				set(varStep, 400)
			),
			Event(
				ifEquals(varStep, 400),
				ifTimer(varTimer),

				setRandomInt(varTourists, minTourists, maxTourists),
				setRandomFloat(varCharge, minCharge, maxCharge),
				setInt(varChargeTotal, s"$varTourists * $varCharge / 2"),

				setShipText(id,
					desc = s"A shuttle that brings |$varTourists| tourists to Sirius Alpha.",
					hailtext = s"We are bringing |$varTourists| tourists to Sirius Alpha."
				),

				setAi(id,
					aiGoto(gate.location),
					aiAvoidBlackHole(),
					aiFollowCommsOrders()
				),

				set(varStep, 500)
			),
			Event(
				ifEquals(varStep, 500),
				ifDistanceSmaller(id, gate.location, 2000),
				sendComms(id, "Initiating hyperjump to Sirius Alpha.", CommsType.Side),

				set(EconomyModule.varCredits, s"${EconomyModule.varCredits} + $varChargeTotal"),
				destroy(id),
				set(varStep, 1),
				setRandomFloat(varDelay, remoteTime * 0.7, remoteTime * 1.3)
			),
			Event(
				ifGreaterOrEqual(varStep, 101),
				ifSmallerOrEqual(varStep, 500),
				ifNotExists(id),

				sendComms("HQ", s"""We lost signal of $id from our radar. What a tragedy...""", CommsType.Station),

				set(varStep, 1),
				// if the ship is destroyed it will pause for one loop
				setRandomFloat(varDelay, remoteTime * 0.7 + loopTime, remoteTime * 1.3 + loopTime)
			)
		)
	}
}
