package mission.zzdefense.modules.science

import artemis.Porcelain._
import lib.SteppedModule.GmTrigger
import lib._

import scala.xml.{Comment, NodeSeq}

class BlackHoleMission(blackHole: BlackHole, person: Person, player: Player, val prefix: String = "module_scibh") extends SteppedModule() {

	val name = "Black Hole"
	val trigger = GmTrigger
	val varToGo = s"_${prefix}_togo"

	val expectedDistance: Long = 1200
	val expectedDuration: Long = 60
	val reminderInterval: Long = 300

	override def onLoad: NodeSeq = super.onLoad ++ set(varToGo, expectedDuration)

	override def events: NodeSeq = super.events ++ Seq[NodeSeq](
		Comment(
			s"""$getClass ($prefix)
			   |
			   | The players should fly into the gravitation field of the black hole (closer than $expectedDistance m) and remain there for $expectedDuration seconds.""".stripMargin
		),
		step(100, 101,
			sendComms(person.name,
				s"Artemis, I have a mission for you. Me and my colleagues are doing research on ${blackHole.name} and we need more gravitational data. Please help us gather data by flying close to the black hole and staying there for a while. I know it is a bit risky, but the data should be quite rewarding.",
				CommsType.Friend
			),
			setTimer(varTimer, reminderInterval)
		),
		step(101, 102,
			ifTimer(varTimer),
			sendComms(person.name,
				s"Are you on your way to the black hole yet? We need those readings.",
				CommsType.Friend
			),
			setTimer(varTimer, reminderInterval)
		),
		step(102, 101,
			ifTimer(varTimer),
			sendComms(person.name,
				s"How is the mission progressing? Please fly to ${blackHole.name} to gather the readings we need for our research.",
				CommsType.Friend
			),
			setTimer(varTimer, reminderInterval)
		),
		step(100.to(102), 200,
			ifInsideSphere(player.id, blackHole.location, 8000),
			sendComms(person.name,
				s"I see you are approaching ${blackHole.name}. Please get as close as ${expectedDistance}m and stay there for $expectedDuration seconds. Your scanners will gather the data that we need.",
				CommsType.Friend
			),
			setTimer(varTimer, reminderInterval)
		),
		step(200.to(201), 201,
			ifTimer(varTimer),
			sendComms(person.name,
				s"What's the hold up? Please scan ${blackHole.name} for us. You need to be ${expectedDistance}m from its core and stay there for $expectedDuration seconds.",
				CommsType.Friend
			),
			setTimer(varTimer, reminderInterval)
		),
		step(200.to(201), 300,
			// entering the sphere
			ifInsideSphere(player.id, blackHole.location, expectedDistance),
			sendComms(person.name,
				s"Great. You are inside the black hole. Now stay there for $expectedDuration seconds.",
				CommsType.Friend
			),
			set(varToGo, expectedDuration),
			setTimer(varTimer, 1)
		),
		step(300, 300,
			ifTimer(varTimer),
			setInt(varToGo, s"$varToGo - 1")
		),
		step(300, 200,
			// exiting
			ifOutsideSphere(player.id, blackHole.location, expectedDistance),
			showTitle("OH NOOOOO!", "You left the circle"),
			sendComms(person.name,
				s"You left the event horizon of the black hole. Please get closer than ${expectedDistance}m again.",
				CommsType.Friend
			),
			setTimer(varTimer, reminderInterval)
		),
		step(300, 300,
			ifTimer(varTimer),
			ifInsideSphere(player.id, blackHole.location, expectedDistance),
			ifOutsideSphere(player.id, blackHole.location, expectedDistance - 50),

			showTitle("TIGHT", "You are very close to leaving the area"),

			setInt(varToGo, s"$varToGo - 1"),
			setTimer(varTimer, 1)
		),
		step(300, 300,
			ifTimer(varTimer),
			ifInsideSphere(player.id, blackHole.location, expectedDistance - 50),
			ifOutsideSphere(player.id, blackHole.location, expectedDistance - 150),

			showTitle("GOOD", "You got the right distance"),

			setInt(varToGo, s"$varToGo - 1"),
			setTimer(varTimer, 1)
		),
		step(300, 300,
			ifTimer(varTimer),
			ifInsideSphere(player.id, blackHole.location, expectedDistance - 150),

			showTitle("DANGER", "You might be too close"),

			setInt(varToGo, s"$varToGo - 1"),
			setTimer(varTimer, 1)
		),
		step(300, 400, // you made it
			ifEquals(varToGo, 0),
			showTitle("DONE", "You gathered the data"),
			sendComms(person.name,
				s"Marvelous. This is all the data we need. Please leave the black hole so we can transmit the data.",
				CommsType.Friend
			)
		),
		step(400, 400,
			ifOutsideSphere(player.id, blackHole.location, 8000),
			sendComms(person.name,
				s"We received your data, ${player.id}. Thanks for helping us out.",
				CommsType.Friend
			),
			//@TODO: reward
			endModule
		)
	)

}
