package fr.leconsulat.core.commands.moderation;

import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.RequiredArgumentBuilder;
import fr.leconsulat.api.commands.ConsulatCommand;
import fr.leconsulat.api.player.ConsulatPlayer;
import fr.leconsulat.api.ranks.Rank;
import fr.leconsulat.core.ConsulatCore;
import fr.leconsulat.core.channel.StaffChannel;
import org.jetbrains.annotations.NotNull;

public class StaffChatCommand extends ConsulatCommand {
    
    public StaffChatCommand(){
        super(ConsulatCore.getInstance(), "staffchat");
        setDescription("Envoyer un message dans le chat de staff").
                setUsage("/staffchat <message> - Envoyer un message").
                setAliases("sc").
                setArgsMin(1).
                setRank(Rank.BUILDER).
                suggest(RequiredArgumentBuilder.argument("message", StringArgumentType.greedyString()));
    }
    
    @Override
    public void onCommand(@NotNull ConsulatPlayer sender, @NotNull String[] args){
        StringBuilder builder = new StringBuilder(args[0]);
        for(int i = 1; i < args.length; i++){
            builder.append(' ').append(args[i]);
        }
        StaffChannel channel = ConsulatCore.getInstance().getStaffChannel();
        channel.sendMessage(channel.speak(sender, builder.toString()));
    }
}
