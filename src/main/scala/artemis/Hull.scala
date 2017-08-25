package artemis

import artemis.Hull._

// from file vesselsData.xml v2.6.0
object Hull {
	sealed trait HullRace {
		def id: Int
		def name: String
	}

	object TSN extends HullRace {
		val id = 0
		val name = "TSN"
	}
	object Terran extends HullRace {
		val id = 1
		val name = "Terran"
	}
	object Kralien extends HullRace {
		val id = 2
		val name = "Kralien"
	}
	object Arvonian extends HullRace {
		val id = 3
		val name = "Arvonian"
	}
	object Torgoth extends HullRace {
		val id = 4
		val name = "Torgoth"
	}
	object Skaraan extends HullRace {
		val id = 5
		val name = "Skaraan"
	}
	object BioMech extends HullRace {
		val id = 6
		val name = "BioMech"
	}
	object Ximni extends HullRace {
		val id = 7
		val name = "Ximni"
	}
	object Pirate extends HullRace {
		val id = 8
		val name = "Pirate"
	}

}

sealed abstract class Hull(val race: HullRace, val className: String, val broadType: String)

object LightCruiser extends Hull(TSN, "Light Cruiser", "player")
object Scout extends Hull(TSN, "Scout", "player")
object Battleship extends Hull(TSN, "Battleship", "player")
object MissileCruiser extends Hull(TSN, "Missile Cruiser", "player")
object Dreadnought extends Hull(TSN, "Dreadnought", "player")
object Carrier extends Hull(TSN, "Carrier", "player")
object MineLayer extends Hull(TSN, "MineLayer", "player")
object Juggernaut extends Hull(TSN, "Juggernaut", "player")
object MediumFighter extends Hull(TSN, "TSN Medium Fighter", "player singleseat fighter")
object Bomber extends Hull(TSN, "TSN Bomber", "player singleseat fighter")
object Shuttle extends Hull(TSN, "TSN Shuttle", "player singleseat shuttle")
object LRShuttle extends Hull(TSN, "TSN LR Shuttle", "player singleseat shuttle")

object XimniLightCruiser extends Hull(Ximni, "Light Cruiser", "player")
object XimniScout extends Hull(Ximni, "Scout", "player")
object XimniMissileCruiser extends Hull(Ximni, "Missile Cruiser", "player")
object XimniBattleship extends Hull(Ximni, "Battleship", "player")
object XimniCarrier extends Hull(Ximni, "Carrier", "player carrier")
object XimniDreadnought extends Hull(Ximni, "Dreadnought", "player carrier")
object XimniFighter extends Hull(Ximni, "Zim Fighter", "player singleseat fighter")
object XimniBomber extends Hull(Ximni, "Zim Bomber", "player singleseat fighter")
object XimniShuttle extends Hull(Ximni, "Zim Shuttle", "player singleseat shuttle")
object XimniLRShuttle extends Hull(Ximni, "Zim LR Shuttle", "player singleseat shuttle")

object Strongbow extends Hull(Pirate, "Strongbow", "player")
object Brigantine extends Hull(Pirate, "Brigantine", "player carrier")
object Avenger extends Hull(Pirate, "Avenger", "player singleseat fighter")
object Adventure extends Hull(Pirate, "Adventure", "player singleseat shuttle")

object DeepSpaceBase extends Hull(Terran, "Deep Space Base", "base")
object CivilianBase extends Hull(Terran, "Civilian Base", "base")
object CommandBase extends Hull(Terran, "Command Base", "base")
object IndustrialBase extends Hull(Terran, "Industrial Base", "base")
object ScienceBase extends Hull(Terran, "Science Base", "base")

object KralienBase extends Hull(Kralien, "Base", "base")
object ArvonianBase extends Hull(Arvonian, "Base", "base")
object TorgothBase extends Hull(Torgoth, "Base", "base")
object SkaraanBase extends Hull(Skaraan, "Base", "base")

object Escort extends Hull(Terran, "Escort", "small warship")
object Destroyer extends Hull(Terran, "Destroyer", "medium warship")
object ScienceVessel extends Hull(Terran, "Science Vessel", "science")
object BulkCargo extends Hull(Terran, "Bulk Cargo", "cargo")
object LuxuryLiner extends Hull(Terran, "Luxury Liner", "luxury")
object Transport extends Hull(Terran, "Transport", "transport")

object KralienCruiser extends Hull(Kralien, "Cruiser", "small")
object KralienBattleship extends Hull(Kralien, "Battleship", "medium")
object KralienDreadnought extends Hull(Kralien, "Dreadnought", "large")

object ArvonianFighter extends Hull(Arvonian, "Fighter", "singleseat")
object ArvonianLightCarrier extends Hull(Arvonian, "Light Carrier", "carrier")
object ArvonianCarrier extends Hull(Arvonian, "Carrier", "carrier")

object TorgothGoliath extends Hull(Torgoth, "Goliath", "small")
object TorgothLeviathan extends Hull(Torgoth, "Leviathan", "medium")
object TorgothBehemoth extends Hull(Torgoth, "Behemoth", "large")

object SkaraanDefiler extends Hull(Skaraan, "Defiler", "small")
object SkaraanEnforcer extends Hull(Skaraan, "Enforcer", "medium")
object SkaraanExecutor extends Hull(Skaraan, "Executor", "large")

object BiomechStage1 extends Hull(BioMech, "Stage 1", "small asteroideater")
object BiomechStage2 extends Hull(BioMech, "Stage 2", "medium asteroideater")
object BiomechStage3 extends Hull(BioMech, "Stage 3", "large asteroideater")
object BiomechStage4 extends Hull(BioMech, "Stage 4", "large sentient")


