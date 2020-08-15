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
import fr.leconsulat.api.gui.GuiManager;
import fr.leconsulat.api.player.CPlayerManager;
import fr.leconsulat.api.player.ConsulatPlayer;
import fr.leconsulat.api.ranks.Rank;
import org.bukkit.Chunk;
import org.jetbrains.annotations.NotNull;

import java.util.Set;

public class ClaimCommand extends ConsulatCommand {
    
    public ClaimCommand(){
        super(ConsulatCore.getInstance(), "claim");
        setDescription("Gérer tes claims").
                setUsage("/claim - Claim un chunk (" + ConsulatCore.formatMoney(Claim.BUY_CLAIM) + ")\n" +
                        "/claim kick <joueur> - Renvoie au spawn un joueur se trouvant sur ta zone\n" +
                        "/claim desc - Reset la description du claim\n" +
                        "/claim desc <description> - Changer la description du claim\n" +
                        "/claim list - Voir tous tes claims").
                setRank(Rank.JOUEUR).
                suggest(LiteralArgumentBuilder.literal("kick").
                                then(Arguments.playerList("joueur")),
                        LiteralArgumentBuilder.literal("desc").
                                then(RequiredArgumentBuilder.argument("description", StringArgumentType.greedyString())),
                        LiteralArgumentBuilder.literal("list"),
                        LiteralArgumentBuilder.literal("options"),
                        LiteralArgumentBuilder.literal("info").
                                requires((t) -> {
                                    ConsulatPlayer player = getConsulatPlayer(t);
                                    return player != null && player.hasPower(Rank.RESPONSABLE);
                                }).
                                then(Arguments.playerList("joueur")));
    }
    
    @Override
    public void onCommand(@NotNull ConsulatPlayer sender, @NotNull String[] args){
        SurvivalPlayer player = (SurvivalPlayer)sender;
        if(args.length == 0){
            if(sender.getPlayer().getLocation().getWorld() != ConsulatCore.getInstance().getOverworld()){
                sender.sendMessage(Text.YOU_CANT_CLAIM_DIMENSION);
                return;
            }
            if(player.getClaim() != null){
                sender.sendMessage(Text.CHUNK_ALREADY_CLAIM);
                return;
            }
            if(!player.hasMoney(Claim.BUY_CLAIM)){
                sender.sendMessage(Text.NOT_ENOUGH_MONEY(Claim.BUY_CLAIM));
                return;
            }
            Chunk chunk = sender.getPlayer().getChunk();
            Zone zone = player.getZone();
            if(zone == null){
                zone = new Zone(sender.getUUID(), sender.getName(), sender.getUUID());
                ZoneManager.getInstance().addZone(zone);
            }
            ClaimManager.getInstance().playerClaim(chunk.getX(), chunk.getZ(), zone);
            player.removeMoney(Claim.BUY_CLAIM);
            sender.sendMessage(Text.YOU_CLAIMED_CHUNK(Claim.BUY_CLAIM));
            return;
        }
        switch(args[0].toLowerCase()){
            case "kick":{
                if(args.length == 1){
                    sender.sendMessage(Text.NO_PLAYER);
                    return;
                }
                SurvivalPlayer target = (SurvivalPlayer)CPlayerManager.getInstance().getConsulatPlayer(CPlayerManager.getInstance().getPlayerUUID(args[1]));
                if(target == null){
                    sender.sendMessage(Text.PLAYER_NOT_CONNECTED);
                    return;
                }
                if(sender.equals(target)){
                    sender.sendMessage(Text.CANT_KICK_YOURSELF);
                    return;
                }
                Claim chunkTarget = target.getClaim();
                if(sender.hasPower(Rank.RESPONSABLE)){
                    if(chunkTarget == null){
                        sender.sendMessage(Text.PLAYER_NOT_IN_CLAIM);
                        return;
                    }
                } else {
                    if(chunkTarget == null || !chunkTarget.isOwner(sender.getUUID())){
                        sender.sendMessage(Text.PLAYER_NOT_IN_YOUR_CLAIM);
                        return;
                    }
                }
                if(target.hasPower(Rank.RESPONSABLE) || target.isInModeration()){
                    sender.sendMessage(Text.CANT_KICK_PLAYER);
                    return;
                }
                target.getPlayer().teleportAsync(ConsulatCore.getInstance().getSpawn());
                sender.sendMessage(Text.YOU_KICKED_PLAYER_CLAIM(target.getName()));
                target.sendMessage(Text.YOU_BEEN_KICKED_BY(sender.getName()));
                return;
            }
            case "desc":
            case "description":{
                Claim claim = player.getClaim();
                if(claim == null || !player.hasPower(Rank.RESPONSABLE) && !claim.isOwner(sender.getUUID())){
                    sender.sendMessage(Text.NOT_IN_YOUR_CLAIM);
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
                if(description == null){
                    sender.sendMessage(Text.YOU_RESET_DESCRIPTION_CLAIM);
                } else {
                    sender.sendMessage(Text.YOU_CHANGE_DESCRIPTION_CLAIM(description));
                }
                claim.setDescription(description);
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
                        sender.sendMessage(Text.NOT_CLAIM);
                        return;
                    }
                    target = claim.getOwnerName();
                } else {
                    target = args[1];
                }
                SPlayerManager.getInstance().fetchOffline(target, survivalOffline -> {
                    if(survivalOffline == null){
                        sender.sendMessage(Text.PLAYER_DOESNT_EXISTS);
                        return;
                    }
                    sender.sendMessage(Text.INFO_CLAIM(survivalOffline));
                });
                return;
            case "list":
                Zone zone = player.getZone();
                Set<Claim> claims;
                if(zone == null || (claims = zone.getZoneClaims()).isEmpty()){
                    sender.sendMessage(Text.YOU_NO_CLAIM);
                    return;
                }
                sender.sendMessage(Text.LIST_CLAIM(claims));
                return;
            case "options":{
                Claim claim = player.getClaim();
                if(claim == null || !claim.isOwner(player.getUUID())){
                    player.sendMessage(Text.NOT_IN_YOUR_CLAIM);
                    return;
                }
                GuiManager.getInstance().getContainer("claim").getGui(claim).open(player);
            }
            break;
        }
        sender.sendMessage(Text.COMMAND_USAGE(this));
    }
}
