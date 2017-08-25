package modules.alu

import artemis.Porcelain._
import lib._

import scala.xml.{Comment, NodeSeq}

object BorderLocationModule extends Module {
	private val prefix = "module_border_location"

	// reflects the state of the ALU
	// 1 - init
	// 99 - done
	val varState = s"${prefix}_state"
	val varX = s"${prefix}_x"
	val varZ = s"${prefix}_z"

	def call(varInputX: String, varInputZ: String): NodeSeq = NodeSeq.fromSeq(Seq(
		setInt(varX, varInputX),
		setInt(varZ, varInputZ),
		set(varState, 1)
	))

	def ifFinished: NodeSeq = ifEquals(varState, 99)

	override def onLoad: NodeSeq = NodeSeq.fromSeq(Seq(
		set(varState, 0),
		set(varX, 0),
		set(varZ, 0)
	))

	override def events: NodeSeq = Seq[NodeSeq](
		Comment(
			s"""$getClass ($prefix)
			   |
			   | A module that calculates the closest way to the border of the map.
			   |
			   | Set $varState to 1 and $varX and $varZ to the X and Z coordinates.
			   | When $varState is 99, you can read $varX and $varZ""".stripMargin
		),
		Event(
			// extraction left
			ifEquals(varState, 1),
			ifGreaterOrEqual(varX, varZ),
			ifGreaterOrEqual(varX, s"100000 - $varZ"),
			set(varX, 100000),
			set(varState, 99)
		),
		Event(
			// extraction bottom
			ifEquals(varState, 1),
			ifSmaller(varX, varZ),
			ifGreaterOrEqual(varX, s"100000 - $varZ"),
			set(varZ, 100000),
			set(varState, 99)
		),
		Event(
			// extraction top
			ifEquals(varState, 1),
			ifGreaterOrEqual(varX, varZ),
			ifSmaller(varX, s"100000 - $varZ"),
			set(varZ, 0),
			set(varState, 99)
		),
		Event(
			ifEquals(varState, 1),
			ifSmaller(varX, varZ),
			ifSmaller(varX, s"100000 - $varZ"),
			set(varX, 0),
			set(varState, 99)
		)
	)
}
