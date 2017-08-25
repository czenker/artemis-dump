package mission.defense

import lib.Module
import modules.al.Chat.Message
import modules.al.PoiModule
import artemis.Porcelain._

class Intro(implicit val world: World) extends Module {

	lazy val gettingToKnowTheSector = {
		val questGiver = "Infomat 3000"
		def say(message: String) = Message(from = questGiver, message, CommsType.Side)

		PoiModule(world.player).withBeginning(
			say(s"""Greetings ${world.player.id},
				   |
				   |My name is $questGiver and I am an automated information system that can help you find your way through this system.
				   |This sector is called "${world.sectorName}" and probably the most important station in this sector is ${world.hq.name}.
				   |
				   |I will assist you as much as I can. Each time you are visiting a new place I will provide you with information on the location.
				 """.stripMargin)
		).addStationPoi(
			world.hq,
			// @TODO
			Some(say(s"""You are currently docked at ${world.hq.name} - the central hub of this sector. Most of the ships dock here.""".stripMargin))
		).addStationPoi(
			world.scienceBase,
			// @TODO
			Some(say(s"""You are currently docked at ${world.scienceBase.name}""".stripMargin))
		).addSpherePoi(
			world.scienceBase.location, 13000,
			// @TODO
			Some(say(s"""A nebula that contains the ${world.scienceBase.name}.""".stripMargin))
		).withSuccess(
			// @TODO
			say(
				s"""And that is all I really know, ${world.player.id}.
				   |
				   |Fortunately you are -- SPOILER ALERT -- in a game and it is customary that I give you a reward. Not that I can do much, as I am just an information system funded by the government, but hey, what would be the fun if there was no reward, right?""".stripMargin)
		).get
	}

	private val allModules = Seq(
		gettingToKnowTheSector
	)

	def enable =  gettingToKnowTheSector.enable

	override def onLoad = super.onLoad ++ allModules.map(_.onLoad)

	override def events = super.events ++ allModules.map(_.events)
}
