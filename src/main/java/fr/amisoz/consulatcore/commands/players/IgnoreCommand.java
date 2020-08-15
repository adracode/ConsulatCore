package fr.amisoz.consulatcore.commands.players;

import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import fr.amisoz.consulatcore.ConsulatCore;
import fr.amisoz.consulatcore.Text;
import fr.amisoz.consulatcore.players.SurvivalPlayer;
import fr.leconsulat.api.commands.Arguments;
import fr.leconsulat.api.commands.ConsulatCommand;
import fr.leconsulat.api.player.CPlayerManager;
import fr.leconsulat.api.player.ConsulatPlayer;
import fr.leconsulat.api.ranks.Rank;
import org.bukkit.Bukkit;
import org.jetbrains.annotations.NotNull;

import java.util.Set;
import java.util.UUID;

public class IgnoreCommand extends ConsulatCommand {
    
    public IgnoreCommand(){
        super(ConsulatCore.getInstance(), "ignore");
        setDescription("Ignorer un joueur").
                setUsage("/ignore add <joueur> - Ignore un joueur\n" +
                        "/ignore remove <joueur> - Dé-ignore un joueur\n" +
                        "/ignore list - Affiche les joueurs ignorés").
                setArgsMin(1).
                setRank(Rank.JOUEUR).
                suggest(LiteralArgumentBuilder.literal("add")
                                .then(Arguments.playerList("joueur")),
                        LiteralArgumentBuilder.literal("remove")
                                .then(Arguments.word("joueur").suggests(((context, builder) -> {
                                    SurvivalPlayer player = (SurvivalPlayer)getConsulatPlayerFromContext(context.getSource());
                                    if(player == null){
                                        return builder.buildFuture();
                                    }
                                    Arguments.suggest(player.getIgnoredPlayers(), uuid -> Bukkit.getOfflinePlayer(uuid).getName(), uuid -> true, builder);
                                    return builder.buildFuture();
                                }))),
                        LiteralArgumentBuilder.literal("list"));
    }
    
    @Override
    public void onCommand(@NotNull ConsulatPlayer sender, @NotNull String[] args){
        SurvivalPlayer player = (SurvivalPlayer)sender;
        switch(args[0].toLowerCase()){
            case "add":{
                if(args.length < 2){
                    break;
                }
                UUID target = CPlayerManager.getInstance().getPlayerUUID(args[1]);
                if(target == null){
                    player.sendMessage(Text.PLAYER_DOESNT_EXISTS);
                    return;
                }
                if(!player.ignorePlayer(target)){
                    player.sendMessage(Text.ALREADY_IGNORED);
                    return;
                }
                player.sendMessage(Text.PLAYER_IGNORED(args[1]));
            }
            return;
            case "remove":{
                if(args.length < 2){
                    break;
                }
                UUID target = CPlayerManager.getInstance().getPlayerUUID(args[1]);
                if(target == null){
                    player.sendMessage(Text.PLAYER_DOESNT_EXISTS);
                    return;
                }
                if(!player.removeIgnoredPlayer(target)){
                    player.sendMessage(Text.NOT_IGNORED);
                    return;
                }
                player.sendMessage(Text.NO_MORE_IGNORED(args[1]));
            }
            return;
            case "list":
                Set<UUID> ignored = player.getIgnoredPlayers();
                if(ignored.isEmpty()){
                    player.sendMessage(Text.NOBODY_IGNORED);
                    return;
                }
                player.sendMessage(Text.LIST_IGNORED(ignored));
                return;
        }
        player.sendMessage(getUsage());
    }
    
    
}
