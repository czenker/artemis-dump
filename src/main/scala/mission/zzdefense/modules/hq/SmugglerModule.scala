package mission.zzdefense.modules.hq

import artemis.Adventure
import artemis.Porcelain._
import lib.SteppedModule.GmTrigger
import lib._

import scala.xml.{Comment, NodeSeq}

class SmugglerModule(idRegistry: IdRegistry,
                     station: Station,
                     gate: Gate,
                     player: Player,
                     val prefix: String = "module_smuggler"
                    ) extends SteppedModule {

	val name = "Smuggler"
	val trigger = GmTrigger

	val id = idRegistry.random("T-")
	val shipDescription = s"A shuttle flying to ${gate.system}."
	val varEscape = s"${prefix}_escape"

	val topSpeed = 1.0


	override def onLoad: NodeSeq = super.onLoad ++ set(varEscape, 0)
	override def onDestruct = super.onDestruct ++ set(varEscape, 0) ++ destroy(id)

	override def events: NodeSeq = super.events ++ Seq[NodeSeq](
		Comment(
			s"""$getClass ($prefix)
			   |
			   | A ship flying from ${station.name} to ${gate.name} smuggling information out of the sector.""".stripMargin
		),
		step(100, 101,
			createNeutral(id, Adventure, station.location)
		),
		step(101, 102,
			ifExists(id),
			setSpecial(id, -1, -1),
			setProperty(id, "topSpeed", topSpeed.toString),
			setProperty(id, "turnRate", "0.004"),
			setShipText(id,
				desc = shipDescription,
				hailtext = s"We are flying to ${gate.system}."
			),
			setAi(id,
				aiGoto(gate.location, 0.5),
				aiAvoidBlackHole()
			),
			setTimer(varTimer, 90)
		),
		step(102, 103,
			ifTimer(varTimer),
			sendComms(station.id, "Alert! This information is highly confidential! Some information on our defenses was stolen from us. We assume the traitor will try to get the information out of the sector. So please hold your eyes open."),
			setAi(id,
				aiGoto(gate.location, 0.7), // go faster, because people are after you ;)
				aiAvoidBlackHole()
			)
		),
		step(102.to(103), 104,
			ifDistanceSmaller(player.id, id, 2500),
			sendComms(id, "What's the problem captain?", CommsType.Friend),
			setShipText(id,
				desc = shipDescription + " You are getting inconsistent readings from their freight room. Something seems strange."
			),
			setTimer(varTimer, 10)
		),
		step(104, 105,
			ifTimer(varTimer),
			sendComms(station.id, s"Hm, that guy in $id might be the traitor who stole our documents. Maybe if you lock your lasers on him he might make a mistake.", CommsType.Station)
		),
		step(104.to(105), 200,
			ifPlayerIsTargeting(id),
			sendComms(id, s"Ok, smart ass. I guess you are on my tracks.", CommsType.Enemy),
			setAi(id,
				aiGoto(gate.location, 1), // make a run for it
				aiAvoidBlackHole()
			),
			setEnemySide(id)
		),
		step(200, 201,
			ifDistanceSmaller(id, gate.location, 5000),
			sendComms(id, s"I'm almost at the gate to ${gate.system}. You won't catch me once I made the jump.", CommsType.Enemy)
		),
		step(201, 9999, // make the jump
			ifDistanceSmaller(id, gate.location, 1000),
			sendComms(station.id, "Oh my god, he made it off the sector. Let's hope those documents won't fall in the wrong hands", CommsType.Station),
			// @TODO: negative reward
			destroy(id)
		),
		step(200.to(201), 300,
			ifBackShieldLessOrEqual(id, 0),
			ifHasWarpDamage(id),
			sendComms(id, "I give up because warp"),
			setAi(id,
				aiGoto(gate.location, 0),
				aiAvoidBlackHole()
			),
			setPlayerSide(id)
		),
		step(200.to(201), 300,
			ifBackShieldLessOrEqual(id, 0),
			ifHasImpulseDamage(id),
			sendComms(id, "I give up because impulse"),
			setAi(id,
				aiGoto(gate.location, 0),
				aiAvoidBlackHole()
			),
			setPlayerSide(id)
		),
		step(200.to(201), 300,
			ifBackShieldLessOrEqual(id, 0),
			ifHasTurningDamage(id),
			sendComms(id, "I give up because turning"),
			setAi(id,
				aiGoto(gate.location, 0),
				aiAvoidBlackHole()
			),
			setPlayerSide(id)
		),
		Event( // if ship made it through
			ifDistanceSmaller(id, gate.location, 800),
			// @TODO: negative reward
			endModule
		)

	)



}
