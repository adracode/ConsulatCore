package fr.amisoz.consulatcore.commands.cities;

import com.mojang.brigadier.arguments.DoubleArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.builder.RequiredArgumentBuilder;
import fr.amisoz.consulatcore.ConsulatCore;
import fr.amisoz.consulatcore.Text;
import fr.amisoz.consulatcore.guis.city.CityGui;
import fr.amisoz.consulatcore.players.CityPermission;
import fr.amisoz.consulatcore.players.SPlayerManager;
import fr.amisoz.consulatcore.players.SurvivalPlayer;
import fr.amisoz.consulatcore.zones.ZoneManager;
import fr.amisoz.consulatcore.zones.cities.City;
import fr.amisoz.consulatcore.zones.cities.CityPlayer;
import fr.amisoz.consulatcore.zones.claims.Claim;
import fr.amisoz.consulatcore.zones.claims.ClaimManager;
import fr.leconsulat.api.ConsulatAPI;
import fr.leconsulat.api.commands.Arguments;
import fr.leconsulat.api.commands.ConsulatCommand;
import fr.leconsulat.api.gui.GuiManager;
import fr.leconsulat.api.gui.gui.IGui;
import fr.leconsulat.api.player.CPlayerManager;
import fr.leconsulat.api.player.ConsulatPlayer;
import fr.leconsulat.api.ranks.Rank;
import fr.leconsulat.api.utils.StringUtils;
import org.bukkit.Bukkit;
import org.bukkit.Chunk;

import java.util.Set;
import java.util.UUID;
import java.util.logging.Level;

public class CityCommand extends ConsulatCommand {
    
    private String help =
            "§6/ville create <nom> §7- §eCréer une ville.\n" +
                    "§6/ville rename <nom> §7- §eChanger le nom de ta ville.\n" +
                    "§6/ville disband <nom> §7- §eSupprimer ta ville (C’est définitif fais attention).\n" +
                    "§6/ville invite <pseudo> §7- §eInviter une personne dans ta ville.\n" +
                    "§6/ville kick <pseudo> §7- §eExclure une personne de ta ville.\n" +
                    "§6/ville accept <nom de la ville> §7- §eAccepter la demande d’invitation d'une ville.\n" +
                    "§6/ville leave §7- §eQuitter la ville dans laquelle tu es.\n" +
                    "§6/ville claim §7- §eClaim un chunk pour " + ConsulatCore.formatMoney(180) + " au nom de ta ville (Assure toi d’avoir de l’argent dans la banque de ville). Tu peux claim le chunk d’un de tes membres.\n" +
                    "§6/ville unclaim §7- §eSupprimer un claim de ta ville.\n" +
                    "§6/ville sethome §7- §eCréer le point d’apparition de ta ville.\n" +
                    "§6/ville home §7- §eTe téléporter au point d’apparition de ta ville. Tu peux utiliser l’abréviation /ville h\n" +
                    "§6/ville banque add <montant> §7- §eDéposer de l’argent dans la banque de ville pour pouvoir claim des chunks.\n" +
                    "§6/ville banque info §7- §eMontre combien d’argent il reste dans la banque de ville.\n" +
                    "§6/ville access <joueur> §7- §eDonner l’accès d’un claim en particulier à un joueur.\n" +
                    "§6/ville access remove <joueur> §7- §eEnlever l’accès d’un joueur d'un claim.\n" +
                    "§6/ville access list §7- §eLister les accès du claim où tu es.\n" +
                    "§6/ville [options|menu|gui] §7- §eDonne des informations complètes sur ta ville et te permets aussi de la gérer (permissions, claim, grade).\n" +
                    "§6/ville chat <message> §7- §eTe permets de parler dans un chat accessibles seulement aux membres de ta ville. Tu peux utiliser l’abréviation /ville c. \n" +
                    "§6/ville chat §7- §eChange de chat pour parler directement dans le chat de ville\n" +
                    "§6/ville info <joueur> §7- §eTe donne les informations globales sur la ville d’un joueur.\n" +
                    "§6/ville desc <description> §7- §eChange la description de ville.\n" +
                    "§6/ville lead <joueur> §7- §eChange le propriétaire de la ville.\n" +
                    "§6/ville help §7- §eAffiche toutes les commandes utiles pour ta ville. C’est ce que tu lis ;).";
    
