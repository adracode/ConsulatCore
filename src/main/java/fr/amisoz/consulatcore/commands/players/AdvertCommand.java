package fr.amisoz.consulatcore.commands.players;

import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.RequiredArgumentBuilder;
import fr.amisoz.consulatcore.moderation.MuteObject;
import fr.amisoz.consulatcore.players.SurvivalPlayer;
import fr.leconsulat.api.commands.ConsulatCommand;
import fr.leconsulat.api.player.ConsulatPlayer;
import fr.leconsulat.api.ranks.Rank;
import org.bukkit.Bukkit;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class AdvertCommand extends ConsulatCommand {
    
    private Map<UUID, Long> delay = new HashMap<>();
    
    public AdvertCommand(){
        super("advert", "/advert <Annonce>", 1, Rank.FINANCEUR);
        suggest(true,
                RequiredArgumentBuilder.argument("annonce", StringArgumentType.greedyString()));
    }
    
    @Override
    public void onCommand(ConsulatPlayer sender, String[] args){
        Long delay = this.delay.get(sender.getUUID());
        if(delay != null){
            if((System.currentTimeMillis() - delay) < 3 * 60 * 60 * 1000){
                sender.sendMessage("§cTu dois attendre pour refaire cette commande.");
                return;
            }
        }
        SurvivalPlayer survivalSender = (SurvivalPlayer)sender;
        if(survivalSender.isMuted()){
            MuteObject muteInfo = survivalSender.getMute();
            if(muteInfo != null){
                sender.sendMessage("§cTu es actuellement mute.\n§4Raison : §c" + muteInfo.getReason() + "\n§4Jusqu'au : §c" + muteInfo.getEndDate());
                return;
            }
        }
        this.delay.put(sender.getUUID(), System.currentTimeMillis());
        StringBuilder builder = new StringBuilder(args[0]);
        for(int i = 1; i < args.length; ++i){
            builder.append(" ").append(args[i]);
        }
        String message = builder.toString();
        Bukkit.broadcastMessage("§e[Annonce] §6" + sender.getName() + "§7 : §r" + message);
    }
}
