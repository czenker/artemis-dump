package artemis

import Commands._
import enumeratum._
import enumeratum.EnumEntry.Uppercase

/**
  * The various ways to communicate with the ship crew
  */
trait CommsPorcelain extends BasePorcelain {
	implicit def commsTypeToOpt(c: CommsType): Option[CommsType] = Some(c)

	sealed trait CommsType
	object CommsType {
		object Alert   extends CommsType
		object Side    extends CommsType
		object Status  extends CommsType
		object Player  extends CommsType
		object Station extends CommsType
		object Enemy   extends CommsType
		object Friend  extends CommsType
	}

	// convert a text block with newlines into the format artemis understands
	private def formatTextBlock(s: String): String = s.replaceAll("\n", "^")
	private def formatTextBlock(s: Option[String]): Option[String] = s.map(formatTextBlock)

	def sendComms(name: String, message: String, commsType: Option[CommsType] = None, sideValue: Option[Int] = None) = incoming_comms_text(name, formatTextBlock(message), commsType.map(_.toString), sideValue)

	def showTitle(title: String, subLine: Option[String] = None, bottomLine: Option[String] = None ) = big_message(title, subLine, bottomLine)

	sealed abstract class Console(val id: String)
	object Console {
		object MainScreen extends Console("M")
		object Helm extends Console("H")
		object Weapons extends Console("W")
		object Engineering extends Console("E")
		object Science extends Console("S")
		object Comms extends Console("C")
		object Observer extends Console("O")
	}

	def popup(message: String, console: Console) = warning_popup_message(message, console.id)
	def popupHelm(message: String) = warning_popup_message(message, Console.Helm.id)
	def popupWeapons(message: String) = warning_popup_message(message, Console.Weapons.id)
	def popupEngineering(message: String) = warning_popup_message(message, Console.Engineering.id)
	def popupScience(message: String) = warning_popup_message(message, Console.Science.id)

}
