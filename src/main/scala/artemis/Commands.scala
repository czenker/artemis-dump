package artemis

import scala.xml._

object Commands {
	implicit def toAttributeSeq(attributes: Iterable[Attribute]): MetaData = attributes.foldRight(Null: MetaData) {
		_.copy(_)
	}

	def create(name: Option[String] = None,
	           `type`: String,
	           use_gm_position: Boolean = false,
	           x: Option[String] = None,
	           y: Option[String] = None,
	           z: Option[String] = None,
	           angle: Option[Double] = None,
	           fleetnumber: Option[Int] = None,
	           raceKeys: Option[String] = None,
	           hullKeys: Option[String] = None,
	           player_slot: Option[Int] = None,
	           accent_color: Option[Int] = None,
	           warp: Option[Boolean] = None,
	           jump: Option[Boolean] = None,
	           meshFileName: Option[String] = None,
	           textureFileName: Option[String] = None,
	           hullRace: Option[String] = None,
	           hullType: Option[String] = None,
	           fakeShieldsFront: Option[Int] = None,
	           fakeShieldsRear: Option[Int] = None,
	           hasFakeShldFreq: Option[Boolean] = None,
	           colorRed: Option[Double] = None,
	           colorGreen: Option[Double] = None,
	           colorBlue: Option[Double] = None,
	           pickupType: Option[Int] = None,
	           monsterType: Option[Int] = None,
	           podnumber: Option[Int] = None,
	           count: Option[Int] = None,
	           radius: Option[Long] = None,
	           randomRange: Option[Long] = None,
	           startX: Option[Double] = None,
	           startY: Option[Double] = None,
	           startZ: Option[Double] = None,
	           endX: Option[Double] = None,
	           endY: Option[Double] = None,
	           endZ: Option[Double] = None,
	           randomSeed: Option[Long] = None,
	           startAngle: Option[Long] = None,
	           endAngle: Option[Long] = None
	          ) = {

		val attributes =
			Some(Attribute("type", Text(`type`), Null)) ++
				name.map(n => Attribute("name", Text(n), Null)) ++
				(if (use_gm_position) {
					Some(Attribute("use_gm_position", Text("yes"), Null))
				} else {
					None
				}) ++
				x.map(n => Attribute("x", Text(n), Null)) ++
				y.map(n => Attribute("y", Text(n), Null)) ++
				z.map(n => Attribute("z", Text(n), Null)) ++
				angle.map(n => Attribute("angle", Text(n.toString), Null)) ++
				fleetnumber.map(n => Attribute("fleetnumber", Text(n.toString), Null)) ++
				raceKeys.map(n => Attribute("raceKeys", Text(n), Null)) ++
				hullKeys.map(n => Attribute("hullKeys", Text(n), Null)) ++
				player_slot.map(n => Attribute("player_slot", Text(n.toString), Null)) ++
				accent_color.map(n => Attribute("accent_color", Text(n.toString), Null)) ++
				warp.map(n => Attribute("warp", Text(if (n) "yes" else "no"), Null)) ++
				jump.map(n => Attribute("jump", Text(if (n) "yes" else "no"), Null)) ++
				meshFileName.map(n => Attribute("meshFileName", Text(n.toString), Null)) ++
				textureFileName.map(n => Attribute("textureFileName", Text(n.toString), Null)) ++
				hullRace.map(n => Attribute("hullRace", Text(n.toString), Null)) ++
				hullType.map(n => Attribute("hullType", Text(n.toString), Null)) ++
				fakeShieldsFront.map(n => Attribute("fakeShieldsFront", Text(n.toString), Null)) ++
				fakeShieldsRear.map(n => Attribute("fakeShieldsRear", Text(n.toString), Null)) ++
				hasFakeShldFreq.map(n => Attribute("hasFakeShldFreq", Text(if (n) "1" else "0"), Null)) ++
				colorRed.map(n => Attribute("colorRed", Text(n.toString), Null)) ++
				colorGreen.map(n => Attribute("colorGreen", Text(n.toString), Null)) ++
				colorBlue.map(n => Attribute("colorBlue", Text(n.toString), Null)) ++
				pickupType.map(n => Attribute("pickupType", Text(n.toString), Null)) ++
				monsterType.map(n => Attribute("monsterType", Text(n.toString), Null)) ++
				podnumber.map(n => Attribute("podnumber", Text(n.toString), Null)) ++
				count.map(n => Attribute("count", Text(n.toString), Null)) ++
				radius.map(n => Attribute("radius", Text(n.toString), Null)) ++
				randomRange.map(n => Attribute("randomRange", Text(n.toString), Null)) ++
				startX.map(n => Attribute("startX", Text(n.toString), Null)) ++
				startY.map(n => Attribute("startY", Text(n.toString), Null)) ++
				startZ.map(n => Attribute("startZ", Text(n.toString), Null)) ++
				endX.map(n => Attribute("endX", Text(n.toString), Null)) ++
				endY.map(n => Attribute("endY", Text(n.toString), Null)) ++
				endZ.map(n => Attribute("endZ", Text(n.toString), Null)) ++
				randomSeed.map(n => Attribute("randomSeed", Text(n.toString), Null)) ++
				startAngle.map(n => Attribute("startAngle", Text(n.toString), Null)) ++
				endAngle.map(n => Attribute("endAngle", Text(n.toString), Null))

			<create/>.copy(attributes = attributes)
	}

