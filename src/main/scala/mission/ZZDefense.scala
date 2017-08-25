//package mission
//
//import artemis._
//import artemis.Porcelain._
//import lib.Person.{Female, Male}
//import lib.SteppedModule.ChatAndScriptlet
//import lib._
//import maps.ArtemisPrime
//import mission.al.MonsterModule
//import modules.tools._
//import mission.zzdefense.modules.EconomyModule
//import modules.al.Chat.{Message, Messages}
//import modules.al.{FlightModule, RaidModule, ShuttleModule, TransportModule}
//
//import scala.xml.NodeSeq
//
//object ZZDefense extends Mission {
//	implicit val idRegistry = new IdRegistry()
//
//	val hq = Station(
//		id = idRegistry.register("HQ"),
//		name = "Headquarters",
//		location = Location(45000, 0, 50000)
//	)
//	val gate1 = Gate(
//		id = idRegistry.register("Gate1"),
//		name = "Gate North",
//		system = "Sirius Alpha",
//		location = Location(55000, 0, 12000)
//	)
//	val gate2 = Gate(
//		id = idRegistry.register("Gate2"),
//		name = "Gate South",
//		system = "Sirius Beta",
//		location = Location(22000, 0, 86000)
//	)
//	val player = Player(
//		id = idRegistry.register("Artemis"),
//		spawnLocation = hq.location.atDistance(200, 0)
//	)
//	val mine = Station(
//		id = idRegistry.register("Mine"),
//		name = "Mine Alpha",
//		location = Location(35000, 0, 30000)
//	)
////	val factory = Station(
////		id = idRegistry.register("Factory"),
////		name = "Weapon Factory",
////		location = Location(45000, 0, 25000)
////	)
////	val science = Station(
////		idRegistry.register("Sci"),
////		"Science Station",
////		location = Location(15000, 0, 15000)
////	)
//	val blackHole = BlackHole(
//		id = idRegistry.register("Darky"),
//		name = "Sailors End",
//		location = Location(42000, 0, 88000)
//	)
//
//	val headScientist = Person("Dr.", "Brakman", Male)
//
//	override def version: String = "0.1"
//
//	override val modules: Seq[Module] = {
////		val foreignTouristsNorth = new ForeignTouristsModule(
////			idRegistry,
////			"module_tourists_north",
////			gate = gate1,
////			station = hq,
////			creditsPerShuttle = 1500,
////			secondsPerShuttle = 360,
////			numberOfShuttles = 4
////		)
////		val foreignTouristsSouth = new ForeignTouristsModule(
////			idRegistry,
////			"module_tourists_south",
////			gate = gate2,
////			station = hq,
////			creditsPerShuttle = 1500,
////			secondsPerShuttle = 480,
////			numberOfShuttles = 3
////		)
////		val mineModule = new MineModule(
////			station = mine,
////			prefix = "module_mine"
////		)
////		val factoryModule = new FactoryModule(
////			station = factory,
////			prefix = "module_factory"
////		)
////		val mineTransport = new MineTransportModule(
////			idRegistry,
////			mineModule = mineModule,
////			factoryModule = factoryModule
////		)
////		val blackHoleShuttleModule = new BlackHoleShuttleModule(
////			idRegistry,
////			station = hq,
////			blackHole = blackHole
////		)
//
//		Seq(
//			ArtemisPrime,
//			EconomyModule,
////			foreignTouristsNorth,
////			foreignTouristsSouth,
////			mineModule,
////			factoryModule,
////			mineTransport,
////			blackHoleShuttleModule,
////			new BlackHoleMission(blackHole, headScientist, player),
////			new HunterAndPreyMission(idRegistry, HunterAndPreyMission.Shark, "Moby", headScientist, player),
////			new HunterAndPreyMission(idRegistry, HunterAndPreyMission.Dragon, "Sharky", headScientist, player),
////			new EscortModule(idRegistry, science, gate1, player),
////			new SmugglerModule(idRegistry, hq, gate1, player),
////			new PirateMission(headScientist, Location.between(gate1.location, hq.location, 0.2))(idRegistry),
////			new ScientistTransitMission(idRegistry, player, hq, science, headScientist),
//			Phase1.shuttle1,
////			Phase1.shuttle2,
////			Phase1.oreGatherer,
////			Phase1.transport1,
////			Phase1.monster1,
//			Phase1.pirate1,
//			ManipulateModule
////			RandomLocationModule,
////			BorderLocationModule
//		)
//	}
//
//	override def onLoad: NodeSeq = Seq(
//		createStation(hq.id, CommandBase, location = hq.location),
//		disableProduction(hq),
//		createStation(mine.id, DeepSpaceBase, location = mine.location),
//		disableProduction(mine),
////		createStation(factory.id, IndustrialBase, location = factory.location),
////		disableProduction(factory),
////		createStation(science.id, ScienceBase, location = science.location),
////		disableProduction(science),
//
//		createBlackHole(blackHole.id, blackHole.location),
//
//		createPlayer(player.id, LightCruiser, location = player.spawnLocation),
//
//		createMesh(gate1.id, gate1.location),
//		createMesh(gate2.id, gate2.location),
//
//		setGmButton(labelPhase1, 285, 300, 200, 30)
//	)
//
//	val labelPhase1 = "Start Phase 1"
//
//	override def events = Event(
//		"PHASE1",
//		ifGmButton(labelPhase1),
////		set("economy_report_enable", 1),
////
////		set("module_tourists_north_enable", 1),
////		set("module_tourists_south_enable", 1),
////
////		set("module_mine_enable", 1),
////		set("module_mine_transport_enable", 1),
////
////		set("module_factory_enable", 1),
////		set("module_bhshuttle_enable", 1),
////		set("module_scibh_enable", 1),
////		set("module_hunt_shark_enable", 1),
////		set("module_hunt_dragon_enable", 1),
////		set("module_escort_enable", 1),
////		set("module_smuggler_enable", 1),
////		set("module_pirate_raid_enable", 1),
////
////		set("module_comms_relay_enable", 1),
////		set("module_science_transit_enable", 1),
//
//		Phase1.shuttle1.enable,
//		Phase1.shuttle2.enable,
//		Phase1.transport1.enable,
//		Phase1.oreGatherer.enable,
//		Phase1.monster1.enable,
//		Phase1.pirate1.enable,
//
//
//
//		clearGmButton(labelPhase1),
//
//		showTitle("Phase 1", "warming up :)")
//	)
//
//	private def disableProduction(station: Station): NodeSeq = Seq[NodeSeq](
//		disableMissileProduction(station.id),
//		setMissiles(station.id, 0, 0, 0, 0, 0)
//	)
//
//	object Phase1 {
//		val shuttle1: ShuttleModule = {
//			val captain = Person()
//			val ship = NeutralShip(
//				idRegistry.random("T-"),
//				captain,
//				Shuttle,
//				defaultDescription = Some(s"The captain of this Shuttle, ${captain.name}, is known to be a gambler."),
//				defaultScanDescription = Some(s"${captain.name} has lost a reasonable amount of credits during ${captain.gender match {
//					case Male => "his"
//					case Female => "her"
//				}} last visit."),
//				defaultHailText = Some("Are you feeling lucky today?")
//			)
//
//			ShuttleModule(from = gate1, to = hq).withComms(
//				onStart = Message(ship, "It's good to back in this sector. Finally, this time I will get rich."),
//				onClosingStation = Seq(
//					Message(ship, s"Hey ${hq.name}. Can you prepare a docking bay for my ${ship.hull.className}? I'll be with you in a few moments."),
//					Message(hq, s"Sure thing, ${captain.firstName}. Docking bay 5 is prepared for you."),
//					Message(ship, s"Yeah, finally back at ${hq.name}. I'm really feeling lucky today.")
//				),
//				onDocking = Seq(
//					Message(ship, s"Do you still have that fancy casino here at ${hq.name}?"),
//					Message(hq, s"Hah, still that gambler, ${ship.captain.firstName}? Go to deck A - you'll find the casino there.")
//				),
//				onUndocking = Message(ship, "I lost a 100 credits gambling at your casino, bah."),
//				onClosingGate = Message(ship, s"There is the gate to ${gate1.system}. But I will be back for sure when I begged another one of my friends for money."),
//				onLeaving = Message(ship, s"${gate1.system} - here I come."),
//				onDestruction = Message(hq, s"${captain.firstName}? ${captain.firstName}??? ... Are you still out there? ... We lost ${ship.id} from our radars.")
//			).withShip(ship).get
//
//		}
//		val shuttle2: FlightModule = {
//			val captain = Person()
//			val ship = NeutralShip(
//				idRegistry.random("T-"),
//				captain,
//				Shuttle,
//				topSpeed = Some(2.0)
//			)
//
//			FlightModule(spawnPoint = gate1.location.fuzz(500)).addStationTarget(  // spawning right on top of a mesh makes the game crash
//				hq,
//				onDeparture = ChatAndScriptlet(
//					Message(ship, s"I have spawned")
//				),
//				onApproach = ChatAndScriptlet(
//					Seq(
//						Message(ship, "Hello. Dock please"),
//						Message(hq, "Ok. Come on in")
//					)
//				),
//				onArrival = ChatAndScriptlet(
//					Message(ship, "Here I come")
//				),
//				delay = 30.0,
//				fuzzPath = true
//			).addLocationTarget(gate1.location,
//				onDeparture = ChatAndScriptlet(
//					Message(ship, "I'm going home now")
//				),
//				onApproach = ChatAndScriptlet(Seq(
//					Message(ship, "Just a few more minutes")
//				)),
//				onArrival = ChatAndScriptlet(
//					Message(ship, "And jump...")
//				),
//				fuzzPath = true
//			).withDestruction(Message(hq, "It is gone... :(")).withShip(ship).get
//		}
//
//		val oreGatherer: FlightModule = {
//			val captain = Person()
//			val ship = NeutralShip(
//				idRegistry.random("T-"),
//				captain,
//				BulkCargo,
//				topSpeed = Some(2.0)
//			)
//
//			val steps = 6
//			val rad = 2 * Math.PI / steps
//			val distance = 10000
//
//			FlightModule(spawnPoint = mine.location.atDistance(200)).withBeginning(
//				Message(ship, "Hello World")
//			).addLocationTarget(
//				mine.location.atDistance(distance, rad),
//				onDeparture = ChatAndScriptlet(
//					Message(ship, "Getting ore")
//				),
//				onArrival = ChatAndScriptlet(
//					Message(ship, "Warming up the drillers")
//				)
//			).addLocationTarget(
//				mine.location.atDistance(distance, 2 * rad)
//			).addLocationTarget(
//				mine.location.atDistance(distance, 3 * rad)
//			).addLocationTarget(
//				mine.location.atDistance(distance, 4 * rad)
//			).addLocationTarget(
//				mine.location.atDistance(distance, 5 * rad)
//			).addLocationTarget(
//				mine.location.atDistance(distance, 6 * rad),
//				onArrival = ChatAndScriptlet(
//					Message(ship, "Ok, so that is enough for now. Let's go home")
//				)
//			).addStationTarget(
//				mine,
//				onApproach =  ChatAndScriptlet(
//					Seq(
//						Message(ship, "Back grom the dirty work"),
//						Message(mine, "We got a shower. Come on in.")
//					)
//				),
//				onArrival = ChatAndScriptlet(
//					Message(ship, "Finally home")
//				),
//				andThenLoopTo = Some(0)
//			).withShip(ship).get
//		}
//
//		val transport1: TransportModule = {
//			val passenger = Person()
//
//			TransportModule(from = hq, to = mine, textBoarding = s"Take ${passenger.name} on board").withBeginning(
//				Message.friend(passenger, s"Howdy. Can you please pick me up at ${hq.name}? I need to go back to ${mine.name} for work.")
//			).withBoarding(
//				Message.friend(passenger, s"Thanks for getting me. Now please drop me off at ${mine.name}.")
//			).withSuccess(
//				Message.friend(passenger, s"Thanks man - you are the best!!")
//			).get
//		}
//
//		val monster1: MonsterModule = {
//			val monsters = Set(
//				Monster(Monster.Shark, "Bitey")
//			)
//
//			MonsterModule(monsters = monsters, spawnLocation = Location(60000, 0, 30000)).withBeginning(
//				Message(hq, s"...BREAKING NEWS... a dragon has been spotted in this sector. Ships are advised to be extra cautious")
//			).withSuccess(
//				Message(hq, s"The dragon has been slain. Hail to the Adventurous who slayed it - let's hope it dropped some useful loot.")
//			).get
//		}
//
//		val pirate1: RaidModule = {
//			val enemies = Seq(
//				EnemyShip(idRegistry.random("P-", 1), Person(), Strongbow),
//				EnemyShip(idRegistry.random("P-", 2), Person(), Avenger),
//				EnemyShip(idRegistry.random("P-", 2), Person(), Avenger),
//				EnemyShip(idRegistry.random("P-", 2), Person(), Avenger)
//			)
//
//			RaidModule(spawnLocation = gate1.location, enemies = enemies).addTarget(shuttle1.ship).get
//		}
//	}
//}
