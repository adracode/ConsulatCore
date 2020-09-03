package fr.leconsulat.core.commands.cities;

import com.mojang.brigadier.arguments.DoubleArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.builder.RequiredArgumentBuilder;
import fr.leconsulat.api.ConsulatAPI;
import fr.leconsulat.api.commands.Arguments;
import fr.leconsulat.api.commands.ConsulatCommand;
import fr.leconsulat.api.gui.GuiManager;
import fr.leconsulat.api.gui.gui.IGui;
import fr.leconsulat.api.player.CPlayerManager;
import fr.leconsulat.api.player.ConsulatPlayer;
import fr.leconsulat.api.ranks.Rank;
import fr.leconsulat.api.utils.StringUtils;
import fr.leconsulat.core.ConsulatCore;
import fr.leconsulat.core.Text;
import fr.leconsulat.core.guis.city.CityGui;
import fr.leconsulat.core.players.CityPermission;
import fr.leconsulat.core.players.SPlayerManager;
import fr.leconsulat.core.players.SurvivalPlayer;
import fr.leconsulat.core.zones.ZoneManager;
import fr.leconsulat.core.zones.cities.City;
import fr.leconsulat.core.zones.cities.CityPlayer;
import fr.leconsulat.core.zones.claims.Claim;
import fr.leconsulat.core.zones.claims.ClaimManager;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.ComponentBuilder;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.Bukkit;
import org.bukkit.Chunk;
import org.jetbrains.annotations.NotNull;

import java.util.*;
import java.util.logging.Level;

public class CityCommand extends ConsulatCommand {
    
    private List<List<TextComponent>> titles = new ArrayList<>();
    private TextComponent close = new TextComponent("    §3§m-§e§m--§c§m---§e§m--§3§m-§a§m---------------§3§m-§e§m--§c§m---§e§m--§3§m-");
    private List<TextComponent> previous = new ArrayList<>();
    private List<TextComponent> next = new ArrayList<>();
    private SubCommand[] subCommand;
    
