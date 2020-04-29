package fr.amisoz.consulatcore.commands.moderation;

import fr.amisoz.consulatcore.Text;
import fr.amisoz.consulatcore.players.SurvivalPlayer;
import fr.leconsulat.api.commands.ConsulatCommand;
import fr.leconsulat.api.player.CPlayerManager;
import fr.leconsulat.api.player.ConsulatPlayer;
import fr.leconsulat.api.ranks.Rank;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.ComponentBuilder;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

public class ReportCommand extends ConsulatCommand {
    
    public ReportCommand(){
        super("report", "/report <Joueur> <Raison>", 2, Rank.JOUEUR);
    }
    
    @Override
    public void onCommand(ConsulatPlayer sender, String[] args){
        SurvivalPlayer target = (SurvivalPlayer)CPlayerManager.getInstance().getConsulatPlayer(args[0]);
        if(target == null){
            sender.sendMessage("§cJoueur ciblé introuvable !§7 ( " + args[0] + " )");
            return;
        }
        StringBuilder stringBuilder = new StringBuilder(args[1]);
        for(int i = 2; i < args.length; ++i){
            stringBuilder.append(" ").append(args[i]);
        }
        String reason = stringBuilder.toString();
        TextComponent textComponent = new TextComponent(Text.MODERATION_PREFIX + "§a" + target.getName() + "§2 a été report.");
        textComponent.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT,
                new ComponentBuilder("§2Raison : §a" + reason +
                        "\n§2Par : §a" + sender.getName() +
                        "\n§7§oClique pour te téléporter au joueur concerné"
                ).create()));
        textComponent.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/tpmod " + target.getName()));
        Bukkit.getOnlinePlayers().forEach(onlinePlayer -> {
            ConsulatPlayer consulatPlayer = CPlayerManager.getInstance().getConsulatPlayer(onlinePlayer.getUniqueId());
            if(consulatPlayer.hasPower(Rank.MODO)){
                onlinePlayer.spigot().sendMessage(textComponent);
            }
        });
        sender.sendMessage("§aTu as report " + target.getName() + " pour " + reason);
    }
}