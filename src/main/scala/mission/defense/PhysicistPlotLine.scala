package mission.defense

import artemis.{LuxuryLiner, Shuttle}
import lib.SteppedModule.ChatAndScriptlet
import lib.{IdRegistry, Module, NeutralShip, Person}
import modules.al.Chat.Message
import modules.al.{FlightModule, TransportModule}
import artemis.Porcelain._

// This is a plot line of a physicist who is interested in the world around him.
// Sometimes he needs help by the Players.
class PhysicistPlotLine(implicit val world: World, implicit val idRegistry: IdRegistry) extends Module {

	val physicist = Person("Gustaf", "Brackman", Person.Male, Some("Dr."))
	val shuttle = NeutralShip(
		id = idRegistry.register(physicist.lastName),
		captain = physicist,
		Shuttle
	)

	lazy val physicistArrivesInSytem: FlightModule = {
		val ship = NeutralShip(
			id = idRegistry.random("T-"),
			captain = Person(),
			LuxuryLiner,
			topSpeed = Some(1.4)
		)

		FlightModule(spawnPoint = world.gateNorth.location.fuzz(10)).addStationTarget(
			world.hq,
			onApproach = ChatAndScriptlet(
				Message(
					world.newsServiceName,
					s"""This is ${world.newsServiceName} with fabulous news.
					    |World renown scientist ${physicist.name} is arriving today at ${world.hq.name} to start his new job working at ${world.scienceBase.name} as head scientist. His works in quantum field drives and non-fluid space fuel made him one of the most renown scientists in the universe. A welcome ceremony for ${physicist.name} will be held this afternoon.""".stripMargin,
					CommsType.Side
				)
			),
			fuzzPath = true
		).withSuccess(
			scriptlet = physicistToScienceStation.enable
		).withShip(ship).get
	}

	lazy val physicistToScienceStation: TransportModule = {
		TransportModule(textBoarding = s"Pick up ${physicist.name}", from = world.hq, to = world.scienceBase).withBeginning(
			Message.friend(physicist, "Please pick me up")
		).withBoarding(
			Message.friend(physicist, "Thanks for picking me up")
		).withSuccess(
			Message.friend(physicist, "Thanks for bringing me here - I will let you know when I need something.")
		).get
	}

	private val allModules = Seq(
		physicistArrivesInSytem,
		physicistToScienceStation
	)

	def enable =  physicistArrivesInSytem.enable

	override def onLoad = super.onLoad ++ allModules.map(_.onLoad)

	override def events = super.events ++ allModules.map(_.events)
}
