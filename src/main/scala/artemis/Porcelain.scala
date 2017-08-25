package artemis
import Commands._
import artemis.Porcelain._
import lib.{Location, RGB}

import scala.xml.{NodeSeq, Text}

object Porcelain extends CommsPorcelain with AiPorcelain with StationPorcelain {

	def createStation(name: String,
	                  hull: Hull,
	                  location: Location,
	                  angle: Option[Double] = None
	                 ) = create(
		Some(name),
		"station",
		x = Some(location.x),
		y = Some(location.y),
		z = Some(location.z),
		angle = angle,
		raceKeys = hull.race.name,
		hullKeys = hull.className
	)

	def createPlayer(
	                 name: Option[String] = None,
	                 hull: Hull,
	                 location: Location,
	                 angle: Option[Double] = None,
	                 player_slot: Option[Int] = None,
	                 accent_color: Option[Int] = None,
	                 warp: Option[Boolean] = None,
	                 jump: Option[Boolean] = None
	                ) = create(
		name = name,
		`type` = "player",
		x = Some(location.x),
		y = Some(location.y),
		z = Some(location.z),
		angle = angle,
		raceKeys = hull.race.name,
		hullKeys = hull.className,
		player_slot = player_slot,
		accent_color = accent_color,
		warp = warp,
		jump = jump
	)

	def createMesh(name: Option[String] = None,
	               location: Location,
	               angle: Option[Double] = None,
	               meshFileName: String = "invis.dxs",
	               textureFileName: String = "dat\\artemis_diffuse.png",
	               fakeShieldsFront: Option[Int] = None,
	               fakeShieldsRear: Option[Int] = None,
	               hasFakeShldFreq: Option[Boolean] = None,
	               color: Option[RGB] = None) = create(
		name = name,
		`type` = "genericMesh",
		x = Some(location.x),
		y = Some(location.y),
		z = Some(location.z),
		angle = angle,
		meshFileName = Some(meshFileName),
		textureFileName = Some(textureFileName),
		fakeShieldsFront = fakeShieldsFront,
		fakeShieldsRear = fakeShieldsRear,
		hasFakeShldFreq = hasFakeShldFreq,
		colorRed = color.map(_.r.toDouble / 255),
		colorGreen = color.map(_.g.toDouble / 255),
		colorBlue = color.map(_.b.toDouble / 255))

	def createPoi(name: String,
	               location: Location,
	               color: Option[RGB] = None) = create(
		name = name,
		`type` = "genericMesh",
		x = Some(location.x),
		y = Some(location.y),
		z = Some(location.z),
		meshFileName = Some("invis.dxs"),
		textureFileName = Some("dat\\artemis_diffuse.png"),
		colorRed = color.map(_.r.toDouble / 255),
		colorGreen = color.map(_.g.toDouble / 255),
		colorBlue = color.map(_.b.toDouble / 255))

	def createPoiAtGmPosition(name: String,
	              color: Option[RGB] = None) = create(
		name = name,
		`type` = "genericMesh",
		use_gm_position = true,
		meshFileName = Some("invis.dxs"),
		textureFileName = Some("dat\\artemis_diffuse.png"),
		colorRed = color.map(_.r.toDouble / 255),
		colorGreen = color.map(_.g.toDouble / 255),
		colorBlue = color.map(_.b.toDouble / 255))

	def createNeutral(name: Option[String] = None,
	                  hull: Hull,
	                  location: Location,
	                  angle: Option[Double] = None) = create(
		name = name,
		`type` = "neutral",
		x = Some(location.x),
		y = Some(location.y),
		z = Some(location.z),
		angle = angle,
		raceKeys = hull.race.name,
		hullKeys = hull.className)

	def createEnemy(name: Option[String] = None,
	                hull: Hull,
                    location: Location,
                    fleetNumber: Option[Int] = None,
	                angle: Option[Double] = None) = create(
		name = name,
		`type` = "enemy",
		x = Some(location.x),
		y = Some(location.y),
		z = Some(location.z),
		angle = angle,
		raceKeys = hull.race.name,
		hullKeys = hull.className,
		fleetnumber = fleetNumber)

	def createMonster(name: Option[String] = None,
	                  monsterType: Int,
                    location: Location,
                      podNumber: Option[Int] = None,
	                angle: Option[Double] = None) = create(
		name = name,
		`type` = "monster",
		x = Some(location.x),
		y = Some(location.y),
		z = Some(location.z),
		angle = angle,
		monsterType = monsterType,
		podnumber = podNumber)

