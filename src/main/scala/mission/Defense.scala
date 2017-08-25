package mission

import lib.{Event, IdRegistry, Mission, Module}
import mission.defense._
import modules.tools.ManipulateModule
import artemis.Porcelain._
import modules.alu.BorderLocationModule

object Defense extends Mission {

	implicit val idRegistry = new IdRegistry()

	implicit val world = World()

	val intro: Intro = new Intro()
	val physicistPlotLine: PhysicistPlotLine = new PhysicistPlotLine()
	val whalePlotLine: WhalePlotLine = new WhalePlotLine()
	val shipyardPlotLine: ShipyardModule = new ShipyardModule()
	val updates: UpdateModule = new UpdateModule()

	override def version: String = "0.1-alpha"

	override def modules: Seq[Module] = Seq(
		world,
		physicistPlotLine,
		whalePlotLine,
		shipyardPlotLine,
		intro,
//		updates,
//		BorderLocationModule,
		ManipulateModule
	)

	private val varPhase = "phase"
	private val textPhase1 = "Start Phase 1"

	override def onLoad = super.onLoad ++ set(varPhase, 1)

	override def events = super.events ++
		Event(
			ifEquals(varPhase, 1),
			setGmButton(textPhase1, 285, 300, 200, 30),
			set(varPhase, 2)
		) ++
		Event(
			ifGmButton(textPhase1),
			clearGmButton(textPhase1),
			set(varPhase, 10),
			// starting Phase 1
			intro.enable,
			physicistPlotLine.enable,
			whalePlotLine.enable,
			shipyardPlotLine.enable,
			updates.enable
		)

}
