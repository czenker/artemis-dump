package artemis

import artemis.Commands._

import scala.xml.{Elem, NodeSeq}

trait StationPorcelain extends BasePorcelain {

	def disableMissileProduction(name: String) = set_object_property(name, "canBuild", "0")
	def setMissiles(name: String, homing: Option[Int] = None, mine: Option[Int] = None, ecm: Option[Int] = None, pshock: Option[Int] = None, nuke: Option[Int] = None): NodeSeq = {
		NodeSeq.fromSeq(
			(homing.map(m => set_object_property(name, "missileStoresHoming", m.toString)) ++
			mine.map(m => set_object_property(name, "missileStoresMine", m.toString)) ++
			ecm.map(m => set_object_property(name, "missileStoresECM", m.toString)) ++
			pshock.map(m => set_object_property(name, "missileStoresPShock", m.toString)) ++
			nuke.map(m => set_object_property(name, "missileStoresNuke", m.toString))).toSeq
		)
	}
}