	def set_gm_button(text: String, x: Option[Int] = None, y: Option[Int] = None, w: Option[Int] = None, h: Option[Int] = None, menu_w: Option[Int] = None) = {
		val attributes =
			Some(Attribute("text", Text(text), Null)) ++
				x.map(n => Attribute("x", Text(n.toString), Null)) ++
				y.map(n => Attribute("y", Text(n.toString), Null)) ++
				w.map(n => Attribute("w", Text(n.toString), Null)) ++
				h.map(n => Attribute("h", Text(n.toString), Null)) ++
				menu_w.map(n => Attribute("menu_w", Text(n.toString), Null))

			<set_gm_button/>.copy(attributes = attributes)
	}

	def clear_gm_button(text: String) = <clear_gm_button text={text}/>

	def if_gm_button(text: String) = <if_gm_button text={text}/>

	def set_comms_button(text: String, sideValue: Option[Int] = None) = {
		val attributes =
			Some(Attribute("text", Text(text), Null)) ++
			sideValue.map(n => Attribute("sideValue", Text(n.toString), Null))
		<set_comms_button/>.copy(attributes = attributes)
	}

	def clear_comms_button(text: String, sideValue: Option[Int] = None) = {
		val attributes =
			Some(Attribute("text", Text(text), Null)) ++
				sideValue.map(n => Attribute("sideValue", Text(n.toString), Null))
			<clear_comms_button/>.copy(attributes = attributes)
	}

	def if_comms_button(text: String) = <if_comms_button text={text}/>

	def set_to_gm_position(name: Option[String] = None, use_gm_selection: Boolean = false, angle: Option[Double] = None, distance: Option[Double] = None) = {
		val attributes =
			name.map(n => Attribute("name", Text(n), Null)) ++
				(if (use_gm_selection) {
					Some(Attribute("use_gm_selection", Text("yes"), Null))
				} else {
					None
				}) ++
				angle.map(n => Attribute("angle", Text(n.toString), Null)) ++
				distance.map(n => Attribute("distance", Text(n.toString), Null))

			<set_to_gm_position/>.copy(attributes = attributes)
	}

	def destroy(name: Option[String] = None, use_gm_selection: Boolean = false) = {
		val attributes =
			name.map(n => Attribute("name", Text(n), Null)) ++
				(if (use_gm_selection) {
					Some(Attribute("use_gm_selection", Text("yes"), Null))
				} else {
					None
				})
			<destroy/>.copy(attributes = attributes)
	}

	def set_skybox_index(index: Int) = <set_skybox_index index={index.toString}/>

	def set_variable(name: String,
	                 value: Option[String] = None,
	                 integer: Boolean = false,
	                 randomIntHigh: Option[String] = None,
	                 randomIntLow: Option[String] = None,
	                 randomFloatHigh: Option[String] = None,
	                 randomFloatLow: Option[String] = None
	                ) = {
		val attributes =
			Some(Attribute("name", Text(name), Null)) ++
				value.map(n => Attribute("value", Text(n), Null)) ++
				randomIntHigh.map(n => Attribute("randomIntHigh", Text(n), Null)) ++
				randomIntLow.map(n => Attribute("randomIntLow", Text(n), Null)) ++
				randomFloatHigh.map(n => Attribute("randomFloatHigh", Text(n), Null)) ++
				randomFloatLow.map(n => Attribute("randomFloatLow", Text(n), Null)) ++
				(if (integer) {
					Some(Attribute("integer", Text("yes"), Null))
				} else {
					None
				})

			<set_variable/>.copy(attributes = attributes)
	}

