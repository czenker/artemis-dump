package lib

import artemis.Hull
import artemis.Porcelain._

case class Player(id: String,
                  hull: Hull,
                  spawnLocation: Location,
                  playerSlot: Option[Int] = None
               ) {
	def create = createPlayer(id, hull, location = spawnLocation, player_slot = playerSlot)
}
