package mission.zzdefense.modules

import artemis.BulkCargo
import artemis.Porcelain._
import lib._

import scala.xml.{Comment, NodeSeq}

class MineTransportModule(idRegistry: IdRegistry,
                          mineModule: MineModule,
                          factoryModule: FactoryModule,
                          prefix: String = "module_mine_transport",
                          orePerLoop: Long = 100,
                          loopTime: Long = 600
                ) extends Module {

	val mine = mineModule.station
	val factory = factoryModule.station

	val varEnabled = s"${prefix}_enable"
	val varStep = s"${prefix}_step"
	val varAmount = s"${prefix}_loaded"
	val varTimer = s"${prefix}_timer"
	val varMineAmount = mineModule.varAmount

	private val dockTime: Long = loopTime / 6
	private val flyTime: Long = loopTime / 3

	private val distance = Calculator.distance(mine.location, factory.location)
	private val speed = Calculator.speed(mine.location, factory.location, flyTime)

	private val id = idRegistry.random("T-")
	private val description = "An unarmed bulk freighter moving ore."

	assert(orePerLoop <= factoryModule.maxOre, "The transport should not carry more than the factory can store")
	assert(orePerLoop <= mineModule.maxOre, "The transport should not carry more than the mine can store")
	assert(speed < 2.0, s"The ship should not have super human speed. $speed")

	override def onLoad: NodeSeq = set(varEnabled, 0) ++ set(varAmount, 0) ++ set(varStep, 0)


	override def events: NodeSeq = Seq[NodeSeq](
		Comment(
			s"""$getClass ($prefix)
			   |
			   | Sending a transport to get ore from ${mine.name} to ${factory.name}.
			   |
			   | $loopTime seconds total loop time
			   |   $flyTime seconds fly time
			   |   $dockTime seconds docked

			   | Distance between locations is ${distance.formatted("%.0f")}km, so speed is set to ${speed.formatted("%.4f")}.
			   |""".stripMargin
		),
		Event(
			s"${prefix}_init",
			ifEquals(varEnabled, 1),

			setTimer(varTimer, 1),
			set(varStep, 0),

			set(varEnabled, 2)
		),
		Event(
			s"${prefix}_spawn",
			ifEquals(varStep, 0),
			ifTimer(varTimer),

			createNeutral(id, BulkCargo, mine.location),

			set(varStep, 1)
		),
		Event(
			s"${prefix}_waitspawn",
			ifEquals(varStep, 1),
			ifExists(id),

			setSpecial(id, -1, -1),
			setProperty(id, "topSpeed", speed.toString),
			setProperty(id, "turnRate", "0.004"),

			setShipText(id, desc = description),

			setAi(id,
				aiGoto(mine.location),
				aiAvoidBlackHole(),
				aiFollowCommsOrders(),
				aiGuardStation()
			),

			set(varStep, 100)
		),
		Event(
			s"${prefix}_load_start",
			ifStep(100),
			ifGreaterOrEqual(varMineAmount, orePerLoop),

			setShipText(id,
				hailtext = s"We are currently loaded to bring $orePerLoop ore to ${factory.name}.",
				desc = s"$description It is currently loaded."
			),

			setTimer(varTimer, dockTime),
			setStep(101)
		),
		Event(
			// if ore in mine drops during loading
			s"${prefix}_load_abort",
			ifStep(101),
			ifSmaller(varMineAmount, orePerLoop),
			setShipText(id,
				hailtext = s"It's a little boring at ${mine.name}. We are waiting for the next shipment of ore to be produced.",
				desc = description
			),

			setStep(100)
		),
		Event(
			s"${prefix}_load_success",
			ifStep(101),
			ifTimer(varTimer),
			ifGreaterOrEqual(varMineAmount, orePerLoop),

			set(varMineAmount, s"$varMineAmount - $orePerLoop"),

			sendComms(mine.id, s"$id, loading is completed and we are prepared for undocking.", CommsType.Station),

			setTimer(varTimer, 5),
			setStep(200)
		),
		Event(
			ifStep(200),
			ifTimer(varTimer),

			sendComms(id, s"Thanks. We are off to ${factory.name}.", CommsType.Side),
			setAi(id,
				aiGoto(factory.location),
				aiAvoidBlackHole(),
				aiFollowCommsOrders()
			),

			setShipText(id,
				hailtext = s"We are bringing $orePerLoop ore from ${mine.name} to ${factory.name}.",
				desc = s"$description It is transporting $orePerLoop ore from ${mine.name} to ${factory.name}."
			),

			setStep(201)
		),
		Event(
			ifStep(201),
			ifDistanceSmaller(factory.id, id, 5000),
			sendComms(id,
				s"This is $id requesting docking permissions to ${factory.id}. We are carrying ore to be sold at your station.",
				CommsType.Side
			),
			setTimer(varTimer, 5),
			setStep(300)
		),
		Event(
			ifStep(300),
			ifTimer(varTimer),
			sendComms(
				factory.id,
				s"$id, nice to have you back at our station. We are preparing Docking Bay 12 for your arrival.",
				CommsType.Station),
			setStep(301)
		),
		Event(
			ifStep(301),
			ifDistanceSmaller(factory.id, id, 800),
			sendComms(id, "Initiating docking maneuver.", CommsType.Side),
			addAi(id,
				aiGuardStation()
			),

			setTimer(varTimer, 5),
			setStep(302)
		),
		Event(
			ifStep(302),
			ifTimer(varTimer),
			sendComms(factory.id,
				s"Welcome onboard ${factory.name}, $id. Have a nice stay.",
				CommsType.Station),

			// @TODO
//			set(EconomyModule.varCredits, s"${EconomyModule.varCredits} + $varChargeTotal"),

			setShipText(id,
				hailtext = s"This is an automatic response. The crew is currently selling ore at the local market.",
				desc = s"$description Its cargo is currently sold at ${factory.name}."
			),
			setTimer(varTimer, dockTime / 10),
			setStep(303)
		),
		Event(
			ifStep(303),
			ifTimer(varTimer),
			ifSmallerOrEqual(factoryModule.varOre, factoryModule.maxOre - orePerLoop),
			set(factoryModule.varOre, s"${factoryModule.varOre} + $orePerLoop"),

			setTimer(varTimer, dockTime * 9 / 10),
			setStep(400)
		),
		Event(
			ifEnabled,
			ifStep(400),
			ifTimer(varTimer),

			sendComms(id,
				s"We made a fine amount of credits. We are heading back to ${mine.name} to bring you some more ore. You guys seem to be eager to get some.",
				CommsType.Side),

			setTimer(varTimer, 5),
			setStep(401)
		),
		Event(
			ifStep(401),
			ifTimer(varTimer),

			sendComms(factory.id,
				s"Have a safe trip home, $id. And bring us some more of this fine ore...",
				CommsType.Station),

			setAi(id,
				aiGoto(mine.location),
				aiAvoidBlackHole(),
				aiFollowCommsOrders()
			),

			setShipText(id,
				hailtext = s"We are flying back to ${mine.name} to take a break.",
				desc = s"$description It is on its way to ${mine.id}."
			),

			setStep(500)
		),
		Event(
			ifStep(500),
			ifDistanceSmaller(mine.id, id, 5000),
			sendComms(id,
				s"${mine.name}, we are back from our trip to ${factory.name} and need a shower and a dry place to sleep. The trip was rather rough.",
				CommsType.Side
			),
			setTimer(varTimer, 5),
			setStep(501)
		),
		Event(
			ifStep(501),
			ifTimer(varTimer),
			sendComms(
				mine.id,
				s"Hah. I fear you have to go to your own chamber then, $id. But we will wait for you at the docking bay.",
				CommsType.Station),
			setStep(502)
		),
		Event(
			ifStep(502),
			ifDistanceSmaller(mine.id, id, 800),
			sendComms(id, "This smells like home. Approaching the docking bay now.", CommsType.Side),
			addAi(id,
				aiGuardStation()
			),

			setTimer(varTimer, 5),
			setStep(503)
		),
		Event(
			ifStep(503),
			ifTimer(varTimer),
			sendComms(mine.id,
				s"Welcome back at ${mine.name}, $id. Be quick with your break, we'll be loading ores right away.",
				CommsType.Station),

			//			set(EconomyModule.varCredits, s"${EconomyModule.varCredits} + $varChargeTotal"),

			setShipText(id,
				hailtext = s"It's a little boring at ${mine.name}. We are waiting for the next shipment of ore to be loaded.",
				desc = description
			),

			setStep(100)
		),

		// if ship was destroyed
		Event(
			s"${prefix}_respawn_timer",
			ifEnabled,
			ifGreaterOrEqual(varStep, 100),
			ifNotExists(id),

			sendComms(mine.id, s"$id! $id! Please reply. Do you copy?... ... That's not a good sign at all...", CommsType.Station),

			setTimer(varTimer, 2 * loopTime),
			set(varStep, 0)
		)
	)

	private def ifEnabled = ifEquals(varEnabled, 2)
	private def ifStep(step: Long) = ifEquals(varStep, step)
	private def setStep(step: Long) = set(varStep, step)
}
