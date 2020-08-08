package fr.amisoz.consulatcore.commands.players;

import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import fr.amisoz.consulatcore.players.SurvivalPlayer;
import fr.leconsulat.api.commands.Arguments;
import fr.leconsulat.api.commands.ConsulatCommand;
import fr.leconsulat.api.player.CPlayerManager;
import fr.leconsulat.api.player.ConsulatPlayer;
import fr.leconsulat.api.ranks.Rank;
import org.bukkit.Bukkit;

import java.util.Set;
import java.util.UUID;

public class IgnoreCommand extends ConsulatCommand {
    
    public IgnoreCommand(){
        super("consulat.core", "ignore", "/ignore [add <joueur>|remove <joueur>|list]", 1, Rank.JOUEUR);
        suggest(LiteralArgumentBuilder.literal("add")
                        .then(Arguments.playerList("joueur")),
                LiteralArgumentBuilder.literal("remove")
                        .then(Arguments.word("joueur").suggests(((context, builder) -> {
                            SurvivalPlayer player = (SurvivalPlayer)CPlayerManager.getInstance().getConsulatPlayerFromContextSource(context.getSource());
                            if(player == null){
                                return builder.buildFuture();
                            }
                            Arguments.suggest(player.getIgnoredPlayers(), uuid -> Bukkit.getOfflinePlayer(uuid).getName(), uuid -> true, builder);
                            return builder.buildFuture();
                        }))),
                LiteralArgumentBuilder.literal("list")
        );
    }
    
    @Override
    public void onCommand(ConsulatPlayer sender, String[] args){
        SurvivalPlayer player = (SurvivalPlayer)sender;
        switch(args[0].toLowerCase()){
            case "add":{
                if(args.length < 2){
                    break;
                }
                UUID target = CPlayerManager.getInstance().getPlayerUUID(args[1]);
                if(target == null){
                    player.sendMessage("§cCe joueur n'existe pas.");
                    return;
                }
                if(!player.ignorePlayer(target)){
                    player.sendMessage("§cTu as déjà ignoré " + args[1]);
                    return;
                }
                player.sendMessage("§a" + args[1] + " a été ignoré.");
            }
            return;
            case "remove":{
                if(args.length < 2){
                    break;
                }
                UUID target = CPlayerManager.getInstance().getPlayerUUID(args[1]);
                if(target == null){
                    player.sendMessage("§cCe joueur n'existe pas.");
                    return;
                }
                if(!player.removeIgnoredPlayer(target)){
                    player.sendMessage("§c" + args[1] + " n'est pas ignoré.");
                    return;
                }
                player.sendMessage("§a" + args[1] + " a été retiré des joueurs ingorés.");
            }
            return;
            case "list":
                Set<UUID> ignored = player.getIgnoredPlayers();
                if(ignored.isEmpty()){
                    player.sendMessage("§aTu n'as ignoré personne.");
                    return;
                }
                StringBuilder listIgnored = new StringBuilder();
                for(UUID ignoredPlayer : ignored){
                    listIgnored.append(Bukkit.getOfflinePlayer(ignoredPlayer)).append(", ");
                }
                player.sendMessage("§aJoueurs ignorés: " + listIgnored.delete(listIgnored.length() - 2, listIgnored.length()).toString());
                return;
        }
        player.sendMessage(getUsage());
    }
    
    
}
