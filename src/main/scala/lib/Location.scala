package lib

import scala.util.{Failure, Random, Success, Try}

case class Location(x: String, y: String, z: String) {
	import Location._

	requireValidValue(x)
	requireValidValue(y)
	requireValidValue(z)

	// validate that the coordinate is inside the map - if not Artemis will likely crash :(
	private def requireValidValue(v: String): Unit = Try[Double] {
		v.toDouble
	} match {
		case Success(double) => require(double >= 0.0 && double <= 100000.0, s"each coordinate in $toString has to be between 0.0 and 100000.0")
		case _ => Unit
	}

	override def toString: String = s"($x, $y, $z)"

	// finds the closest extraction point at the border of the map
	def extractionPoint(distance: Double = 0): Location = {
		val toMinX = x.toDouble
		val toMaxX = 100000.0 - x.toDouble
		val toMinZ = z.toDouble
		val toMaxZ = 100000.0 - z.toDouble

		val min = Math.min(Math.min(toMinX, toMaxX), Math.min(toMinZ, toMaxZ))
		if(min == toMinX) Location(distance, y.toDouble, z.toDouble)
		else if(min == toMaxX) Location(100000.0 - distance, y.toDouble, z.toDouble)
		else if (min == toMinZ) Location(x.toDouble, y.toDouble, distance)
		else Location(x.toDouble, y.toDouble, 100000.0 - distance)
	}

	// creates a different location that has a perfect distance from the location
	def atDistance(distance: Double, angle: Option[Double] = None): Location = {
		val theAngle = angle.getOrElse(rand.nextDouble() * 2 * Math.PI)

		normalize(Location(x.toDouble + Math.sin(theAngle) * distance, y.toDouble, z.toDouble + Math.cos(theAngle) * distance))
	}

	def atDistance(distance: Double, angle: Double): Location = atDistance(distance, Some(angle))

	// takes a random point within circle with radius maxDistance
	def fuzz(maxDistance: Double): Location = atDistance(rand.nextDouble() * maxDistance, rand.nextDouble() * 2 * Math.PI)

	def toSector: Sector = Sector.all.find( sector => sector.minX <= x.toDouble && sector.maxX >= x.toDouble && sector.minZ <= z.toDouble && sector.maxZ >= z.toDouble ).get
}

object Location {
	private val rand = new Random()

	def apply(x: Double, y: Double, z: Double): Location = apply(x.toString, y.toString, z.toString)

	protected def normalize(location: Location) = Location(minMax(location.x.toDouble), minMax(location.y.toDouble), minMax(location.z.toDouble))

	private def minMax(value: Double) = Math.min(Math.max(value, 0.0), 100000.0)

	def between(one: Location, other: Location, ratio: Double = 0.5): Location = {
		assert(ratio >= 0.0 && ratio <= 1.0, "ratio should be between 0.0 and 1.0")

		Location(
			one.x.toDouble * (1 - ratio) + other.x.toDouble * ratio,
			one.y.toDouble * (1 - ratio) + other.y.toDouble * ratio,
			one.z.toDouble * (1 - ratio) + other.z.toDouble * ratio
		)
	}

	def fuzzyPath(one: Location, other:Location, fuzzFactor: Double = 0.05, minDistance: Double = 5000, maxSegments: Int = 3): Seq[Location] = {
		val numberOfSegments = Math.min(Calculator.distance(one, other) / ((1 + fuzzFactor) * minDistance), maxSegments).toInt

		val locations = 2.to(numberOfSegments).foldRight(List[Location](other)) { case (noOfSegs, waypoints) =>
			// we already have the waypoints
			val waypoint = between(waypoints.head, one, 1.0 / noOfSegs)

			val d = Calculator.distance(waypoints.head, waypoint)

			waypoint.fuzz(d * fuzzFactor) :: waypoints
		}

		one :: locations
	}
}
