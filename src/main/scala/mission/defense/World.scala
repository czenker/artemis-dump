package mission.defense

import artemis._
import artemis.Porcelain._
import lib._

import scala.xml.NodeSeq

case class World(implicit val idRegistry: IdRegistry) extends Module {

	val sectorName = "Omikron System"

	val hq = Station(
		id = idRegistry.register("HQ"),
		name = "Headquarters",
		hull = CommandBase,
		location = Location(45000, 0, 50000)
	)

	val scienceBase = Station(
		id = idRegistry.register("Sci"),
		name = "Science Station",
		hull = ScienceBase,
		location = Location(15000, 0, 15000)
	)

	val shipyard = Station(
		id = idRegistry.register("Ship"),
		name = "Shipyard",
		hull = IndustrialBase,
		location = Location(22000, 0, 67000)
	)

	val gateNorth = Gate(
		id = idRegistry.register("Gate"),
		name = "Gate North",
		system = "Sirius Sector",
		location = Location(55000, 0, 12000)
	)

	val player = Player(
		id = idRegistry.register("Artemis"),
		hull = LightCruiser,
		spawnLocation = hq.location.atDistance(200, 0)
	)

	val newsServiceName = "Channel No 5"

	override def onLoad: NodeSeq = NodeSeq.fromSeq(Seq[NodeSeq](
		setSkyboxIndex(17),

		createNebulasLine(71, startX = 18374.0, startZ = 10060.0, endX = 13314.0, endZ = 22891.0, randomRange = Some(10307)),
		createNebulasLine(49, startX = 86211.0, startZ = 32308.0, endX = 84405.0, endZ = 46612.0, randomRange = Some(4491)),
		createNebulasLine(53, startX = 84103.0, startZ = 74466.0, endX = 74768.0, endZ = 84855.0, randomRange = Some(5811)),
		createNebulasLine(37, startX = 75220.0, startZ = 2797.0, endX = 79737.0, endZ = 9272.0, randomRange = Some(5573)),
		createNebulasLine(30, startX = 389.0, startZ = 2797.0, endX = 5659.0, endZ = 39686.0, randomRange = Some(10926)),
		createNebulasLine(20, startX = 4304.0, startZ = 46612.0, endX = 691.0, endZ = 90878.0, randomRange = Some(1887)),

		createAsteroidsLine(145, startX = 84254.0, startY = -500.0, startZ = 45106.0, endX = 69800.0, endY = 500.0, endZ = 86662.0, randomRange = Some(20349)),
		createAsteroidsArc(67, startX = 16540.0, startY = 0.0, startZ = 15826.0, startAngle=280, endAngle=154, radius = Some(23003), randomRange = Some(4139)),
		createAsteroidsLine(42, startX = 34278.0, startY = -500.0, startZ = 14216.0, endX = 31567.0, endY = 500.0, endZ = 1024.0, randomRange = Some(9218)),
		createAsteroidsLine(19, startX = 30302.0, startY = -500.0, startZ = 18373.0, endX = 31386.0, endY = 500.0, endZ = 28855.0, randomRange = Some(2942)),
		createAsteroidsLine(20, startX = 25422.0, startY = -500.0, startZ = 27590.0, endX = 17470.0, endY = 500.0, endZ = 32289.0, randomRange = Some(3725)),
		createAsteroidsLine(33, startX = 22169.0, startY = -500.0, startZ = 41686.0, endX = 4278.0, endY = 500.0, endZ = 51084.0, randomRange = Some(7959)),
		createAsteroidsLine(10, startX = 7531.0, startY = -500.0, startZ = 37168.0, endX = 1928.0, endY = 500.0, endZ = 38433.0, randomRange = Some(3615)),

		hq.create,
		disableProduction(hq),
		scienceBase.create,
		disableProduction(scienceBase),
		shipyard.create,
		disableProduction(shipyard),
		gateNorth.create,
		player.create,

		// TODO: make this upgradable
		setSensorSetting(SensorUnlimited),
		showShipsInNebula
	))

	private def disableProduction(station: Station): NodeSeq = Seq[NodeSeq](
		disableMissileProduction(station.id),
		setMissiles(station.id, 0, 0, 0, 0, 0)
	)
}
