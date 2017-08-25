package modules.al

import lib._
import RaidModule._
import artemis.Porcelain._

import scala.xml.{Comment, NodeSeq}

class RaidModule(conf: Configuration) extends SteppedModule {
	override val name: String = conf.name
	override val prefix: String = conf.prefix
	override protected val trigger: SteppedModule.Trigger = conf.trigger

	val flagship = conf.enemies.head
	val fleet = conf.enemies.tail

	val varShipsDestroyed = s"_${prefix}_ships_destroyed"
	val varShipsFled = s"_${prefix}_ships_fled"
	val varShipsGone = s"_${prefix}_ships_gone"
	val varDirectivesFulfilled = s"_${prefix}_directives_done"

	val varShipStatus = conf.enemies.indices.map(i => s"_${prefix}_ship${i}_step")

	override def onLoad: NodeSeq = super.onLoad ++ set(varShipsDestroyed, 0) ++ set(varShipsFled, 0) ++ set(varShipsGone, 0) ++ varShipStatus.map(s => set(s, 0)) ++ set(varDirectivesFulfilled, 0)

	override def events: NodeSeq = {
		// the step when raiders will retreat
		val retreatStep = 200 + conf.directives.length * 100


		super.events ++ Seq[NodeSeq](
			moduleComment,
			step(100, 101,
				flagship.create(conf.spawnLocation.fuzz(10), Some(conf.fleetNumber)),
				fleet.map(_.create(conf.spawnLocation.atDistance(100), Some(conf.fleetNumber))),
				set(varShipsDestroyed, 0),
				set(varShipsFled, 0),
				set(varShipsGone, 0),
				varShipStatus.map(s => set(s, 0)),
				set(varDirectivesFulfilled, 0)
			),
			step(101, 200,
				flagship.ifCreated,
				fleet.map(_.ifCreated),
				flagship.afterCreate(),
				fleet.map(_.afterCreate())
			),
			conf.directives.zipWithIndex.map { case(directive, idx) =>
				val minStep = 200 + idx * 100
				val nextStep = minStep + 100

				directive match {
					case d: DestroyNeutral => destroyNeutralDirective(d, minStep, nextStep)
				}
			},
			Comment("retreating"),
			step(retreatStep, retreatStep + 1,
				conf.enemies.map(retreatAi)
			),
			varShipStatus.zip(conf.enemies).map {case (s, ship) =>
				Event(
					ifGreaterOrEqual(varStep, retreatStep),
					ifDistanceSmaller(ship.id, conf.spawnLocation, 1000),
					set(s, 2),
					destroy(ship.id),
					set(varShipsFled, s"$varShipsFled + 1"),
					set(varShipsGone, s"$varShipsGone + 1")
				) ++
				Event(
					ifEquals(s, 0),
					ifGreaterOrEqual(varStep, 200),
					ifNotExists(ship.id),
					set(s, 1),
					set(varShipsDestroyed, s"$varShipsDestroyed + 1"),
					set(varShipsGone, s"$varShipsGone + 1")
				)
			},
			conf.finishes.map{ finish =>
				stepWithChat(200.to(900), 9999, finish.chatAndScriptlet.chat,
					ifEquals(varShipsGone, conf.enemies.size),
					rangeToCondition(varShipsDestroyed, finish.pirateShipsDestroyed),
					rangeToCondition(varDirectivesFulfilled, finish.objectivesSuccessful),
					finish.chatAndScriptlet.scriptlet
				)
			},
			// @TODO: differentiate more
			step(200.to(900), 9999,
				Comment("catch all: to make sure module finishes"),
				ifEquals(varShipsGone, conf.enemies.size)
			)
		)
	}

	private def attackAi(ship: EnemyShip, attackId: String) = setAi(ship.id,
//		aiTryToBecomeLeader(),
		aiAttack(attackId),
		aiChasePlayer(5000, 2500),
//		aiLeaderLeads(),
//		aiFollowLeader(),
		aiChaseAnger(),
		aiAvoidBlackHole(),
		// do not launch fighters, because they might attack bases...
		//		if (ship.hull == Brigantine) {aiLaunchFighters()} else { aiNoop },
		aiUseSpecialPowers()
	)

	private def retreatAi(ship: EnemyShip) = setAi(ship.id,
		// no leadership here - because it seems FollowLeader does not translate to "peaceful" commands like
		// moving to a certain position. Pirates are really not easy to work with.
		aiGoto(conf.spawnLocation),
		aiChasePlayer(5000, 2500),
		aiChaseAnger(),
		aiAvoidBlackHole(),
		aiUseSpecialPowers()
	)

