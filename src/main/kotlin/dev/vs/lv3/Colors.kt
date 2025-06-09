package dev.vs.lv3

import net.minecraft.scoreboard.Team
import net.minecraft.server.MinecraftServer
import net.minecraft.server.network.ServerPlayerEntity
import net.minecraft.text.Text
import net.minecraft.util.Formatting

fun getTeam(server: MinecraftServer, name: String, teamColor: Formatting): Team {
    val scoreboard = server.scoreboard
    var team = scoreboard.getTeam(name)
    if (team == null) {
        team = scoreboard.addTeam(name).apply {
            color = teamColor
        }
    }
    return team
}

fun getDead(server: MinecraftServer) = getTeam(server, "lv3_dead", Formatting.GRAY)
fun get1Life(server: MinecraftServer) = getTeam(server, "lv3_1life", Formatting.RED)
fun get2Life(server: MinecraftServer) = getTeam(server, "lv3_2life", Formatting.YELLOW)
fun get3Life(server: MinecraftServer) = getTeam(server, "lv3_3life", Formatting.GREEN)

fun updateDisplayName(player: ServerPlayerEntity) {
    player.isCustomNameVisible = true
    val data = LivesLoader.getPlayerState(player) ?: return
    val server = player.server
    val scoreboard = server.scoreboard

    when (data.lives) {
        0 -> scoreboard.addPlayerToTeam(player.name.string, getDead(server))
        1 -> scoreboard.addPlayerToTeam(player.name.string, get1Life(server))
        2 -> scoreboard.addPlayerToTeam(player.name.string, get2Life(server))
        3 -> scoreboard.addPlayerToTeam(player.name.string, get3Life(server))
    }
}