    public CityCommand(){
        super(ConsulatCore.getInstance(), "ville");
        setDescription("Gérer la ville").
                setUsage("/ville help - Affiche toutes les commandes de ville").
                setAliases("city").
                setArgsMin(1).
                setRank(Rank.JOUEUR).
                suggest(LiteralArgumentBuilder.literal("create").
                                then(Arguments.word("nom")),
                        LiteralArgumentBuilder.literal("rename").
                                then(Arguments.word("nom")),
                        LiteralArgumentBuilder.literal("disband"),
                        LiteralArgumentBuilder.literal("leave"),
                        LiteralArgumentBuilder.literal("top"),
                        LiteralArgumentBuilder.literal("kick").
                                then(Arguments.playerList("joueur")),
                        LiteralArgumentBuilder.literal("invite").
                                then(Arguments.playerList("joueur")),
                        LiteralArgumentBuilder.literal("accept").
                                then(RequiredArgumentBuilder.argument("ville",
                                        StringArgumentType.word()).suggests(((context, builder) -> {
                                    ConsulatPlayer player = getConsulatPlayerFromContext(context.getSource());
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
                        LiteralArgumentBuilder.literal("money"),
                        LiteralArgumentBuilder.literal("h"),
                        LiteralArgumentBuilder.literal("bank").
                                then(LiteralArgumentBuilder.literal("info")).
                                then(LiteralArgumentBuilder.literal("add").
                                        then(RequiredArgumentBuilder.argument("montant", DoubleArgumentType.doubleArg(0, 1_000_000)))).
                                then(LiteralArgumentBuilder.literal("withdraw").
                                        then(RequiredArgumentBuilder.argument("montant", DoubleArgumentType.doubleArg(0, 1_000_000)))),
                        LiteralArgumentBuilder.literal("access").
                                then(LiteralArgumentBuilder.literal("add").
                                        then(Arguments.playerList("joueur"))).
                                then(LiteralArgumentBuilder.literal("addall").
                                        then(Arguments.playerList("joueur"))).
                                then(LiteralArgumentBuilder.literal("remove").
                                        then(Arguments.playerList("joueur"))).
                                then(LiteralArgumentBuilder.literal("removeall").
                                        then(Arguments.playerList("joueur"))).
                                then(LiteralArgumentBuilder.literal("list")),
                        LiteralArgumentBuilder.literal("info").
                                then(Arguments.playerList("joueur")).
                                then(Arguments.word("ville")),
                        LiteralArgumentBuilder.literal("options"),
                        LiteralArgumentBuilder.literal("gui"),
                        LiteralArgumentBuilder.literal("menu"),
                        LiteralArgumentBuilder.literal("chat").
                                then(RequiredArgumentBuilder.argument("message", StringArgumentType.greedyString())),
                        LiteralArgumentBuilder.literal("c").
                                then(RequiredArgumentBuilder.argument("message", StringArgumentType.greedyString())),
                        LiteralArgumentBuilder.literal("desc").
                                then(RequiredArgumentBuilder.argument("description", StringArgumentType.greedyString())),
                        LiteralArgumentBuilder.literal("lead").
                                then(Arguments.playerList("joueur")),
                        LiteralArgumentBuilder.literal("help"));
        this.subCommand = new TreeSet<>(Arrays.asList(
                new SubCommand("create <nom>", "Créer une nouvelle ville"),
                new SubCommand("rename <nom>", "Changer le nom de ville (" + ConsulatCore.formatMoney(City.RENAME_TAX) + ")"),
                new SubCommand("disband", "Détruire la ville (fenêtre de confirmation)"),
                new SubCommand("top", "Voir les villes les plus riches"),
                new SubCommand("leave", "Quitter une ville"),
                new SubCommand("kick <joueur>", "Expulser un membre"),
                new SubCommand("invite <joueur>", "Inviter un joueur"),
                new SubCommand("accept <ville>", "Rejoindre une ville"),
                new SubCommand("claim", "Claim un chunk pour la ville"),
                new SubCommand("unclaim", "Unclaim un chunk de ville"),
                new SubCommand("sethome", "Définir un home de ville"),
                new SubCommand("home", "Se TP au home de ville"),
                new SubCommand("money", "Affiche l'argent de la banque"),
                new SubCommand("bank add <montant>", "Ajouter de l'argent"),
                new SubCommand("bank withdraw <montant>", "Retirer de l'argent"),
                new SubCommand("bank info", "Afficher l'argent de la banque"),
                new SubCommand("access add <joueur>", "Ajouter un joueur à un claim"),
                new SubCommand("access addall <joueur>", "Ajouter un joueur à tous les claims"),
                new SubCommand("access remove <joueur>", "Retirer un joueur d'un claim"),
                new SubCommand("access removeall <joueur>", "Retirer un joueur de tous les claims"),
                new SubCommand("access list", "Afficher les membres d'un claim"),
                new SubCommand("info <joueur|ville>", "Afficher les information de ville"),
                new SubCommand("chat", "Switcher de chat (global ↔ ville)"),
                new SubCommand("chat <message>", "Parler dans le chat de ville"),
                new SubCommand("options", "Ouvrir le menu de ville"),
                new SubCommand("desc <description>", "Définir une description de ville"),
                new SubCommand("desc", "Supprimer la description de ville"),
                new SubCommand("lead <joueur>", "Changer le propriétaire de ville"))).toArray(new SubCommand[0]);
    }
    
    @Override
    public void onCommand(@NotNull ConsulatPlayer sender, @NotNull String[] args){
        SurvivalPlayer player = (SurvivalPlayer)sender;
        switch(args[0]){
            case "top":
                player.sendMessage(Text.BALTOP(ConsulatCore.getInstance().getCityBaltop().getBaltop(),
                        City::getName, City::getMoney));
                break;
            case "create":{
                if(player.belongsToCity()){
                    player.sendMessage(Text.CANT_CREATE_CITY);
                    return;
                }
                if(!player.hasMoney(City.CREATE_TAX)){
                    player.sendMessage(Text.NOT_ENOUGH_MONEY(City.CREATE_TAX));
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
                player.removeMoney(City.CREATE_TAX);
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
                }
                city.removePlayer(player.getUUID());
                player.sendMessage(Text.LEFT_CITY(city.getName()));
                city.sendMessage(Text.PLAYER_LEFT_CITY(city, player.getName()));
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
                if(targetUUID.equals(player.getUUID())){
                    player.sendMessage(Text.CANT_KICK_YOURSELF);
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
                city.sendMessage(Text.PLAYER_KICKED_FROM_CITY(city, player.getName(), (target == null ? args[1] : target.getName())));
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
                city.sendMessage(Text.HAS_INVITED_PLAYER_TO_CITY(city, player.getName(), target.getName()));
                target.sendMessage(Text.YOU_BEEN_INVITED_TO_CITY(city.getName(), player.getName()));
            }
            break;
            case "accept":{
                if(player.belongsToCity()){
                    player.sendMessage(Text.YOU_ALREADY_BELONGS_CITY);
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
                if(!city.addPlayer(player.getUUID())){
                    player.sendMessage(Text.ERROR);
                    ConsulatAPI.getConsulatAPI().log(Level.WARNING, player + " " + city);
                    return;
                }
                city.sendMessage(Text.HAS_JOINED_CITY(city, player.getName()));
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
                double price = claim == null ? Claim.BUY_CITY_CLAIM : Claim.SURCLAIM;
                city.removeMoney(price);
                ClaimManager.getInstance().cityClaim(chunk.getX(), chunk.getZ(), city);
                player.sendMessage(Text.YOU_CLAIMED_CHUNK_FOR_CITY(price));
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
            case "money":{
                if(!player.belongsToCity()){
                    player.sendMessage(Text.YOU_NOT_IN_CITY);
                    return;
                }
                player.sendMessage(Text.MONEY_CITY(player.getCity().getMoney()));
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
                        Claim claim = player.getClaim();
                        if(claim == null || !city.isClaim(claim)){
                            player.sendMessage(Text.NOT_CLAIM_CITY);
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
                        if((targetUUID.equals(player.getUUID()) || targetUUID.equals(city.getOwner()))){
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
                    case "addall":{
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
                        if((targetUUID.equals(player.getUUID()) || targetUUID.equals(city.getOwner()))){
                            player.sendMessage(Text.CANT_MANAGE_ACCESS_PLAYER);
                            return;
                        }
                        if(!city.addAccess(targetUUID)){
                            player.sendMessage(Text.PLAYER_ALREADY_ACCESS_CLAIMS);
                            return;
                        }
                        sender.sendMessage(Text.ADD_PLAYER_CLAIMS(args[2]));
                    }
                    break;
                    case "remove":{
                        if(args.length < 3){
                            player.sendMessage(Text.COMMAND_USAGE(this));
                            return;
                        }
                        Claim claim = player.getClaim();
                        if(claim == null || !city.isClaim(claim)){
                            player.sendMessage(Text.NOT_CLAIM_CITY);
                            return;
                        }
                        UUID targetUUID = CPlayerManager.getInstance().getPlayerUUID(args[2]);
                        if(targetUUID == null){
                            player.sendMessage(Text.PLAYER_DOESNT_EXISTS);
                            return;
                        }
                        if((targetUUID.equals(player.getUUID()) || targetUUID.equals(city.getOwner()))){
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
                    case "removeall":{
                        if(args.length < 3){
                            player.sendMessage(Text.COMMAND_USAGE(this));
                            return;
                        }
                        UUID targetUUID = CPlayerManager.getInstance().getPlayerUUID(args[2]);
                        if(targetUUID == null){
                            player.sendMessage(Text.PLAYER_DOESNT_EXISTS);
                            return;
                        }
                        if((targetUUID.equals(player.getUUID()) || targetUUID.equals(city.getOwner()))){
                            player.sendMessage(Text.CANT_MANAGE_ACCESS_PLAYER);
                            return;
                        }
                        if(!city.removeAccess(targetUUID)){
                            player.sendMessage(Text.PLAYER_NOT_ACCESS_CLAIMS);
                            return;
                        }
                        player.sendMessage(Text.REMOVE_PLAYER_CLAIMS(args[2]));
                    }
                    break;
                    case "list":{
                        Claim claim = player.getClaim();
                        if(claim == null || !city.isClaim(claim)){
                            player.sendMessage(Text.NOT_CLAIM_CITY);
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
                        player.sendMessage(Text.COMMAND_USAGE(this));
                }
            }
            break;
            case "info":{
                if(args.length < 2){
                    if(!player.belongsToCity()){
                        player.sendMessage(Text.YOU_NOT_IN_CITY);
                        return;
                    }
                    GuiManager.getInstance().getContainer("city-info").getGui(player.getCity()).open(player);
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
                            City city = ZoneManager.getInstance().getCity(args[1]);
                            if(city == null){
                                player.sendMessage(Text.CITY_DOESNT_EXISTS);
                                return;
                            }
                            GuiManager.getInstance().getContainer("city-info").getGui(city).open(player);
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
                city.sendMessage(Text.CHANGE_LEADER_CITY(city, Bukkit.getOfflinePlayer(newOwner).getName(), player.getName()));
            }
            break;
            case "help":{
                int page;
                if(args.length == 1){
                    page = 1;
                } else {
                    try {
                        page = Integer.parseInt(args[1]);
                    } catch(NumberFormatException e){
                        sender.sendMessage(Text.INVALID_NUMBER);
                        return;
                    }
                }
                int pages;
                float nbCmd = subCommand.length;
                if((nbCmd / 5) > (int)(nbCmd / 5)){
                    pages = (int)(nbCmd / 5) + 1;
                } else {
                    pages = (int)(nbCmd / 5);
                }
                if(page > pages){
                    page = pages;
                }
                sender.sendMessage("");
                sender.sendMessage(getTitle(page, pages));
                if(page != 1){
                    sender.sendMessage(getPrevious(page - 1));
                } else {
                    sender.sendMessage("");
                }
                for(int i = (page - 1) * 5; i < page * 5; i++){
                    if(i < subCommand.length){
                        SubCommand command = subCommand[i];
                        sender.sendMessage(new ComponentBuilder(
                                " §e/" + getName() + " " + command.getArgument() + " - §7" + command.getDescription())
                                .event(new ClickEvent(ClickEvent.Action.SUGGEST_COMMAND, "/" + command.getSuggestion()))
                                .create());
                    }
                }
                if(page != pages){
                    sender.sendMessage(getNext(page + 1));
                } else {
                    sender.sendMessage("");
                }
                sender.sendMessage(close);
            }
            break;
            default:
                player.getPlayer().performCommand("ville help");
        }
        
    }
    
    private TextComponent getTitle(int page, int size){
        while(size >= titles.size()){
            titles.add(new ArrayList<>());
        }
        List<TextComponent> pageTitles = titles.get(size);
        for(int i = pageTitles.size(); i <= page; ++i){
            pageTitles.add(new TextComponent("    §3§m-§e§m--§c§m---§e§m--§3§m-§a Commandes [" + i + "/" + size + "] §3§m-§e§m--§c§m---§e§m--§3§m-"));
        }
        return pageTitles.get(page);
    }
    
    private TextComponent getPrevious(int page){
        if(page < 0){
            return null;
        }
        for(int i = previous.size(); i <= page; ++i){
            TextComponent current = new TextComponent("                          §b« Précédent");
            current.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT,
                    new ComponentBuilder("Page précédente").color(ChatColor.GRAY).create()));
            current.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/city help " + i));
            previous.add(current);
        }
        return previous.get(page);
    }
    
    private TextComponent getNext(int page){
        for(int i = next.size(); i <= page; ++i){
            TextComponent current = new TextComponent("                          §bSuivant »");
            current.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT,
                    new ComponentBuilder("Page suivante").color(ChatColor.GRAY).create()));
            current.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/city help " + i));
            next.add(current);
        }
        return next.get(page);
    }
    
    public class SubCommand implements Comparable<SubCommand> {
        
        private final String argument;
        private final String description;
        private final String suggestion;
        
        public SubCommand(String argument, String description){
            this.argument = argument;
            this.description = description;
            StringBuilder builder = new StringBuilder(CityCommand.this.getName() + ' ');
            for(String sub : argument.split(" ")){
                if(sub.indexOf('<') != -1 && sub.indexOf('>') != -1){
                    break;
                }
                builder.append(sub).append(' ');
            }
            this.suggestion = builder.toString();
        }
        
        public String getArgument(){
            return argument;
        }
        
        public String getDescription(){
            return description;
        }
        
        public String getSuggestion(){
            return suggestion;
        }
        
        @Override
        public int compareTo(@NotNull CityCommand.SubCommand o){
            return argument.compareTo(o.argument);
        }
    }
    
}