	def if_variable(name: String, comparator: String, value: String) = {
		val attributes =
			Some(Attribute("name", Text(name), Null)) ++
				Some(Attribute("comparator", Text(comparator), Null)) ++
				Some(Attribute("value", Text(value), Null))

			<if_variable/>.copy(attributes = attributes)
	}

	def if_distance(name1: String, name2: Option[String] = None, pointX: Option[String] = None, pointY: Option[String] = None, pointZ: Option[String] = None, comparator: String, value: String) = {
		val attributes =
			Some(Attribute("name1", Text(name1), Null)) ++
				name2.map(n => Attribute("name2", Text(n), Null)) ++
				pointX.map(n => Attribute("pointX", Text(n), Null)) ++
				pointY.map(n => Attribute("pointY", Text(n), Null)) ++
				pointZ.map(n => Attribute("pointZ", Text(n), Null)) ++
				Some(Attribute("comparator", Text(comparator), Null)) ++
				Some(Attribute("value", Text(value), Null))

			<if_distance/>.copy(attributes = attributes)
	}

	def set_timer(name: String, seconds: String) = <set_timer name={name} seconds={seconds}/>

	def if_timer_finished(name: String) = <if_timer_finished name={name}/>

	def if_exists(name: String) = <if_exists name={name}/>

	def if_not_exists(name: String) = <if_not_exists name={name}/>

	def if_docked(name: String) = <if_docked name={name}/>

	def set_special(name: String,
	                ship: Option[Int] = None,
	                captain: Option[Int] = None,
	                ability: Option[String] = None,
	                clear: Boolean = false) = {
		val attributes =
			Some(Attribute("name", Text(name), Null)) ++
				ship.map(n => Attribute("ship", Text(n.toString), Null)) ++
				captain.map(n => Attribute("captain", Text(n.toString), Null)) ++
				ability.map(n => Attribute("ability", Text(n), Null)) ++
				(if (clear) {
					Some(Attribute("clear", Text("yes"), Null))
				} else {
					None
				})

			<set_special/>.copy(attributes = attributes)
	}

	def set_object_property(name: Option[String] = None, property: String, value: String) = {
		val attributes =
			name.map(n => Attribute("name", Text(n), Null)) ++
			Some(Attribute("property", Text(property), Null)) ++
			Some(Attribute("value", Text(value), Null))

			<set_object_property/>.copy(attributes = attributes)
	}

	def if_object_property(name: Option[String] = None, property: String, comparator: String, value: String) = {
		val attributes =
			name.map(n => Attribute("name", Text(n), Null)) ++
				Some(Attribute("property", Text(property), Null)) ++
				Some(Attribute("comparator", Text(comparator), Null)) ++
				Some(Attribute("value", Text(value), Null))

			<if_object_property/>.copy(attributes = attributes)
	}

	def add_to_object_property(name: Option[String] = None, property: String, value: String) = {
		val attributes =
			name.map(n => Attribute("name", Text(n), Null)) ++
				Some(Attribute("property", Text(property), Null)) ++
				Some(Attribute("value", Text(value), Null))

			<add_to_object_property/>.copy(attributes = attributes)
	}


	def set_ship_text(name: String,
	                  newname: Option[String] = None,
	                  race: Option[String] = None,
	                  `class`: Option[String] = None,
	                  desc: Option[String] = None,
	                  scan_desc: Option[String] = None,
	                  hailtext: Option[String] = None) = {
		val attributes =
			Some(Attribute("name", Text(name), Null)) ++
				newname.map(n => Attribute("newname", Text(n), Null)) ++
				race.map(n => Attribute("race", Text(n), Null)) ++
				`class`.map(n => Attribute("class", Text(n), Null)) ++
				desc.map(n => Attribute("desc", Text(n), Null)) ++
				scan_desc.map(n => Attribute("scan_desc", Text(n), Null)) ++
				hailtext.map(n => Attribute("hailtext", Text(n), Null))

			<set_ship_text/>.copy(attributes = attributes)
	}

	def clear_ai(name: Option[String] = None, use_gm_selection: Boolean = false) = {
		val attributes =
			name.map(n => Attribute("name", Text(n), Null)) ++
				(if (use_gm_selection) {
					Some(Attribute("use_gm_selection", Text("yes"), Null))
				} else {
					None
				})

			<clear_ai/>.copy(attributes = attributes)
	}

