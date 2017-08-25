package artemis

import artemis.Commands._
import lib.Location

import scala.xml.{Elem, Node, NodeSeq, Text}

trait AiPorcelain extends BasePorcelain{

//	def aiAttack(name: String, target: String, throttle: Double = 1.0) = add_ai(name = name, `type` = "ATTACK", targetName = target, value1 = throttle.toString)
	def aiAvoidBlackHole(distance: Double = 6000) = { name: String => add_ai(name = name, `type` = "AVOID_BLACK_HOLE", value1 = distance.toString) }
//	def aiChaseAiShip(name: String, distance: Double = 3000, nebulaDistance: Double = 500) = add_ai(name = name, `type` = "CHASE_AI_SHIP", value1 = distance.toString, value2 = nebulaDistance.toString)
	def aiChaseAnger() = { name: String => add_ai(name = name, `type` = "CHASE_ANGER") }
	def aiChaseEnemy(distance: Double = 3000, nebulaDistance: Double = 500) = { name: String => add_ai(name = name, `type` = "CHASE_ENEMY", value1 = distance.toString, value2 = nebulaDistance.toString) }
	def aiChaseAiShip(distance: Double = 3000, nebulaDistance: Double = 500) = { name: String => add_ai(name = name, `type` = "CHASE_AI_SHIP", value1 = distance.toString, value2 = nebulaDistance.toString) }
//	def aiChaseFleet(name: String, distance: Double = 3000, nebulaDistance: Double = 500) = add_ai(name = name, `type` = "CHASE_FLEET", value1 = distance.toString, value2 = nebulaDistance.toString)
	def aiChaseMonster(distance: Double = 3000) = { name: String => add_ai(name = name, `type` = "CHASE_MONSTER", value1 = distance.toString) }
	def aiChaseNeutral(distance: Double = 3000, nebulaDistance: Double = 500) = { name: String => add_ai(name = name, `type` = "CHASE_NEUTRAL", value1 = distance.toString, value2 = nebulaDistance.toString) }
	def aiChasePlayer(distance: Double = 3000, nebulaDistance: Double = 500) = { name: String => add_ai(name = name, `type` = "CHASE_PLAYER", value1 = distance.toString, value2 = nebulaDistance.toString) }
//	def aiChaseStation(name: String, distance: Double = 3000) = add_ai(name = name, `type` = "CHASE_STATION", value1 = distance.toString)
	def aiChaseWhale(distance: Double = 3000) = { name: String => add_ai(name = name, `type` = "CHASE_WHALE", value1 = distance.toString) }

	def aiGoto(location: Location, throttle: Double = 1.0) = { name: String => add_ai(name, `type` = "POINT_THROTTLE", value1 = location.x, value2 = location.y, value3 = location.z, value4 = throttle.toString) }
	def aiTarget(target: String, throttle: Double = 1.0) = { name: String => add_ai(name, `type` = "TARGET_THROTTLE", targetName = target, value1 = throttle.toString, value2 = "1") }
	def aiAttack(target: String, throttle: Double = 1.0) = { name: String => add_ai(name, `type` = "TARGET_THROTTLE", targetName = target, value1 = throttle.toString, value2 = "0") }
	def aiFollowCommsOrders() = {name: String => add_ai(name, `type` = "FOLLOW_COMMS_ORDERS") }
	def aiGuardStation() = {name: String => add_ai(name, `type` = "GUARD_STATION") }
	def aiTryToBecomeLeader() = {name: String => add_ai(name, `type` = "TRY_TO_BECOME_LEADER") }
	def aiLeaderLeads() = {name: String => add_ai(name, `type` = "LEADER_LEADS") }
	def aiFollowLeader() = {name: String => add_ai(name, `type` = "FOLLOW_LEADER") }
	def aiLaunchFighters(minDistance: Double = 11000) = {name: String => add_ai(name, `type` = "LAUNCH_FIGHTERS", value1 = minDistance.toString) }
	def aiUseSpecialPowers() = {name: String => add_ai(name, `type` = "SPCL_AI") }
	def aiMoveWithGroup(throttle: Double = 1.0) = {name: String => add_ai(name, `type` = "MOVE_WITH_GROUP", value1 = throttle.toString) }
	def aiStayClose() = {name: String => add_ai(name, `type` = "STAY_CLOSE", value1 = "2000", value2 = "1") }

	def aiNoop = { name: String => Text("") }

	def setAi(name: String, commands: ((String) => Node)* ): NodeSeq = {
		clear_ai(name) ++ commands.map(_(name))
	}

	def addAi(name: String, commands: ((String) => Node)* ): NodeSeq = {
		commands.map(_(name))
	}

}