	def createAsteroidsCircle(
		                         count: Int,
		                         startX: Double,
		                         startY: Double,
		                         startZ: Double,
		                         radius: Option[Long] = None,
		                         randomRange: Option[Long] = None,
		                         randomSeed: Option[Long] = None
	                         ) = create(
		`type` = "asteroids",
		count = Some(count),
		startX = Some(startX),
		startY = Some(startY),
		startZ = Some(startZ),
		radius = radius,
		randomRange = randomRange,
		randomSeed = randomSeed
	)
	def createAsteroidsLine(
		                         count: Int,
		                         startX: Double,
		                         startY: Double,
		                         startZ: Double,
		                         endX: Double,
		                         endY: Double,
		                         endZ: Double,
		                         randomRange: Option[Long] = None,
		                         randomSeed: Option[Long] = None
	                         ) = create(
		`type` = "asteroids",
		count = Some(count),
		startX = Some(startX),
		startY = Some(startY),
		startZ = Some(startZ),
		endX = Some(endX),
		endY = Some(endY),
		endZ = Some(endZ),
		randomRange = randomRange,
		randomSeed = randomSeed
	)
	def createAsteroidsArc(
		                         count: Int,
		                         startX: Double,
		                         startY: Double,
		                         startZ: Double,
		                         startAngle: Long,
		                         endAngle: Long,
		                         radius: Option[Long] = None,
		                         randomRange: Option[Long] = None,
		                         randomSeed: Option[Long] = None
	                         ) = create(
		`type` = "asteroids",
		count = Some(count),
		startX = Some(startX),
		startY = Some(startY),
		startZ = Some(startZ),
		startAngle = Some(startAngle),
		endAngle = Some(endAngle),
		radius = radius,
		randomRange = randomRange,
		randomSeed = randomSeed
	)
	def createNebulasCircle(
		                         count: Int,
		                         startX: Double,
		                         startY: Double = 0.0,
		                         startZ: Double,
		                         radius: Option[Long] = None,
		                         randomRange: Option[Long] = None,
		                         randomSeed: Option[Long] = None
	                         ) = create(
		`type` = "nebulas",
		count = Some(count),
		startX = Some(startX),
		startY = Some(startY),
		startZ = Some(startZ),
		radius = radius,
		randomRange = randomRange,
		randomSeed = randomSeed
	)
	def createNebulasLine(
		                         count: Int,
		                         startX: Double,
		                         startY: Double = 0.0,
		                         startZ: Double,
		                         endX: Double,
		                         endY: Double = 0.0,
		                         endZ: Double,
		                         randomRange: Option[Long] = None,
		                         randomSeed: Option[Long] = None
	                         ) = create(
		`type` = "nebulas",
		count = Some(count),
		startX = Some(startX),
		startY = Some(startY),
		startZ = Some(startZ),
		endX = Some(endX),
		endY = Some(endY),
		endZ = Some(endZ),
		randomRange = randomRange,
		randomSeed = randomSeed
	)
	def createNebulasArc(
		                         count: Int,
		                         startX: Double,
		                         startY: Double = 0.0,
		                         startZ: Double,
		                         startAngle: Long,
		                         endAngle: Long,
		                         radius: Option[Long] = None,
		                         randomRange: Option[Long] = None,
		                         randomSeed: Option[Long] = None
	                         ) = create(
		`type` = "nebulas",
		count = Some(count),
		startX = Some(startX),
		startY = Some(startY),
		startZ = Some(startZ),
		startAngle = Some(startAngle),
		endAngle = Some(endAngle),
		radius = radius,
		randomRange = randomRange,
		randomSeed = randomSeed
	)

	def createBlackHole(name: String, location: Location) = create(
		Some(name),
		"blackHole",
		x = Some(location.x),
		y = Some(location.y),
		z = Some(location.z)
	)

	def createShark(name: String, location: Location) = create(
		Some(name),
		"monster",
		monsterType = 2,
		x = Some(location.x),
		y = Some(location.y),
		z = Some(location.z)
	)

	def createDragon(name: String, location: Location) = create(
		Some(name),
		"monster",
		monsterType = 3,
		x = Some(location.x),
		y = Some(location.y),
		z = Some(location.z)
	)

