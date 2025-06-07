package dev.vs.lv3

import com.mojang.brigadier.CommandDispatcher
import net.minecraft.command.argument.EntityArgumentType
import net.minecraft.server.command.CommandManager
import net.minecraft.server.command.ServerCommandSource
import net.minecraft.text.Text
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
                            }

                            if (sender != to && toData.lives == 1) {
                                to.changeGameMode(GameMode.SURVIVAL)
                            }

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
                        it.source.sendFeedback({
                            Text.literal("${data.lives}.")
                        }, false)

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
                CommandManager.literal("lives")
                    .requires { it.hasPermissionLevel(1) }
                    .executes {
                        val to = EntityArgumentType.getPlayer(it, "player")
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
