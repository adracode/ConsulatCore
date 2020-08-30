package fr.leconsulat.core.commands.claims;

import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import fr.leconsulat.api.commands.Arguments;
import fr.leconsulat.api.commands.ConsulatCommand;
import fr.leconsulat.api.player.CPlayerManager;
import fr.leconsulat.api.player.ConsulatPlayer;
import fr.leconsulat.api.ranks.Rank;
import fr.leconsulat.core.ConsulatCore;
import fr.leconsulat.core.Text;
import fr.leconsulat.core.players.SurvivalPlayer;
import fr.leconsulat.core.zones.Zone;
import fr.leconsulat.core.zones.claims.Claim;
import fr.leconsulat.core.zones.claims.ClaimManager;
import org.jetbrains.annotations.NotNull;

import java.util.Set;
import java.util.UUID;

public class AccessCommand extends ConsulatCommand {
    
    public AccessCommand(){
        super(ConsulatCore.getInstance(), "access");
        setDescription("Gérer les accès de tes claims").
                setUsage("/access add <joueur> - Ajoute un joueur au claim\n" +
                        "/access addall <joueur> - Ajoute un joueur à tous tes claims\n" +
                        "/access list - Affiche les joueurs ayant accès\n" +
                        "/access remove <joueur> - Retire un joueur du claim\n" +
                        "/access removeall <joueur> - Retire un joueur de tous tes claims").
                setArgsMin(1).
                setRank(Rank.JOUEUR).
                suggest(LiteralArgumentBuilder.literal("list"),
                        LiteralArgumentBuilder.literal("add").
                                then(Arguments.playerList("player")),
                        LiteralArgumentBuilder.literal("addall").
                                then(Arguments.playerList("player")),
                        LiteralArgumentBuilder.literal("remove").
                                then(Arguments.playerList("player")),
                        LiteralArgumentBuilder.literal("removeall").
                                then(Arguments.playerList("player")));
    }
    
    @Override
    public void onCommand(@NotNull ConsulatPlayer sender, @NotNull String[] args){
        ClaimManager claimManager = ClaimManager.getInstance();
        SurvivalPlayer player = (SurvivalPlayer)sender;
        switch(args[0].toLowerCase()){
            case "addall":{
                if(args.length == 1){
                    sender.sendMessage(Text.COMMAND_USAGE(this));
                    return;
                }
                Zone zone = player.getZone();
                if(zone == null){
                    sender.sendMessage(Text.YOU_NO_CLAIM);
                    return;
                }
                UUID targetUUID = CPlayerManager.getInstance().getPlayerUUID(args[1]);
                if(targetUUID == null){
                    sender.sendMessage(Text.PLAYER_DOESNT_EXISTS);
                    return;
                }
                if(targetUUID.equals(player.getUUID()) || !player.getZone().addPlayer(targetUUID)){
                    sender.sendMessage(Text.PLAYER_ALREADY_ACCESS_CLAIMS);
                    return;
                }
                sender.sendMessage(Text.ADD_PLAYER_CLAIMS(args[1]));
            }
            break;
            case "removeall":{
                if(args.length == 1){
                    sender.sendMessage(Text.COMMAND_USAGE(this));
                    return;
                }
                Zone zone = player.getZone();
                if(zone == null){
                    sender.sendMessage(Text.YOU_NO_CLAIM);
                    return;
                }
                UUID targetUUID = CPlayerManager.getInstance().getPlayerUUID(args[1]);
                if(targetUUID == null){
                    sender.sendMessage(Text.PLAYER_DOESNT_EXISTS);
                    return;
                }
                if(targetUUID.equals(player.getUUID())){
                    sender.sendMessage(Text.CANT_MANAGE_ACCESS_PLAYER);
                    return;
                }
                if(!player.getZone().removePlayer(targetUUID)){
                    sender.sendMessage(Text.PLAYER_NOT_ACCESS_CLAIMS);
                    return;
                }
                sender.sendMessage(Text.REMOVE_PLAYER_CLAIMS(args[1]));
            }
            break;
            case "add":{
                if(args.length == 1){
                    sender.sendMessage(Text.COMMAND_USAGE(this));
                    return;
                }
                Claim claim = player.getClaim();
                if(claim == null){
                    sender.sendMessage(Text.NOT_IN_YOUR_CLAIM);
                    return;
                }
                if(!claim.canManageAccesses(sender.getUUID())){
                    sender.sendMessage(Text.CANT_MANAGE_ACCESS);
                    return;
                }
                UUID targetUUID = CPlayerManager.getInstance().getPlayerUUID(args[1]);
                if(targetUUID == null){
                    sender.sendMessage(Text.PLAYER_DOESNT_EXISTS);
                    return;
                }
                if(targetUUID.equals(player.getUUID()) || !claim.addPlayer(targetUUID)){
                    sender.sendMessage(Text.PLAYER_ALREADY_ACCESS_CLAIM);
                    return;
                }
                sender.sendMessage(Text.ADD_PLAYER_CLAIM(args[1]));
            }
            break;
            case "remove":{
                if(args.length == 1){
                    sender.sendMessage(Text.COMMAND_USAGE(this));
                    return;
                }
                Claim claim = claimManager.getClaim(sender.getPlayer().getLocation().getChunk());
                if(claim == null){
                    sender.sendMessage(Text.NOT_IN_YOUR_CLAIM);
                    return;
                } else if(!claim.canManageAccesses(sender.getUUID())){
                    sender.sendMessage(Text.CANT_MANAGE_ACCESS);
                    return;
                }
                UUID targetUUID = CPlayerManager.getInstance().getPlayerUUID(args[1]);
                if(targetUUID == null){
                    sender.sendMessage(Text.PLAYER_DOESNT_EXISTS);
                    return;
                }
                if(targetUUID.equals(player.getUUID())){
                    sender.sendMessage(Text.CANT_MANAGE_ACCESS_PLAYER);
                    return;
                }
                if(!claim.removePlayer(targetUUID)){
                    sender.sendMessage(Text.PLAYER_NOT_ACCESS_CLAIM);
                    return;
                }
                sender.sendMessage(Text.REMOVE_PLAYER_CLAIM(args[1]));
            }
            break;
            case "list":{
                Claim claim = claimManager.getClaim(sender.getPlayer().getLocation().getChunk());
                if(claim == null){
                    sender.sendMessage(Text.NOT_IN_YOUR_CLAIM);
                    return;
                } else if(!claim.canManageAccesses(sender.getUUID())){
                    sender.sendMessage(Text.CANT_MANAGE_ACCESS);
                    return;
                }
                Set<UUID> accesses = claim.getPlayers();
                if(accesses.isEmpty()){
                    sender.sendMessage(Text.NOBODY_ACCESS_CLAIM);
                    return;
                }
                sender.sendMessage(Text.LIST_ACCESS_CLAIM(accesses));
            }
            break;
            default:
                sender.sendMessage(Text.COMMAND_USAGE(this));
        }
    }
    
}