	def setSkyboxIndex(index: Int) = set_skybox_index(index)

	def setGmButton(text: String, x: Option[Int] = None, y: Option[Int] = None, width: Option[Int] = None, height: Option[Int] = None, menuWidth: Option[Int] = None) = set_gm_button(text, x, y, width, height, menuWidth)
	def setGmButton(text: String, x: Int, y: Int) = set_gm_button(text, Some(x), Some(y))
	def setGmButton(text: String, x: Int, y: Int, width: Int, height: Int) = set_gm_button(text, Some(x), Some(y), Some(width), Some(height))

	def ifGmButton(text: String) = if_gm_button(text)
	def clearGmButton(text: String) = clear_gm_button(text)

	def setCommsButton(text: String) = set_comms_button(text)
	def ifCommsButton(text: String) = if_comms_button(text)
	def clearCommsButton(text: String) = clear_comms_button(text)

	def setToGmPosition(name: String, angle: Option[Double] = None, distance: Option[Double] = None) = set_to_gm_position(Some(name), angle = angle, distance = distance)
	def setSelectionToGmPosition(angle: Option[Double] = None, distance: Option[Double] = None) = set_to_gm_position(use_gm_selection = true, angle = angle, distance = distance)

	def destroy(name: String) = Commands.destroy(Some(name))
	def destroyGmSelection() = Commands.destroy(use_gm_selection = true)

	def set(name: String, value: Long) = set_variable(name, Some(value.toString), true)
	def setInt(name: String, value: String) = set_variable(name, Some(value), true)
	def set(name: String, value: Double) = set_variable(name, Some(value.toString), false)
	def set(name: String, value: String) = set_variable(name, Some(value), false)
	def setFloat(name: String, value: String) = set_variable(name, Some(value), false)
	def setRandomInt(name: String, low: Long, high: Long) = set_variable(name, randomIntLow = Some(low.toString), randomIntHigh = Some(high.toString), integer = true)
	def setRandomInt(name: String, low: Double, high: Double) = set_variable(name, randomIntLow = Some(low.toString), randomIntHigh = Some(high.toString), integer = true)
	def setRandomInt(name: String, low: String, high: String) = set_variable(name, randomIntLow = Some(low), randomIntHigh = Some(high), integer = true)
	def setRandomFloat(name: String, low: Long, high: Long) = set_variable(name, randomFloatLow = Some(low.toString), randomFloatHigh = Some(high.toString))
	def setRandomFloat(name: String, low: Double, high: Double) = set_variable(name, randomFloatLow = Some(low.toString), randomFloatHigh = Some(high.toString))
	def setRandomFloat(name: String, low: String, high: String) = set_variable(name, randomFloatLow = Some(low), randomFloatHigh = Some(high))

	def ifEquals(varname: String, value: String) = if_variable(varname, "EQUALS", value)
	def ifEquals(varname: String, value: Long) = if_variable(varname, "EQUALS", value.toString)
	def ifNotEquals(varname: String, value: String) = if_variable(varname, "NOT", value)
	def ifNotEquals(varname: String, value: Long) = if_variable(varname, "NOT", value.toString)
	def ifGreater(varname: String, value: String) = if_variable(varname, "GREATER", value)
	def ifGreater(varname: String, value: Long) = if_variable(varname, "GREATER", value.toString)
	def ifGreater(varname: String, value: Double) = if_variable(varname, "GREATER", value.toString)
	def ifSmaller(varname: String, value: String) = if_variable(varname, "LESS", value)
	def ifSmaller(varname: String, value: Long) = if_variable(varname, "LESS", value.toString)
	def ifSmaller(varname: String, value: Double) = if_variable(varname, "LESS", value.toString)
	def ifGreaterOrEqual(varname: String, value: String) = if_variable(varname, "GREATER_EQUAL", value)
	def ifGreaterOrEqual(varname: String, value: Long) = if_variable(varname, "GREATER_EQUAL", value.toString)
	def ifGreaterOrEqual(varname: String, value: Double) = if_variable(varname, "GREATER_EQUAL", value.toString)
	def ifSmallerOrEqual(varname: String, value: String) = if_variable(varname, "LESS_EQUAL", value)
	def ifSmallerOrEqual(varname: String, value: Long) = if_variable(varname, "LESS_EQUAL", value.toString)
	def ifSmallerOrEqual(varname: String, value: Double) = if_variable(varname, "LESS_EQUAL", value.toString)

