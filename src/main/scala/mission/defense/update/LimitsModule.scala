package mission.defense.update

import lib._
import mission.defense.update.LimitsModule.Configuration
import artemis.Porcelain._
import lib.SteppedModule.AutoTrigger

import scala.xml.NodeSeq

class LimitsModule(conf: Configuration) extends SteppedModule {
	// @TODO: Could be an "Enablable Module" instead

	override val name: String = conf.name
	override val prefix: String = conf.prefix
	override protected val trigger: SteppedModule.Trigger = AutoTrigger

	val varMaxWarp = s"_${conf.prefix}_warp"


	override def disable = super.disable ++ endModule

	override def onLoad = super.onLoad ++ set(varMaxWarp, 0)

	override def events = super.events ++ NodeSeq.fromSeq(Seq[NodeSeq](
		// allow a brief boost, but quickly damage afterwards
		Event(
			ifGreater(varStep, 100),
			ifSmaller(varStep, 200),
			ifPropertySmallerOrEqual(conf.player.id, "warpState", s"$varMaxWarp"),
			set(varStep, 100)
		),
		step(100, 101,
			ifPropertyEquals(conf.player.id, "warpState", s"$varMaxWarp + 1"),
			setTimer(varTimer, 5.0)
		),
		step(101, 102,
			ifTimer(varTimer),
			ifPropertyEquals(conf.player.id, "warpState", s"$varMaxWarp + 1"),
			damagePlayerWarp(conf.player.id, systems = Seq(0)),
			setTimer(varTimer, 2.0)
		),
		step(102, 103,
			ifTimer(varTimer),
			ifPropertyEquals(conf.player.id, "warpState", s"$varMaxWarp + 1"),
			damagePlayerWarp(conf.player.id, systems = Seq(1)),
			setTimer(varTimer, 2.0)
		),
		step(103, 104,
			ifTimer(varTimer),
			ifPropertyEquals(conf.player.id, "warpState", s"$varMaxWarp + 1"),
			damagePlayerWarp(conf.player.id, systems = Seq(2)),
			setTimer(varTimer, 2.0)
		),
		step(104, 101,
			ifTimer(varTimer),
			ifPropertyEquals(conf.player.id, "warpState", s"$varMaxWarp + 1"),
			damagePlayerWarp(conf.player.id, systems = Seq(3)),
			setTimer(varTimer, 2.0)
		),
		// if players are waaaay to fast, kill their warp really fast too
		step(
			100.to(104), 110,
			ifPropertyGreaterOrEqual(conf.player.id, "warpState", s"$varMaxWarp + 2"),
			setTimer(varTimer, 1.0)
		),
		step(110, 110,
			ifTimer(varTimer),
			ifPropertyGreaterOrEqual(conf.player.id, "warpState", s"$varMaxWarp + 1"),
			damagePlayerWarp(conf.player.id),
			setTimer(varTimer, 1.0)
		)
	))


}

object LimitsModule {

	case class Configuration(mayBeName: Option[String] = None,
	                         mayBePrefix: Option[String] = None,
	                         player: Player) {
		def get = new LimitsModule(this)

		lazy val prefix: String = mayBePrefix.getOrElse(s"module_defense_ship_limit")
		lazy val name: String = mayBeName.getOrElse(s"Ship Limit Module")
	}

	def apply(player: Player) = Configuration(player = player)
}
