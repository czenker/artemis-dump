package mission.zzdefense.modules

import lib._
import artemis.Porcelain._
import artemis.{ScienceVessel, Strongbow}
import lib.SteppedModule.GmTrigger

import scala.util.Random
import scala.xml.{Comment, NodeSeq}

class EscortModule(idRegistry: IdRegistry,
                   station: Station,
                   gate: Gate,
                   player: Player,
                   val prefix: String = "module_escort"
                  ) extends SteppedModule {

	val name = "Escort"
	val trigger = GmTrigger

	val id = idRegistry.random("T-")
	val idPirate1 = idRegistry.random("P-")
	val idPirate2 = idRegistry.random("P-")

	/* 0 - ship tries to destroy
	 * 1 - ship tries to flee
	 */
	val varShip1Step = s"_${prefix}_ship1_step"
	val varShip2Step = s"_${prefix}_ship2_step"

	val raidLocation = Location.between(gate.location, station.location, Random.nextDouble * 0.6 + 0.15)
	val retreatLocation = raidLocation.extractionPoint()

	override def onLoad: NodeSeq = super.onLoad ++ set(varShip1Step, 0) ++ set(varShip2Step, 0)

	val distanceToRaid = Calculator.distance(station.location, raidLocation)
	val distanceToGate = Calculator.distance(raidLocation, gate.location)

	val topSpeed = 1.0
	val waitTime = 30
	val expectedDistance = 3500

//	override def start: NodeSeq = super.start

	override def onDestruct = super.onDestruct ++ destroy(id) ++ destroy(idPirate1) ++ destroy(idPirate2) ++ set(varShip1Step, 0) ++ set(varShip2Step, 0)

	override def events: NodeSeq = super.events ++ Seq[NodeSeq](
		Comment(
			s"""$getClass ($prefix)
			   |
			   | A ship flying from ${station.name} to ${gate.name} is raided by pirates.
			   |
			   | start: ${station.location}
			   | raid: ${raidLocation}
			   | extraction: ${retreatLocation}
			   | end: ${gate.location}
			   | """.stripMargin
		),
		step(100, 101,
			createNeutral(id, ScienceVessel, station.location),
			set(varShip1Step, 0),
			set(varShip2Step, 0)
		),
		step(101, 102,
			ifExists(id),
			setAi(id,
				aiGoto(station.location),
				aiGuardStation()
			),
			setProperty(id, "topSpeed", topSpeed.toString),
			setTimer(varTimer, 30)
		),
		step(102, 103,
			ifTimer(varTimer),
			sendComms(id, s"We will be heading for ${gate.name} in $waitTime seconds. As we are unarmed, we are looking for a ship to guard us for the trip.", CommsType.Friend),
			setTimer(varTimer, waitTime)
		),
		step(103, 200, // if the player comes close
			ifDistanceSmaller(player.id, id, expectedDistance),
			sendComms(id, s"Greetings ${player.id}. Are you going to escort us to ${gate.name} - we were hearing of roaming pirates. Maybe we could wash each others hands: You get some pirates to kill and we have a save travel. What do you think?", CommsType.Friend)
		),
		step(103, 200, // if the player does not come to help
			ifTimer(varTimer),
			sendComms(id, s"Let's hope we are not raided on our way to ${gate.name}.", CommsType.Friend)
		),
		step(200, 201,
			setAi(id,
				aiGoto(raidLocation),
				aiAvoidBlackHole()
			)
		),
		step(201, 202,
			ifDistanceSmaller(id, raidLocation, Math.max(distanceToRaid / 4, 2500)),
			ifDistanceGreater(id, player.id, expectedDistance),
			sendComms(id, s"I've got a bad feeling about this.", CommsType.Friend)
		),
		step(201, 202,
			ifDistanceSmaller(id, raidLocation, Math.max(distanceToRaid / 4, 2500)),
			ifDistanceSmaller(id, player.id, expectedDistance),
			sendComms(id, s"I'm glad you are accompany us, ${player.id}. Something bad is going to happen soon, I guess.", CommsType.Friend)
		),
		step(200.to(202), 300, // this is where the raid happens
			ifDistanceSmaller(id, raidLocation, 1000),
			createEnemy(idPirate1, Strongbow, Location(0,0,0)),
			createEnemy(idPirate2, Strongbow, Location(0,0,0))
		),
		step(300, 301,
			ifDistanceSmaller(player.id, id, expectedDistance),
			ifExists(idPirate1),
			ifExists(idPirate2),
			setRelativePosition(idPirate1, id, 60, 5000),
			setRelativePosition(idPirate2, id, 300, 5000),
			attack(idPirate1),
			attack(idPirate2),
			setProperty(idPirate1, "topSpeed", topSpeed.toString),
			setProperty(idPirate2, "topSpeed", topSpeed.toString),
			setAi(id,
				aiGoto(gate.location),
				aiAvoidBlackHole()
			)
		),
		step(300, 301, // spawn the pirates closer if the player seems not to care defending the ship anyways
			ifDistanceGreater(player.id, id, expectedDistance),
			ifExists(idPirate1),
			ifExists(idPirate2),
			setRelativePosition(idPirate1, id, 60, 2500),
			setRelativePosition(idPirate2, id, 300, 2500),
			attack(idPirate1),
			attack(idPirate2),
			setProperty(idPirate1, "topSpeed", topSpeed.toString),
			setProperty(idPirate2, "topSpeed", topSpeed.toString),
			setAi(id,
				aiGoto(gate.location),
				aiAvoidBlackHole()
			)
		),
		step(301, 301,
			ifNotExists(idPirate1),
			ifEquals(varShip1Step, 0),
			sendComms(idPirate1, s"Argh - you bastards. Our family will avenge us! Har.", CommsType.Enemy),

			// @TODO: reward

			set(varShip1Step, 1)
		),
		step(301, 301,
			ifNotExists(idPirate2),
			ifEquals(varShip2Step, 0),
			sendComms(idPirate2, s"We meeting Davy Jones...", CommsType.Enemy),

			// @TODO: reward

			set(varShip2Step, 1)
		),
		step(301, 400,
			ifNotExists(id),
			sendComms(id, s"Oh nooo. Our reactor caught fire. The pirates will shre... ***CONNECTION LOST***", CommsType.Friend),
			retreat(idPirate1),
			retreat(idPirate2)
		),
		step(400, 400,
			ifDistanceSmaller(idPirate1, retreatLocation, 1000),
			sendComms(idPirate1, s"So long, suckers. We should do raids like that more often.", CommsType.Enemy),
			destroy(idPirate1)
		),
		step(400, 400,
			ifDistanceSmaller(idPirate2, retreatLocation, 1000),
			sendComms(idPirate2, s"And off we go. That was as easy as stealing a lolli from a puppy.", CommsType.Enemy),
			destroy(idPirate2)
		),
		step(400, 9999, // end module if both ships are fled or dead
			ifNotExists(idPirate1),
			ifNotExists(idPirate2),
			sendComms(station.id, s"What a mess. At least we will have some peace from the pirates for a while now.", CommsType.Station)
		),
		step(301, 500, // if the ship survived and the pirates are dead
			ifExists(id),
			ifNotExists(idPirate1),
			ifNotExists(idPirate2),
			setTimer(varTimer, 5)
		),
		step(500, 501,
			ifTimer(varTimer),
			sendComms(id, s"Hah, you saved us, ${player.id}. Those pirates did not know what hit them. I hope they do pay a nice bounty.", CommsType.Friend)
		),
		step(501, 502,
			ifDistanceSmaller(id, gate.location, 2000),
			sendComms(id, s"So, here is the gate. Thanks ${player.id} for helping us out.", CommsType.Friend),
			setTimer(varTimer, 10)
		),
		step(502, 9999,
			ifTimer(varTimer),
			sendComms(id, s"Initiating hyperjump to ${gate.system}.", CommsType.Friend)
		),
		step(102.to(999), 9999,
			ifNotExists(id),
			ifNotExists(idPirate1),
			ifNotExists(idPirate2)
		)
	)

	def attack(name: String) = setAi(name,
		aiGoto(raidLocation),
		aiAttack(id, 1.2),
		aiChasePlayer(expectedDistance, expectedDistance / 2),
		aiChaseAnger()
	)

	def retreat(name: String) = setAi(name,
		aiGoto(retreatLocation),
		aiChasePlayer(expectedDistance, expectedDistance / 2),
		aiChaseAnger()
	)

}
