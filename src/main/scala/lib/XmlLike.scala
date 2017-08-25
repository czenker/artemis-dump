package lib

import scala.xml.NodeSeq

object XmlLike {
	implicit def xmlLikeToSeqNode(xmlLike: XmlLike): NodeSeq = xmlLike.toXml
	implicit def xmlLikeToSeqNode(xmlLikes: Seq[XmlLike]): NodeSeq = xmlLikes.flatMap(_.toXml)
}

trait XmlLike {
	def toXml: NodeSeq
	override def toString: String = toXml.toString
}
