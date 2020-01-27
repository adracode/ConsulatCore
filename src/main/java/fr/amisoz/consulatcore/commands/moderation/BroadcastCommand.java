package fr.amisoz.consulatcore.commands.moderation;

import fr.amisoz.consulatcore.commands.manager.ConsulatCommand;
import fr.amisoz.consulatcore.moderation.ModerationUtils;
import fr.leconsulat.api.ranks.RankEnum;
import org.apache.commons.lang.StringUtils;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;

public class BroadcastCommand extends ConsulatCommand {


    public BroadcastCommand() {
        super("/annonce <Message>", 1, RankEnum.ADMIN);
    }

    @Override
    public void consulatCommand() {
        String message = StringUtils.join(getArgs(), " ");
        Bukkit.broadcastMessage(ModerationUtils.BROADCAST_PREFIX + ChatColor.DARK_RED + getPlayer().getName() + ChatColor.GRAY + " : §r" + ChatColor.AQUA + message);
    }
}
