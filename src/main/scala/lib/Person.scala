package lib

import scala.io.Source
import scala.util.Random

case class Person(firstName: String, lastName: String, gender: Person.Gender, title: Option[String] = None) {
	val name = s"${title.map(_ + " ").getOrElse("")}$firstName $lastName"
}

object Person {

	sealed trait Gender

	object Male extends Gender

	object Female extends Gender

	def apply(): Person = {
		val lastName = PersonName.randomLastName
		val (firstName, gender) = PersonName.randomFirstName
		Person(firstName, lastName, gender)
	}
}

private object PersonName {
	lazy val femaleFirstNames: List[String] = read("names/nl_femaleNames.txt")
	lazy val maleFirstNames: List[String] = read("names/nl_maleNames.txt")
	lazy val lastNames: List[String] = read("names/nl_lastNames.txt")

	private def read(resource: String) = Source.fromResource(resource).getLines.filter(line => line.nonEmpty && !line.startsWith("#")).toList

	private val rand = new Random()

	def randomLastName: String = lastNames(rand.nextInt(lastNames.size))

	def randomFirstName: (String, Person.Gender) = {
		if (rand.nextBoolean()) {
			val theFirstName = femaleFirstNames(rand.nextInt(femaleFirstNames.size))
			(theFirstName, Person.Female)
		} else {
			val theFirstName = maleFirstNames(rand.nextInt(maleFirstNames.size))
			(theFirstName, Person.Male)
		}
	}
}