	def ifDistanceGreater(name1: String, name2: String, value: String) = if_distance(name1, name2, comparator = "GREATER", value = value)
	def ifDistanceGreater(name1: String, location: Location, value: String) = if_distance(name1, pointX = location.x, pointY = location.y, pointZ = location.z, comparator = "GREATER", value = value)
	def ifDistanceGreater(name1: String, name2: String, value: Double) = if_distance(name1, name2, comparator = "GREATER", value = value.toString)
	def ifDistanceGreater(name1: String, location: Location, value: Double) = if_distance(name1, pointX = location.x, pointY = location.y, pointZ = location.z, comparator = "GREATER", value = value.toString)
	def ifDistanceSmaller(name1: String, name2: String, value: String) = if_distance(name1, name2, comparator = "LESS", value = value)
	def ifDistanceSmaller(name1: String, location: Location, value: String) = if_distance(name1, pointX = location.x, pointY = location.y, pointZ = location.z, comparator = "LESS", value = value)
	def ifDistanceSmaller(name1: String, name2: String, value: Double) = if_distance(name1, name2, comparator = "LESS", value = value.toString)
	def ifDistanceSmaller(name1: String, location: Location, value: Double) = if_distance(name1, pointX = location.x, pointY = location.y, pointZ = location.z, comparator = "LESS", value = value.toString)
	def ifDistanceGreaterOrEqual(name1: String, name2: String, value: String) = if_distance(name1, name2, comparator = "GREATER_EQUAL", value = value)
	def ifDistanceGreaterOrEqual(name1: String, location: Location, value: String) = if_distance(name1, pointX = location.x, pointY = location.y, pointZ = location.z, comparator = "GREATER_EQUAL", value = value)
	def ifDistanceGreaterOrEqual(name1: String, name2: String, value: Double) = if_distance(name1, name2, comparator = "GREATER_EQUAL", value = value.toString)
	def ifDistanceGreaterOrEqual(name1: String, location: Location, value: Double) = if_distance(name1, pointX = location.x, pointY = location.y, pointZ = location.z, comparator = "GREATER_EQUAL", value = value.toString)
	def ifDistanceSmallerOrEqual(name1: String, name2: String, value: String) = if_distance(name1, name2, comparator = "LESS_EQUAL", value = value)
	def ifDistanceSmallerOrEqual(name1: String, location: Location, value: String) = if_distance(name1, pointX = location.x, pointY = location.y, pointZ = location.z, comparator = "LESS_EQUAL", value = value)
	def ifDistanceSmallerOrEqual(name1: String, name2: String, value: Double) = if_distance(name1, name2, comparator = "LESS_EQUAL", value = value.toString)
	def ifDistanceSmallerOrEqual(name1: String, location: Location, value: Double) = if_distance(name1, pointX = location.x, pointY = location.y, pointZ = location.z, comparator = "LESS_EQUAL", value = value.toString)

	def setTimer(varname: String, seconds: String) = set_timer(varname, seconds)
	def setTimer(varname: String, seconds: Long) = set_timer(varname, seconds.toString)
	def setTimer(varname: String, seconds: Double) = set_timer(varname, seconds.toString)
	def ifTimer(varname: String) = if_timer_finished(varname)
	def ifExists(name: String) = if_exists(name)
	def ifNotExists(name: String) = if_not_exists(name)
	def ifDocked(station: String) = if_docked(station)
	def ifNotDocked(station: String, player: String) = ifDistanceGreater(station, player, 500) // is this the best thing possible??

	def ifInsideSphere(name: String, location: Location, radius: Double) = if_inside_sphere(name, location.x, location.y, location.z, radius.toString)
	def ifInsideSphere(name: String, location: Location, radius: String) = if_inside_sphere(name, location.x, location.y, location.z, radius)
	def ifOutsideSphere(name: String, location: Location, radius: Double) = if_outside_sphere(name, location.x, location.y, location.z, radius.toString)
	def ifOutsideSphere(name: String, location: Location, radius: String) = if_outside_sphere(name, location.x, location.y, location.z, radius)
	def ifInsideBox(name: String, minX: Double, maxX: Double, minZ: Double, maxZ: Double) = if_inside_box(name, minX.toString, minZ.toString, maxX.toString, maxZ.toString)
	def ifInsideBox(name: String, minX: String, maxX: String, minZ: String, maxZ: String) = if_inside_box(name, minX, minZ, maxX, maxZ)
	def ifOutsideBox(name: String, minX: Double, maxX: Double, minZ: Double, maxZ: Double) = if_outside_box(name, minX.toString, minZ.toString, maxX.toString, maxZ.toString)
	def ifOutsideBox(name: String, minX: String, maxX: String, minZ: String, maxZ: String) = if_outside_box(name, minX, minZ, maxX, maxZ)

