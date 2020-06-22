package fr.amisoz.consulatcore.commands.moderation;

import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.RequiredArgumentBuilder;
import fr.amisoz.consulatcore.Text;
import fr.leconsulat.api.commands.ConsulatCommand;
import fr.leconsulat.api.player.ConsulatPlayer;
import fr.leconsulat.api.ranks.Rank;
import org.bukkit.Bukkit;

import java.util.Arrays;

public class BroadcastCommand extends ConsulatCommand {
    
    public BroadcastCommand(){
        super("annonce",
                Arrays.asList("broadcast", "bc"),
                "/annonce <Message>", 1, Rank.RESPONSABLE);
        suggest(true, RequiredArgumentBuilder.argument("message", StringArgumentType.greedyString()));
    }
    
    @Override
    public void onCommand(ConsulatPlayer sender, String[] args){
        StringBuilder builder = new StringBuilder(args[0]);
        for(int i = 1; i < args.length; ++i){
            builder.append(" ").append(args[i]);
        }
        String message = builder.toString();
        Bukkit.broadcastMessage(Text.BROADCAST_PREFIX + "§4" + sender.getName() + "§7 : §b" + message);
    }
}
