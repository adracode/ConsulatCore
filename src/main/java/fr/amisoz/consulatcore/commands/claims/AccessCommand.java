package fr.amisoz.consulatcore.commands.claims;

import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import fr.amisoz.consulatcore.ConsulatCore;
import fr.amisoz.consulatcore.Text;
import fr.amisoz.consulatcore.claims.Claim;
import fr.amisoz.consulatcore.claims.ClaimManager;
import fr.amisoz.consulatcore.players.SPlayerManager;
import fr.amisoz.consulatcore.players.SurvivalOffline;
import fr.leconsulat.api.commands.Arguments;
import fr.leconsulat.api.commands.ConsulatCommand;
import fr.leconsulat.api.player.ConsulatPlayer;
import fr.leconsulat.api.ranks.Rank;
import org.bukkit.Bukkit;

import java.sql.SQLException;
import java.util.Collections;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

public class AccessCommand extends ConsulatCommand {
    
    public AccessCommand(){
        super("access", Collections.emptyList(), "/access [add <Joueur>, remove <Joueur>, list, addall <Joueur>, removeall <Joueur>]", 1, Rank.JOUEUR,
                LiteralArgumentBuilder.literal("access")
                        .then(LiteralArgumentBuilder.literal("list"))
                        .then(LiteralArgumentBuilder.literal("add")
                                .then(Arguments.player("player"))
                                .then(Arguments.word("player")))
                        .then(LiteralArgumentBuilder.literal("addall")
                                .then(Arguments.player("player"))
                                .then(Arguments.word("player")))
                        .then(LiteralArgumentBuilder.literal("remove")
                                .then(Arguments.player("player"))
                                .then(Arguments.word("player")))
                        .then(LiteralArgumentBuilder.literal("removeall")
                                .then(Arguments.player("player"))
                                .then(Arguments.word("player")))
        );
    }
    
