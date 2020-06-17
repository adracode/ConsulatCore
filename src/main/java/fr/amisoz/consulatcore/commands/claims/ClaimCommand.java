package fr.amisoz.consulatcore.commands.claims;

import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.builder.RequiredArgumentBuilder;
import fr.amisoz.consulatcore.ConsulatCore;
import fr.amisoz.consulatcore.Text;
import fr.amisoz.consulatcore.players.SPlayerManager;
import fr.amisoz.consulatcore.players.SurvivalPlayer;
import fr.amisoz.consulatcore.zones.Zone;
import fr.amisoz.consulatcore.zones.ZoneManager;
import fr.amisoz.consulatcore.zones.claims.Claim;
import fr.amisoz.consulatcore.zones.claims.ClaimManager;
import fr.leconsulat.api.commands.Arguments;
import fr.leconsulat.api.commands.ConsulatCommand;
import fr.leconsulat.api.player.CPlayerManager;
import fr.leconsulat.api.player.ConsulatPlayer;
import fr.leconsulat.api.ranks.Rank;
import org.bukkit.Bukkit;
import org.bukkit.Chunk;

import java.sql.SQLException;
import java.util.Set;

public class ClaimCommand extends ConsulatCommand {

    public ClaimCommand(){
        super("claim", "/claim | /claim kick <Joueur> | /claim desc <Description> | /claim list", 0, Rank.JOUEUR);
        suggest(true,
                LiteralArgumentBuilder.literal("kick")
                        .then(Arguments.playerList("joueur")),
                LiteralArgumentBuilder.literal("desc")
                        .then(RequiredArgumentBuilder.argument("description", StringArgumentType.greedyString())),
                LiteralArgumentBuilder.literal("list"),
                LiteralArgumentBuilder.literal("info")
                        .requires((t) -> {
                            ConsulatPlayer player = getConsulatPlayer(t);
                            return player != null && player.hasPower(Rank.RESPONSABLE);
                        })
                        .then(Arguments.playerList("joueur")));
    }

