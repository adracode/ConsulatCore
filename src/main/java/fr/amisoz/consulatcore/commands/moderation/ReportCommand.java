package fr.amisoz.consulatcore.commands.moderation;

import fr.amisoz.consulatcore.ConsulatCore;
import fr.amisoz.consulatcore.commands.manager.ConsulatCommand;
import fr.amisoz.consulatcore.moderation.ModerationUtils;
import fr.amisoz.consulatcore.players.CoreManagerPlayers;
import fr.amisoz.consulatcore.players.CorePlayer;
import fr.leconsulat.api.player.ConsulatPlayer;
import fr.leconsulat.api.player.PlayersManager;
import fr.leconsulat.api.ranks.RankEnum;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.ComponentBuilder;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

public class ReportCommand extends ConsulatCommand {

    private ConsulatCore consulatCore;

    public ReportCommand(ConsulatCore consulatCore) {
        super( "/report <Joueur> <Raison>", 2, RankEnum.JOUEUR);
        this.consulatCore = consulatCore;
    }


    @Override
    public void consulatCommand() {

        Player target = Bukkit.getPlayer(getArgs()[0]);
        if (target == null) {
            getPlayer().sendMessage(ChatColor.RED + "Joueur ciblé introuvable ! " + ChatColor.GRAY + "( " + getArgs()[0] + " )");
            return;
        }

        StringBuilder stringBuilder = new StringBuilder() ;
        for(int i = 1; i < getArgs().length; i++){
            stringBuilder.append(" ").append(getArgs()[i]);
        }
        String reason = stringBuilder.toString();

        Bukkit.getOnlinePlayers().forEach(onlinePlayer -> {
            ConsulatPlayer consulatPlayer = PlayersManager.getConsulatPlayer(onlinePlayer);
            if(consulatPlayer.getRank().getRankPower() >= RankEnum.MODO.getRankPower()){
                net.md_5.bungee.api.chat.TextComponent textComponent = new TextComponent(ModerationUtils.MODERATION_PREFIX + ChatColor.GREEN + target.getName() + ChatColor.DARK_GREEN + " a été report.");
                textComponent.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT,
                        new ComponentBuilder(ChatColor.DARK_GREEN + "Raison : " + ChatColor.GREEN + reason +
                                ChatColor.DARK_GREEN + "\nPar : " + ChatColor.GREEN + getPlayer().getName() +
                                ChatColor.GRAY + "\n§oClique pour te téléporter au joueur concerné"
                        ).create()));
                textComponent.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/tpmod " + target.getName()));
                onlinePlayer.spigot().sendMessage(textComponent);
            }
        });
    }
}