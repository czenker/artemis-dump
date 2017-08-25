package mission.defense

import lib._
import mission.defense.shipyard.RaceAgainstTheClock
import mission.defense.update.LimitsModule
import modules.al.Chat.Message
import artemis.Porcelain._

class ShipyardModule(implicit val world: World, implicit val idRegistry: IdRegistry) extends Module {

	val station = world.shipyard
	val engineer = Person("Werner", "Lightyear", Person.Male)

	val raceAgainstTheClock = RaceAgainstTheClock(world.player,
		station,
		"Help test the new space ship drive",
		"Ready for the test drive"
	).withBeginning(
		Message.friend(engineer, s"Greetings ${world.player.id}. I'm testing out a new drive for space ships and need a fearless spaceship crew to test it out. If you are willing to have it installed in your space ship meet me at ${station.name}.")
	).withAccept(Seq(
		Message.friend(engineer, s"Great. I hope your engineer is up for the task. Sometimes the drive causes overheating problems and also obstructing other parts of your space ship. Also I needed to disable your warp to get enough energy."),
		Message("Game hint", "Your engineer should be prepared to repair your impulse and maneuver with the highest priority. All other systems are not needed to complete the mission. It is highly recommended the engineer manages the damcom teams manually to send them to the highest priority targets.", commsType = Some(CommsType.Side))
	), popupEngineering("Prepare to repair impulse and maneuver quickly") ++ popupHelm("Warp temporarily disabled")
	).addWaypoint(
		idRegistry.register("WP1"),
		Location(60000, 0, 60000),
		onStartChat = Message.friend(engineer, "I marked your first Waypoint on your map. Fly right there."),
		onApproachingChat = Message.friend(engineer, "The first check point is in front of you. Don't be confused by its look though. It looks like a mine, but it won't blow up, I promise - that's what my engine might do.", 0.0),
		onStartScriptlet = showTitle("Go to WP1")
	).addWaypoint(
		idRegistry.register("WP2"),
		Location(60000, 0, 70000),
		onStartChat = Message.friend(engineer, "Let's get to the next waypoint"),
		onStartScriptlet = showTitle("Go to WP2")
	).addWaypoint(
		idRegistry.register("WP3"),
		Location(70000, 0, 70000),
		onStartChat = Message.friend(engineer, "I marked the third waypoint on your map"),
		onStartScriptlet = showTitle("Go to WP3")
	).withReturn(
		Message.friend(engineer, s"Your last checkpoint is back on ${station.name}. Come here and I will have a look at the test results."),
		showTitle(s"Dock at ${station.name}")
	).withSuccess(Seq(
		Message.friend(engineer, "So how was your flight with the new engine?"),
		Message.friend(engineer, """What do you mean by "It almost blew up our ship"? Hm, I guess there might be something wrong with the vaporizer of the plasma injector that might drain energy from other parts of the ship. Maybe I should have a look at that."""),
		Message.friend(engineer, "Anyways for giving it a try. The data really helps me a lot improving the engine.")
	))

		.get

	private val allModules = Seq[SteppedModule](
		raceAgainstTheClock
	)

	def enable = raceAgainstTheClock.enable

	override def onLoad = super.onLoad ++ allModules.map(_.onLoad)

	override def events = super.events ++ allModules.map(_.events)

}
