package modules.tools

import artemis.Porcelain._
import lib.{Event, Module}

import scala.xml.NodeSeq

object ManipulateModule extends Module {
	val textDestroy = "Selection/Destroy"
	val textTeleport = "Selection/Teleport"

	override def onLoad: NodeSeq = NodeSeq.fromSeq(Seq(
		setGmButton(textTeleport),
		setGmButton(textDestroy)
	))

	override def events: NodeSeq = Seq[NodeSeq](
		Event(
			"destroy selection",
			ifGmButton(textDestroy),
			destroyGmSelection()
		),
		Event(
			"teleport selection",
			ifGmButton(textTeleport),
			setSelectionToGmPosition()
		)
	)
}
