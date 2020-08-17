package fr.amisoz.consulatcore;

import fr.amisoz.consulatcore.economy.BaltopManager;
import fr.amisoz.consulatcore.moderation.MutedPlayer;
import fr.amisoz.consulatcore.players.SurvivalOffline;
import fr.amisoz.consulatcore.zones.cities.City;
import fr.amisoz.consulatcore.zones.claims.Claim;
import fr.leconsulat.api.commands.ConsulatCommand;
import fr.leconsulat.api.player.ConsulatPlayer;
import fr.leconsulat.api.ranks.Rank;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.ComponentBuilder;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;

import java.util.Collection;
import java.util.Map;
import java.util.SortedSet;
import java.util.UUID;
import java.util.function.Function;

final public class Text {
    
    public static final String PREFIX = "§7[§6Consulat§7]§6 ";
    public static String PREFIX_CITY(City city){ return "§8[§d" + city.getName() + "§8] ";}
    public static final String MODERATION_PREFIX = ChatColor.DARK_GREEN + "(Staff)" + ChatColor.GRAY + "[" + ChatColor.GOLD + "Modération" + ChatColor.GRAY + "] ";
    public static final String ANNOUNCE_PREFIX = ChatColor.GRAY + "§l[" + ChatColor.GOLD + "Modération" + ChatColor.GRAY + "§l]§r ";
    public static final String BROADCAST_PREFIX = ChatColor.RED + "§l[ANNONCE] ";
    public static final String FLY = "§7[§6Fly§7] §e";
    //@formatter:off
    public static final String CANT_CREATE_CITY = PREFIX + "§cTu appartiens déjà à une ville, tu ne peux pas en créer une.";
    public static final String NO_CITY_NAME = PREFIX + "§cMerci de spécifier le nom de La ville.";
    public static final String INVALID_CITY_NAME = PREFIX + "§cLe nom de ville n'est pas valide.";
    public static final String CITY_ALREADY_EXISTS = PREFIX + "§cIl existe déjà une ville portant ce nom.";
    public static String CITY_CREATED(String cityName){return PREFIX + "§7Tu viens de créer la ville nommée §a" + cityName + "§7 !";}
    public static final String CITY_DOESNT_EXISTS = PREFIX + "§cCette ville n'existe pas";
    public static final String YOU_NOT_IN_CITY = PREFIX + "§cTu n'es pas dans une ville.";
    public static final String CANT_RENAME_CITY = PREFIX + "§cTu ne peux pas renommer cette ville.";
    public static String NOT_ENOUGH_MONEY_CITY(double money){return PREFIX + "§cLa banque de ville n'a pas assez d'argent (argent requis: " + ConsulatCore.formatMoney(money) + ").";}
    public static String CITY_RENAMED(String old, String name){return PREFIX + "§7Tu as renommé la ville §a" + name + " §7! §8(§7Ancien nom: §e" + old + "§8)§7.";}
    public static final String CANT_DISBAND_CITY = PREFIX + "§cTu ne peux pas détruire cette ville.";
    public static String LEFT_CITY(String city){return PREFIX + "§7Tu as quitté la ville §a" + city + "§7.";}
    public static String PLAYER_LEFT_CITY(City city, String player){return PREFIX_CITY(city) + "§c" + player + " §7a quitté la ville.";}
    public static final String CANT_KICK_CITY = PREFIX + "§cTu ne peux pas kick un joueur de la ville.";
    public static final String NO_PLAYER = PREFIX + "§cMerci de spécifier le joueur.";
    public static final String PLAYER_DOESNT_EXISTS = PREFIX + "§cCe joueur n'existe pas.";
    public static final String PLAYER_NOT_CONNECTED = PREFIX + "§cCe joueur n'est pas connecté.";
    public static final String PLAYER_DOESNT_BELONGS_CITY = PREFIX + "§cCe joueur n'appartient pas à la ville.";
    public static final String PLAYER_DOESNT_BELONGS_A_CITY = PREFIX + "§cCe joueur n'appartient pas à une ville.";
    public static final String PLAYER_ALREADY_BELONGS_CITY = PREFIX + "§cCe joueur est déjà dans une ville.";
    public static final String YOU_ALREADY_BELONGS_CITY = PREFIX + "§cTu es déjà dans une ville.";
    public static TextComponent COMMAND_USAGE(ConsulatCommand command){
        TextComponent usage = new TextComponent("§c§lErreur: §7Mauvaise syntaxe §o(survolez pour voir)§7.");
        usage.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new ComponentBuilder("§cUtilisation:\n§7" + command.getUsage()).create()));
        return usage;
    }
    public static String BEEN_KICKED_FROM_CITY_BY(String city, String player){return PREFIX + "§cTu as été kick de la ville §7" + city + "§c par §7" + player + "§c.";}
    public static String KICK_PLAYER_FROM_CITY(String player){return PREFIX + "§cTu as kick §7" + player + " §ade la ville";}
    public static String PLAYER_KICKED_FROM_CITY(City city, String kicker, String kicked){return PREFIX_CITY(city) + "§c" + kicker + " §7a kick §c" + kicked + "§7 la ville.";}
    public static final String CANT_INVITE_CITY = PREFIX + "§cTu ne peux pas inviter des joueurs dans la ville.";
    public static final String ALREADY_INVITED_CITY = PREFIX + "§cCe joueur est déjà invité dans la ville.";
    public static String YOU_INVITED_PLAYER_TO_CITY(String invited, String city){return PREFIX + "§aTu as invité §7" + invited + " §a à rejoindre la ville §7" + city + "§a.";}
    public static String HAS_INVITED_PLAYER_TO_CITY(City city, String inviter, String invited){return PREFIX_CITY(city) + "§a" + inviter + "§7 a invité §a" + invited + "§7.";}
    public static TextComponent YOU_BEEN_INVITED_TO_CITY(String city, String inviter){
        TextComponent message = new TextComponent("§aTu as été invité à rejoindre la ville §7" + city + "§a par §7" + inviter + "§a.");
                message.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new ComponentBuilder("§aClique pour rejoindre " + city).create()));
                message.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/city accept " + city));
                return message;}
    public static final String YOU_NOT_BEEN_INVITED_TO_CITY = PREFIX + "§cTu n'as pas été invité dans cette ville.";
    public static String HAS_JOINED_CITY(City city, String player){return PREFIX_CITY(city) + "§a" + player + " §7a rejoint la ville !";}
    public static final String ERROR = PREFIX + "§cUne erreur est survenue";
    public static String YOU_JOINED_CITY(String city){return PREFIX + "§aTu as rejoint §7" + city + "§a.";}
    public static final String YOU_CANT_CLAIM_CITY = PREFIX + "§cTu ne peux pas claim pour la ville.";
    public static final String YOU_CANT_UNCLAIM_CITY = PREFIX + "§cTu ne peux pas unclaim pour la ville.";
    public static final String YOU_CANT_CLAIM_DIMENSION = PREFIX + "§cTu ne peux pas claim dans cette dimension.";
    public static final String CHUNK_ALREADY_CLAIM = PREFIX + "§cCe chunk est déjà claim";
    public static final String CLAIM_NOT_BELONGS_TO_CITY_MEMBER = PREFIX + "§cCe claim n'appartient pas à un membre de la ville.";
    public static final String CANT_CLAIM_ALONE_CHUNK = PREFIX + "§cTu ne peux pas claim un chunk isolé.";
    public static String YOU_CLAIMED_CHUNK_FOR_CITY(double money){return PREFIX + "§aTu as claim ce chunk pour la ville pour " + ConsulatCore.formatMoney(money) + ".";}
    public static String YOU_CLAIMED_CHUNK(double money){return PREFIX + "§aTu as claim ce chunk pour " + ConsulatCore.formatMoney(money) + ".";}
    public static final String NOT_CLAIM_CITY = PREFIX + "§cCe claim n'appartient pas à la ville.";
    public static final String NOT_CLAIM = PREFIX + "§cCe chunk n'est pas claim.";
    public static final String CANT_UNCLAIM_CITY_CHUNK_ALONE = PREFIX + "§cTu ne peux pas unclaim ce claim, les chunks de la ville ne serait plus connectées.";
    public static final String HOME_IN_CITY_CLAIM = PREFIX + "§cCe claim contient le home de la ville.";
    public static final String UNCLAIM_CITY = PREFIX + "§aTu as unclaim ce chunk de la ville.";
    public static final String CANT_SET_HOME_CITY = PREFIX + "§cTu ne peux pas définir le spawn de la ville.";
    public static final String YOU_SET_HOME_CITY = PREFIX + "§aTu as défini le home de la ville.";
    public static final String HOME_CITY_NOT_SET = PREFIX + "§cLe home de la ville n'est pas définie";
    public static final String TELEPORTATION = PREFIX + "§aTéléportation en cours...";
    public static final String INVALID_NUMBER = PREFIX + "§cCe nombre n'est pas valide.";
    public static final String INVALID_MONEY = PREFIX +  "§cCe montant n'est pas valide.";
    public static final String NOT_ENOUGH_MONEY = PREFIX + "§cTu n'as pas assez d'argent !";
    public static String NOT_ENOUGH_MONEY(double money){return PREFIX + "§cTu n'as pas assez d'argent (argent requis: " + ConsulatCore.formatMoney(money) + ").";}
    public static String ADD_MONEY_CITY(double money){return PREFIX + "§aTu as donné §7" + ConsulatCore.formatMoney(money) + " §aà la ville";}
    public static final String CANT_WITHDRAW_MONEY_CITY = PREFIX + "§cTu ne peux pas retirer de l'argent de la banque.";
    public static final String NOT_ENOUGH_MONEY_CITY = PREFIX + "§cLa banque de ville n'a pas assez d'argent.";
    public static String WITHDRAW_MONEY_CITY(double money){return PREFIX + "§aTu as retiré §7" + ConsulatCore.formatMoney(money) + " §ade la ville";}
    public static String MONEY_CITY(double money){return PREFIX + "§ala ville a §e" + ConsulatCore.formatMoney(money);}
    public static final String CANT_MANAGE_ACCESS = PREFIX + "§cTu ne peux pas gérer les accès.";
    public static final String CANT_MANAGE_ACCESS_PLAYER = PREFIX + "§cTu ne peux pas modifier l'accès de ce joueur.";
    public static final String PLAYER_ALREADY_ACCESS_CLAIM = PREFIX + "§cCe joueur a déjà accès à ce claim.";
    public static String ADD_PLAYER_CLAIM(String player){return PREFIX + "§a" + player + " a été ajouté à ce claim.";}
    public static String REMOVE_PLAYER_CLAIM(String player){return PREFIX + "§a" + player + " a été retiré de ce claim.";}
    public static final String PLAYER_NOT_ACCESS_CLAIM = PREFIX + "§cCe joueur n'a pas accès à ce claim.";
    public static final String PLAYER_NOT_ACCESS_CLAIMS = PREFIX + "§cCe joueur n'a pas accès à tes claim.";
    public static final String NOBODY_ACCESS_CLAIM = PREFIX + "§cAucun joueur n'a accès à ce claim.";
    public static <E> String LIST_ACCESS_CLAIM(Collection<UUID> toShow){ return PREFIX + "§6Liste des joueurs ayant accès à ce claim: §e" + toString(toShow, uuid -> Bukkit.getOfflinePlayer(uuid).getName()); }
    public static final String NOW_SPEAK_IN_CITY_CHAT = PREFIX + "§aTu parles maintenant dans le chat de ville.";
    public static final String NOW_SPEAK_IN_GLOBAL_CHAT = PREFIX + "§aTu parles maintenant dans le chat global.";
    public static final String CANT_CHANGE_DESCRIPTION_CITY = PREFIX + "§cTu ne peux pas changer la description de la ville.";
    public static final String YOU_RESET_DESCRIPTION_CITY = PREFIX + "§aTu as reset la description de la ville.";
    public static final String YOU_RESET_DESCRIPTION_CLAIM = PREFIX + "§aTu as reset la description de ce claim.";
    public static String YOU_CHANGE_DESCRIPTION_CITY(String description){return PREFIX + "§aTu as ajouté la description suivante à la ville:§7\n" + description;}
    public static String YOU_CHANGE_DESCRIPTION_CLAIM(String description){return PREFIX + "§aTu as ajouté la description suivante à ce claim:§7\n" + description;}
    public static final String CANT_CHANGE_LEADER_CITY = PREFIX + "§cTu ne peux pas changer le propriétaire de la ville.";
    public static final String ALREADY_LEADER_CITY = PREFIX + "§cCe joueur est déjà le propriétaire de la ville.";
    public static String CHANGE_LEADER_CITY(City city, String newLeader, String by){return PREFIX_CITY(city) + "§a" + newLeader + " §7est passé §epropriétaire §7de la ville par §a" + by;}
    public static final String YOU_NO_CLAIM = PREFIX + "§cTu n'as pas de claims.";
    public static final String PLAYER_ALREADY_ACCESS_CLAIMS = PREFIX + "§cCe joueur a déjà accès à tes claims.";
    public static String ADD_PLAYER_CLAIMS(String player){return PREFIX + "§a" + player + " a été ajouté à tes claims";}
    public static String REMOVE_PLAYER_CLAIMS(String player){return PREFIX + "§a" + player + " a été retiré de tes claims";}
    public static final String NOT_IN_YOUR_CLAIM = PREFIX + "§cTu n'es pas dans un de tes claims.";
    public static final String CANT_KICK_YOURSELF = PREFIX + "§cTu ne peux pas te kick toi-même.";
    public static final String PLAYER_NOT_IN_CLAIM = PREFIX + "§cCe joueur n'est pas sur un claim.";
    public static final String PLAYER_NOT_IN_YOUR_CLAIM = PREFIX + "§cCe joueur n'est pas sur l'un de tes claims.";
    public static final String CANT_KICK_PLAYER = PREFIX + "§cTu ne peux pas kick ce joueur.";
    public static String YOU_KICKED_PLAYER_CLAIM(String player){return PREFIX + "§aTu as kick §e" + player + "§a de tes claims.";}
    public static String YOU_BEEN_KICKED_BY(String by){return PREFIX + "§e" + by + "§c t'a kick du claim dans lequel tu te trouvais. Tu as été téléporté au spawn.";}
    public static String INFO_CLAIM(SurvivalOffline player){
        return Text.PREFIX + "§7Informations sur §a" + player.getName() + "§7:\n" +
                Text.PREFIX + "§eGrade: §a" + player.getRank().getRankName() + "\n"+
                Text.PREFIX + "§eArgent: §a" + player.getMoney() + "\n"+
                Text.PREFIX + "§eA rejoint le: §a" + player.getRegistered() + "\n"+
                Text.PREFIX + "§eDernière connexion: §a" + player.getLastConnection();
    }
    public static String LIST_CLAIM(Collection<Claim> claims){
        StringBuilder builder = new StringBuilder();
        int i = 0;
        for(Claim claim : claims){
            builder.append("§6Claim §e").append(++i).append("§6: §eX:§c ").append(claim.getX() * 16).append(" §eY:§c ").append(claim.getZ() * 16).append(", ");
        }
        return PREFIX + builder.substring(0, builder.length() - 2);
    }
    public static final String YOU_CANT_UNCLAIM = PREFIX + "§cTu ne peux pas unclaim ce chunk.";
    public static String CHUNK_UNCLAIM(double money){return PREFIX + "§aChunk unclaim, tu as récupéré " + ConsulatCore.formatMoney(money) + ".";}
    public static final String CHUNK_UNCLAIM = PREFIX + "§aChunk unclaim.";
    public static final String DIMENSION_SHOP = PREFIX + "§cCe shop ne peut pas être placé dans cette dimension";
    public static final String NO_ITEM_IN_HAND = PREFIX + "§cTu n'as pas d'item en main";
    public static final String NO_TARGETED_BLOCK = PREFIX + "§cAucun bloc n'est visé.";
    public static final String BLOCK_HERE = PREFIX + "§cIl y a un bloc ici.";
    public static String BALTOP(SortedSet<BaltopManager.MoneyOwner> players){
        StringBuilder builder = new StringBuilder(PREFIX + "§e========= Baltop =========\n");
        for(BaltopManager.MoneyOwner moneyOwner : players){
            builder.append("§6").append(moneyOwner.getName()).append(":§e ").append(ConsulatCore.formatMoney(moneyOwner.getMoney())).append('\n');
        }
        return builder.deleteCharAt(builder.length()).toString();}
    public static String MONEY(double money){return PREFIX + "Tu as §e" + ConsulatCore.formatMoney(money) + ".";}
    public static String YOU_RECEIVED_MONEY_FROM(double money, String from){return PREFIX + "§aTu as reçu §2" + ConsulatCore.formatMoney(money) + " §ade §2" + from;}
    public static String YOU_SEND_MONEY_TO(double money, String from){return PREFIX + "§aTu as envoyé §2" + ConsulatCore.formatMoney(money) + " §aà §2" + from;}
    public static String INVALID_ITEM(String item){return PREFIX + "§cItem invalide §7(" + item + ")§c.";}
    public static String ITEM_NOT_IN_SELL(String item){return PREFIX + "§cL'item §7" + item + " §cn'est pas en vente actuellement.";}
    public static String BRODCAST(String player, String message){return BROADCAST_PREFIX + "§4" + player + "§7: §b" + message;}
    public static final String NEED_SPECTATOR = PREFIX + "§cTu dois être en spectateur pour regarder les enderchest.";
    public static final String NEED_STAFF_MODE = PREFIX + "§cTu dois être en staff mode.";
    public static String KICK_PLAYER(String reason){return "§7§l§m ----[§r §6§lLe Consulat §7§l§m]----\n\n§cTu as été exclu.\n§cRaison: §4" + reason;}
    public static final String YOU_KICKED_PLAYER = PREFIX + "§aJoueur exclu !";
    public static final String NO_MORE_IN_STAFF_MODE = MODERATION_PREFIX + "§cTu n'es plus en mode modérateur.";
    public static final String NOW_IN_STAFF_MODE = MODERATION_PREFIX + "§aTu es désormais en mode modérateur.";
    public static TextComponent REPORT(String toReport, String reporter, String reason){
        TextComponent textComponent = new TextComponent(Text.MODERATION_PREFIX + "§a" + toReport + "§2 a été report.");
        textComponent.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT,
                new ComponentBuilder("§2Raison: §a" + reason +
                        "\n§2Par: §a" + reporter +
                        "\n§7§oClique pour te téléporter au joueur concerné"
                ).create()));
        textComponent.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/tpmod " + toReport));
        return textComponent;
    }
    public static String YOU_REPORTED(String toReport, String reason){return PREFIX + "§aTu as report " + toReport + " pour " + reason + ".";}
    public static final String NO_MORE_IN_SPY = PREFIX + "Tu ne vois plus les messages.";
    public static final String NOW_IN_SPY = PREFIX + "Tu vois désormais les messages.";
    public static String STAFF_LIST(Iterable<ConsulatPlayer> staff){
        StringBuilder builder = new StringBuilder(PREFIX + "§6§uListe du staff en ligne: ");
        for(ConsulatPlayer player : staff){
            Rank rank = player.getRank();
            builder.append(rank.getRankColor()).append("[").append(rank.getRankName()).append("] ").append(player.getName());
        }
        return builder.toString();}
    public static String YOU_TELEPORTED_PLAYER_TO(String player, String to){return PREFIX + "§aTu as téléporté " + player + " à " + to;}
    public static String YOU_TELEPORTED_TO(String to){return PREFIX + "§aTu t'es téléporté à " + to + ".";}
    public static final String MAYBE_UNBAN_PLAYER = Text.MODERATION_PREFIX + "Si le joueur était banni, il a été dé-banni.";
    public static final String MAYBE_UNMUTE_PLAYER = PREFIX + "Si le joueur était mute, il a été dé-mute." ;
    public static final String UNMUTE_PLAYER = PREFIX + "§aJoueur démute." ;
    public static final String PLAYER_NOT_MUTE = PREFIX + "§cCe joueur n'est pas mute.";
    public static final String NEED_WAIT = PREFIX + "§cTu dois attendre pour refaire cette commande.";
    public static String YOU_MUTE(MutedPlayer muteInfo){return PREFIX + "§cTu es actuellement mute.\n§4Raison: §c" + muteInfo.getReason() + "\n§4Jusqu'au: §c" + muteInfo.getEndDate();}
    public static String ADVERT(String sender, String message){return  "§e[Annonce] §6" + sender + "§7: §r" + message;}
    public static final String CANT_MP = PREFIX + "§cTu ne peux pas MP ce joueur.";
    public static TextComponent[] MP_FROM(String sender, String rawMessage){
        TextComponent message = new TextComponent("§7[§6MP§7] §6§l" + sender + "§r§7 >> §6Toi§r§7: §f" + rawMessage);
        TextComponent answer = new TextComponent("\n§a[Répondre]");
        answer.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new ComponentBuilder("§7§oRépondre à: §6" + sender).create()));
        answer.setClickEvent(new ClickEvent(ClickEvent.Action.SUGGEST_COMMAND, "/msg " + sender + " "));
        return new TextComponent[]{message, answer};
    }
    public static String MP_TO(String to, String rawMessage){return "§7[§6MP§7] §r§6Toi §7>> §6§l" + to + "§r§7: §f" + rawMessage;}
    public static String SPY(String from, String to, String message){return "§2(Spy) §a" + from + "§7 > §a" + to + "§7: " + message;}
    public static final String NOT_YET_TELEPORTED = PREFIX + "§cTu n'as pas encore été téléporté.";
    public static final String HOME_DELETED = PREFIX + "§aHome supprimé avec succès.";
    public static final String PLAYER_HAS_NO_HOME = PREFIX + "§cCe joueur ne possède pas ce home.";
    public static final String UNKNOWN_HOME = PREFIX + "§cHome introuvable !";
    public static final String NO_FLY = PREFIX + "Tu n'as pas le fly.";
    public static final String CANT_FLY_DIMENSION = PREFIX + "§cTu ne peux pas fly dans cette dimension.";
    public static final String CANT_USE_COMMAND_STAFF_MODE = PREFIX + "§cTu ne peux pas utiliser cette commande en modération.";
    public static final String FLY_ALREADY_ON = PREFIX + "Ton fly est déjà actif.";
    public static String WAIT_FLY(long minute, long second){return PREFIX + "Tu n'as pas attendu assez longtemps. Tu dois encore attendre " + minute + "m" + second + "s.";}
    public static final String CANT_FLY_HERE = PREFIX + "Tu ne peux pas fly dans un autre claim que le tien ou ceux que tu as accès.";
    public static final String FLY_ON = PREFIX + "Tu as activé ton fly !";
    public static final String INFINITE_FLY = PREFIX + "§aTu as le fly infini.";
    public static String FLY_INFO(long minute, long second){return PREFIX + "Tu as encore ton fly pendant " + minute + "m " + second + "s.";}
    public static final String NOT_IN_FLY = PREFIX + "§cTu n'es pas en fly.";
    public static final String FLY_OFF = PREFIX + "Ton fly est en pause !";
    public static final String FLY_INFINITE_OFF = PREFIX + "Tu as enlevé ton fly infini";
    public static final String NO_HOME = PREFIX + "§cTu ne possèdes aucun home.";
    public static String LIST_HOME(Collection<String> homes){return PREFIX + "§eVoici la liste de tes homes: " + toString(homes, home -> home);}
    public static TextComponent LIST_HOME_PLAYER(Map<String, Location> homes, String player){
        TextComponent message = new TextComponent("§6Liste des homes de: §c" + player + "\n§7---------------------------------");
        for(Map.Entry<String, Location> home : homes.entrySet()){
            TextComponent textComponent = new TextComponent("§a" + home.getKey() + " §7| §cX§7:§6" + home.getValue().getBlockX() + " §cY§7:§6" + home.getValue().getBlockY() + " §cZ§7:§6" + home.getValue().getBlockZ());
            textComponent.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new ComponentBuilder("§7Clique pour t'y téléporter.").create()));
            textComponent.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/tp " + home.getValue().getBlockX() + " " + home.getValue().getBlockY() + " " + home.getValue().getBlockZ()));
            message.addExtra(textComponent);
        }
        return message;}
    public static final String ALREADY_IGNORED = PREFIX + "§cTu as déjà ignoré ce joueur.";
    public static String PLAYER_IGNORED(String player){return PREFIX + "§a" + player + " a été ignoré.";}
    public static final String NOT_IGNORED = PREFIX + "§cCe joueur n'est pas ignoré.";
    public static String NO_MORE_IGNORED(String player){return PREFIX + "§a" + player + " n'est plus ignoré.";}
    public static final String NOBODY_IGNORED = PREFIX + "§aTu n'as ignoré personne.";
    public static String LIST_IGNORED(Collection<UUID> ignored){return PREFIX + "§aJoueurs ignorés: " + toString(ignored, uuid -> Bukkit.getOfflinePlayer(uuid).getName());}
    public static final String NO_CUSTOM_RANK = PREFIX + "§cTu n'as pas de grade personnalisé.";
    public static final String CUSTOM_RANK_RESET = PREFIX + "§aTon grade personnalisé a été réinitialisé.";
    public static final String CHOOSE_CUSTOM_RANK_COLOR = PREFIX + "§6Choisis la couleur de ton grade: ";
    public static String CUSTOM_RANK_COLOR_CHOSEN(ChatColor color){return PREFIX + "§7Tu as choisi " + color + "cette couleur !\n§6Écris dans le chat le nom de ton grade: §o(10 caractères maximum, celui-ci aura des crochets par défaut)";}
    public static String NEW_CUSTOM_RANK(ConsulatPlayer player){return PREFIX + "§6Voilà ton nouveau grade: " + player.getDisplayName();}
    public static final String INVALID_HOME_NAME = PREFIX + "§cNom de home invalide.";
    public static final String DIMENSION_HOME = PREFIX + "§cTu ne peux pas mettre de home dans cette dimension.";
    public static final String NO_MORE_HOME_AVAILABLE = PREFIX + "§cTu as atteint ta limite de homes, définis la position d'un home existant ou supprime en un.";
    public static final String HOME_SET = PREFIX + "§aHome sauvegardé.";
    public static final String DONT_HAVE_PERK = PREFIX + "§cTu n'as pas ce privilège.";
    public static final String TOP_TELEPORTED = PREFIX + "§aTu as été téléporté en haut !";
    public static String ALREADY_ASK_TPA(String to){return PREFIX + "§cTu as déjà fait une demande de téléportation à " + to;}
    public static String TPA_TO(String to){return PREFIX + "§aTu as fait une demande de téléportation à " + to;}
    public static TextComponent TPA_FROM(String from){
        TextComponent tpaRequest = new TextComponent(PREFIX + "§eTu as reçu une demande de téléportation de " + from);
        tpaRequest.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new ComponentBuilder("§aAccepter").create()));
        tpaRequest.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/tpa accept " + from));
        return tpaRequest;}
    public static final String DIDNT_TPA = PREFIX + "§cCe joueur ne t'a pas demandé en téléportation.";
    public static String HAVE_BEEN_TPA(double money, String to){return PREFIX + "§aTu as été téléporté à " + to + " pour " + ConsulatCore.formatMoney(money) + ".";}
    public static String HAVE_BEEN_TPA(String to){return PREFIX + "§aTu as été téléporté à " + to + ".";}
    public static String NOW_IN_QUEUE(int position, int size){return PREFIX + "§aTu es desormais dans la queue: " + position + "/" + size;}
    public static String IN_QUEUE(int position, int size){return PREFIX + "§cTu es déjà dans la queue: " + position + "/" + size;}
    public static final String FLY_IS_FINISHED = PREFIX +  "§cTon fly est terminé !";
    public static final String FLY_OUTSIDE_CLAIM = PREFIX + "§cTon fly est terminé car tu as quitté ton claim !";
    public static final String TP_BECAUSE_STUCK = PREFIX + "§aTu as été téléporté au spawn pour cause de suffocation.";
    public static final String YOUR_ALONE = PREFIX + "§cTu es seul.... désolé";
    public static final String NOW_VISIBLE = PREFIX + "§aTu es désormais visible.";
    public static final String NOW_INVISIBLE = PREFIX + "§cTu es désormais invisible.";
    public static final String BEEN_UNFROZEN = ANNOUNCE_PREFIX + "Tu as été un-freeze.";
    public static final String BEEN_FROZEN = ANNOUNCE_PREFIX + "Tu as été freeze par un modérateur.";
    public static final String PLAYER_UNFREEZE = ANNOUNCE_PREFIX + "Joueur un-freeze";
    public static final String PLAYER_FREEZE = ANNOUNCE_PREFIX + "Joueur freeze";
    public static final String CHEST_IS_PRIVATE = PREFIX + "§cCe coffre est privé";
    public static final String ANOTHER_PLAYER_NEAR = PREFIX + "§cUn autre joueur est à proximité.";
    public static final String BLOCK_LIMIT_CHUNK = PREFIX + "§cCe chunk à atteint la limite pour ce bloc.";
    public static final String LOADING_INVENTORY = PREFIX + "§7Chargement de l'inventaire...";
    public static String PLAYER_LEFT_FREEZE(String player){return Text.MODERATION_PREFIX + "§6" + player + "§c s'est déconnecté en étant freeze.";}
    public static final String TUTORIAL_SHOP =
            Text.PREFIX + "Comment créer un shop:\n" +
            Text.PREFIX + "Mets l'item à vendre dans un coffre, place un panneau dessus, et écris ceci sur ton panneau\n" +
            "§e                   [ConsulShop]\n" +
            "§e     prix à l'unité (une virgule est un point)\n" +
            "§e                       VIDE\n" +
            "§e                       VIDE";
    public static final String USE_OAK_WIGN = PREFIX + "§cUtilise un panneau en bois de chêne.";
    public static final String SHOP_ONLY_ON_CHEST = PREFIX + "§cUn shop ne peut être que sur un coffre simple.";
    public static final String ALREADY_SHOP = PREFIX + "§cCe coffre est déjà un shop.";
    public static final String HIT_SHOP_LIMIT = PREFIX + "§cTu as atteint ta limite de shops.";
    public static final String SHOP_CANT_BE_EMPTY = PREFIX + "§cLe coffre ne doit pas être vide pour être crée.";
    public static String MUST_BE_AT_LEAST(double money){return PREFIX + "§cLe prix doit être d'au moins " + ConsulatCore.formatMoney(money) + ".";}
    public static final String REMOVE_ITEM_FRAME = PREFIX + "§cMerci de retirer le cadre.";
    public static final String ONLY_ONE_ENCHANT_SHOP = PREFIX + "§cUn livre ne peut être vendu qu'avec un seul enchantement.";
    public static final String ITEMS_MUST_BE_EQUALS = PREFIX + "§cLes items dans ton shop doivent être identiques.";
    public static final String SHOP_CREATED = PREFIX + "§aTon shop a bien été crée.";
    public static final String MUST_BREAK_SIGN = PREFIX + "§cTu dois casser le panneau pour supprimer ton shop.";
    public static String SHOP_OWNED_BY(String player){return PREFIX + "§cCe shop appartient à §4" + player + "§c.";}
    public static final String SHOP_REMOVED = PREFIX + "§aTu viens de détruire un de tes shops.";
    public static final String CANT_BREAK_SHOP = PREFIX + "§cTu ne peux pas casser ce shop.";
    public static final String SHOP_IS_EMPTY = PREFIX + "§cCe shop est actuellement vide.";
    public static final String SHOP_IS_NOT_AVAILABLE = PREFIX + "§cCe shop n'est pas disponible.";
    public static final String CANT_BUY_OWN_SHOP = PREFIX + "§cTu ne peux pas acheter à ton propre shop.";
    public static final String NOT_ENOUGH_SPACE_INVENTORY = PREFIX + "§cTu n'as pas assez de place dans ton inventaire.";
    public static final String SET_HOME_CANCELLED = PREFIX + "§cTu as annulé le déplacement du home.";
    public static final String CANT_CHANGE_PERMISSION = PREFIX + "§cTu ne peux pas modifier les permissions de ce joueur.";
    public static final String INVALID_RANK = PREFIX + "§cLe grade entré n'est pas valide.";
    public static final String CITY_HOME_CHANGED = PREFIX + "§cLe home de la ville a été changé.";
    public static final String CITY_DISBANDED = PREFIX + "§aTu as détruit ta ville :(";
    public static final String NO_ANTECEDENT = PREFIX + "§cCe joueur n'a pas d'antécédents.";
    public static final String ALREADY_MUTED = PREFIX + "§cCe joueur est déjà mute.";
    public static final String NO_ITEM_TO_SELL = PREFIX + "§cTu n'as pas d'item à vendre.";
    public static final String SHOP_NOT_FOUND = PREFIX + "§cCe shop n'a pas été trouvé.";
    public static final String BUY_HOME = PREFIX + "Tu as acheté un home supplémentaire.";
    public static final String NOW_TOURISTE = PREFIX + "Tu es désormais touriste !";
    public static String TELEPORTATION(double money){return "§eTéléportation réussie pour §c" + ConsulatCore.formatMoney(money) + ".";}
    public static TextComponent SANCTION_BANNED(String targetName, String sanctionName, String duration, String modName, int recidive){
        TextComponent textComponent = new TextComponent(Text.MODERATION_PREFIX + "§c" + targetName + "§4 a été banni.");
        textComponent.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT,
                new ComponentBuilder("§7Motif: §8" + sanctionName +
                        "§7\nPendant: §8" + duration +
                        "§7\nPar: §8" + modName +
                        "§7\nRécidive: §8" + recidive
                ).create()));
        return textComponent;
    }
    public static TextComponent SANCTION_MUTED(String targetName, String sanctionName, String duration, String modName, int recidive){
        TextComponent textComponent = new TextComponent(Text.MODERATION_PREFIX + "§e" + targetName + "§6 a été mute.");
        textComponent.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT,
                new ComponentBuilder("§7Motif: §8" + sanctionName +
                        "§7\nPendant: §8" + duration +
                        "§7\nPar: §8" + modName +
                        "§7\nRécidive: §8" + recidive
                ).create()));
        return textComponent;
    }
    public static String PLAYER_BANNED(String player){return ANNOUNCE_PREFIX + "§c" + player + "§4 a été banni.";}
    public static String SHOP_NOTIFICATION(double money){return PREFIX + "§aTu as reçu " + ConsulatCore.formatMoney(money) + " grâce à un de tes shops.";}
    public static String CLAIM_DESCRIPTION(String description){return PREFIX + "§7" + description;}
    private static <E> String toString(Collection<E> collection, Function<E, String> toString){
        StringBuilder builder = new StringBuilder();
        for(E element : collection){
            builder.append(toString.apply(element)).append(", ");
        }
        return builder.substring(0, builder.length() - 2);
    }
    
}
