package lib

import scala.xml.NodeSeq

case class Event(name: Option[String], nodes: NodeSeq*) {
	def toNodeSeq: NodeSeq = name match {
		case Some(value) => <event name={ value }>{ nodes }</event>
		case None => <event>{ nodes }</event>
	}
}

object Event {
	def apply(contents: NodeSeq*): Event = apply(None, contents: _*)
	def apply(name: String, contents: NodeSeq*): Event = apply(Some(name), contents: _*)

	implicit def toNodeSeq(event: Event): NodeSeq = event.toNodeSeq
	implicit def toNodeSeq(events: Seq[Event]): NodeSeq = events.flatMap(_.toNodeSeq.toSeq)
}