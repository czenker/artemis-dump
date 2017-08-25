package modules.al

import lib._
import modules.al.Chat._
import artemis.Porcelain._

import scala.xml.{Elem, NodeSeq}

sealed trait Chat {
	protected def sendMessage(message: Message): NodeSeq = sendComms(message.from, message.message, message.commsType)
}

// a chat that uses all the variables supported by a SteppedModule
case class SteppedChat(messages: Messages, varStep: String, varTimer: String, initialStep: Int, finishStep: Int, onFinish: NodeSeq = NodeSeq.Empty) extends Chat {

	if (initialStep >= finishStep) throw new IllegalArgumentException("finishStep(%d) should be bigger than currentStep(%d).".format(finishStep, initialStep))
	if (initialStep + messages.size > finishStep) throw new IllegalArgumentException("The number of %d messages is too much for this chat. Please reduce the number to %d or less.".format(messages.length, finishStep - initialStep))

	def onStart: NodeSeq = if (messages.isEmpty) {
		onFinish ++
		set(varStep, finishStep)
	} else if(messages.length == 1) {
		sendMessage(messages.head) ++
		onFinish ++
		set(varStep, finishStep)
	} else {
		sendMessage(messages.head) ++
		setTimer(varTimer, messages.head.timeToNext) ++
		set(varStep, initialStep + 1)
	}

	def events: NodeSeq = if (messages.length <= 1) {
		noop
	} else {
		messages.tail.zipWithIndex.map{ case (message, i) =>
			val isLast = i == messages.length - 2
			val currentStep = initialStep + i + 1
			Event(
				ifEquals(varStep, currentStep),
				ifTimer(varTimer),
				sendMessage(message),
				if (isLast) {
					onFinish ++ set(varStep, finishStep)
				} else {
					setTimer(varTimer, message.timeToNext) ++
					set(varStep, currentStep + 1)
				}
			)
		}
	}
}

object Chat {
	// the words per minute the comms officer is supposed to read
	private val wordsPerMinute = 150

//	val Empty: Chat = new SteppedChat {
//		override def messages: Messages = Messages(Seq.empty)
//	}

	case class Message(from: String, message: String, commsType: Option[CommsType], timeToNext: Double)

	object Message {
		def apply(from: String, message: String, timeToNext: Double, commsType: Option[CommsType]): Message = apply(from, message, commsType, timeToNext)

		def apply(from: String, message: String, commsType: Option[CommsType]): Message = {
			val words = message.split("\\W+").length

			apply(from, message, commsType, words.toDouble / wordsPerMinute * 60 + 1)
		}

		def apply(ship: NeutralShip, message: String): Message = apply(s"${ship.captain.name} (${ship.id})", message, Some(CommsType.Friend))
		def apply(ship: NeutralShip, message: String, timeToNext: Double): Message = apply(s"${ship.captain.name} (${ship.id})", message, timeToNext, Some(CommsType.Friend))

		def apply(ship: EnemyShip, message: String): Message = apply(s"${ship.captain.name} (${ship.id})", message, Some(CommsType.Enemy))
		def apply(ship: EnemyShip, message: String, timeToNext: Double): Message = apply(s"${ship.captain.name} (${ship.id})", message, timeToNext, Some(CommsType.Enemy))

		def apply(station: Dockable, message: String): Message = apply(
			station match {
				case s: Dockable with NamedLocation => s"${s.name} (${s.id})"
				case _ => station.id
			},
			message,
			Some(CommsType.Station)
		)
		def apply(station: Dockable, message: String, timeToNext: Double): Message = apply(
			station match {
				case s: Dockable with NamedLocation => s"${s.name} (${s.id})"
				case _ => station.id
			},
			message,
			timeToNext,
			Some(CommsType.Station)
		)

		def friend(person: Person, message: String): Message = apply(person.name, message, Some(CommsType.Friend))
		def friend(person: Person, message: String, timeToNext: Double): Message = apply(person.name, message, timeToNext, Some(CommsType.Friend))
	}

	case class Messages(messages: Seq[Message]) {
		// distribute messages evenly over totalSeconds
		def distributeEven(totalSeconds: Int): Messages = {
			val timeToNext = totalSeconds.toDouble / (messages.length - 1)
			Messages(messages.map(_.copy(timeToNext = timeToNext)))
		}
	}

	object Messages {
		val Empty = Messages(Seq.empty)
	}

	implicit def seqToMessages(messages: Seq[Message]): Messages = Messages(messages)
	implicit def messagesToSeq(messages: Messages): Seq[Message] = messages.messages
	implicit def messageToMessages(message: Message): Messages = Messages(Seq(message))

}
