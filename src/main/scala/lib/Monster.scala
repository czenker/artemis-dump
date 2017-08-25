package lib

import artemis.Porcelain._
import lib.Monster._

import scala.xml.NodeSeq

case class Monster(monsterType: MonsterType, id: String, podNumber: Option[Int] = None) {
	require(
		podNumber.isEmpty || monsterType.id == Whale.id || monsterType.id == Piranha.id,
		"Only Piranhas and Whales can have a podNumber"
	)
	podNumber.foreach(i => require(i>=0 && i<=9, "podNumber must be in range 0..9"))

	def create(location: Location = Location(0,0,0)): NodeSeq = {
		createMonster(Some(id), monsterType.id, location, podNumber)
	}

	def ifCreated: NodeSeq = ifExists(id)

	def afterCreate(): NodeSeq = {
		// @TODO: what possibilities are there for customization?
		NodeSeq.Empty
	}
}

object Monster {
	sealed abstract class MonsterType(val id: Int, val name: String) {
		override def toString = name
	}

	object Classic extends MonsterType(0, "Classic")
	object Whale extends MonsterType(1, "Whale")
	object Shark extends MonsterType(2, "Shark")
	object Dragon extends MonsterType(3, "Dragon")
	object Piranha extends MonsterType(4, "Piranha")
	object Tube extends MonsterType(5, "Tube")
	object Bug extends MonsterType(6, "Bug")
	object Derelic extends MonsterType(7, "Derelic")
}