	def ifPlayerIsTargeting(name: String) = if_player_is_targeting(name)
	def ifBackShieldLessOrEqual(name: String, threshold: Long) = if_object_property(name, "shieldStateBack", "LESS_EQUAL", threshold.toString)
	// max Damage is said to be 20 (http://px2owffng8.talkiforum.com/20110415/mission-script-ref-v140-506396/)
	def ifHasWarpDamage(name: String) = if_object_property(name, "systemDamageWarp", "GREATER", "0")
	def ifHasImpulseDamage(name: String) = if_object_property(name, "systemDamageImpulse", "GREATER", "0")
	def ifHasTurningDamage(name: String) = if_object_property(name, "systemDamageTurning", "GREATER", "0")

	def ifPropertyEquals(name: Option[String], property: String, value: String) = if_object_property(name, property, "EQUALS", value)
	def ifPropertyEquals(name: Option[String], property: String, value: Long) = if_object_property(name, property, "EQUALS", value.toString)
	def ifPropertyNotEquals(name: Option[String], property: String, value: String) = if_object_property(name, property, "NOT", value)
	def ifPropertyNotEquals(name: Option[String], property: String, value: Long) = if_object_property(name, property, "NOT", value.toString)
	def ifPropertyGreater(name: Option[String], property: String, value: String) = if_object_property(name, property, "GREATER", value)
	def ifPropertyGreater(name: Option[String], property: String, value: Long) = if_object_property(name, property, "GREATER", value.toString)
	def ifPropertyGreater(name: Option[String], property: String, value: Double) = if_object_property(name, property, "GREATER", value.toString)
	def ifPropertySmaller(name: Option[String], property: String, value: String) = if_object_property(name, property, "LESS", value)
	def ifPropertySmaller(name: Option[String], property: String, value: Long) = if_object_property(name, property, "LESS", value.toString)
	def ifPropertySmaller(name: Option[String], property: String, value: Double) = if_object_property(name, property, "LESS", value.toString)
	def ifPropertyGreaterOrEqual(name: Option[String], property: String, value: String) = if_object_property(name, property, "GREATER_EQUAL", value)
	def ifPropertyGreaterOrEqual(name: Option[String], property: String, value: Long) = if_object_property(name, property, "GREATER_EQUAL", value.toString)
	def ifPropertyGreaterOrEqual(name: Option[String], property: String, value: Double) = if_object_property(name, property, "GREATER_EQUAL", value.toString)
	def ifPropertySmallerOrEqual(name: Option[String], property: String, value: String) = if_object_property(name, property, "LESS_EQUAL", value)
	def ifPropertySmallerOrEqual(name: Option[String], property: String, value: Long) = if_object_property(name, property, "LESS_EQUAL", value.toString)
	def ifPropertySmallerOrEqual(name: Option[String], property: String, value: Double) = if_object_property(name, property, "LESS_EQUAL", value.toString)

	def addToProperty(name: Option[String], property: String, value: Double) = add_to_object_property(name, property, value.toString)
	def addToProperty(name: Option[String], property: String, value: String) = add_to_object_property(name, property, value)

	def damageBeam(name: String, amount: Double = 1.0) = set_object_property(name, "systemDamageBeam", amount.toString)
	def damageTorpedo(name: String, amount: Double = 1.0) = set_object_property(name, "systemDamageTorpedo", amount.toString)
	def damageTactical(name: String, amount: Double = 1.0) = set_object_property(name, "systemDamageTactical", amount.toString)
	def damageTurning(name: String, amount: Double = 1.0) = set_object_property(name, "systemDamageTurning", amount.toString)
	def damageImpulse(name: String, amount: Double = 1.0) = set_object_property(name, "systemDamageImpulse", amount.toString)
	def damageWarp(name: String, amount: Double = 1.0) = set_object_property(name, "systemDamageWarp", amount.toString)
	def damageFrontShield(name: String, amount: Double = 1.0) = set_object_property(name, "systemDamageFrontShield", amount.toString)
	def damageBackShield(name: String, amount: Double = 1.0) = set_object_property(name, "systemDamageBackShield", amount.toString)

