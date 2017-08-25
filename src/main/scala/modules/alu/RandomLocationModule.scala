package modules.alu

import artemis.Porcelain._
import lib._

import scala.xml.{Comment, NodeSeq}

/** Can be used as a library for GM-led sessions. The GM gets an interface
  * where they can (pseudo-randomly) select a location which can then
  * be used by modules for certain POIs.
  */
object RandomLocationModule extends Module {
	private sealed abstract class Quadrant(val name: String)
	private object Quadrant1 extends Quadrant("Q I")
	private object Quadrant2 extends Quadrant("Q II")
	private object Quadrant3 extends Quadrant("Q III")
	private object Quadrant4 extends Quadrant("Q IV")

	private val prefix = "module_random_location"

	// reflects the state of the ALU
	// 1 - init
	// 10 - random determination needed
	// 11 - drawing needed
	// 20 - waiting for border location
	// 99 - done
	val varState = s"${prefix}_state"
	val varOutputX = s"${prefix}_x"
	val varOutputY = s"${prefix}_y"
	val varOutputZ = s"${prefix}_z"

	private val varMinX = s"_${prefix}_minX"
	private val varMinZ = s"_${prefix}_minZ"
	private val varRangeX = s"_${prefix}_rangeX"
	private val varRangeZ = s"_${prefix}_rangeZ"
	private val varRand = s"_${prefix}_rand"
	// if 1, then marker is shown
	private val varMarker = s"_${prefix}_marker"

	private val buttonWidth = 50
	private val buttonHeight = 50
	private val buttonTop = 200
	private val buttonLeft = 200

	private val id = "?"

	private val quadrants = List(Quadrant1, Quadrant2, Quadrant4, Quadrant3)

	private val textConfirm = "Confirm"
	private val textShowMarker = "Show"
	private val textHideMarker = "Hide"
	private val textBorder = "To Border"
	private val textReset = "Reset"

	def call: NodeSeq = set(varState, 1)

	def ifFinished(varX: String, varY: String, varZ: String): NodeSeq = NodeSeq.fromSeq(Seq(
		ifEquals(varState, 99),
		set(varX, varOutputX),
		set(varY, varOutputY),
		set(varZ, varOutputZ)
	))

	def ifFinished(varX: String, varZ: String): NodeSeq = NodeSeq.fromSeq(Seq(
		ifEquals(varState, 99),
		set(varX, varOutputX),
		set(varZ, varOutputZ)
	))

	override def onLoad: NodeSeq = NodeSeq.fromSeq(Seq(
		set(varState, 0),
		set(varOutputX, 0),
		set(varOutputY, 0),
		set(varOutputZ, 0),
		set(varMarker, 1)
	))

	override def events: NodeSeq = Seq[NodeSeq](
		Comment(
			s"""$getClass ($prefix)
			   |
			   | A module to allow other modules to generate GM-influenced random locations.
			   |
			   | To show up the UI set $varState to 1.
			   | When $varState is 99, you can read:
			   |    $varOutputX,
			   |    $varOutputY, and
			   |    $varOutputZ
			   | """.stripMargin
		),
		Event(
			ifEquals(varState, 1),
			showSectorSelector,
			showQuadrantSelector,
			showUi,
			setWorldBorders,
			set(varState, 10),
			hideMarker
		),
		Event(
			ifGmButton(textReset),
			setWorldBorders,
			set(varState, 10)
		),
		Event(
			ifGmButton(textConfirm),
			set(varState, 99),
			hideSectorSelector,
			hideQuadrantSelector,
			hideUi,
			hideMarker
		),
		Sector.all.map { sector =>
			Event(
				ifGmButton(sector.name),
				set(varMinX, sector.minX),
				set(varRangeX, sector.maxX - sector.minX),
				set(varMinZ, sector.minZ),
				set(varRangeZ, sector.maxZ - sector.minZ),
				set(varState, 10)
			)
		},
		Event(
			ifGmButton(Quadrant1.name),
			setInt(varRangeX, s"$varRangeX / 2"),
			setInt(varMinX, s"$varMinX + $varRangeX"),
			setInt(varRangeZ, s"$varRangeZ / 2"),
			set(varState, 10)
		),
		Event(
			ifGmButton(Quadrant2.name),
			setInt(varRangeX, s"$varRangeX / 2"),
			setInt(varRangeZ, s"$varRangeZ / 2"),
			set(varState, 10)
		),
		Event(
			ifGmButton(Quadrant3.name),
			setInt(varRangeX, s"$varRangeX / 2"),
			setInt(varRangeZ, s"$varRangeZ / 2"),
			setInt(varMinZ, s"$varMinZ + $varRangeZ"),
			set(varState, 10)
		),
		Event(
			ifGmButton(Quadrant4.name),
			setInt(varRangeX, s"$varRangeX / 2"),
			setInt(varMinX, s"$varMinX + $varRangeX"),
			setInt(varRangeZ, s"$varRangeZ / 2"),
			setInt(varMinZ, s"$varMinZ + $varRangeZ"),
			set(varState, 10)
		),
		Event(
			ifEquals(varState, 10),
			setRandomInt(varRand, "0", s"$varRangeX / 5"), // the random number generator only works till 2^15 (~32.000), so lets use smaller steps
			setInt(varOutputX, s"$varMinX + ($varRand * 5)"),
			setRandomInt(varRand, "0", s"$varRangeZ / 5"), // the random number generator only works till 2^15 (~32.000), so lets use smaller steps
			setInt(varOutputZ, s"$varMinZ + ($varRand * 5)"),
			set(varState, 11)
		),
		Event(
			ifEquals(varState, 11),
			hideMarker,
			showMarker,
			set(varState, 12)
		),
		Event(
			ifGmButton(textShowMarker),
			ifNotExists(id),
			set(varMarker, 1),
			showMarker
		),
		Event(
			ifGmButton(textHideMarker),
			set(varMarker, 0),
			hideMarker
		),
		Event(
			ifNotEquals(varMarker, 1),
			ifExists(id),
			hideMarker
		),
		Event(
			ifGmButton(textBorder),
			set(varState, 20),
			BorderLocationModule.call(varOutputX, varOutputZ)
		),
		Event(
			ifEquals(varState, 20),
			BorderLocationModule.ifFinished,
			set(varState, 11),
			setInt(varOutputX, BorderLocationModule.varX),
			setInt(varOutputZ, BorderLocationModule.varZ)
		)
	)