	def add_ai(name: Option[String] = None, use_gm_selection: Boolean = false, targetName: Option[String] = None, `type`: Option[String] = None, value1: Option[String] = None, value2: Option[String] = None, value3: Option[String] = None, value4: Option[String] = None): Node = {
		val attributes =
			name.map(n => Attribute("name", Text(n), Null)) ++
				(if (use_gm_selection) {
					Some(Attribute("use_gm_selection", Text("yes"), Null))
				} else {
					None
				}) ++
				targetName.map(n => Attribute("targetName", Text(n), Null)) ++
				`type`.map(n => Attribute("type", Text(n), Null)) ++
				value1.map(n => Attribute("value1", Text(n), Null)) ++
				value2.map(n => Attribute("value2", Text(n), Null)) ++
				value3.map(n => Attribute("value3", Text(n), Null)) ++
				value4.map(n => Attribute("value4", Text(n), Null))

			<add_ai/>.copy(attributes = attributes)
	}

	def incoming_comms_text(from: String, message: String, `type`: Option[String] = None, sideValue: Option[Int] = None) = {
		val attributes =
			Some(Attribute("from", Text(from), Null)) ++
				`type`.map(n => Attribute("type", Text(n), Null)) ++
				sideValue.map(n => Attribute("sideValue", Text(n.toString), Null))

		<incoming_comms_text>
			{message}
		</incoming_comms_text>.copy(attributes = attributes)
	}

	def big_message(title: Option[String] = None, subtitle1: Option[String] = None, subtitle2: Option[String] = None, side: Option[Int] = None) = {
		val attributes =
			title.map(n => Attribute("title", Text(n), Null)) ++
				subtitle1.map(n => Attribute("subtitle1", Text(n), Null)) ++
				subtitle2.map(n => Attribute("subtitle2", Text(n), Null)) ++
				side.map(n => Attribute("side", Text(n.toString), Null))

			<big_message/>.copy(attributes = attributes)
	}

	def if_inside_sphere(name: String, centerX: String, centerY: String, centerZ: String, radius: String) =
			<if_inside_sphere name={name} centerX={centerX} centerY={centerY} centerZ={centerZ} radius={radius}/>

	def if_outside_sphere(name: String, centerX: String, centerY: String, centerZ: String, radius: String) =
			<if_outside_sphere name={name} centerX={centerX} centerY={centerY} centerZ={centerZ} radius={radius}/>

	def if_inside_box(name: String, leastX: String, leastZ: String, mostX: String, mostZ: String) =
			<if_inside_box name={name} leastX={leastX} leastZ={leastZ} mostX={mostX} mostZ={mostZ}/>

	def if_outside_box(name: String, leastX: String, leastZ: String, mostX: String, mostZ: String) =
			<if_outside_box name={name} leastX={leastX} leastZ={leastZ} mostX={mostX} mostZ={mostZ}/>

	def set_relative_position(name1: String, name2: String, angle: String, distance: String) =
		<set_relative_position name1={name1} name2={name2} angle={angle} distance={distance} />

	def if_player_is_targeting(name: String) = <if_player_is_targeting name={name} />
	def set_side_value(name: String, value: String) = <set_side_value name={name} value={value} />

	def set_fleet_property(fleetIndex: Int, property: String, value: String) = {
		val attributes =
			Some(Attribute("fleetIndex", Text(fleetIndex.toString), Null)) ++
			Some(Attribute("property", Text(property), Null)) ++
			Some(Attribute("value", Text(value), Null))

			<set_fleet_property/>.copy(attributes = attributes)
	}

	def set_player_grid_damage(name: Option[String] = None, systemType: String, value: Double, countFrom: String, index: Int) = {
		val attributes =
			name.map(n => Attribute("name", Text(n), Null)) ++
			Some(Attribute("systemType", Text(systemType), Null)) ++
			Some(Attribute("value", Text(value.toString), Null)) ++
			Some(Attribute("countFrom", Text(countFrom.toString), Null)) ++
			Some(Attribute("index", Text(index.toString), Null))

			<set_player_grid_damage/>.copy(attributes = attributes)
	}

	def warning_popup_message(message: String, consoles: String) = <warning_popup_message message={message} consoles={consoles} />
}