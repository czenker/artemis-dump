package mission.zzdefense.modules

import lib.{Event, Module}
import artemis.Porcelain._

import scala.xml.{Comment, NodeSeq}

object EconomyModule extends Module {

	val prefix = "economy"
	val varCredits = s"${prefix}_money"

	val initialCredits: Long = 2000
	val reportInterval = 60

	override def onLoad = set(varCredits, initialCredits)

	override def events: NodeSeq = eventsForReport()

	def eventsForReport(): NodeSeq = {
		val prefix = s"${this.prefix}_report"
		val varEnabled = s"${prefix}_enable"
		val varTimer = s"_${prefix}_timer"

		Seq[NodeSeq](
			Comment(
				s"""$getClass ($prefix)
				   | Send regular reports on the state of the economy""".stripMargin
			),
			Event(
				s"${prefix}_init",
				ifEquals(varEnabled, 1),
				setTimer(varTimer, reportInterval),
				set(varEnabled, 2)
			),
			Event(
				s"${prefix}",
				ifGreaterOrEqual(varEnabled, 2),
				ifTimer(varTimer),
				sendComms("HQ", s"Your current balance is: |,0,$varCredits| credits.", CommsType.Status),
				setTimer(varTimer, reportInterval)
			)
		)
	}

}
