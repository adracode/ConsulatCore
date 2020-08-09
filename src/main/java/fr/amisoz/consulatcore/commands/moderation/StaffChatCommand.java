package fr.amisoz.consulatcore.commands.moderation;

import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.RequiredArgumentBuilder;
import fr.amisoz.consulatcore.ConsulatCore;
import fr.amisoz.consulatcore.channel.StaffChannel;
import fr.leconsulat.api.commands.ConsulatCommand;
import fr.leconsulat.api.player.ConsulatPlayer;
import fr.leconsulat.api.ranks.Rank;

public class StaffChatCommand extends ConsulatCommand {
    
    public StaffChatCommand(){
        super("consulat.core", "staffchat", "sc", "/sc <Message>", 1, Rank.MODO);
        suggest(RequiredArgumentBuilder.argument("message", StringArgumentType.greedyString()));
    }
    
    @Override
    public void onCommand(ConsulatPlayer sender, String[] args){
        StringBuilder builder = new StringBuilder(args[0]);
        for(int i = 1; i < args.length; i++){
            builder.append(' ').append(args[i]);
        }
        StaffChannel channel = ConsulatCore.getInstance().getStaffChannel();
        channel.sendMessage(channel.speak(sender, builder.toString()));
    }
}
