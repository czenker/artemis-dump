package lib

sealed trait Sector {
	def name: String
	def minX: Long
	def maxX: Long
	def minZ: Long
	def maxZ: Long
}

object Sector {
	val all = List(
		SectorA1,
		SectorA2,
		SectorA3,
		SectorA4,
		SectorA5,
		SectorB1,
		SectorB2,
		SectorB3,
		SectorB4,
		SectorB5,
		SectorC1,
		SectorC2,
		SectorC3,
		SectorC4,
		SectorC5,
		SectorD1,
		SectorD2,
		SectorD3,
		SectorD4,
		SectorD5,
		SectorE1,
		SectorE2,
		SectorE3,
		SectorE4,
		SectorE5
	)

}

object SectorA5 extends Sector {
	val name = "A5"
	val minX: Long = 0
	val maxX: Long = 20000
	val minZ: Long = 0
	val maxZ: Long = 20000
}
object SectorA4 extends Sector {
	val name = "A4"
	val minX: Long = 20000
	val maxX: Long = 40000
	val minZ: Long = 0
	val maxZ: Long = 20000
}
object SectorA3 extends Sector {
	val name = "A3"
	val minX: Long = 40000
	val maxX: Long = 60000
	val minZ: Long = 0
	val maxZ: Long = 20000
}
object SectorA2 extends Sector {
	val name = "A2"
	val minX: Long = 60000
	val maxX: Long = 80000
	val minZ: Long = 0
	val maxZ: Long = 20000
}
object SectorA1 extends Sector {
	val name = "A1"
	val minX: Long = 80000
	val maxX: Long = 100000
	val minZ: Long = 0
	val maxZ: Long = 20000
}
object SectorB5 extends Sector {
	val name = "B5"
	val minX: Long = 0
	val maxX: Long = 20000
	val minZ: Long = 20000
	val maxZ: Long = 40000
}
object SectorB4 extends Sector {
	val name = "B4"
	val minX: Long = 20000
	val maxX: Long = 40000
	val minZ: Long = 20000
	val maxZ: Long = 40000
}
object SectorB3 extends Sector {
	val name = "B3"
	val minX: Long = 40000
	val maxX: Long = 60000
	val minZ: Long = 20000
	val maxZ: Long = 40000
}
object SectorB2 extends Sector {
	val name = "B2"
	val minX: Long = 60000
	val maxX: Long = 80000
	val minZ: Long = 20000
	val maxZ: Long = 40000
}
object SectorB1 extends Sector {
	val name = "B1"
	val minX: Long = 80000
	val maxX: Long = 100000
	val minZ: Long = 20000
	val maxZ: Long = 40000
}
object SectorC5 extends Sector {
	val name = "C5"
	val minX: Long = 0
	val maxX: Long = 20000
	val minZ: Long = 40000
	val maxZ: Long = 60000
}
object SectorC4 extends Sector {
	val name = "C4"
	val minX: Long = 20000
	val maxX: Long = 40000
	val minZ: Long = 40000
	val maxZ: Long = 60000
}
object SectorC3 extends Sector {
	val name = "C3"
	val minX: Long = 40000
	val maxX: Long = 60000
	val minZ: Long = 40000
	val maxZ: Long = 60000
}
object SectorC2 extends Sector {
	val name = "C2"
	val minX: Long = 60000
	val maxX: Long = 80000
	val minZ: Long = 40000
	val maxZ: Long = 60000
}
object SectorC1 extends Sector {
	val name = "C1"
	val minX: Long = 80000
	val maxX: Long = 100000
	val minZ: Long = 40000
	val maxZ: Long = 60000
}
object SectorD5 extends Sector {
	val name = "D5"
	val minX: Long = 0
	val maxX: Long = 20000
	val minZ: Long = 60000
	val maxZ: Long = 80000
}
object SectorD4 extends Sector {
	val name = "D4"
	val minX: Long = 20000
	val maxX: Long = 40000
	val minZ: Long = 60000
	val maxZ: Long = 80000
}
object SectorD3 extends Sector {
	val name = "D3"
	val minX: Long = 40000
	val maxX: Long = 60000
	val minZ: Long = 60000
	val maxZ: Long = 80000
}
object SectorD2 extends Sector {
	val name = "D2"
	val minX: Long = 60000
	val maxX: Long = 80000
	val minZ: Long = 60000
	val maxZ: Long = 80000
}
object SectorD1 extends Sector {
	val name = "D1"
	val minX: Long = 80000
	val maxX: Long = 100000
	val minZ: Long = 60000
	val maxZ: Long = 80000
}
object SectorE5 extends Sector {
	val name = "E5"
	val minX: Long = 0
	val maxX: Long = 20000
	val minZ: Long = 80000
	val maxZ: Long = 100000
}
object SectorE4 extends Sector {
	val name = "E4"
	val minX: Long = 20000
	val maxX: Long = 40000
	val minZ: Long = 80000
	val maxZ: Long = 100000
}
object SectorE3 extends Sector {
	val name = "E3"
	val minX: Long = 40000
	val maxX: Long = 60000
	val minZ: Long = 80000
	val maxZ: Long = 100000
}
object SectorE2 extends Sector {
	val name = "E2"
	val minX: Long = 60000
	val maxX: Long = 80000
	val minZ: Long = 80000
	val maxZ: Long = 100000
}
object SectorE1 extends Sector {
	val name = "E1"
	val minX: Long = 80000
	val maxX: Long = 100000
	val minZ: Long = 80000
	val maxZ: Long = 100000
}