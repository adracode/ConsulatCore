package fr.amisoz.consulatcore.commands.cities;

import com.mojang.brigadier.arguments.DoubleArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.builder.RequiredArgumentBuilder;
import fr.amisoz.consulatcore.Text;
import fr.amisoz.consulatcore.guis.city.CityGui;
import fr.amisoz.consulatcore.players.CityPermission;
import fr.amisoz.consulatcore.players.SPlayerManager;
import fr.amisoz.consulatcore.players.SurvivalPlayer;
import fr.amisoz.consulatcore.zones.ZoneManager;
import fr.amisoz.consulatcore.zones.cities.City;
import fr.amisoz.consulatcore.zones.claims.Claim;
import fr.amisoz.consulatcore.zones.claims.ClaimManager;
import fr.leconsulat.api.commands.Arguments;
import fr.leconsulat.api.commands.ConsulatCommand;
import fr.leconsulat.api.gui.GuiManager;
import fr.leconsulat.api.gui.gui.IGui;
import fr.leconsulat.api.player.CPlayerManager;
import fr.leconsulat.api.player.ConsulatPlayer;
import fr.leconsulat.api.ranks.Rank;
import org.bukkit.Bukkit;
import org.bukkit.Chunk;

import java.util.Collections;
import java.util.Set;
import java.util.UUID;

public class CityCommand extends ConsulatCommand {
    
    private String help =
            "/ville create <nom> - Créer sa ville\n" +
                    "/ville rename <nom> - Renommer sa ville\n" +
                    "/ville disband - Détruire sa ville\n" +
                    "/ville leave - Quitter la ville\n" +
                    "/ville kick <joueur> - Éjecter un membre\n" +
                    "/ville invite <joueur> - Inviter un joueur\n" +
                    "/ville accept <ville> - Rejoindre une ville (si invitation)\n" +
                    "/ville claim - Claim un chunk pour la ville\n" +
                    "/ville unclaim - Unclaim un chunk de la ville\n" +
                    "/ville sethome - Définir le point de spawn de la ville\n" +
                    "/ville [home|h] - Se téléporter au spawn de la ville\n" +
                    "/ville banque info - Voir le montant de la banque de ville\n" +
                    "/ville banque add - Ajouter de l'argent à la banque de ville\n" +
                    "/ville banque withdraw - Retirer de l'argent de la banque de ville\n" +
                    "/ville access [add|remove|list] <joueur> - Gérer les accès aux claims\n" +
                    "/ville info <joueur> - Voir la ville d'un joueur\n" +
                    "/ville [chat|c] <message> - Envoyer un message au chat de ville\n" +
                    "/ville options - Gérer sa ville\n" +
                    "/ville help - Affiche ce menu";
    
    public CityCommand(){
        super("ville", Collections.singletonList("city"), "/ville help", 1, Rank.JOUEUR);
        suggest(false,
                LiteralArgumentBuilder.literal("create")
                        .then(Arguments.word("nom")),
                LiteralArgumentBuilder.literal("rename")
                        .then(Arguments.word("nom")),
                LiteralArgumentBuilder.literal("disband"),
                LiteralArgumentBuilder.literal("leave"),
                LiteralArgumentBuilder.literal("kick")
                        .then(Arguments.playerList("joueur")),
                LiteralArgumentBuilder.literal("invite")
                        .then(Arguments.playerList("joueur")),
                LiteralArgumentBuilder.literal("accept")
                        .then(RequiredArgumentBuilder.argument("ville",
                                StringArgumentType.word()).suggests(((context, builder) -> {
                            ConsulatPlayer player = CPlayerManager.getInstance().getConsulatPlayerFromContextSource(context.getSource());
                            if(player == null){
                                return builder.buildFuture();
                            }
                            Set<City> invitations = ZoneManager.getInstance().getInvitations(player.getUUID());
                            if(invitations == null){
                                return builder.buildFuture();
                            }
                            Arguments.suggest(invitations,
                                    City::getName, (city -> true), builder);
                            return builder.buildFuture();
                        }))),
                LiteralArgumentBuilder.literal("claim"),
                LiteralArgumentBuilder.literal("unclaim"),
                LiteralArgumentBuilder.literal("sethome"),
                LiteralArgumentBuilder.literal("home"),
                LiteralArgumentBuilder.literal("h"),
                LiteralArgumentBuilder.literal("banque")
                        .then(LiteralArgumentBuilder.literal("info"))
                        .then(LiteralArgumentBuilder.literal("add")
                                .then(RequiredArgumentBuilder.argument("montant", DoubleArgumentType.doubleArg(0, 1_000_000))))
                        .then(LiteralArgumentBuilder.literal("withdraw")
                                .then(RequiredArgumentBuilder.argument("montant", DoubleArgumentType.doubleArg(0, 1_000_000)))),
                LiteralArgumentBuilder.literal("access")
                        .then(LiteralArgumentBuilder.literal("add")
                                .then(Arguments.playerList("joueur")))
                        .then(LiteralArgumentBuilder.literal("remove")
                                .then(Arguments.playerList("joueur")))
                        .then(LiteralArgumentBuilder.literal("list")),
                LiteralArgumentBuilder.literal("info")
                        .then(Arguments.playerList("joueur")),
                LiteralArgumentBuilder.literal("options"),
                LiteralArgumentBuilder.literal("chat")
                        .then(RequiredArgumentBuilder.argument("message", StringArgumentType.greedyString())),
                LiteralArgumentBuilder.literal("c")
                        .then(RequiredArgumentBuilder.argument("message", StringArgumentType.greedyString())),
                LiteralArgumentBuilder.literal("help")
        );
    }
    
