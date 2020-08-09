package fr.amisoz.consulatcore.commands.players;

import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.RequiredArgumentBuilder;
import fr.amisoz.consulatcore.Text;
import fr.amisoz.consulatcore.moderation.MuteObject;
import fr.amisoz.consulatcore.players.SurvivalPlayer;
import fr.leconsulat.api.commands.ConsulatCommand;
import fr.leconsulat.api.player.ConsulatPlayer;
import fr.leconsulat.api.ranks.Rank;
import it.unimi.dsi.fastutil.objects.Object2LongMap;
import it.unimi.dsi.fastutil.objects.Object2LongOpenHashMap;
import org.bukkit.Bukkit;

import java.util.UUID;

public class AdvertCommand extends ConsulatCommand {
    
    private Object2LongMap<UUID> delay = new Object2LongOpenHashMap<>();
    
    public AdvertCommand(){
        super("consulat.core", "advert", "/advert <annonce>", 1, Rank.FINANCEUR);
        suggest(RequiredArgumentBuilder.argument("annonce", StringArgumentType.greedyString()));
        delay.defaultReturnValue(-1);
    }
    
    @Override
    public void onCommand(ConsulatPlayer sender, String[] args){
        long delay = this.delay.getLong(sender.getUUID());
        if(delay != -1){
            if((System.currentTimeMillis() - delay) < 3 * 60 * 60 * 1000){
                sender.sendMessage(Text.NEED_WAIT);
                return;
            }
        }
        SurvivalPlayer survivalSender = (SurvivalPlayer)sender;
        if(survivalSender.isMuted()){
            MuteObject muteInfo = survivalSender.getMute();
            if(muteInfo != null){
                sender.sendMessage(Text.YOU_MUTE(muteInfo));
                return;
            }
        }
        this.delay.put(sender.getUUID(), System.currentTimeMillis());
        StringBuilder message = new StringBuilder(args[0]);
        for(int i = 1; i < args.length; ++i){
            message.append(" ").append(args[i]);
        }
        Bukkit.broadcastMessage(Text.ADVERT(sender.getName(), message.toString()));
    }
}
