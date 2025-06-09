package dev.vs.lv3

import com.mojang.brigadier.CommandDispatcher
import net.minecraft.command.argument.EntityArgumentType
import net.minecraft.server.PlayerManager
import net.minecraft.server.command.CommandManager
import net.minecraft.server.command.ServerCommandSource
import net.minecraft.server.network.ServerPlayerEntity
import net.minecraft.text.Style
import net.minecraft.text.Text
import net.minecraft.util.Formatting
import net.minecraft.world.GameMode

class ExchangeCommand {
    companion object {
        fun register(dispatcher: CommandDispatcher<ServerCommandSource>) {
            dispatcher.register(
                CommandManager.literal("exchange")
                    .then(CommandManager.argument("player", EntityArgumentType.player())
                        .executes {
                            val sender = it.source.player ?: return@executes 1
                            val to = EntityArgumentType.getPlayer(it, "player")

                            val senderData = LivesLoader.getPlayerState(sender) ?: return@executes 1
                            val toData = LivesLoader.getPlayerState(to) ?: return@executes 1

                            if (senderData.lives > 0) {
                                senderData.lives -= 1
                                toData.lives += 1
                            }
                            if (senderData.lives == 0) {
                                sender.changeGameMode(GameMode.SPECTATOR)
                                val server = sender.server
                                server.commandManager.executeWithPrefix(server.commandSource, "/origin set ${sender.name.string} origins:origin origins:human")

                                return@executes 1
                            }

                            if (sender != to && toData.lives == 1) {
                                to.teleport(sender.serverWorld, sender.x, sender.y, sender.z, sender.yaw, sender.pitch)
                                to.changeGameMode(GameMode.SURVIVAL)
                            }

                            updateDisplayName(to)
                            updateDisplayName(sender)

                            it.source.sendFeedback({
                                Text.literal("Done.")
                            }, false)
                            return@executes 1

                        }
                    )
            )
        }
    }
}

class LivesCommand {
    companion object {
        fun register(dispatcher: CommandDispatcher<ServerCommandSource>) {
            dispatcher.register(
                CommandManager.literal("lives")
                    .executes {
                        val player = it.source.player ?: return@executes 1
                        val data = LivesLoader.getPlayerState(player) ?: return@executes 1
                        player.sendMessage(Text.literal("${data.lives}."), true)

                        return@executes 1
                    }

            )
        }
    }
}

class AddLiveCommand {
    companion object {
        fun register(dispatcher: CommandDispatcher<ServerCommandSource>) {
            dispatcher.register(
                CommandManager.literal("addlive")
                    .requires { it.hasPermissionLevel(2) }
                    .executes {
                        val to = it.source.player ?: return@executes 1
                        val toData = LivesLoader.getPlayerState(to) ?: return@executes 1

                        toData.lives += 1
                        if (toData.lives == 1) {
                            to.changeGameMode(GameMode.SURVIVAL)
                        }

                        return@executes 1
                    }

            )
        }
    }
}
