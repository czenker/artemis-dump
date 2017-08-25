package lib

import artemis.Porcelain._

import scala.xml.NodeSeq

case class Gate(id: String,
                name: String,
                system: String,
                location: Location
               ) extends NamedLocation {
	def create: NodeSeq = createMesh(id, location, meshFileName = "dat/Missions/MISS_HamakSector_2/teleporter.dxs", textureFileName="dat/electricNoise2.png")

	// @TODO add a pushRadius - this might help with spawning ships at the same spot (the game crashes)
}