    @Override
    public void onCommand(ConsulatPlayer sender, String[] args){
        SurvivalPlayer player = (SurvivalPlayer)sender;
        switch(args[0]){
            case "create":{
                if(player.belongsToCity()){
                    player.sendMessage("§cTu appartiens déjà à une ville, tu ne peux pas en créer une.");
                    return;
                }
                if(args.length < 2){
                    player.sendMessage("§cMerci de spécifier le nom de ta ville.");
                    return;
                }
                if(args[1].length() > City.MAX_LENGTH_NAME){
                    player.sendMessage("§cLe nom est trop long.");
                    return;
                }
                ZoneManager cityManager = ZoneManager.getInstance();
                if(cityManager.getCity(args[1]) != null){
                    player.sendMessage("§cIl existe déjà une ville portant ce nom.");
                    return;
                }
                City newCity = cityManager.createCity(args[1], player.getUUID());
                newCity.addPlayer(player.getUUID(), CityPermission.values());
                newCity.getCityPlayer(player.getUUID()).setRank(newCity.getRank(0));
                player.sendMessage("§7Tu viens de créer ta ville nommée §a" + newCity.getName() + "§7 !");
            }
            break;
            case "rename":{
                City city;
                String newName;
                if(player.hasPower(Rank.RESPONSABLE) && args.length == 3){
                    city = ZoneManager.getInstance().getCity(args[1]);
                    if(city == null){
                        player.sendMessage("§cCette ville n'existe pas");
                        return;
                    }
                    newName = args[2];
                } else {
                    if(!player.belongsToCity()){
                        player.sendMessage("§cTu n'es pas dans une ville.");
                        return;
                    }
                    city = player.getCity();
                    if(!city.canRename(player.getUUID())){
                        player.sendMessage("§cTu ne peux pas renommer cette ville.");
                        return;
                    }
                    if(args.length < 2){
                        player.sendMessage("§cMerci de spécifier le nouveau nom de ta ville.");
                        return;
                    }
                    newName = args[1];
                }
                if(newName.length() > City.MAX_LENGTH_NAME){
                    player.sendMessage("§cLe nouveau nom est trop long.");
                    return;
                }
                if(ZoneManager.getInstance().getCity(newName) != null){
                    player.sendMessage("§cIl existe déjà une ville portant ce nom.");
                    return;
                }
                player.sendMessage("§7Tu as renommé la ville §a" + newName + " §7! §8(§7Ancien nom: §e" + city.getName() + "§8)§7.");
                ZoneManager.getInstance().renameCity(city, newName);
            }
            break;
            case "disband":{
                if(!player.belongsToCity()){
                    player.sendMessage("§cTu n'es pas dans une ville.");
                    return;
                }
                City city = player.getCity();
                if(!city.canDisband(player.getUUID())){
                    player.sendMessage("§cTu ne peux pas détruire cette ville.");
                    return;
                }
                GuiManager.getInstance().getContainer("city-disband").getGui(city).open(player);
            }
            break;
            case "leave":{
                if(!player.belongsToCity()){
                    player.sendMessage("§cTu n'es pas dans une ville.");
                    return;
                }
                City city = player.getCity();
                if(city.isOwner(player.getUUID())){
                    player.sendMessage("§cTu ne peux pas quitter ta ville. Pour la quitter, tu dois la détruire.");
                    return;
                }
                city.removePlayer(player.getUUID());
                player.sendMessage("§7Tu as quitté la ville §a" + city.getName() + "§7.");
                city.sendMessage("§c" + player.getName() + " §7a quitté la ville.");
            }
            break;
            case "kick":{
                if(!player.belongsToCity()){
                    player.sendMessage("§cTu n'es pas dans une ville.");
                    return;
                }
                City city = player.getCity();
                if(!city.canKick(player.getUUID())){
                    player.sendMessage("§cTu ne peux pas kick un joueur.");
                    return;
                }
                if(args.length < 2){
                    player.sendMessage("§cMerci de spécifier le joueur à kick.");
                    return;
                }
                UUID targetUUID = CPlayerManager.getInstance().getPlayerUUID(args[1]);
                if(targetUUID == null){
                    player.sendMessage("§cCe joueur n'existe pas.");
                    return;
                }
                if(!city.removePlayer(targetUUID)){
                    player.sendMessage("§cCe joueur n'appartient pas à cette ville.");
                    return;
                }
                ConsulatPlayer target = CPlayerManager.getInstance().getConsulatPlayer(targetUUID);
                if(target != null){
                    target.sendMessage("§cTu as été kick de la ville §7" + city.getName() + "§c par §7" + player.getName() + "§c.");
                }
                player.sendMessage("§cTu as kick §7" + (target == null ? args[1] : target.getName()) + " §ade ta ville");
                city.sendMessage("§c" + player.getName() + " §7a kick §c" + (target == null ? args[1] : target.getName()) + "§7 la ville.");
            }
            break;
            case "invite":{
                if(!player.belongsToCity()){
                    player.sendMessage("§cTu n'es pas dans une ville.");
                    return;
                }
                City city = player.getCity();
                if(!city.canInvite(player.getUUID())){
                    player.sendMessage("§cTu ne peux pas inviter");
                    return;
                }
                if(args.length < 2){
                    player.sendMessage("§cMerci de spécifier le joueur à inviter.");
                    return;
                }
                UUID targetUUID = CPlayerManager.getInstance().getPlayerUUID(args[1]);
                if(targetUUID == null){
                    player.sendMessage("§cCe joueur n'existe pas.");
                    return;
                }
                SurvivalPlayer target = (SurvivalPlayer)CPlayerManager.getInstance().getConsulatPlayer(targetUUID);
                if(target == null){
                    player.sendMessage("§cCe joueur n'est pas connecté.");
                    return;
                }
                if(target.belongsToCity()){
                    player.sendMessage("§cCe joueur est déjà dans une ville.");
                    return;
                }
                if(!ZoneManager.getInstance().invitePlayer(city, targetUUID)){
                    player.sendMessage("§cCe joueur est déjà invité dans la ville.");
                    return;
                }
                player.sendMessage("§aTu as invité §7" + target.getName() + " §a à rejoindre la ville §7" + city.getName() + "§a.");
                target.sendMessage("§aTu as été invité à rejoindre la ville §7" + city.getName() + "§a par §7" + player.getName() + "§a.");
                city.sendMessage("§a" + player.getName() + "§7 a invité §a" + target.getName() + "§7.");
            }
            break;
            case "accept":{
                if(player.belongsToCity()){
                    player.sendMessage("§cTu es déjà dans une ville.");
                    return;
                }
                if(args.length < 2){
                    player.sendMessage("§cMerci de spécifier le nom de la ville.");
                    return;
                }
                ZoneManager manager = ZoneManager.getInstance();
                Set<City> cities = manager.getInvitations(player.getUUID());
                City city;
                if(cities == null || (city = manager.getCity(args[1])) == null || !cities.contains(city)){
                    player.sendMessage("§cTu n'a pas été invité dans cette ville.");
                    return;
                }
                manager.removeInvitation(player.getUUID());
                city.sendMessage("§a" + player.getName() + " §7a rejoint la ville !");
                city.addPlayer(player.getUUID());
                player.sendMessage("§aTu as rejoint §7" + city.getName() + "§a.");
            }
            break;
            case "claim":{
                if(!player.belongsToCity()){
                    player.sendMessage("§cTu n'es pas dans une ville.");
                    return;
                }
                City city = player.getCity();
                if(!city.canClaim(player.getUUID())){
                    player.sendMessage("§cTu ne peux pas claim pour ta ville.");
                    return;
                }
                if(sender.getPlayer().getLocation().getWorld() != Bukkit.getWorlds().get(0)){
                    sender.sendMessage(Text.PREFIX + "§cIl faut être dans le monde de base pour claim.");
                    return;
                }
                if(!city.hasMoney(Claim.BUY_CITY_CLAIM)){
                    player.sendMessage("§cTa ville n'a pas assez d'argent pour acheter ce claim.");
                    return;
                }
                Chunk chunk = player.getPlayer().getChunk();
                Claim claim = player.getClaim();
                if(claim != null){
                    if(claim.getOwner() instanceof City){
                        player.sendMessage("§cCe claim appartient déjà à une ville");
                        return;
                    }
                    if(!city.isMember(claim.getOwnerUUID())){
                        player.sendMessage("§cCe claim n'appartient pas à un membre de ta ville.");
                        return;
                    }
                }
                if(city.hasClaims()){
                    boolean canClaim = false;
                    for(Claim surroundingClaim : Claim.getSurroundingClaims(chunk.getX(), chunk.getZ())){
                        if(city.isClaim(surroundingClaim)){
                            canClaim = true;
                            break;
                        }
                    }
                    if(!canClaim){
                        player.sendMessage("§cTu ne peux pas claim un chunk isolé.");
                        return;
                    }
                }
                city.removeMoney(Claim.BUY_CITY_CLAIM);
                city.addClaim(ClaimManager.getInstance().cityClaim(chunk.getX(), chunk.getZ(), city));
                player.sendMessage("§aTu as claim ce chunk pour ta ville.");
            }
            break;
            case "unclaim":{
                if(!player.belongsToCity()){
                    player.sendMessage("§cTu n'es pas dans une ville.");
                    return;
                }
                City city = player.getCity();
                if(!city.canClaim(player.getUUID())){
                    player.sendMessage("§cTu ne peux pas unclaim pour ta ville.");
                    return;
                }
                Claim claim = player.getClaim();
                if(claim == null || !city.isClaim(claim)){
                    player.sendMessage("§cCe claim n'appartient pas à ta ville");
                    return;
                }
                if(!city.areClaimsConnected(claim)){
                    player.sendMessage("§cTu ne peux pas unclaim ce claim, les chunks de ta ville ne serait plus connectées.");
                    return;
                }
                if(city.isHomeIn(claim)){
                    player.sendMessage("§cCe claim contient le home de la ville.");
                    return;
                }
                ClaimManager.getInstance().unClaim(claim);
                player.sendMessage("§aTu as unclaim ce chunk de ta ville.");
            }
            break;
            case "sethome":{
                if(!player.belongsToCity()){
                    player.sendMessage("§cTu n'es pas dans une ville.");
                    return;
                }
                City city = player.getCity();
                if(!city.canSetSpawn(player.getUUID())){
                    player.sendMessage("§cTu ne peux pas définir le spawn de ta ville.");
                    return;
                }
                Claim claim = player.getClaim();
                if(claim == null || !city.isClaim(claim)){
                    player.sendMessage("§cCe claim n'appartient pas à ta ville");
                    return;
                }
                if(!city.hasHome()){
                    ZoneManager.getInstance().setHome(city, player.getPlayer().getLocation());
                    player.sendMessage("§aTu as déplacé le home de ta ville.");
                } else {
                    ((CityGui)(IGui)GuiManager.getInstance().getContainer("city").getGui(city)).confirmSethome(player);
                }
            }
            break;
            case "h":
            case "home":{
                if(!player.belongsToCity()){
                    player.sendMessage("§cTu n'es pas dans une ville.");
                    return;
                }
                City city = player.getCity();
                if(!city.hasHome()){
                    player.sendMessage("§cLe spawn de ta ville n'est pas définie");
                    return;
                }
                player.getPlayer().teleportAsync(city.getHome());
                player.sendMessage("§aTéléportation à ta ville en cours...");
            }
            break;
            case "banque":
            case "bank":{
                if(!player.belongsToCity()){
                    player.sendMessage("§cTu n'es pas dans une ville.");
                    return;
                }
                if(args.length < 2){
                    player.sendMessage("§c" + getUsage());
                    return;
                }
                switch(args[1]){
                    case "add":{
                        if(args.length < 3){
                            player.sendMessage("§c" + getUsage());
                            return;
                        }
                        double moneyToGive;
                        try {
                            moneyToGive = Double.parseDouble(args[2]);
                        } catch(NumberFormatException exception){
                            player.sendMessage("§cCe nombre n'est pas valide.");
                            return;
                        }
                        if(moneyToGive <= 0 || moneyToGive > 1_000_000){
                            sender.sendMessage(Text.PREFIX + "§cTu ne peux pas donner " + moneyToGive + " € à la banque de ta ville.");
                            return;
                        }
                        if(!player.hasMoney(moneyToGive)){
                            sender.sendMessage(Text.PREFIX + "§cTu n'as pas assez d'argent !");
                            return;
                        }
                        player.removeMoney(moneyToGive);
                        player.getCity().addMoney(moneyToGive);
                        player.sendMessage("§aTu as ajouté §7" + moneyToGive + " §aà ta ville");
                    }
                    break;
                    case "withdraw":{
                        City city = player.getCity();
                        if(!city.canWithdraw(player.getUUID())){
                            player.sendMessage("§cTu ne peux pas retirer de l'argent de la banque.");
                            return;
                        }
                        if(args.length < 3){
                            player.sendMessage("§c" + getUsage());
                            return;
                        }
                        double moneyToWithdraw;
                        try {
                            moneyToWithdraw = Double.parseDouble(args[2]);
                        } catch(NumberFormatException exception){
                            player.sendMessage("§cCe nombre n'est pas valide.");
                            return;
                        }
                        if(moneyToWithdraw <= 0 || moneyToWithdraw > 1_000_000){
                            sender.sendMessage(Text.PREFIX + "§cTu ne peux pas retirer " + moneyToWithdraw + " € de la banque de ta ville.");
                            return;
                        }
                        if(!city.hasMoney(moneyToWithdraw)){
                            moneyToWithdraw = city.getMoney();
                        }
                        city.removeMoney(moneyToWithdraw);
                        player.addMoney(moneyToWithdraw);
                        player.sendMessage("§aTu as retiré §7" + moneyToWithdraw + " §ade ta ville");
                    }
                    break;
                    case "info":{
                        player.sendMessage("§aTa ville a §e" + player.getCity().getMoney() + " €§a.");
                    }
                    break;
                    default:
                        player.sendMessage("§c" + getUsage());
                }
            }
            break;
            case "access":{
                if(!player.belongsToCity()){
                    player.sendMessage("§cTu n'es pas dans une ville.");
                    return;
                }
                City city = player.getCity();
                if(!city.canManageAccesses(player.getUUID())){
                    player.sendMessage("§cTu ne peux pas gérer les accès.");
                    return;
                }
                Claim claim = player.getClaim();
                if(claim == null || !city.isClaim(claim)){
                    player.sendMessage("§cCe claim n'appartient pas à ta ville.");
                    return;
                }
                if(args.length < 2){
                    player.sendMessage("§c" + getUsage());
                    return;
                }
                switch(args[1].toLowerCase()){
                    case "add":{
                        if(args.length < 3){
                            player.sendMessage("§c" + getUsage());
                            return;
                        }
                        UUID targetUUID = CPlayerManager.getInstance().getPlayerUUID(args[2]);
                        if(targetUUID == null){
                            player.sendMessage("§cCe joueur n'existe pas.");
                            return;
                        }
                        if(targetUUID.equals(player.getUUID()) || targetUUID.equals(city.getOwner())){
                            player.sendMessage("§cTu ne peux pas modifier l'accès de ce joueur.");
                            return;
                        }
                        if(!claim.addPlayer(targetUUID)){
                            player.sendMessage("§cCe joueur a déjà accès à ce claim.");
                            return;
                        }
                        player.sendMessage("§aTu as ajouté " + args[2] + " à ce claim");
                    }
                    break;
                    case "remove":{
                        if(args.length < 3){
                            player.sendMessage("§c" + getUsage());
                            return;
                        }
                        UUID targetUUID = CPlayerManager.getInstance().getPlayerUUID(args[2]);
                        if(targetUUID == null){
                            player.sendMessage("§cCe joueur n'existe pas.");
                            return;
                        }
                        if(targetUUID.equals(player.getUUID()) || targetUUID.equals(city.getOwner())){
                            player.sendMessage("§cTu ne peux pas modifier l'accès de ce joueur.");
                            return;
                        }
                        if(!claim.removePlayer(targetUUID)){
                            player.sendMessage("§cCe joueur n'a pas accès à ce claim.");
                            return;
                        }
                        player.sendMessage("§aTu as retiré " + args[2] + " de ce claim");
                    }
                    break;
                    case "list":{
                        StringBuilder builder = new StringBuilder();
                        for(UUID uuid : claim.getPlayers()){
                            builder.append(Bukkit.getOfflinePlayer(uuid).getName()).append(", ");
                        }
                        if(builder.length() == 0){
                            sender.sendMessage("§cAucun joueur n'a accès à ce claim.");
                            return;
                        }
                        sender.sendMessage("§6Liste des joueurs ayant accès à ce claim : §e" + builder.substring(0, builder.length() - 2));
                    }
                    break;
                    default:
                        player.sendMessage("§c" + getUsage());
                }
            }
            break;
            case "info":{
                if(args.length < 2){
                    player.sendMessage("§cMerci de spécifier le joueur.");
                    return;
                }
                SurvivalPlayer target = (SurvivalPlayer)CPlayerManager.getInstance().getConsulatPlayer(args[1]);
                if(target != null){
                    if(!target.belongsToCity()){
                        player.sendMessage("§cCe joueur n'appartient pas à une ville.");
                        return;
                    }
                    GuiManager.getInstance().getContainer("city-info").getGui(target.getCity()).open(player);
                } else {
                    SPlayerManager.getInstance().fetchOffline(args[1], offlineTarget -> {
                        if(offlineTarget == null){
                            player.sendMessage("§cCe joueur n'existe pas.");
                            return;
                        }
                        if(offlineTarget.getCity() == null){
                            player.sendMessage("§cCe joueur n'appartient pas à une ville.");
                            return;
                        }
                        GuiManager.getInstance().getContainer("city-info").getGui(offlineTarget.getCity()).open(player);
                    });
                }
            }
            break;
            case "chat":
            case "c":{
                if(!player.belongsToCity()){
                    player.sendMessage("§cTu n'appartiens pas à une ville");
                    return;
                }
                if(args.length < 2){
                    player.sendMessage("§cMerci de spécifier le message à envoyer.");
                    return;
                }
                StringBuilder builder = new StringBuilder("§8[§d").append(player.getCity().getName()).append("§8] ")
                        .append("§7(§d").append(player.getCity().getCityPlayer(player.getUUID()).getRank().getRankName()).append("§7) §a")
                        .append(player.getName()).append("§7 > §e").append(args[1]);
                for(int i = 2; i < args.length; i++){
                    builder.append(' ').append(args[i]);
                }
                player.getCity().sendMessage(builder.toString());
            }
            break;
            case "options":{
                if(!player.belongsToCity()){
                    player.sendMessage("§cTu n'as pas de ville.");
                    return;
                }
                City city = player.getCity();
                if(!city.isOwner(player.getUUID())){
                    player.sendMessage("§cTu n'est pas le propriétaire de la ville.");
                    return;
                }
                GuiManager.getInstance().getContainer("city").getGui(city).open(player);
            }
            break;
            case "help":{
                player.sendMessage(help);
            }
            break;
            default:
                player.getPlayer().performCommand("ville help");
        }
        
    }
}
