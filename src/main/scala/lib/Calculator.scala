package lib

object Calculator {

	// distance between two points
	def distance(location1: Location, location2: Location): Double = Math.sqrt(
		Math.pow(location1.x.toDouble - location2.x.toDouble, 2) +
		Math.pow(location1.y.toDouble - location2.y.toDouble, 2) +
		Math.pow(location1.z.toDouble - location2.z.toDouble, 2)
	)

	def distance(locations: Seq[Location]): Double = locations.sliding(2).foldLeft(0.0) { case (sum, locs) => sum + distance(locs.head, locs(1)) }

	// expected duration to fly from A to B in seconds
	def duration(location1: Location, location2: Location, speed: Double) = distance(location1, location2) / 100 / speed

	def speed(location1: Location, location2: Location, duration: Double) = distance(location1, location2) / 100 / duration
	def speed(distance: Double, duration: Double) = distance / 100 / duration

}