    @Override
    public void onCommand(ConsulatPlayer sender, String[] args){
        ClaimManager claimManager = ClaimManager.getInstance();
        switch(args[0].toLowerCase()){
            case "addall":
                if(args.length == 1){
                    sender.sendMessage("§c" + getUsage());
                    return;
                }
                if(!claimManager.hasClaim(sender.getUUID())){
                    sender.sendMessage(Text.PREFIX + "§cTu n'a pas de claims.");
                    return;
                }
                Bukkit.getScheduler().runTaskAsynchronously(ConsulatCore.getInstance(), () -> {
                    try {
                        Optional<SurvivalOffline> result = SPlayerManager.getInstance().fetchOffline(args[1]);
                        if(!result.isPresent()){
                            sender.sendMessage(Text.PREFIX + "§cLe joueur spécifié n'existe pas.");
                            return;
                        }
                        SurvivalOffline target = result.get();
                        claimManager.giveAccesses(sender.getUUID(), target.getUUID());
                        sender.sendMessage(Text.PREFIX + "§aLe joueur a été ajouté à tes claims : §2" + target.getName());
                    } catch(SQLException e){
                        sender.sendMessage(Text.PREFIX + "§cUne erreur interne est survenue.");
                        e.printStackTrace();
                    }
                });
                break;
            case "removeall":
                if(args.length == 1){
                    sender.sendMessage("§c" + getUsage());
                    return;
                }
                if(!claimManager.hasClaim(sender.getUUID())){
                    sender.sendMessage(Text.PREFIX + "§cTu n'a pas de claims.");
                    return;
                }
                Bukkit.getScheduler().runTaskAsynchronously(ConsulatCore.getInstance(), () -> {
                    try {
                        Optional<SurvivalOffline> result = SPlayerManager.getInstance().fetchOffline(args[1]);
                        if(!result.isPresent()){
                            sender.sendMessage(Text.PREFIX + "§cLe joueur spécifié n'existe pas.");
                            return;
                        }
                        SurvivalOffline target = result.get();
                        claimManager.removeAccesses(sender.getUUID(), target.getUUID());
                        sender.sendMessage(Text.PREFIX + "§aLe joueur a été retiré de tes claims : §2" + target.getName());
                    } catch(SQLException e){
                        sender.sendMessage(Text.PREFIX + "§cUne erreur interne est survenue.");
                        e.printStackTrace();
                    }
                });
                break;
            case "add":{
                if(args.length == 1){
                    sender.sendMessage("§c" + getUsage());
                    return;
                }
                Claim claim = claimManager.getClaim(sender.getPlayer().getLocation().getChunk());
                if(claim == null || !claim.isOwner(sender)){
                    sender.sendMessage(Text.PREFIX + "§cTu dois être dans un claim t'appartenant pour ajouter un joueur.");
                    return;
                }
                Bukkit.getScheduler().runTaskAsynchronously(ConsulatCore.getInstance(), () -> {
                    try {
                        Optional<SurvivalOffline> result = SPlayerManager.getInstance().fetchOffline(args[1]);
                        if(!result.isPresent()){
                            sender.sendMessage(Text.PREFIX + "§cLe joueur spécifié n'existe pas.");
                            return;
                        }
                        SurvivalOffline target = result.get();
                        if(claim.isAllowed(target.getUUID())){
                            sender.sendMessage("§cCe joueur a déjà accès à ce claim.");
                            return;
                        }
                        claimManager.giveAccess(claim, target.getUUID());
                        sender.sendMessage(Text.PREFIX + "§aLe joueur a été ajouté à ton claim : §2" + target.getName());
                    } catch(SQLException e){
                        sender.sendMessage(Text.PREFIX + "§cUne erreur interne est survenue.");
                        e.printStackTrace();
                    }
                });
            }
            break;
            case "remove":{
                if(args.length == 1){
                    sender.sendMessage("§c" + getUsage());
                    return;
                }
                Claim claim = claimManager.getClaim(sender.getPlayer().getLocation().getChunk());
                if(claim == null || !claim.isOwner(sender)){
                    sender.sendMessage(Text.PREFIX + "§cTu dois être dans un claim t'appartenant pour enlever un joueur.");
                    return;
                }
                Bukkit.getScheduler().runTaskAsynchronously(ConsulatCore.getInstance(), () -> {
                    try {
                        Optional<SurvivalOffline> result = SPlayerManager.getInstance().fetchOffline(args[1]);
                        if(!result.isPresent()){
                            sender.sendMessage(Text.PREFIX + "§cLe joueur spécifié n'existe pas.");
                            return;
                        }
                        SurvivalOffline target = result.get();
                        if(!claim.isAllowed(target.getUUID())){
                            sender.sendMessage("§cLe joueur n'est pas dans ton claim.");
                            return;
                        }
                        claimManager.removeAccess(claim, target.getUUID());
                        sender.sendMessage(Text.PREFIX + "§2" + args[1] + "§a a été retiré de ton claim.");
                    } catch(SQLException e){
                        sender.sendMessage(Text.PREFIX + "§cUne erreur interne est survenue");
                    }
                });
            }
            break;
            case "list":{
                Claim claim = claimManager.getClaim(sender.getPlayer().getLocation().getChunk());
                if(claim == null || !claim.isOwner(sender)){
                    sender.sendMessage(Text.PREFIX + "§cTu dois être dans un claim t'appartenant pour voir la liste des joueurs ayant accès à ton claim.");
                    return;
                }
                Set<UUID> accesses = claim.getAllowedPlayers();
                if(accesses.size() == 0){
                    sender.sendMessage("§cAucun joueur n'a accès à ton claim.");
                    return;
                }
                //getOfflinePlayer est potentiellement bloquante (selon le commentaire du code source)
                Bukkit.getScheduler().runTaskAsynchronously(ConsulatCore.getInstance(), () -> {
                    StringBuilder builder = new StringBuilder();
                    for(UUID uuid : accesses){
                        builder.append(Bukkit.getOfflinePlayer(uuid).getName()).append(", ");
                    }
                    sender.sendMessage("§6Liste des joueurs ayant accès à ton claim : §e" + builder.substring(0, builder.length() - 2));
                });
            }
            break;
            default:
                sender.sendMessage("§c" + getUsage());
        }
    }
    
}