    public CityCommand(){
        super("consulat.core", "ville", "city", "/ville help", 1, Rank.JOUEUR);
        suggest(LiteralArgumentBuilder.literal("create")
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
                LiteralArgumentBuilder.literal("gui"),
                LiteralArgumentBuilder.literal("menu"),
                LiteralArgumentBuilder.literal("chat")
                        .then(RequiredArgumentBuilder.argument("message", StringArgumentType.greedyString())),
                LiteralArgumentBuilder.literal("c")
                        .then(RequiredArgumentBuilder.argument("message", StringArgumentType.greedyString())),
                LiteralArgumentBuilder.literal("desc")
                        .then(RequiredArgumentBuilder.argument("description", StringArgumentType.greedyString())),
                LiteralArgumentBuilder.literal("lead")
                        .then(Arguments.playerList("joueur")),
                LiteralArgumentBuilder.literal("help")
        );
    }
    
    @Override
    public void onCommand(ConsulatPlayer sender, String[] args){
        SurvivalPlayer player = (SurvivalPlayer)sender;
        switch(args[0]){
            case "create":{
                if(player.belongsToCity()){
                    player.sendMessage(Text.CANT_CREATE_CITY);
                    return;
                }
                if(args.length < 2){
                    player.sendMessage(Text.NO_CITY_NAME);
                    return;
                }
                if(!City.VALID_NAME.matcher(args[1]).matches()){
                    player.sendMessage(Text.INVALID_CITY_NAME);
                    return;
                }
                String name = StringUtils.capitalize(args[1]);
                ZoneManager cityManager = ZoneManager.getInstance();
                if(cityManager.getCity(name) != null){
                    player.sendMessage(Text.CITY_ALREADY_EXISTS);
                    return;
                }
                City newCity = cityManager.createCity(name, player.getUUID());
                newCity.addPlayer(player.getUUID(), CityPermission.values());
                newCity.setRank(player.getUUID(), newCity.getRank(0));
                player.sendMessage(Text.CITY_CREATED(newCity.getName()));
            }
            break;
            case "rename":{
                City city;
                String newName;
                if(player.hasPower(Rank.RESPONSABLE) && args.length == 3){
                    city = ZoneManager.getInstance().getCity(args[1]);
                    if(city == null){
                        player.sendMessage(Text.CITY_DOESNT_EXISTS);
                        return;
                    }
                    newName = args[2];
                } else {
                    if(!player.belongsToCity()){
                        player.sendMessage(Text.YOU_NOT_IN_CITY);
                        return;
                    }
                    city = player.getCity();
                    if(!city.canRename(player.getUUID())){
                        player.sendMessage(Text.CANT_RENAME_CITY);
                        return;
                    }
                    if(args.length < 2){
                        player.sendMessage(Text.NO_CITY_NAME);
                        return;
                    }
                    newName = args[1];
                }
                if(!city.hasMoney(City.RENAME_TAX)){
                    player.sendMessage(Text.NOT_ENOUGH_MONEY_CITY(City.RENAME_TAX));
                    return;
                }
                if(!City.VALID_NAME.matcher(newName).matches()){
                    player.sendMessage(Text.INVALID_CITY_NAME);
                    return;
                }
                newName = StringUtils.capitalize(newName);
                if(ZoneManager.getInstance().getCity(newName) != null){
                    player.sendMessage(Text.CITY_ALREADY_EXISTS);
                    return;
                }
                player.sendMessage(Text.CITY_RENAMED(city.getName(), newName));
                ZoneManager.getInstance().renameCity(city, newName);
            }
            break;
            case "disband":{
                if(!player.belongsToCity()){
                    player.sendMessage(Text.YOU_NOT_IN_CITY);
                    return;
                }
                City city = player.getCity();
                if(!city.canDisband(player.getUUID())){
                    player.sendMessage(Text.CANT_DISBAND_CITY);
                    return;
                }
                GuiManager.getInstance().getContainer("city-disband").getGui(city).open(player);
            }
            break;
            case "leave":{
                if(!player.belongsToCity()){
                    player.sendMessage(Text.YOU_NOT_IN_CITY);
                    return;
                }
                City city = player.getCity();
                UUID playerUUID = player.getUUID();
                if(city.isOwner(playerUUID)){
                    CityPlayer nextOwner = null;
                    for(CityPlayer cityPlayer : city.getMembers()){
                        if(cityPlayer.getUUID().equals(playerUUID)){
                            continue;
                        }
                        if(nextOwner == null || nextOwner.getRank().getId() > cityPlayer.getRank().getId()){
                            nextOwner = cityPlayer;
                        }
                    }
                    if(nextOwner == null){
                        GuiManager.getInstance().getContainer("city-disband").getGui(city).open(player);
                        return;
                    }
                    city.setOwner(nextOwner.getUUID());
                    return;
                }
                city.removePlayer(player.getUUID());
                player.sendMessage(Text.LEFT_CITY(city.getName()));
                city.sendMessage(Text.PLAYER_LEFT_CITY(player.getName()));
            }
            break;
            case "kick":{
                if(!player.belongsToCity()){
                    player.sendMessage(Text.YOU_NOT_IN_CITY);
                    return;
                }
                City city = player.getCity();
                if(!city.canKick(player.getUUID())){
                    player.sendMessage(Text.CANT_KICK_CITY);
                    return;
                }
                if(args.length < 2){
                    player.sendMessage(Text.NO_PLAYER);
                    return;
                }
                UUID targetUUID = CPlayerManager.getInstance().getPlayerUUID(args[1]);
                if(targetUUID == null){
                    player.sendMessage(Text.PLAYER_DOESNT_EXISTS);
                    return;
                }
                if(!city.removePlayer(targetUUID)){
                    player.sendMessage(Text.PLAYER_DOESNT_BELONGS_CITY);
                    return;
                }
                ConsulatPlayer target = CPlayerManager.getInstance().getConsulatPlayer(targetUUID);
                if(target != null){
                    target.sendMessage(Text.BEEN_KICKED_FROM_CITY_BY(city.getName(), player.getName()));
                }
                player.sendMessage(Text.KICK_PLAYER_FROM_CITY(target == null ? args[1] : target.getName()));
                city.sendMessage(Text.PLAYER_KICKED_FROM_CITY(player.getName(), (target == null ? args[1] : target.getName())));
            }
            break;
            case "invite":{
                if(!player.belongsToCity()){
                    player.sendMessage(Text.YOU_NOT_IN_CITY);
                    return;
                }
                City city = player.getCity();
                if(!city.canInvite(player.getUUID())){
                    player.sendMessage(Text.CANT_INVITE_CITY);
                    return;
                }
                if(args.length < 2){
                    player.sendMessage(Text.NO_PLAYER);
                    return;
                }
                UUID targetUUID = CPlayerManager.getInstance().getPlayerUUID(args[1]);
                if(targetUUID == null){
                    player.sendMessage(Text.PLAYER_DOESNT_EXISTS);
                    return;
                }
                SurvivalPlayer target = (SurvivalPlayer)CPlayerManager.getInstance().getConsulatPlayer(targetUUID);
                if(target == null){
                    player.sendMessage(Text.PLAYER_NOT_CONNECTED);
                    return;
                }
                if(target.belongsToCity()){
                    player.sendMessage(Text.PLAYER_ALREADY_BELONGS_CITY);
                    return;
                }
                if(!ZoneManager.getInstance().invitePlayer(city, targetUUID)){
                    player.sendMessage(Text.ALREADY_INVITED_CITY);
                    return;
                }
                player.sendMessage(Text.YOU_INVITED_PLAYER_TO_CITY(target.getName(), city.getName()));
                city.sendMessage(Text.HAS_INVITED_PLAYER_TO_CITY(player.getName(), target.getName()));
                target.sendMessage(Text.YOU_BEEN_INVITED_TO_CITY(city.getName(), player.getName()));
            }
            break;
            case "accept":{
                if(player.belongsToCity()){
                    player.sendMessage(Text.PLAYER_ALREADY_BELONGS_CITY);
                    return;
                }
                if(args.length < 2){
                    player.sendMessage(Text.NO_CITY_NAME);
                    return;
                }
                ZoneManager manager = ZoneManager.getInstance();
                Set<City> cities = manager.getInvitations(player.getUUID());
                City city;
                if(cities == null || (city = manager.getCity(args[1])) == null || !cities.contains(city)){
                    player.sendMessage(Text.YOU_NOT_BEEN_INVITED_TO_CITY);
                    return;
                }
                manager.removeInvitation(player.getUUID());
                city.sendMessage(Text.HAS_JOINED_CITY(player.getName()));
                if(!city.addPlayer(player.getUUID())){
                    player.sendMessage(Text.ERROR);
                    ConsulatAPI.getConsulatAPI().log(Level.WARNING, player + " " + city);
                    return;
                }
                player.sendMessage(Text.YOU_JOINED_CITY(city.getName()));
            }
            break;
            case "claim":{
                if(!player.belongsToCity()){
                    player.sendMessage(Text.YOU_NOT_IN_CITY);
                    return;
                }
                City city = player.getCity();
                if(!city.canClaim(player.getUUID())){
                    player.sendMessage(Text.YOU_CANT_CLAIM_CITY);
                    return;
                }
                if(sender.getPlayer().getLocation().getWorld() != ConsulatCore.getInstance().getOverworld()){
                    sender.sendMessage(Text.YOU_CANT_CLAIM_DIMENSION);
                    return;
                }
                if(!city.hasMoney(Claim.BUY_CITY_CLAIM)){
                    player.sendMessage(Text.NOT_ENOUGH_MONEY_CITY(Claim.BUY_CITY_CLAIM));
                    return;
                }
                Chunk chunk = player.getPlayer().getChunk();
                Claim claim = player.getClaim();
                if(claim != null){
                    if(claim.getOwner() instanceof City){
                        player.sendMessage(Text.CHUNK_ALREADY_CLAIM);
                        return;
                    }
                    if(!city.isMember(claim.getOwnerUUID())){
                        player.sendMessage(Text.CLAIM_NOT_BELONGS_TO_CITY_MEMBER);
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
                        player.sendMessage(Text.CANT_CLAIM_ALONE_CHUNK);
                        return;
                    }
                }
                city.removeMoney(Claim.BUY_CITY_CLAIM);
                ClaimManager.getInstance().cityClaim(chunk.getX(), chunk.getZ(), city);
                player.sendMessage(Text.YOU_CLAIMED_CHUNK_FOR_CITY(Claim.BUY_CITY_CLAIM));
            }
            break;
            case "unclaim":{
                if(!player.belongsToCity()){
                    player.sendMessage(Text.YOU_NOT_IN_CITY);
                    return;
                }
                City city = player.getCity();
                if(!city.canClaim(player.getUUID())){
                    player.sendMessage(Text.YOU_CANT_UNCLAIM_CITY);
                    return;
                }
                Claim claim = player.getClaim();
                if(claim == null || !city.isClaim(claim)){
                    player.sendMessage(Text.NOT_CLAIM_CITY);
                    return;
                }
                if(!city.areClaimsConnected(claim)){
                    player.sendMessage(Text.CANT_UNCLAIM_CITY_CHUNK_ALONE);
                    return;
                }
                if(city.isHomeIn(claim)){
                    player.sendMessage(Text.HOME_IN_CITY_CLAIM);
                    return;
                }
                ClaimManager.getInstance().unClaim(claim);
                player.sendMessage(Text.UNCLAIM_CITY);
            }
            break;
            case "sethome":{
                if(!player.belongsToCity()){
                    player.sendMessage(Text.YOU_NOT_IN_CITY);
                    return;
                }
                City city = player.getCity();
                if(!city.canSetSpawn(player.getUUID())){
                    player.sendMessage(Text.CANT_SET_HOME_CITY);
                    return;
                }
                Claim claim = player.getClaim();
                if(claim == null || !city.isClaim(claim)){
                    player.sendMessage(Text.NOT_CLAIM_CITY);
                    return;
                }
                if(!city.hasHome()){
                    ZoneManager.getInstance().setHome(city, player.getPlayer().getLocation());
                    player.sendMessage(Text.YOU_SET_HOME_CITY);
                } else {
                    ((CityGui)GuiManager.getInstance().getContainer("city").getGui(city)).confirmSethome(player);
                }
            }
            break;
            case "h":
            case "home":{
                if(!player.belongsToCity()){
                    player.sendMessage(Text.YOU_NOT_IN_CITY);
                    return;
                }
                City city = player.getCity();
                if(!city.hasHome()){
                    player.sendMessage(Text.HOME_CITY_NOT_SET);
                    return;
                }
                player.getPlayer().teleportAsync(city.getHome());
                player.sendMessage(Text.TELEPORTATION);
            }
            break;
            case "banque":
            case "bank":{
                if(!player.belongsToCity()){
                    player.sendMessage(Text.YOU_NOT_IN_CITY);
                    return;
                }
                if(args.length < 2){
                    player.sendMessage(Text.COMMAND_USAGE(this));
                    return;
                }
                switch(args[1]){
                    case "add":{
                        if(args.length < 3){
                            player.sendMessage(Text.COMMAND_USAGE(this));
                            return;
                        }
                        double moneyToGive;
                        try {
                            moneyToGive = Double.parseDouble(args[2]);
                        } catch(NumberFormatException exception){
                            player.sendMessage(Text.INVALID_NUMBER);
                            return;
                        }
                        if(moneyToGive <= 0 || moneyToGive >= 1_000_000){
                            sender.sendMessage(Text.INVALID_MONEY);
                            return;
                        }
                        if(!player.hasMoney(moneyToGive)){
                            sender.sendMessage(Text.NOT_ENOUGH_MONEY);
                            return;
                        }
                        player.removeMoney(moneyToGive);
                        player.getCity().addMoney(moneyToGive);
                        player.sendMessage(Text.ADD_MONEY_CITY(moneyToGive));
                    }
                    break;
                    case "withdraw":{
                        City city = player.getCity();
                        if(!city.canWithdraw(player.getUUID())){
                            player.sendMessage(Text.CANT_WITHDRAW_MONEY_CITY);
                            return;
                        }
                        if(args.length < 3){
                            player.sendMessage(Text.COMMAND_USAGE(this));
                            return;
                        }
                        double moneyToWithdraw;
                        try {
                            moneyToWithdraw = Double.parseDouble(args[2]);
                        } catch(NumberFormatException exception){
                            player.sendMessage(Text.INVALID_NUMBER);
                            return;
                        }
                        if(moneyToWithdraw <= 0 || moneyToWithdraw >= 1_000_000){
                            sender.sendMessage(Text.INVALID_MONEY);
                            return;
                        }
                        if(!city.hasMoney(moneyToWithdraw)){
                            player.sendMessage(Text.NOT_ENOUGH_MONEY_CITY);
                            return;
                        }
                        city.removeMoney(moneyToWithdraw);
                        player.addMoney(moneyToWithdraw);
                        player.sendMessage(Text.WITHDRAW_MONEY_CITY(moneyToWithdraw));
                    }
                    break;
                    case "info":{
                        player.sendMessage(Text.MONEY_CITY(player.getCity().getMoney()));
                    }
                    break;
                    default:
                        player.sendMessage(Text.COMMAND_USAGE(this));
                }
            }
            break;
            case "access":{
                if(!player.belongsToCity()){
                    player.sendMessage(Text.YOU_NOT_IN_CITY);
                    return;
                }
                City city = player.getCity();
                if(!city.canManageAccesses(player.getUUID())){
                    player.sendMessage(Text.CANT_MANAGE_ACCESS);
                    return;
                }
                Claim claim = player.getClaim();
                if(claim == null || !city.isClaim(claim)){
                    player.sendMessage(Text.NOT_CLAIM_CITY);
                    return;
                }
                if(args.length < 2){
                    player.sendMessage(Text.COMMAND_USAGE(this));
                    return;
                }
                switch(args[1].toLowerCase()){
                    case "add":{
                        if(args.length < 3){
                            player.sendMessage(Text.COMMAND_USAGE(this));
                            return;
                        }
                        UUID targetUUID = CPlayerManager.getInstance().getPlayerUUID(args[2]);
                        if(targetUUID == null){
                            player.sendMessage(Text.PLAYER_DOESNT_EXISTS);
                            return;
                        }
                        if(!city.isMember(targetUUID)){
                            player.sendMessage(Text.PLAYER_DOESNT_BELONGS_CITY);
                            return;
                        }
                        if((targetUUID.equals(player.getUUID()) || targetUUID.equals(city.getOwner())) && !ConsulatAPI.getConsulatAPI().isDebug()){
                            player.sendMessage(Text.CANT_MANAGE_ACCESS_PLAYER);
                            return;
                        }
                        if(!claim.addPlayer(targetUUID)){
                            player.sendMessage(Text.PLAYER_ALREADY_ACCESS_CLAIM);
                            return;
                        }
                        player.sendMessage(Text.ADD_PLAYER_CLAIM(args[2]));
                    }
                    break;
                    case "remove":{
                        if(args.length < 3){
                            player.sendMessage(Text.COMMAND_USAGE(this));
                            return;
                        }
                        UUID targetUUID = CPlayerManager.getInstance().getPlayerUUID(args[2]);
                        if(targetUUID == null){
                            player.sendMessage(Text.PLAYER_DOESNT_EXISTS);
                            return;
                        }
                        if((targetUUID.equals(player.getUUID()) || targetUUID.equals(city.getOwner())) && !ConsulatAPI.getConsulatAPI().isDebug()){
                            player.sendMessage(Text.CANT_MANAGE_ACCESS_PLAYER);
                            return;
                        }
                        if(!claim.removePlayer(targetUUID)){
                            player.sendMessage(Text.PLAYER_NOT_ACCESS_CLAIM);
                            return;
                        }
                        player.sendMessage(Text.REMOVE_PLAYER_CLAIM(args[2]));
                    }
                    break;
                    case "list":{
                        Set<UUID> accesses = claim.getPlayers();
                        if(accesses.isEmpty()){
                            sender.sendMessage(Text.NOBODY_ACCESS_CLAIM);
                            return;
                        }
                        sender.sendMessage(Text.LIST_ACCESS_CLAIM(accesses));
                    }
                    break;
                    default:
                        player.sendMessage(Text.COMMAND_USAGE(this));
                }
            }
            break;
            case "info":{
                if(args.length < 2){
                    player.sendMessage(Text.NO_PLAYER);
                    return;
                }
                SurvivalPlayer target = (SurvivalPlayer)CPlayerManager.getInstance().getConsulatPlayer(args[1]);
                if(target != null){
                    if(!target.belongsToCity()){
                        player.sendMessage(Text.PLAYER_DOESNT_BELONGS_A_CITY);
                        return;
                    }
                    GuiManager.getInstance().getContainer("city-info").getGui(target.getCity()).open(player);
                } else {
                    SPlayerManager.getInstance().fetchOffline(args[1], offlineTarget -> {
                        if(offlineTarget == null){
                            player.sendMessage(Text.PLAYER_DOESNT_EXISTS);
                            return;
                        }
                        if(offlineTarget.getCity() == null){
                            player.sendMessage(Text.PLAYER_DOESNT_BELONGS_A_CITY);
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
                    player.sendMessage(Text.YOU_NOT_IN_CITY);
                    return;
                }
                if(args.length < 2){
                    if(player.getCurrentChannel() == null){
                        player.setCurrentChannel(player.getCity().getChannel());
                        player.sendMessage(Text.NOW_SPEAK_IN_CITY_CHAT);
                    } else {
                        player.setCurrentChannel(null);
                        player.sendMessage(Text.NOW_SPEAK_IN_GLOBAL_CHAT);
                    }
                    return;
                }
                StringBuilder builder = new StringBuilder(args[1]);
                for(int i = 2; i < args.length; i++){
                    builder.append(' ').append(args[i]);
                }
                String chat = player.chat(builder.toString());
                if(chat == null){
                    return;
                }
                City city = player.getCity();
                city.sendMessage(city.getChannel().speak(player, chat));
            }
            break;
            case "gui":
            case "menu":
            case "options":{
                if(!player.belongsToCity()){
                    player.sendMessage(Text.YOU_NOT_IN_CITY);
                    return;
                }
                IGui cityGui = GuiManager.getInstance().getContainer("city").getGui(player.getCity());
                cityGui.open(player);
            }
            break;
            case "desc":{
                if(!player.belongsToCity()){
                    player.sendMessage(Text.YOU_NOT_IN_CITY);
                    return;
                }
                City city = player.getCity();
                if(!city.isOwner(player.getUUID())){
                    sender.sendMessage(Text.CANT_CHANGE_DESCRIPTION_CITY);
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
                city.setDescription(description);
                if(description == null){
                    sender.sendMessage(Text.YOU_RESET_DESCRIPTION_CITY);
                } else {
                    sender.sendMessage(Text.YOU_CHANGE_DESCRIPTION_CITY(description));
                }
            }
            break;
            case "lead":{
                if(!player.belongsToCity()){
                    player.sendMessage(Text.YOU_NOT_IN_CITY);
                    return;
                }
                City city = player.getCity();
                if(!city.isOwner(player.getUUID())){
                    player.sendMessage(Text.CANT_CHANGE_LEADER_CITY);
                    return;
                }
                if(args.length < 2){
                    player.sendMessage(Text.NO_PLAYER);
                    return;
                }
                UUID newOwner = CPlayerManager.getInstance().getPlayerUUID(args[1]);
                if(!city.isMember(newOwner)){
                    player.sendMessage(Text.PLAYER_DOESNT_BELONGS_CITY);
                    return;
                }
                if(city.isOwner(newOwner)){
                    player.sendMessage(Text.ALREADY_LEADER_CITY);
                    return;
                }
                city.setOwner(newOwner);
                city.sendMessage(Text.CHANGE_LEADER_CITY(Bukkit.getOfflinePlayer(newOwner).getName(), player.getName()));
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
