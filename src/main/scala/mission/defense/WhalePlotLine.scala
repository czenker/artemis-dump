package mission.defense

import artemis.{TorgothGoliath, TorgothLeviathan}
import lib.Person.{Female, Male}
import lib._
import mission.defense.whale.{FreeWillyModule, SwimmingWithTheWhalesModule, WhaleModule, WhalerModule}
import modules.al.Chat.Message

class WhalePlotLine(implicit val world: World, implicit val idRegistry: IdRegistry) extends Module {

	val zoologist = Person("Jane", "Goodsome", Person.Female)
	val station = world.scienceBase

	val podNumber = idRegistry.podnumber

	val whales = Seq(
		Monster(Monster.Whale, idRegistry.register("Alon"), Some(podNumber)),
		Monster(Monster.Whale, idRegistry.register("Glan"), Some(podNumber)),
		Monster(Monster.Whale, idRegistry.register("Jaladri"), Some(podNumber)),
		Monster(Monster.Whale, idRegistry.register("Murdoch"), Some(podNumber)),
		Monster(Monster.Whale, idRegistry.register("Sedna"), Some(podNumber))
	)

	val willy = Monster(Monster.Whale, idRegistry.register("Willy"), Some(podNumber))

	lazy val whaleModule: WhaleModule = {
		WhaleModule(whales, Seq(willy), Rectangle(SectorA1, SectorC2)).withFailure(
			Message.friend(zoologist, "The whale family is wiped out. What am I going to research now?")
		).get
	}

	lazy val swimmingWithTheWhales = {
		SwimmingWithTheWhalesModule(world.player, whales, s"Pick up ${zoologist.name}", station).withBeginning(
			Message.friend(zoologist, s"Have you seen that whale family in sector B2 yet? I really need to get photographs of the whales. Can you pick me up at ${station.name} and take me to them in your ship, ${world.player.id}?")
		).withBoarding(
			Message.friend(zoologist, s"Thank you, ${world.player.id}, for picking me up. Please fly close to the whales so I can take photos of them.")
		).withApproaching(
			Message.friend(zoologist, s"Are these the whales over there? Please take us as close to them as you can. I like to swim with the whales.")
		).withContact(Seq(
			Message.friend(zoologist, s"Yes that is it. Stay with them. That's some nice picture I took here."),
			Message.friend(zoologist, s"Awwww, see that big one over there? Isn't she cute?"),
			Message.friend(zoologist, s"I could stay here forever. This feels so natural."),
			Message.friend(zoologist, s"Well, ok - time to go home. Can you drop me off at ${station.name} again, ${world.player.id}?")
		)).withSuccess(
			Message.friend(zoologist, s"Thank you for giving me the chance to swim with the whales, ${world.player.id}. You are a true friend of the animals."),
			freeWilly.enable
		).get
	}

	lazy val freeWilly = {
		FreeWillyModule(world.player, whales, willy, station, s"Load ${willy.id} into bay", s"Free ${willy.id}").withBeginning(
			Message.friend(zoologist,
				s"""Hey ${world.player.id}. It's ${zoologist.firstName} again.
				   |I need you for a top secret undercover mission. It came to my knowledge that one of my superiors is planning of killing one of our research subjects, a whale called ${willy.id}.
				   |Of course I can't let that happen and so I am counting on you to help me release him into freedom. Please pick the whale up at ${station.name} as soon as possible and bring it to the other whales in this sector.
				 """.stripMargin)
		).withBoarding(
			Message.friend(zoologist, s"You are real good hearted people, ${world.player.id} Crew. You need to get ${willy.id} to the other whales in this sector. I will load him into your loading bay. Just leave the door open - it is a Space Whale after all and it needs to breath... Space...")
		).withApproaching(
			Message.friend(zoologist, s"There are the Space Whales over there. Get real close to release the whale into freedom.")
		).withSuccess(
			Message.friend(zoologist, s"Can you see how happy ${willy.id} looks? Thanks for releasing him to freedom. He will never forget you - I guess."),
			whaleModule.addWhaleToGroup(willy) ++ whalerModule.enable
		).get
	}

	lazy val whalerModule = {
		val person = Person()
		val whaler = EnemyShip(
			idRegistry.random("T-"),
			person,
			TorgothGoliath,
			topSpeed = Some(0.9), // it wants to hunt whales - so it should be fast
			turnRate = Some(0.006),
			defaultDescription = Some(s"Narcissistic whale hunter with a ship almost as big as ${person.name}'s pride."),
			defaultScanDescription = Some(s"${person.gender match {
				case Male => "He"
				case Female => "She"
			}} thinks whales are to blame for all the bad things happening in the world."),
			defaultHailText = Some("I drink whale blood for breakfast, but I would not mind mixing it with yours")
		)
		val location = Location(88000, 0, 100000).extractionPoint(2500)

		WhalerModule(whaler, whales ++ Seq(willy), location).withBeginning(Seq(
			Message.friend(zoologist, s"My scanners show a Torgoth raider in Sector ${location.toSector.name}. Can you confirm this?"),
			Message(whaler, "[Singing] We're whalers on the Moon, we carry a harpoon. But there ain't no whales so we tell tall tales and sing a whaling tune."),
			Message.friend(zoologist, s"Torgoths love hunting whales way more than crushing enemy ships. Can you make sure the whales are save, ${world.player.id}? Destroy this stupid Torgoth scum!")
		)).withSuccess(
			Message.friend(zoologist, s"Yeah, show those filthy bastards that the children of Mother Earth are some to reckon with.")
		).withFailure(Seq(
			Message(whaler, "Die you filthy whales with those stupid names!!! I hate you!!! You killed all my family!!! Ok, it was also scurvy and typhus - but also whales!")
		)).get
	}

	private val allModules = Seq[SteppedModule](
		whaleModule,
		swimmingWithTheWhales,
		freeWilly,
		whalerModule
	)

	def enable =  whaleModule.enable ++ swimmingWithTheWhales.enable

	override def onLoad = super.onLoad ++ allModules.map(_.onLoad)

	override def events = super.events ++ allModules.map(_.events)

}