	private def showSectorSelector: NodeSeq = NodeSeq.fromSeq(
		Sector.all.zipWithIndex.map { case (sector, idx) =>
			val col = idx % 5
			val row = idx / 5
			setGmButton(sector.name, buttonLeft + col * buttonWidth, buttonTop + row * buttonHeight, buttonWidth, buttonHeight)
		}
	)

	private def hideSectorSelector: NodeSeq = NodeSeq.fromSeq(
		Sector.all.map { sector =>
			clearGmButton(sector.name)
		}
	)

	private def showQuadrantSelector: NodeSeq = NodeSeq.fromSeq(
		quadrants.zipWithIndex.map { case (quadrant, idx) =>
			val col = idx % 2
			val row = idx / 2
			setGmButton(quadrant.name, buttonLeft + (col+5) * buttonWidth + 10, buttonTop + row * buttonHeight + 40, buttonWidth, buttonHeight)
		}
	)

	private def hideQuadrantSelector: NodeSeq = NodeSeq.fromSeq(
		quadrants.map { quadrant =>
			clearGmButton(quadrant.name)
		}
	)

	private def showUi: NodeSeq = NodeSeq.fromSeq(Seq(
		setGmButton(textReset, buttonLeft + 5 * buttonWidth + 10, buttonTop, buttonWidth * 2, 30),
		setGmButton(textConfirm, buttonLeft + 5 * buttonWidth + 10, buttonTop + 5 * buttonHeight - 30, buttonWidth * 2, 30),
		setGmButton(textShowMarker, buttonLeft + 5 * buttonWidth + 10, buttonTop + 5 * buttonHeight - 70, buttonWidth, 30),
		setGmButton(textHideMarker, buttonLeft + 6 * buttonWidth + 10, buttonTop + 5 * buttonHeight - 70, buttonWidth, 30),
		setGmButton(textBorder, buttonLeft + 5 * buttonWidth + 10, buttonTop + 5 * buttonHeight - 100, buttonWidth * 2, 30)
	))

	private def hideUi: NodeSeq = NodeSeq.fromSeq(Seq(
		clearGmButton(textReset),
		clearGmButton(textConfirm),
		clearGmButton(textBorder),
		clearGmButton(textShowMarker),
		clearGmButton(textHideMarker)
	))

	private def showMarker: NodeSeq = createPoi(id, Location(varOutputX, varOutputY, varOutputZ), RGB(255, 255, 255))
	private def hideMarker: NodeSeq = destroy(id)

	private def setWorldBorders: NodeSeq = NodeSeq.fromSeq(Seq(
		set(varMinX, 0),
		set(varRangeX, 100000),
		set(varMinZ, 0),
		set(varRangeZ, 100000)
	))
}
