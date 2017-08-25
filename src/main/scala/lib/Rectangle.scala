package lib

case class Rectangle(minX: Double, maxX: Double, minZ: Double, maxZ: Double)

object Rectangle {
	def apply(sector1: Sector, sector2: Sector): Rectangle = Rectangle(
		Math.min(sector1.minX, sector2.minX),
		Math.max(sector1.maxX, sector2.maxX),
		Math.min(sector1.minZ, sector2.minZ),
		Math.max(sector1.maxZ, sector2.maxZ)
	)
}
