package fr.amisoz.consulatcore.commands.moderation;

import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.builder.RequiredArgumentBuilder;
import fr.amisoz.consulatcore.Text;
import fr.amisoz.consulatcore.moderation.ModerationUtils;
import fr.leconsulat.api.commands.ConsulatCommand;
import fr.leconsulat.api.player.ConsulatPlayer;
import fr.leconsulat.api.ranks.Rank;
import org.apache.commons.lang.StringUtils;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

import java.util.Arrays;

public class BroadcastCommand extends ConsulatCommand {
    
    public BroadcastCommand(){
        super("annonce",
                Arrays.asList("broadcast", "bc"),
                "/annonce <Message>", 1, Rank.RESPONSABLE);
        suggest(LiteralArgumentBuilder.literal("annonce")
                .then(RequiredArgumentBuilder.argument("message", StringArgumentType.greedyString())));
    }
    
    @Override
    public void onCommand(ConsulatPlayer sender, String[] args){
        //TODO -> Stringbuilder
        String message = StringUtils.join(args, " ");
        Bukkit.broadcastMessage(Text.BROADCAST_PREFIX + ChatColor.DARK_RED + sender.getName() + ChatColor.GRAY + " : §r" + ChatColor.AQUA + message);
    }
}
