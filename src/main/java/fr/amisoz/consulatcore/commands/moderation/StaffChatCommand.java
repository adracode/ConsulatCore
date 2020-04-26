package fr.amisoz.consulatcore.commands.moderation;

import fr.leconsulat.api.commands.ConsulatCommand;
import fr.leconsulat.api.player.CPlayerManager;
import fr.leconsulat.api.player.ConsulatPlayer;
import fr.leconsulat.api.ranks.Rank;
import org.apache.commons.lang.StringUtils;
import org.bukkit.Bukkit;

public class StaffChatCommand extends ConsulatCommand {
    
    public StaffChatCommand() {
        super("/sc <Message>", 1, Rank.MODO);
    }

    @Override
    public void onCommand(ConsulatPlayer sender, String[] args){
        String message = StringUtils.join(args, " ");
        Bukkit.getOnlinePlayers().forEach(player -> {
            ConsulatPlayer consulatPlayer = CPlayerManager.getInstance().getConsulatPlayer(player.getUniqueId());
            if(consulatPlayer != null && consulatPlayer.hasPower(Rank.MODO)){
                player.sendMessage("§2(Staff)§a " + sender.getName() + "§7 : " + message);
            }
        });
    }
}
