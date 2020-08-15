package fr.amisoz.consulatcore.commands.players;

import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.RequiredArgumentBuilder;
import fr.amisoz.consulatcore.ConsulatCore;
import fr.amisoz.consulatcore.Text;
import fr.amisoz.consulatcore.players.SPlayerManager;
import fr.amisoz.consulatcore.players.SurvivalPlayer;
import fr.leconsulat.api.commands.ConsulatCommand;
import fr.leconsulat.api.player.ConsulatPlayer;
import fr.leconsulat.api.ranks.Rank;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.jetbrains.annotations.NotNull;

import java.sql.SQLException;
import java.util.Map;
import java.util.Set;

public class HomeCommand extends ConsulatCommand {
    
    public HomeCommand(){
        super(ConsulatCore.getInstance(), "home");
        setDescription("Se téléporter à un home").
                setUsage("/home <home> - Se TP à un home").
                setRank(Rank.JOUEUR).
                suggest(RequiredArgumentBuilder.argument("home", StringArgumentType.word()).
                        suggests((context, builder) -> {
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
        SurvivalPlayer survivalPlayer = (SurvivalPlayer)sender;
        if(args.length == 0){
            Set<String> homes = survivalPlayer.getNameHomes();
            if(homes.size() == 0){
                sender.sendMessage(Text.NO_HOME);
                return;
            }
            sender.sendMessage(Text.LIST_HOME(homes));
            return;
        }
        String homeName = args[0];
        if(survivalPlayer.hasPower(Rank.MODPLUS) && homeName.endsWith(":")){
            homeName = homeName.substring(0, homeName.length() - 1);
            String player = homeName;
            Bukkit.getScheduler().runTaskAsynchronously(ConsulatCore.getInstance(), () -> {
                try {
                    Map<String, Location> homes = SPlayerManager.getInstance().getHomes(player, true);
                    if(homes.isEmpty()){
                        sender.sendMessage(Text.PLAYER_HAS_NO_HOME);
                    } else {
                        sender.sendMessage(Text.LIST_HOME_PLAYER(homes, player));
                    }
                } catch(SQLException e){
                    sender.sendMessage(Text.ERROR);
                }
            });
            return;
        }
        Location home = survivalPlayer.getHome(homeName);
        if(home != null){
            survivalPlayer.setOldLocation(sender.getPlayer().getLocation());
            sender.getPlayer().teleportAsync(home);
            sender.sendMessage(Text.TELEPORTATION);
        } else {
            Set<String> names = survivalPlayer.getNameHomes();
            if(names.isEmpty()){
                sender.sendMessage(Text.NO_HOME);
                return;
            }
            sender.sendMessage(Text.LIST_HOME(survivalPlayer.getNameHomes()));
        }
    }
}