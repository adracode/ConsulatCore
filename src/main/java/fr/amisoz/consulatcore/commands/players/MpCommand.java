package fr.amisoz.consulatcore.commands.players;

import fr.amisoz.consulatcore.ConsulatCore;
import fr.amisoz.consulatcore.commands.manager.ConsulatCommand;
import fr.amisoz.consulatcore.moderation.MuteObject;
import fr.amisoz.consulatcore.players.CoreManagerPlayers;
import fr.amisoz.consulatcore.players.CorePlayer;
import fr.leconsulat.api.ranks.RankEnum;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.ComponentBuilder;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.TextComponent;
import org.apache.commons.lang.StringUtils;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

import java.util.Arrays;
import java.util.Calendar;

public class MpCommand extends ConsulatCommand {

    public MpCommand() {
        super("/mp <Joueur> <Message>", 2, RankEnum.JOUEUR);
    }

    @Override
    public void consulatCommand() {
        String[] message = Arrays.copyOfRange(getArgs(), 1, getArgs().length);
        Player target = Bukkit.getPlayer(getArgs()[0]);

        if(target == null){
            getPlayer().sendMessage(ChatColor.RED + "Le joueur n'existe pas.");
            return;
        }

        CorePlayer targetCore = CoreManagerPlayers.getCorePlayer(target);
        if(targetCore.isModerate()){
            getPlayer().sendMessage(ChatColor.RED + "Le joueur n'existe pas.");
            return;
        }

        if(getCorePlayer().isMuted){
            MuteObject muteInfo = getCorePlayer().getMute();
            if(muteInfo != null) {
                getPlayer().sendMessage("§cTu es actuellement mute.\n§4Raison : §c" + muteInfo.getReason() +"\n§4Jusqu'au : §c" + muteInfo.getEndDate());
                return;
            }
        }

        String messageResult = StringUtils.join(message, " ");
        String messageFormat = "§7[§6MP§7] §6§l" + getPlayer().getName() + "§r§7 >> §6Toi§r§7 : §f" + messageResult;
        TextComponent messageFormatComponent = new TextComponent(messageFormat);
        TextComponent answerComponent = new TextComponent("\n§a[Répondre]");
        answerComponent.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT,
                new ComponentBuilder("§7§oRépondre à : §6" + getPlayer().getName()).create()));
        answerComponent.setClickEvent(new ClickEvent(ClickEvent.Action.SUGGEST_COMMAND, "/mp " + getPlayer().getName() + " "));
        target.spigot().sendMessage(messageFormatComponent, answerComponent);

        targetCore.lastPrivate = getPlayer();
        getPlayer().sendMessage("§7[§6MP§7] §r§6Toi §7>> §6§l" + target.getName() + "§r§7 : §f" + messageResult);
    }
}
