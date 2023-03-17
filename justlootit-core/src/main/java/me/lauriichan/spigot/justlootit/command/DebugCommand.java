package me.lauriichan.spigot.justlootit.command;

import org.bukkit.Bukkit;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Player;
import org.bukkit.loot.LootTable;
import org.bukkit.util.RayTraceResult;

import me.lauriichan.laylib.command.Actor;
import me.lauriichan.laylib.command.annotation.Action;
import me.lauriichan.laylib.command.annotation.Argument;
import me.lauriichan.laylib.command.annotation.Command;
import me.lauriichan.laylib.localization.Key;
import me.lauriichan.spigot.justlootit.message.Messages;

@Command(name = "debug", description = "A debug command")
public class DebugCommand {
    
    @Action("container frame")
    public void frameContainer(Actor<?> actor) {
        Actor<Player> playerActor = actor.as(Player.class);
        if(!playerActor.isValid()) {
            actor.sendTranslatedMessage(Messages.COMMAND_SYSTEM_ACTOR_NOT$SUPPORTED, Key.of("actorType", "Player"));
            return;
        }
        Player player = playerActor.getHandle();
        RayTraceResult result = player.getWorld().rayTraceEntities(player.getEyeLocation(), player.getEyeLocation().getDirection(), 5);
        if(result == null) {
            actor.sendMessage("&cYou have to look at a entity!");
            return;
        }
        // TODO: Create item frame container
    }
    
    @Action("container vanilla")
    public void vanillaContainer(Actor<?> actor, @Argument(name = "loottable") String loottable) {
        Actor<Player> playerActor = actor.as(Player.class);
        if(!playerActor.isValid()) {
            actor.sendTranslatedMessage(Messages.COMMAND_SYSTEM_ACTOR_NOT$SUPPORTED, Key.of("actorType", "Player"));
            return;
        }
        NamespacedKey key = NamespacedKey.fromString(loottable);
        if(key == null) {
           actor.sendMessage("&cInvalid key '%s'!".formatted(loottable));
           return;
        }
        LootTable table = Bukkit.getLootTable(key);
        if(table == null) {
            actor.sendMessage("&cInvalid loottable '%s'!".formatted(key.toString()));
            return;
        }
        Player player = playerActor.getHandle();
        RayTraceResult result = player.getWorld().rayTraceBlocks(player.getEyeLocation(), player.getEyeLocation().getDirection(), 5);
        if(result == null) {
            actor.sendMessage("&cYou have to look at a block!");
            return;
        }
        // TODO: Create vanilla container
    }
    
    @Action("container static")
    public void vanillaContainer(Actor<?> actor) {
        Actor<Player> playerActor = actor.as(Player.class);
        if(!playerActor.isValid()) {
            actor.sendTranslatedMessage(Messages.COMMAND_SYSTEM_ACTOR_NOT$SUPPORTED, Key.of("actorType", "Player"));
            return;
        }
        Player player = playerActor.getHandle();
        RayTraceResult result = player.getWorld().rayTraceBlocks(player.getEyeLocation(), player.getEyeLocation().getDirection(), 5);
        if(result == null) {
            actor.sendMessage("&cYou have to look at a block!");
            return;
        }
        // TODO: Create static container
    }

}
