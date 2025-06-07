package dev.vs.lv3

import net.fabricmc.api.DedicatedServerModInitializer
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback
import net.fabricmc.fabric.api.entity.event.v1.ServerLivingEntityEvents
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents
import net.minecraft.nbt.NbtCompound
import net.minecraft.server.MinecraftServer
import net.minecraft.server.network.ServerPlayerEntity
import net.minecraft.world.GameMode
import net.minecraft.world.PersistentState
import net.minecraft.world.World
import java.util.UUID


class PlayerData (var lives: Int = 0)

class LivesLoader : PersistentState() {
	var players = HashMap<UUID, PlayerData>()

	companion object {
		fun fromNbt(tag: NbtCompound): LivesLoader {
			val state = LivesLoader()
			val playersNbt = tag.getCompound("players")
			playersNbt.keys.forEach {
				val playerData = PlayerData()
				playerData.lives = playersNbt.getCompound(it).getInt("lives")
				val uuid = UUID.fromString(it)
				state.players.put(uuid, playerData)
			}
			return state
		}

		fun getServerState(server: MinecraftServer?): LivesLoader? {
			val world = server?.getWorld(World.OVERWORLD) ?: return null
			val state = world.persistentStateManager.getOrCreate(::fromNbt, ::LivesLoader, Lv3.MOD_ID)
			state.markDirty()
			return state
		}

		fun getPlayerState(player: ServerPlayerEntity): PlayerData? {
			val state = getServerState(player.world.server)
			val playerState = state?.players?.computeIfAbsent(player.uuid) { uuid -> PlayerData(3) }
            return playerState
		}
	}

	override fun writeNbt(nbt: NbtCompound?): NbtCompound? {
		val playersNbt = NbtCompound()
		players.forEach { uuid, data ->
			val playerNbt = NbtCompound()
			playerNbt.putInt("lives", data.lives)
			playersNbt.put(uuid.toString(), playerNbt)
		}
		nbt?.put("players", playersNbt)

		return nbt
	}
}

object Lv3 : DedicatedServerModInitializer {
	const val MOD_ID = "lv3"

	override fun onInitializeServer() {
		ServerLifecycleEvents.SERVER_STARTED.register(::onServerStarted)

		CommandRegistrationCallback.EVENT.register { dispatcher, registryAccess, environment ->
			ExchangeCommand.register(dispatcher)
			LivesCommand.register(dispatcher)
			AddLiveCommand.register(dispatcher)
		}
	}

	private fun onServerStarted(server: MinecraftServer) {
		ServerPlayConnectionEvents.JOIN.register { listener, sender, server ->
			val player = listener.player
			val data = LivesLoader.getPlayerState(player)
            data?.lives?.let {
                if (it <= 0) {
                    player.changeGameMode(GameMode.SPECTATOR)
                }
            }
		}

		ServerLivingEntityEvents.AFTER_DEATH.register { entity, source ->
			if (entity is ServerPlayerEntity) {
				val lives = LivesLoader.getPlayerState(entity)
				lives?.lives -= 1

                lives?.lives?.let {
                    if (it <= 0) {
						server.commandManager.executeWithPrefix(server.commandSource, "/origin set ${entity.name.string} origins:origin origins:human")
                        entity.changeGameMode(GameMode.SPECTATOR)
                    }
                }
			}
		}
	}
}