	private def destroyNeutralDirective(destroyNeutral: DestroyNeutral, minStep: Int, nextStep: Int): NodeSeq = {
		Comment(s"attacking ${destroyNeutral.shipId}") ++
		step(minStep.until(minStep + 20), nextStep,
			ifNotExists(destroyNeutral.shipId)
		) ++
		stepWithChat(minStep, minStep + 10, destroyNeutral.onBeginning.chat,
			ifExists(destroyNeutral.shipId),
			conf.enemies.map(s => attackAi(s, destroyNeutral.shipId)),
			destroyNeutral.onBeginning.scriptlet,
			setTimer(varTimer, 0.1)
		) ++
		conf.enemies.map { enemy =>
			step((minStep + 10).to(minStep + 20), minStep + 20,
				// make sure ship is not faster than pirates by disabling their engines
				ifExists(enemy.id),
				ifTimer(varTimer),
				ifDistanceSmaller(enemy.id, destroyNeutral.shipId, 2000),
				damageImpulse(destroyNeutral.shipId),
				setTimer(varTimer, 5.0)
			)
		} ++
		stepWithChat(minStep + 20, nextStep, destroyNeutral.onShipDestroyed.chat,
			ifNotExists(destroyNeutral.shipId),
			set(varDirectivesFulfilled, s"$varDirectivesFulfilled + 1"),
			destroyNeutral.onShipDestroyed.scriptlet
		)
	}

	private def moduleComment = {
		Comment(
			s"""$getClass ($prefix)
			   |
			   | TODO
			 """.stripMargin
		)
	}
}

object RaidModule {
	import SteppedModule._

	sealed trait Directive
	case class DestroyNeutral(shipId: String,
	                          onBeginning: ChatAndScriptlet = ChatAndScriptlet(),
	                          onShipDestroyed: ChatAndScriptlet = ChatAndScriptlet()) extends Directive

	case class Finish(objectivesSuccessful: Range, pirateShipsDestroyed: Range, chatAndScriptlet: ChatAndScriptlet)

	case class Configuration(mayBeName: Option[String],
	                         mayBePrefix: Option[String],
	                         spawnLocation: Location,
	                         enemies: Seq[EnemyShip],
	                         directives: Seq[Directive] = Seq.empty,
	                         finishes: Seq[Finish] = Seq.empty,
	                         fleetNumber: Int = 0,
	                         trigger: Trigger = AutoTrigger
	                        ) {
		def get(implicit idRegistry: IdRegistry) = new RaidModule(copy(fleetNumber = idRegistry.fleetnumber))

		lazy val prefix: String = mayBePrefix.getOrElse(s"module_raid_$randomId")
		lazy val name: String = mayBeName.getOrElse(s"Raid Module")

//		def withBeginning(chat: Messages = Messages.Empty, scriptlet: NodeSeq = NodeSeq.Empty) = copy(onBeginning = ChatAndScriptlet(chat, scriptlet))
//		def withBoarding(chat: Messages = Messages.Empty, scriptlet: NodeSeq = NodeSeq.Empty) = copy(onBoarding = ChatAndScriptlet(chat, scriptlet))
//		def withSuccess(chat: Messages = Messages.Empty, scriptlet: NodeSeq = NodeSeq.Empty) = copy(onSuccess = ChatAndScriptlet(chat, scriptlet))

		def addTarget(ship: NeutralShip,
		              onBeginning: ChatAndScriptlet = ChatAndScriptlet(),
		              onShipDestroyed: ChatAndScriptlet = ChatAndScriptlet()) = copy(directives = directives :+ DestroyNeutral(ship.id, onBeginning, onShipDestroyed))

		def withFinish(objectivesSuccessful: Int, pirateShipsDestroyed: Int, chatAndScriptlet: ChatAndScriptlet) = copy(finishes = finishes :+ Finish(objectivesSuccessful.to(objectivesSuccessful), pirateShipsDestroyed.to(pirateShipsDestroyed), chatAndScriptlet))
		def withFinish(objectivesSuccessful: Range, pirateShipsDestroyed: Int, chatAndScriptlet: ChatAndScriptlet) = copy(finishes = finishes :+ Finish(objectivesSuccessful, pirateShipsDestroyed.to(pirateShipsDestroyed), chatAndScriptlet))
		def withFinish(objectivesSuccessful: Int, pirateShipsDestroyed: Range, chatAndScriptlet: ChatAndScriptlet) = copy(finishes = finishes :+ Finish(objectivesSuccessful.to(objectivesSuccessful), pirateShipsDestroyed, chatAndScriptlet))
		def withFinish(objectivesSuccessful: Range, pirateShipsDestroyed: Range, chatAndScriptlet: ChatAndScriptlet) = copy(finishes = finishes :+ Finish(objectivesSuccessful, pirateShipsDestroyed, chatAndScriptlet))

		def withTrigger(trigger: Trigger): Configuration = this.copy(trigger = trigger)
	}

	def apply(name: Option[String] = None, prefix: Option[String] = None, spawnLocation: Location, enemies: Seq[EnemyShip]) = Configuration(name, prefix, spawnLocation, enemies)
}
