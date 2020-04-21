package fr.amisoz.consulatcore.commands.moderation;

import fr.amisoz.consulatcore.Text;
import fr.amisoz.consulatcore.moderation.ModerationUtils;
import fr.leconsulat.api.commands.ConsulatCommand;
import fr.leconsulat.api.player.ConsulatPlayer;
import fr.leconsulat.api.ranks.Rank;
import org.apache.commons.lang.StringUtils;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

public class BroadcastCommand extends ConsulatCommand {
    
    public BroadcastCommand() {
        super("/annonce <Message>", 1, Rank.RESPONSABLE);
    }

    @Override
    public void onCommand(ConsulatPlayer sender, String[] args) {
        //TODO -> Stringbuilder
        String message = StringUtils.join(args, " ");
        Bukkit.broadcastMessage(Text.BROADCAST_PREFIX + ChatColor.DARK_RED + sender.getName() + ChatColor.GRAY + " : §r" + ChatColor.AQUA + message);
    }
}
