package fr.leconsulat.core.commands.players;

import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.RequiredArgumentBuilder;
import fr.leconsulat.api.commands.ConsulatCommand;
import fr.leconsulat.api.player.CPlayerManager;
import fr.leconsulat.api.player.ConsulatPlayer;
import fr.leconsulat.api.ranks.Rank;
import fr.leconsulat.core.ConsulatCore;
import fr.leconsulat.core.Text;
import fr.leconsulat.core.players.SPlayerManager;
import fr.leconsulat.core.players.SurvivalPlayer;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.jetbrains.annotations.NotNull;

import java.sql.SQLException;
import java.util.Map;

public class DelHomeCommand extends ConsulatCommand {
    
    public DelHomeCommand(){
        super(ConsulatCore.getInstance(), "delhome");
        setDescription("Supprimer un home").
                setUsage("/delhome <home> - Supprimer un home").
                setArgsMin(1).
                setRank(Rank.JOUEUR).
                suggest(RequiredArgumentBuilder.argument("home", StringArgumentType.word()).suggests((context, builder) -> {
                    SurvivalPlayer player = (SurvivalPlayer)getConsulatPlayer(context.getSource());
                    if(player == null){
                        return builder.buildFuture();
                    }
                    for(String home : player.getNameHomes()){
                        if(home.toLowerCase().startsWith(builder.getRemaining().toLowerCase())){
                            builder.suggest(home);
                        }
                    }
                    return builder.buildFuture();
                }));
    }
    
    @Override
    public void onCommand(@NotNull ConsulatPlayer sender, @NotNull String[] args){
        SurvivalPlayer survivalSender = (SurvivalPlayer)sender;
        if(args.length == 2 && survivalSender.hasPower(Rank.MODPLUS)){
            Bukkit.getScheduler().runTaskAsynchronously(ConsulatCore.getInstance(), () -> {
                try {
                    SurvivalPlayer target = (SurvivalPlayer)CPlayerManager.getInstance().getConsulatPlayer(args[0]);
                    if(target != null){
                        if(target.hasHome(args[1])){
                            target.removeHome(args[1]);
                            sender.sendMessage(Text.HOME_DELETED);
                        } else {
                            sender.sendMessage(Text.PLAYER_HAS_NO_HOME);
                        }
                        return;
                    }
                    Map<String, Location> homes = SPlayerManager.getInstance().getHomes(args[0], false);
                    if(!homes.containsKey(args[1].toLowerCase())){
                        sender.sendMessage(Text.PLAYER_HAS_NO_HOME);
                    } else {
                        if(SPlayerManager.getInstance().removeHome(args[0], args[1])){
                            sender.sendMessage(Text.HOME_DELETED);
                        } else {
                            sender.sendMessage(Text.PLAYER_HAS_NO_HOME);
                        }
                    }
                } catch(SQLException e){
                    sender.sendMessage(Text.ERROR);
                    e.printStackTrace();
                }
            });
            return;
        }
        if(!survivalSender.hasHome(args[0])){
            sender.sendMessage(Text.UNKNOWN_HOME);
            return;
        }
        Bukkit.getScheduler().runTaskAsynchronously(ConsulatCore.getInstance(), () -> {
            try {
                survivalSender.removeHome(args[0]);
                sender.sendMessage(Text.HOME_DELETED);
            } catch(SQLException e){
                sender.sendMessage(Text.ERROR);
                e.printStackTrace();
            }
        });
    }
}
