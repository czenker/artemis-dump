package mission.zzdefense.modules.hq

import artemis.Brigantine
import artemis.Porcelain._
import lib.SteppedModule.GmTrigger
import lib._

import scala.xml.{Comment, NodeSeq, Text}

class PirateMission(questGiver: Person,
                    raidLocation: Location)(implicit val idRegistry: IdRegistry) extends SteppedModule {

	val name = "Pirate Raid"
	val prefix = s"module_pirate_raid"
	val trigger = GmTrigger

	val numberOfShips: Int = 4
	val fleetId: Int = idRegistry.fleetnumber

	val varShipsDestroyed = s"_${prefix}_ships_destroyed"
	val varShipsFled = s"_${prefix}_ships_fled"
	val varShipsGone = s"_${prefix}_ships_gone"

	// 0 - all ok, 1 - destroyed
	val varShipStatus: List[String] = 0.until(numberOfShips).map(i => s"_${prefix}_ship_$i").toList

	val numberOfBigShips = Math.max(1, numberOfShips / 4)
	val numberOfMediumShips = numberOfShips - numberOfBigShips

	assert(numberOfBigShips >= 1, "At least one big ship")

	val ships: List[EnemyShip] = 0.until(numberOfShips).map {
		case i if i < numberOfBigShips =>
			PirateShip(PirateShip.Carrier)
		case _ => PirateShip(PirateShip.Medium)
	}.toList

	val leader = ships.head
	val wingmen = ships.tail

	val spawnLocation = raidLocation.extractionPoint()
	val pirateCommander = Person()

	val autoRetreatTimer: Int = 1000

	val textRetreat = s"Pirate Retreat"

	override def onLoad: NodeSeq = super.onLoad ++ set(varShipsDestroyed, 0) ++ set(varShipsFled, 0) ++ set(varShipsGone, 0) ++ varShipStatus.map(shipVar => set(shipVar, 0))

	override def onDestruct = super.onDestruct ++ ships.map(ship => destroy(ship.id)) ++
		clearGmButton(textRetreat)

	override def events: NodeSeq = super.events ++ Seq[NodeSeq](
		Comment(
			s"""$getClass ($prefix)
			   |
			   | The sector is attacked by pirates.
			   |
			   | $numberOfBigShips big ships and $numberOfMediumShips support ships.
			   | raiding at $raidLocation for $autoRetreatTimer seconds, spawning at $spawnLocation
			   | """.stripMargin
		),
		// ships arrive at sector
		step(100, 101,
			ships.map(_.create(spawnLocation.atDistance(1000), fleetId)),
			set(varShipsDestroyed, 0),
			set(varShipsFled, 0),
			set(varShipsGone, 0),
			varShipStatus.map(shipVar => set(shipVar, 0)),
			setGmButton(textRetreat, 50, 150, 100, 30)
		),
		step(101, 200,
			ships.map(_.ifCreated),
			ships.map(_.afterCreate()),
			ships.map(ship => havocAi(ship)),
			setFleetProperties(fleetId, fleetSpacing = 500.0, fleetMaxRadius = 2500.0),
			setTimer(varTimer, 30)
		),
		step(200, 201,
			ifTimer(varTimer),
			sendComms(pirateCommander.name, s"We are pillaging your ships for plunder and fame.", CommsType.Enemy),
			setTimer(varTimer, 10)
		),
		step(201, 202,
			ifTimer(varTimer),
			sendComms(questGiver.name, s"Oh my. We spotted pirates in our sector. To all ships out there: Be careful. A helping hand is appreciated.", CommsType.Friend),
			setTimer(varTimer, autoRetreatTimer)
		),
		Event(
			// remove the leader AI if the leader was destroyed in attack mode. Ships would hang otherwise
			ifGreaterOrEqual(varStep, 200),
			ifSmaller(varStep, 300),
			ifEquals(varShipStatus.head, 0),
			ifNotExists(leader.id),
			set(varShipsDestroyed, s"$varShipsDestroyed + 1"),
			set(varShipsGone, s"$varShipsGone + 1"),
			set(varShipStatus.head, 1),
			wingmen.map(ship => havocAi(ship, hasLeader = false))
		),
		ships.zipWithIndex.map{case (ship, i) =>
			val varShipStat = varShipStatus(i)
			Event(
				// if ship was destroyed
				ifGreaterOrEqual(varStep, if(ship == leader) 300 else 200),
				ifEquals(varShipStat, 0),
				ifNotExists(ship.id),
				set(varShipsDestroyed, s"$varShipsDestroyed + 1"),
				set(varShipsGone, s"$varShipsGone + 1"),
				set(varShipStat, 1)
			)
		},
		// retreat after timer
		step(202, 300,
			ifTimer(varTimer)
		),
		// retreat at button
		Event(
			ifGmButton(textRetreat),
			setStep(300)
		),
		step(300, 301,
			sendComms(pirateCommander.name, s"Ok, lads. We made enough booty lets get back home.", CommsType.Enemy),
			ships.map(retreatAi),
			clearGmButton(textRetreat)
		),
		ships.zipWithIndex.map{case (ship, i) =>
			val varShipStat = varShipStatus(i)
			Event(
				// if ship fled
				ifGreaterOrEqual(varStep, 300),
				ifEquals(varShipStat, 0),
				ifDistanceSmaller(ship.id, spawnLocation, 2000),
				set(varShipsFled, s"$varShipsFled + 1"),
				set(varShipsGone, s"$varShipsGone + 1"),
				set(varShipStat, 1),
				destroy(ship.id)
			)
		},
		// if all ships fled
		Event(
			ifGreaterOrEqual(varStep, 200),
			ifEquals(varShipsGone, numberOfShips),
			ifEquals(varShipsFled, numberOfShips),
			// @TODO: reward
			endModule
		),
		// if all ships destroyed
		Event(
			ifGreaterOrEqual(varStep, 200),
			ifEquals(varShipsGone, numberOfShips),
			ifEquals(varShipsDestroyed, numberOfShips),
			// @TODO: reward
			endModule
		),
		// mixed end
		Event(
			ifGreaterOrEqual(varStep, 200),
			ifEquals(varShipsGone, numberOfShips),
			ifGreaterOrEqual(varShipsDestroyed, 1),
			ifGreaterOrEqual(varShipsFled, 1),
			// @TODO: reward
			endModule
		)
	)

	private def retreatAi(ship: EnemyShip) = setAi(ship.id,
		aiGoto(spawnLocation, 1.5),
		aiChaseAiShip(20000, 10000),
		aiChasePlayer(7000, 3000),
		aiChaseAnger(),
		aiAvoidBlackHole(),
		aiUseSpecialPowers()
	)

	private def havocAi(ship: EnemyShip, hasLeader: Boolean = true) = setAi(ship.id,
		if (hasLeader && ship == leader) {aiTryToBecomeLeader()} else { aiNoop },
		aiGoto(raidLocation),
		aiChaseAiShip(20000, 10000),
		aiChasePlayer(5000, 2500),
		aiChaseAnger(),
		if (hasLeader && ship == leader) {aiLeaderLeads()} else { aiNoop },
		if (hasLeader && ship != leader) {aiFollowLeader()} else { aiNoop },
		aiAvoidBlackHole(),
		// do not launch fighters, because they might attack bases...
//		if (ship.hull == Brigantine) {aiLaunchFighters()} else { aiNoop },
		aiUseSpecialPowers()
	)

}
