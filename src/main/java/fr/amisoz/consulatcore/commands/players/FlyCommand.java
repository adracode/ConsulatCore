package fr.amisoz.consulatcore.commands.players;

import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import fr.amisoz.consulatcore.ConsulatCore;
import fr.amisoz.consulatcore.Text;
import fr.amisoz.consulatcore.players.SurvivalPlayer;
import fr.leconsulat.api.commands.ConsulatCommand;
import fr.leconsulat.api.player.ConsulatPlayer;
import fr.leconsulat.api.ranks.Rank;
import org.bukkit.Bukkit;

import java.sql.SQLException;

public class FlyCommand extends ConsulatCommand {
    
    public FlyCommand(){
        super("consulat.core", "fly", "/fly [start/stop/info]", 1, Rank.JOUEUR);
        suggest((listener) -> {
                    SurvivalPlayer player = (SurvivalPlayer)getConsulatPlayer(listener);
                    return player != null && player.hasFly();
                },
                LiteralArgumentBuilder.literal("start"),
                LiteralArgumentBuilder.literal("stop"),
                LiteralArgumentBuilder.literal("info")
        );
    }
    
    @Override
    public void onCommand(ConsulatPlayer sender, String[] args){
        SurvivalPlayer player = (SurvivalPlayer)sender;
        if(!player.hasFly()){
            sender.sendMessage(Text.FLY + "Erreur | Tu n'as pas acheté le fly !");
            return;
        }
        if(sender.getPlayer().getWorld() != Bukkit.getWorlds().get(0)){
            sender.sendMessage(Text.FLY + "Erreur | Tu dois être dans le monde de base !");
            return;
        }
        if(player.isInModeration()){
            sender.sendMessage(Text.PREFIX + "§cTu ne peux pas utiliser cette commande en modération");
            return;
        }
        switch(args[0].toLowerCase()){
            case "start":
                if(player.isFlying()){
                    sender.sendMessage(Text.FLY + "Erreur | Ton fly est déjà actif !");
                    return;
                }
                if(!player.isFlyAvailable()){
                    long timeWait = (player.getFlyReset() - System.currentTimeMillis()) / 1000;
                    long minutes = ((timeWait / 60) % 60);
                    long seconds = (timeWait % 60);
                    player.sendMessage(Text.FLY + "Erreur | Tu n'as pas attendu assez longtemps ! Tu dois encore attendre " + minutes + "m" + seconds + "s.");
                    return;
                }
                if(!player.canFlyHere()){
                    sender.sendMessage(Text.FLY + "Erreur | Tu ne peux pas fly dans un autre claim que le tien ou ceux que tu as accès !");
                    return;
                }
                player.enableFly();
                sender.sendMessage(Text.FLY + "Tu as activé ton fly !");
                break;
            case "info":{
                if(player.hasInfiniteFly()){
                    player.sendMessage(Text.FLY + "§aTu as le fly infini.");
                    return;
                }
                int timeLeft;
                if(player.getFlyReset() < System.currentTimeMillis()){
                    timeLeft = player.getFlyTime();
                } else {
                    timeLeft = player.getFlyTimeLeft();
                }
                long minutes = ((timeLeft / 60) % 60);
                long seconds = timeLeft % 60;
                sender.sendMessage(Text.FLY + "Tu as encore ton fly pendant " + minutes + "m " + seconds + "s.");
            }
            break;
            case "stop":{
                if(!player.hasFly()){
                    sender.sendMessage(Text.FLY + "Erreur | Tu n'as pas de fly.");
                    return;
                }
                if(!player.isFlying()){
                    sender.sendMessage(Text.FLY + "Erreur | Tu n'es pas en fly.");
                    return;
                }
                Bukkit.getScheduler().runTaskAsynchronously(ConsulatCore.getInstance(), () -> {
                    try {
                        player.disableFly();
                        sender.sendMessage(Text.FLY + (player.hasInfiniteFly() ? "Tu as enlevé ton fly infini" : "Ton fly est en pause !"));
                    } catch(SQLException e){
                        sender.sendMessage("§cUne erreur interne est survenue");
                        e.printStackTrace();
                    }
                });
            }
            break;
            default:
                sender.sendMessage(Text.FLY + getUsage());
                break;
        }
    }
}
