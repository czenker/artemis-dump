package lib

// something the Artemis can dock at
trait Dockable {
	def id: String
	def location: Location
}
