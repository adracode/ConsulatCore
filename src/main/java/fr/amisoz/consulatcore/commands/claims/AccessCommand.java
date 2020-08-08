package fr.amisoz.consulatcore.commands.claims;

import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import fr.amisoz.consulatcore.ConsulatCore;
import fr.amisoz.consulatcore.Text;
import fr.amisoz.consulatcore.players.SurvivalPlayer;
import fr.amisoz.consulatcore.zones.Zone;
import fr.amisoz.consulatcore.zones.claims.Claim;
import fr.amisoz.consulatcore.zones.claims.ClaimManager;
import fr.leconsulat.api.commands.Arguments;
import fr.leconsulat.api.commands.ConsulatCommand;
import fr.leconsulat.api.player.CPlayerManager;
import fr.leconsulat.api.player.ConsulatPlayer;
import fr.leconsulat.api.ranks.Rank;
import org.bukkit.Bukkit;

import java.util.Set;
import java.util.UUID;

public class AccessCommand extends ConsulatCommand {
    
    //TODO: bug save ?
    public AccessCommand(){
        super("consulat.core", "access", "/access [add <Joueur>, remove <Joueur>, list, addall <Joueur>, removeall <Joueur>]", 1, Rank.JOUEUR);
        suggest(LiteralArgumentBuilder.literal("list"),
                LiteralArgumentBuilder.literal("add")
                        .then(Arguments.playerList("player"))
                        .then(Arguments.word("player")),
                LiteralArgumentBuilder.literal("addall")
                        .then(Arguments.playerList("player"))
                        .then(Arguments.word("player")),
                LiteralArgumentBuilder.literal("remove")
                        .then(Arguments.playerList("player"))
                        .then(Arguments.word("player")),
                LiteralArgumentBuilder.literal("removeall")
                        .then(Arguments.playerList("player"))
                        .then(Arguments.word("player"))
        );
    }
    
    @Override
    public void onCommand(ConsulatPlayer sender, String[] args){
        ClaimManager claimManager = ClaimManager.getInstance();
        SurvivalPlayer player = (SurvivalPlayer)sender;
        switch(args[0].toLowerCase()){
            case "addall":{
                if(args.length == 1){
                    sender.sendMessage("§c" + getUsage());
                    return;
                }
                Zone zone = player.getZone();
                if(zone == null){
                    sender.sendMessage(Text.PREFIX + "§cTu n'as pas de claims.");
                    return;
                }
                UUID targetUUID = CPlayerManager.getInstance().getPlayerUUID(args[1]);
                if(targetUUID == null){
                    sender.sendMessage(Text.PREFIX + "§cLe joueur spécifié n'existe pas.");
                    return;
                }
                if(targetUUID.equals(player.getUUID()) || !player.getZone().addPlayer(targetUUID)){
                    sender.sendMessage("§cCe joueur a déjà accès à tes claims.");
                    return;
                }
                sender.sendMessage(Text.PREFIX + "§aLe joueur a été ajouté à tes claims : §2" + args[1]);
            }
            break;
            case "removeall":{
                if(args.length == 1){
                    sender.sendMessage("§c" + getUsage());
                    return;
                }
                Zone zone = player.getZone();
                if(zone == null){
                    sender.sendMessage(Text.PREFIX + "§cTu n'as pas de claims.");
                    return;
                }
                UUID targetUUID = CPlayerManager.getInstance().getPlayerUUID(args[1]);
                if(targetUUID == null){
                    sender.sendMessage(Text.PREFIX + "§cLe joueur spécifié n'existe pas.");
                    return;
                }
                if(targetUUID.equals(player.getUUID())){
                    sender.sendMessage(Text.PREFIX + "§cTu ne peux pas te retirer tes accès.");
                    return;
                }
                if(!player.getZone().removePlayer(targetUUID)){
                    sender.sendMessage("§cCe joueur n'a aucun accès sur tes claims.");
                    return;
                }
                sender.sendMessage(Text.PREFIX + "§aLe joueur a été retiré de tes claims : §2" + args[2]);
            }
            break;
            case "add":{
                if(args.length == 1){
                    sender.sendMessage("§c" + getUsage());
                    return;
                }
                Claim claim = player.getClaim();
                if(claim == null || !claim.canManageAccesses(sender.getUUID())){
                    sender.sendMessage(Text.PREFIX + "§cTu dois être dans un claim t'appartenant pour ajouter un joueur.");
                    return;
                }
                UUID targetUUID = CPlayerManager.getInstance().getPlayerUUID(args[1]);
                if(targetUUID == null){
                    sender.sendMessage(Text.PREFIX + "§cLe joueur spécifié n'existe pas.");
                    return;
                }
                if(targetUUID.equals(player.getUUID()) || !claim.addPlayer(targetUUID)){
                    sender.sendMessage("§cCe joueur a déjà accès à ce claim.");
                    return;
                }
                sender.sendMessage(Text.PREFIX + "§aLe joueur a été ajouté à ton claim : §2" + args[1]);
            }
            break;
            case "remove":{
                if(args.length == 1){
                    sender.sendMessage("§c" + getUsage());
                    return;
                }
                Claim claim = claimManager.getClaim(sender.getPlayer().getLocation().getChunk());
                if(claim == null || !claim.canManageAccesses(sender.getUUID())){
                    sender.sendMessage(Text.PREFIX + "§cTu dois être dans un claim t'appartenant pour enlever un joueur.");
                    return;
                }
                UUID targetUUID = CPlayerManager.getInstance().getPlayerUUID(args[1]);
                if(targetUUID == null){
                    sender.sendMessage(Text.PREFIX + "§cLe joueur spécifié n'existe pas.");
                    return;
                }
                if(targetUUID.equals(player.getUUID())){
                    sender.sendMessage(Text.PREFIX + "§cTu ne peux pas te retirer tes accès.");
                    return;
                }
                if(!claim.removePlayer(targetUUID)){
                    sender.sendMessage("§cLe joueur n'est pas dans ton claim.");
                    return;
                }
                sender.sendMessage(Text.PREFIX + "§2" + args[1] + "§a a été retiré de ton claim.");
            }
            break;
            case "list":{
                Claim claim = claimManager.getClaim(sender.getPlayer().getLocation().getChunk());
                if(claim == null || !claim.canManageAccesses(sender.getUUID())){
                    sender.sendMessage(Text.PREFIX + "§cTu dois être dans un claim t'appartenant pour voir la liste des joueurs ayant accès à ton claim.");
                    return;
                }
                Set<UUID> accesses = claim.getPlayers();
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
