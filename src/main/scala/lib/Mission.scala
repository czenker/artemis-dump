package lib

import scala.xml.Elem

trait Mission extends Module {
	def version: String
	def modules: Seq[Module]

	def toNode: Elem = <mission_data version={ version }>
		<start>{ onLoad } { modules.flatMap(_.onLoad) }</start>
		{ events }
		{ modules.flatMap(_.events) }
	</mission_data>
}
