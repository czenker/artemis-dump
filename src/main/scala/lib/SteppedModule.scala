package lib

import artemis.Porcelain._
import lib.SteppedModule._
import modules.al.Chat.Messages
import modules.al.SteppedChat

import scala.util.Random
import scala.xml.{Comment, Elem, Node, NodeSeq}

// helps create modules that are step-by-step
abstract class SteppedModule extends Module {

	val name: String
	val prefix: String

	protected val trigger: Trigger
	protected val loopedModule: Boolean = false

	protected lazy val varEnabled = s"${prefix}_enable"
	protected lazy val varStep = s"_${prefix}_step"
	protected lazy val varTimer = s"_${prefix}_timer"

	protected lazy val textStart = s"Mission/Start $name"
	protected lazy val textAbort = s"Mission/Abort $name"

	override def onLoad = set(varEnabled, 0) ++ set(varStep, 0)
	override def events = super.events ++ (trigger match {
		case GmTrigger => gmTrigger
		case AutoTrigger => autoTrigger
	})

	private def gmTrigger: Seq[NodeSeq] = Seq(
		Comment(
			s"""Constructor for $name
			   |To start the module set $varEnabled to 1
			   |To make the module stop in a graceful way set $varEnabled to 0""".stripMargin),
		step(0, 1,
			ifEnabled,
			setGmButton(textStart)
		),
		step(1, 100,
			ifGmButton(textStart),
			clearGmButton(textStart),
			setGmButton(textAbort)
		),
		Comment(
			s"""Destructor for $name""".stripMargin),
		Event(
			ifGmButton(textAbort),
			setStep(9999)
		),
		step(9999, 0,
			clearGmButton(textStart),
			clearGmButton(textAbort),
			onDestruct
		)
	)

	private def autoTrigger: Seq[NodeSeq] = Seq(
		Comment(
			s"""Constructor for $name
			   |To start the module set $varEnabled to 1
			   |To make the module stop in a graceful way set $varEnabled to 0""".stripMargin),
		step(0, 100,
			ifEnabled
		),
		step(9999, 0,
			onDestruct
		)
	)

	def onDestruct: NodeSeq = if (loopedModule) {NodeSeq.Empty} else {disable}

	def enable: NodeSeq = set(varEnabled, 1)
	def disable: NodeSeq = set(varEnabled, 0)
	protected def ifEnabled = ifEquals(varEnabled, 1)
	protected def ifStep(step: Int) = ifEquals(varStep, step)
	protected def setStep(step: Int) = set(varStep, step)
	def endModule = set(varStep, 9999)

	def step(step: Int, nextStep: Int, nodes: NodeSeq*): NodeSeq = Event(
		s"${prefix}_${step}",
		ifEquals(varStep, step),
		nodes,
		if(step == nextStep) NodeSeq.Empty else set(varStep, nextStep)
	)

	def stepWithChat(step: Int, nextStep: Int, messages: Messages, nodes: NodeSeq*): NodeSeq = {
		val (onFinishNodes, initialNodes) = nodes.partition(node => node.length == 1 && node.head.label == "set_timer")
		val chat = SteppedChat(messages, varStep, varTimer, step, nextStep, onFinish = onFinishNodes)

		Event(
			s"${prefix}_${step}",
			ifEquals(varStep, step),
			initialNodes,
			chat.onStart
		) ++ chat.events
	}

	def step(step: Range, nextStep: Int, nodes: NodeSeq*): NodeSeq = Event(
		s"${prefix}_${step.start}-${step.end}",
		rangeToCondition(varStep, step),
		nodes,
		set(varStep, nextStep)
	)

	def stepWithChat(step: Range, nextStep: Int, messages: Messages, nodes: NodeSeq*): NodeSeq = {
		val (onFinishNodes, initialNodes) = nodes.partition(node => node.length == 1 && node.head.label == "set_timer")
		val chat = SteppedChat(messages, varStep, varTimer, step.last, nextStep, onFinish = onFinishNodes)

		Event(
			s"${prefix}_${step.start}-${step.end}",
			rangeToCondition(varStep, step),
			initialNodes,
			chat.onStart
		) ++ chat.events
	}

	protected def rangeToCondition(varName: String, range: Range): NodeSeq = NodeSeq.fromSeq(Seq(
		ifGreaterOrEqual(varStep, range.start),
		if (range.isInclusive) ifSmallerOrEqual(varStep, range.end) else ifSmaller(varStep, range.end)
	))
}

object SteppedModule {
	sealed trait Trigger
	object GmTrigger extends Trigger
	object AutoTrigger extends Trigger
	// @TODO TimedTrigger

	case class ChatAndScriptlet(chat: Messages = Messages.Empty, scriptlet: NodeSeq = NodeSeq.Empty) {
		def nonEmpty = chat.nonEmpty || scriptlet.nonEmpty
	}

	private val rand = new Random()
	def randomId = "%x".format(rand.nextInt())
}