package lib

import org.scalatest._

class LocationSpec  extends FlatSpec with Matchers {

	"Location.between" should "return a location in the middle of all the given coordinates" in {
		Location.between(
			Location(0, 100, 200), Location(100, 200, 300)
		) shouldEqual Location(50, 150, 250)
	}

	it should "return a location based on the ratio " in {
		Location.between(
			Location(0, 100, 200), Location(100, 200, 300), 0.3
		) shouldEqual Location(30, 130, 230)
	}


	"Location.fuzzyPath" should "only return the target if the locations are too close" in {
		Location.fuzzyPath(
			Location(0, 0, 0), Location(3000, 0, 3000)
		) shouldEqual Seq(Location(3000, 0, 3000))
	}

	it should "return segments on a long path, but not more than maxSegments" in {
		Location.fuzzyPath(
			Location(0, 0, 0), Location(100000, 0, 100000), maxSegments = 3
		).size shouldEqual 3
	}

	it should "return segments that close in on the target" in {
		val target = Location(100000, 0, 100000)
		val fuzzyPath = Location.fuzzyPath(
			Location(0, 0, 0), target, maxSegments = 3
		)

		val distanceToTarget = fuzzyPath.map(l => Calculator.distance(l, target))

		distanceToTarget.reverse shouldEqual distanceToTarget.sorted
	}

	it should "return segments that are further apart than minDistance" in {
		val fuzzyPath = Location.fuzzyPath(
			Location(0, 0, 0), Location(100000, 0, 100000), maxSegments = 5, minDistance = 5000
		)
		fuzzyPath.tail

		fuzzyPath.sliding(2).foreach { case one :: two :: Nil  => Calculator.distance(one, two) should be >= 5000.0}
	}

	it should "return segments that are about same size" in {
		val from = Location(0, 0, 0)
		val fuzzyPath = Location.fuzzyPath(
			from, Location(100000, 0, 100000), maxSegments = 2
		).toList

		val distanceSeg1 = Calculator.distance(from, fuzzyPath(0))
		val distanceSeg2 = Calculator.distance(fuzzyPath(0), fuzzyPath(1))

		distanceSeg1 should be <= (distanceSeg2 * 1.2)
		distanceSeg2 should be <= (distanceSeg1 * 1.2)
	}

	it should "chunks of identical distance if fuzz is disabled" in {
		val fuzzyPath = Location.fuzzyPath(
			Location(0, 0, 0), Location(0, 0, 100000), maxSegments = 4, fuzzFactor = 0.0
		).toList

		fuzzyPath should have size 4

		fuzzyPath(0).z.toDouble.round shouldEqual 25000
		fuzzyPath(1).z.toDouble.round shouldEqual 50000
		fuzzyPath(2).z.toDouble.round shouldEqual 75000
		fuzzyPath(3).z.toDouble.round shouldEqual 100000
	}

	it should "return different routes if fuzz is used" in {
		val first = Location.fuzzyPath(
			Location(0, 0, 0), Location(100000, 0, 100000)
		)
		val second = Location.fuzzyPath(
			Location(0, 0, 0), Location(100000, 0, 100000)
		)

		first should not equal second
	}
}