    @Override
    public void onCommand(ConsulatPlayer sender, String[] args){
        SurvivalPlayer player = (SurvivalPlayer)sender;
        if(args.length == 0){
            if(sender.getPlayer().getLocation().getWorld() != Bukkit.getWorlds().get(0)){
                sender.sendMessage(Text.PREFIX + "§cIl faut être dans le monde de base pour claim.");
                return;
            }
            if(player.getClaim() != null){
                sender.sendMessage(Text.PREFIX + "§cCe chunk est déjà claim.");
                return;
            }
            if(!player.hasMoney(Claim.BUY_CLAIM)){
                sender.sendMessage(Text.PREFIX + "§cTu n'as pas assez d'argent pour claim ce chunk, il te faut " + Claim.BUY_CLAIM + "€.");
                return;
            }
            sender.sendMessage("§aClaim du chunk en cours...");
            Chunk chunk = sender.getPlayer().getChunk();
            Zone zone = player.getZone();
            if(zone == null){
                zone = new Zone(sender.getUUID(), sender.getName(), sender.getUUID());
                ZoneManager.getInstance().addZone(zone);
            }
            ClaimManager.getInstance().playerClaim(chunk.getX(), chunk.getZ(), zone);
            player.removeMoney(Claim.BUY_CLAIM);
            sender.sendMessage(Text.PREFIX + "§aTu as claim ce chunk pour un prix de " + Claim.BUY_CLAIM + " €.");
            return;
        }
        switch(args[0].toLowerCase()){
            case "kick":{
                if(args.length == 1){
                    sender.sendMessage(Text.PREFIX + "§cMerci de spécifier le joueur à kick.");
                    return;
                }
                SurvivalPlayer target = (SurvivalPlayer)CPlayerManager.getInstance().getConsulatPlayer(CPlayerManager.getInstance().getPlayerUUID(args[1]));
                if(target == null){
                    sender.sendMessage(Text.PREFIX + "§cCe joueur n'est pas connecté.");
                    return;
                }
                if(sender.equals(target)){
                    sender.sendMessage(Text.PREFIX + "§cTu ne peux pas te kick toi-même.");
                    return;
                }
                Claim chunkTarget = target.getClaim();
                if(sender.hasPower(Rank.RESPONSABLE)){
                    if(chunkTarget == null){
                        sender.sendMessage(Text.PREFIX + "§cCe joueur n'est pas sur un claim.");
                        return;
                    }
                } else {
                    if(chunkTarget == null || !chunkTarget.isOwner(sender.getUUID())){
                        sender.sendMessage(Text.PREFIX + "§cCe joueur n'est pas sur l'un de tes claims.");
                        return;
                    }
                }
                if(target.hasPower(Rank.RESPONSABLE)){
                    sender.sendMessage(Text.PREFIX + "§cTu ne peux pas kick ce joueur.");
                    return;
                }
                target.getPlayer().teleport(ConsulatCore.getInstance().getSpawn());
                sender.sendMessage(Text.PREFIX + "§aTu as kick §e" + target.getName() + "§a de tes claims.");
                target.sendMessage(Text.PREFIX + "§e" + sender.getName() + "§c t'a kick du claim dans lequel tu te trouvais. Tu as été téléporté au spawn.");
                return;
            }
            case "desc":
            case "description":{
                Claim claim = player.getClaim();
                if(claim == null){
                    sender.sendMessage(Text.PREFIX + "§cCe chunk n'est pas claim.");
                    return;
                }
                if(!player.hasPower(Rank.RESPONSABLE) && !claim.isOwner(sender.getUUID())){
                    sender.sendMessage(Text.PREFIX + "§cCe claim ne t'appartient pas.");
                    return;
                }
                final String description;
                if(args.length == 1){
                    description = null;
                } else {
                    StringBuilder descriptionBuilder = new StringBuilder(args[1]);
                    for(int i = 2; i < args.length; ++i){
                        descriptionBuilder.append(' ').append(args[i]);
                    }
                    description = descriptionBuilder.toString();
                }
                Bukkit.getScheduler().runTaskAsynchronously(ConsulatCore.getInstance(), () -> {
                    try {
                        claim.setDescription(description);
                        if(description == null){
                            sender.sendMessage(Text.PREFIX + "§aTu as reset la description de ce claim.");
                        } else {
                            sender.sendMessage(Text.PREFIX + "§aTu as ajouté la description suivante à ce claim:");
                            sender.sendMessage(Text.PREFIX + "§7" + description);
                        }
                    } catch(SQLException e){
                        sender.sendMessage("§cUne erreur interne est survenue.");
                        e.printStackTrace();
                    }
                });
                return;
            }
            case "info":
                if(!player.hasPower(Rank.RESPONSABLE)){
                    break;
                }
                String target;
                if(args.length == 1){
                    Claim claim = player.getClaim();
                    if(claim == null){
                        sender.sendMessage(Text.PREFIX + "§cCette zone n'est pas claim.");
                        return;
                    }
                    target = claim.getOwnerName();
                } else {
                    target = args[1];
                }
                SPlayerManager.getInstance().fetchOffline(args[1], survivalOffline -> {
                    if(survivalOffline == null){
                        sender.sendMessage(Text.PREFIX + "§cLe joueur " + target + " ne s'est jamais connecté");
                        return;
                    }
                    sender.sendMessage(Text.PREFIX + "§7Informations sur §a" + survivalOffline.getName() + "§7: ");
                    sender.sendMessage(Text.PREFIX + "§eRang: §a" + survivalOffline.getRank().getRankName());
                    sender.sendMessage(Text.PREFIX + "§eArgent: §a" + survivalOffline.getMoney());
                    sender.sendMessage(Text.PREFIX + "§eA rejoint le: §a" + survivalOffline.getRegistered());
                    sender.sendMessage(Text.PREFIX + "§eDernière connexion: §a" + survivalOffline.getLastConnection());

                });
                return;
            case "list":
                Zone zone = player.getZone();
                Set<Claim> claims;
                if(zone == null || (claims = zone.getZoneClaims()).size() == 0){
                    sender.sendMessage("§eTu ne possèdes aucun claim.");
                    return;
                }
                int i = 0;
                for(Claim claim : claims){
                    sender.sendMessage("§6Claim §e" + (++i) + "§6: §eX:§c " + (claim.getX() * 16) + " §eY:§c " + (claim.getZ() * 16));
                }
                return;
            case "options":{
                Claim claim = player.getClaim();
                if(claim == null || !claim.isOwner(player.getUUID())){
                    player.sendMessage("§cTu dois être dans un de tes claims pour effectuer cette commande.");
                    return;
                }
                claim.openManageClaim(player);
            }
            break;
        }
        sender.sendMessage("§c" + getUsage());
    }
}
