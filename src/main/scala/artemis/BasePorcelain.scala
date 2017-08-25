package artemis

import lib.RGB

trait BasePorcelain {
	implicit def stringToOpt(s: String): Option[String] = Some(s)
	implicit def doubleToOpt(d: Double): Option[Double] = Some(d)
	implicit def doubleToOpt(d: Long): Option[Long] = Some(d)
	implicit def intToOpt(i: Int): Option[Int] = Some(i)
	implicit def colorToOpt(color: RGB): Option[RGB] = Some(color)
}
