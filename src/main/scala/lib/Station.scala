package lib

import artemis.Hull

import scala.xml.NodeSeq
import artemis.Porcelain._

case class Station(id: String, name: String, hull: Hull, location: Location) extends Dockable with NamedLocation {
	def create: NodeSeq = createStation(id, hull, location)
}
