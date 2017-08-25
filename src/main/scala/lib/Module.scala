package lib

import scala.xml.NodeSeq

trait Module {
	implicit def seqToNodeSeq(nodes: Seq[NodeSeq]): NodeSeq = NodeSeq.fromSeq(nodes.flatMap(_.toSeq))
	implicit def setToNodeSeq(nodes: Set[NodeSeq]): NodeSeq = seqToNodeSeq(nodes.toSeq)

	def onLoad: NodeSeq = NodeSeq.Empty
	@deprecated("usage of start is deprecated due to it's ambigious naming. Use onLoad instead")
	final def start: NodeSeq = onLoad

	def events: NodeSeq = NodeSeq.Empty
}
