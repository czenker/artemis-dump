package lib

import artemis.Porcelain._
import artemis._

import scala.xml.NodeSeq

trait Ship {
	def id: String
	def captain: Person
	def hull: Hull
	def topSpeed: Option[Double]
	def turnRate: Option[Double]
	def defaultDescription: Option[String]
	def defaultScanDescription: Option[String]
	def defaultHailText: Option[String]

	def ifCreated: NodeSeq = ifExists(id)

	def afterCreate(): NodeSeq = {
		NodeSeq.fromSeq(Seq(
			setSpecial(id, -1, -1),
			setProperty(id, "surrenderChance", "0")
		) ++
			topSpeed.map(ts => setProperty(id, "topSpeed", ts.toString)) ++
			turnRate.map(tr => setProperty(id, "turnRate", tr.toString)) ++
			defaultDescription.map(desc => setShipText(id, desc = desc)) ++
			defaultScanDescription.map(desc => setShipText(id, scan_desc = desc)) ++
			defaultHailText.map(hail => setShipText(id, hailtext = hail))
		)
	}

}

case class EnemyShip(id: String,
                     captain: Person,
                     hull: Hull,
                     topSpeed: Option[Double] = None,
                     turnRate: Option[Double] = None,
                     defaultDescription: Option[String] = None,
                     defaultScanDescription: Option[String] = None,
                     defaultHailText: Option[String] = None) extends Ship {

	def create(location: Location = Location(0,0,0), fleetNumber: Option[Int] = None): NodeSeq = {
		createEnemy(Some(id), hull, location, fleetNumber)
	}
}

case class NeutralShip(id: String,
                     captain: Person,
                     hull: Hull,
                     topSpeed: Option[Double] = None,
                     turnRate: Option[Double] = None,
                     defaultDescription: Option[String] = None,
                     defaultScanDescription: Option[String] = None,
                     defaultHailText: Option[String] = None) extends Ship {

	def create(location: Location = Location(0,0,0)): NodeSeq = {
		createNeutral(Some(id), hull, location)
	}
}

object PirateShip {

	sealed trait Size
	object Fighter extends Size
	object Shuttle extends Size
	object Medium extends Size
	object Carrier extends Size

	def apply(size: Size)(implicit idRegistry: IdRegistry): EnemyShip = {
		def captain = Person()

		size match {
			case Fighter =>
				val id = idRegistry.random("P-", 3)
				EnemyShip(id, captain, Avenger)
			case Shuttle =>
				val id = idRegistry.random("P-", 3)
				EnemyShip(id, captain, Adventure)
			case Medium =>
				val id = idRegistry.random("P-", 2)
				EnemyShip(id, captain, Strongbow)
			case Carrier =>
				val id = idRegistry.random("P-", 1)
				EnemyShip(id, captain, Brigantine)
		}
	}
}

