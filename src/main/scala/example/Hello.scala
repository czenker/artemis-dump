package example

import java.nio.charset.StandardCharsets
import java.nio.file.{Files, Paths}

import mission.Defense

object Hello extends App {

	val mission = Defense

	val prettyPrinter = new scala.xml.PrettyPrinter(240, 4)
	val prettyXml = prettyPrinter.format(mission.toNode)

	Files.write(Paths.get("MISS_defence.XML"), prettyXml.getBytes(StandardCharsets.UTF_8))
}
