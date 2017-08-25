package lib

import scala.collection.mutable
import scala.util.{Failure, Random, Success, Try}

class IdRegistry {

	private val registeredIds: mutable.Set[String] = mutable.Set.empty
	private var lastFleetnumber: Int = 0
	private var lastPodnumber: Int = -1

	private val rand = new Random()

	def contains(id: String) = registeredIds.contains(id)

	def fleetnumber = {
		lastFleetnumber += 1
		if(lastFleetnumber > 99) throw new RuntimeException("no fleetnumber larger than 99 is possible")
		lastFleetnumber
	}

	def podnumber = {
		lastPodnumber += 1
		if(lastPodnumber > 9) throw new RuntimeException("no podnumber larger than 9 is possible")
		lastPodnumber
	}

	def register(id: String): String = if (contains(id)) {
		throw new IllegalArgumentException(s"Ship with id $id is already registered")
	} else {
		registeredIds.add(id)
		id
	}

	/**
	  * registers a unique name that displays the same in the game
	  * by adding spaces
	  * @param id
	  * @return
	  */
	def registerDisplay(id: String): String = {
		var retries = 10
		var name = id

		while(retries > 0) {
			Try[String] {
				register(name)
			} match {
				case Failure(_) =>
				case Success(value) => return value
			}
			name = if(rand.nextInt(1) > 0) s"$name " else s" $name"
			retries -= 1
		}

		throw new RuntimeException(s"Could not find a free id containing $id")
	}

	private def randUnprintable: String = {
		val idx1 = rand.nextInt(invisTable.size)
		invisTable.values.toList.apply(idx1).toString
	}

	// generates a random ship or station id with the given prefix
	def random(prefix: String, digits: Int = 2): String = {
		var retries = 10

		while(retries > 0) {
			Try[String] {
				def name = s"$prefix${rand(digits)}"

				register(name)
			} match {
				case Failure(_) =>
				case Success(value) => return value
			}
			retries -= 1
		}

		throw new RuntimeException(s"Could not find a free id starting with $prefix")
	}

	private def invisTable = Map(
		'A' -> 'Α',
		'B' -> 'Β',
		'C' -> 'Ϲ',
		'D' -> 'Ḋ',
		'E' -> 'Ε',
		'F' -> 'Ḟ',
		'G' -> 'Ԍ',
		'H' -> 'Η',
		'I' -> 'Ι',
		'J' -> 'Ј',
		'K' -> 'Κ',
		'L' -> 'Ḷ',
		'M' -> 'Μ',
		'N' -> 'Ν',
		'O' -> 'Ο',
		'P' -> 'Ρ',
		'Q' -> 'Ọ',
		'R' -> 'Ṙ',
		'S' -> 'Ѕ',
		'T' -> 'Τ',
		'U' -> 'υ',
		'V' -> 'ᴠ',
		'W' -> 'ᴡ',
		'X' -> 'Χ',
		'Y' -> 'Υ',
		'Z' -> 'Ζ',
		' ' -> ' '
	)

	def registerInvisible(s: String) = register(s.toUpperCase.map(invisTable))

	private def rand(digits: Int): Long = {
		val min = Math.pow(10, digits - 1).toInt
		val max = (Math.pow(10, digits) - 1).toInt
		rand.nextInt(max - min + 1) + min
	}
}
