//package modules.al
//
//import artemis.Porcelain._
//import lib._
//import modules.al.ChatModule._
//
//import scala.util.Random
//import scala.xml.NodeSeq
//
//trait ChatModule extends Module {
//	// will start sending the messages
//	def startChat: NodeSeq = NodeSeq.Empty
//	// will make sure no further messages are sent
//	def endChat: NodeSeq = NodeSeq.Empty
//}
//
//class SimpleChatModule(message: Message) extends ChatModule {
//	override def startChat: NodeSeq = sendComms(message.from, message.message, message.commsType)
//}
//
//// send messages asynchronously, not using any variables of the calling module
//class AsyncChatModule(prefix: String, messages: Messages) extends ChatModule {
//
//	protected val varStep = s"${prefix}_step"
//	protected val varTimer = s"_${prefix}_timer"
//
//	override def startChat = sendComms(messages.head.from, messages.head.message, messages.head.commsType) ++ set(varStep, 1) ++ setTimer(varTimer, messages.head.timeToNext)
//	override def endChat = set(varStep, 0)
//
//	override def onLoad: NodeSeq = set(varStep, 0)
//	override def events: NodeSeq = super.events ++ Seq[NodeSeq](
//		messages.tail.toList.zipWithIndex.map { case (message, idx) =>
//			val isLast: Boolean = idx == messages.tail.length - 1
//			val id = idx + 1
//
//			Event(s"${prefix}_$id",
//				ifEquals(varStep, id),
//				ifTimer(varTimer),
//
//				sendComms(message.from, message.message, message.commsType),
//
//				if (isLast) {
//					set(varStep, 0)
//				} else {
//					set(varStep, id + 1) ++
//					setTimer(varTimer, message.timeToNext)
//				}
//			)
//		}
//	)
//}
//
//// send messages in a loop
//class LoopChatModule(prefix: String, messages: Messages, initialDelay: Double = 0.0) extends ChatModule {
//
//	protected val varStep = s"${prefix}_step"
//	protected val varTimer = s"_${prefix}_timer"
//
//	override def startChat = set(varStep, 1) ++ setTimer(varTimer, initialDelay)
//	override def endChat = set(varStep, 0)
//
//	override def onLoad: NodeSeq = set(varStep, 0)
//	override def events: NodeSeq = super.events ++ Seq[NodeSeq](
//		messages.toList.zipWithIndex.map { case (message, idx) =>
//			val isLast: Boolean = idx == messages.length - 1
//			val id = idx + 1
//
//			Event(s"${prefix}_$id",
//				ifEquals(varStep, id),
//				ifTimer(varTimer),
//
//				sendComms(message.from, message.message, message.commsType),
//
//				set(varStep, if(isLast) 1 else id + 1),
//				setTimer(varTimer, message.timeToNext)
//			)
//		}
//	)
//}
//
//// first it just fires a few one-time messages, but sticks in an infinite loop afterwards. Nice thing if you want to tempt
//// players into accepting a quest or taking an action
//class PartialLoopChatModule(prefix: String, messages: Messages, loopedMessages: Messages, initialDelay: Double = 0.0) extends ChatModule {
//
//	protected val varStep = s"${prefix}_step"
//	protected val varTimer = s"_${prefix}_timer"
//
//	override def startChat = set(varStep, 1) ++ setTimer(varTimer, initialDelay)
//	override def endChat = set(varStep, 0)
//
//	override def onLoad: NodeSeq = set(varStep, 0)
//	override def events: NodeSeq = super.events ++ Seq[NodeSeq](
//		messages.toList.zipWithIndex.map { case (message, idx) =>
//			val id = idx + 1
//
//			Event(s"${prefix}_$id",
//				ifEquals(varStep, id),
//				ifTimer(varTimer),
//
//				sendComms(message.from, message.message, message.commsType),
//
//				set(varStep, id + 1),
//				setTimer(varTimer, message.timeToNext)
//			)
//		},
//		loopedMessages.toList.zipWithIndex.map { case (message, idx) =>
//			val isLast: Boolean = idx == messages.length - 1
//			val id = idx + messages.length + 1
//
//			Event(s"${prefix}_$id",
//				ifEquals(varStep, id),
//				ifTimer(varTimer),
//
//				sendComms(message.from, message.message, message.commsType),
//
//				set(varStep, if(isLast) messages.length + 1 else id + 1),
//				setTimer(varTimer, message.timeToNext)
//			)
//		}
//	)
//}
//
//object ChatModule {
//
//	private val rand = new Random()
//
//	def apply(message: Message): ChatModule = new SimpleChatModule(message)
//	def apply(messages: Message*): ChatModule = new AsyncChatModule(s"chat_${rand.alphanumeric.slice(0, 15).foldLeft("")(_ + _)}", messages)
//
//	def loop(messages: Message*): ChatModule = new LoopChatModule(s"chat_${rand.alphanumeric.slice(0, 15).foldLeft("")(_ + _)}", messages)
//	def loop(initialDelay: Double, messages: Message*): ChatModule = new LoopChatModule(s"chat_${rand.alphanumeric.slice(0, 15).foldLeft("")(_ + _)}", messages, initialDelay)
//
//	def partialLoop(messages: Seq[Message], loopedMessages: Seq[Message], initialDelay: Double = 0.0) = {
//		assert(messages.nonEmpty, "messages should not be empty")
//		assert(loopedMessages.nonEmpty, "loopedMessages should not be empty")
//		new PartialLoopChatModule(s"chat_${rand.alphanumeric.slice(0, 15).foldLeft("")(_ + _)}", messages, loopedMessages, initialDelay)
//	}
//
//}
