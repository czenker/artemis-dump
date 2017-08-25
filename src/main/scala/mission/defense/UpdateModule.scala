package mission.defense

import lib.{Event, IdRegistry, Module, SteppedModule}
import artemis.Porcelain._
import mission.defense.update.LimitsModule

import scala.xml.NodeSeq

class UpdateModule(implicit val world: World, implicit val idRegistry: IdRegistry) extends Module {

	val station = world.shipyard

	val limitModule = LimitsModule(world.player).get

	private val allModules = Seq[SteppedModule](
		limitModule
	)

	def enable = limitModule.enable

	override def onLoad = super.onLoad ++ allModules.map(_.onLoad)

	override def events = super.events ++ allModules.map(_.events)

}
