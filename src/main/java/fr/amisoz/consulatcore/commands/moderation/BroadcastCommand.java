package fr.amisoz.consulatcore.commands.moderation;

import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.RequiredArgumentBuilder;
import fr.amisoz.consulatcore.ConsulatCore;
import fr.amisoz.consulatcore.Text;
import fr.leconsulat.api.commands.ConsulatCommand;
import fr.leconsulat.api.player.ConsulatPlayer;
import fr.leconsulat.api.ranks.Rank;
import org.bukkit.Bukkit;
import org.jetbrains.annotations.NotNull;

public class BroadcastCommand extends ConsulatCommand {
    
    public BroadcastCommand(){
        super(ConsulatCore.getInstance(), "annonce");
        setDescription("Envoyer un message à tous les joueurs").
                setUsage("/annonce <message> - Envoyer un message").
                setAliases("broadcast", "bc").
                setArgsMin(1).
                setRank(Rank.RESPONSABLE).
                suggest(RequiredArgumentBuilder.argument("message", StringArgumentType.greedyString()));
    }
    
    @Override
    public void onCommand(@NotNull ConsulatPlayer sender, @NotNull String[] args){
        StringBuilder builder = new StringBuilder(args[0]);
        for(int i = 1; i < args.length; ++i){
            builder.append(" ").append(args[i]);
        }
        Bukkit.broadcastMessage(Text.BRODCAST(sender.getName(), builder.toString()));
    }
}