	trait SensorSetting { val id: Int}
	object Sensor08K extends SensorSetting { val id = 4 }
	object Sensor11K extends SensorSetting { val id = 3 }
	object Sensor16K extends SensorSetting { val id = 2 }
	object Sensor33K extends SensorSetting { val id = 1 }
	object SensorUnlimited extends SensorSetting { val id = 0 }
	def setSensorSetting(setting: SensorSetting) = set_object_property(None, "sensorSetting", setting.id.toString)
	def hideShipsInNebula = set_object_property(None, "nebulaIsOpaque", "1")
	def showShipsInNebula = set_object_property(None, "nebulaIsOpaque", "0")

	private def damagePlayer(name: Option[String], systemType: String, value: Double, systems: Seq[Int]) = NodeSeq.fromSeq(systems.map { i =>
		set_player_grid_damage(name, systemType, value.toDouble, "front", i)
	})
	def damagePlayerBeam(name: Option[String], value: Double = 1.0, systems: Seq[Int] = 0.to(3)) = damagePlayer(name, "systemBeam", value, systems)
	def damagePlayerTorpedo(name: Option[String], value: Double = 1.0, systems: Seq[Int] = 0.to(1)) = damagePlayer(name, "systemTorpedo", value, systems)
	def damagePlayerTactical(name: Option[String], value: Double = 1.0, systems: Seq[Int] = 0.to(3)) = damagePlayer(name, "systemTactical", value, systems)
	def damagePlayerTurning(name: Option[String], value: Double = 1.0, systems: Seq[Int] = 0.to(3)) = damagePlayer(name, "systemTurning", value, systems)
	def damagePlayerImpulse(name: Option[String], value: Double = 1.0, systems: Seq[Int] = 0.to(5)) = damagePlayer(name, "systemImpulse", value, systems)
	def damagePlayerWarp(name: Option[String], value: Double = 1.0, systems: Seq[Int] = 0.to(3)) = damagePlayer(name, "systemWarp", value, systems)
	def damagePlayerFrontShield(name: Option[String], value: Double = 1.0, systems: Seq[Int] = 0.to(3)) = damagePlayer(name, "systemFrontShield", value, systems)
	def damagePlayerBackShield(name: Option[String], value: Double = 1.0, systems: Seq[Int] = 0.to(3)) = damagePlayer(name, "systemBackShield", value, systems)

	@deprecated
	def setSpecial(name: String, ship: Int, captain: Int) = set_special(name, Some(ship), Some(captain))
	@deprecated
	def setProperty(name: String, property: String, value: Double) = set_object_property(Some(name), property, value.toString)
	@deprecated
	def setProperty(property: String, value: Double) = set_object_property(None, property, value.toString)
	@deprecated
	def setProperty(name: String, property: String, value: String) = set_object_property(Some(name), property, value)
	@deprecated
	def setProperty(property: String, value: String) = set_object_property(None, property, value)
	def setShipText(name: String, newname: Option[String] = None, race: Option[String] = None, shipClass: Option[String] = None, desc: Option[String] = None, scan_desc: Option[String] = None, hailtext: Option[String] = None) = set_ship_text(name, newname, race, shipClass, desc, scan_desc, hailtext)
	def setNeutralSide(name: String) = set_side_value(name, "0")
	def setEnemySide(name: String) = set_side_value(name, "1")
	def setPlayerSide(name: String) = set_side_value(name, "2")

	def setRelativePosition(name: String, closeTo: String, angle: Double = 0.0, distance: Double = 100) = set_relative_position(name1 = closeTo, name2 = name, angle = angle.toString, distance = distance.toString)

	def setFleetProperties(fleetId: Int, fleetSpacing: Option[Double] = None, fleetMaxRadius: Option[Double] = None): NodeSeq = {
		NodeSeq.fromSeq((
			fleetSpacing.map(i => set_fleet_property(fleetId, "fleetSpacing", i.toString)) ++
			fleetMaxRadius.map(i => set_fleet_property(fleetId, "fleetMaxRadius", i.toString))
		).toSeq)
	}

	def noop = Text("")
}
