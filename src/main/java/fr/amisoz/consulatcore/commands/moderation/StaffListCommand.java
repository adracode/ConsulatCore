package fr.amisoz.consulatcore.commands.moderation;

import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import fr.leconsulat.api.commands.ConsulatCommand;
import fr.leconsulat.api.player.CPlayerManager;
import fr.leconsulat.api.player.ConsulatPlayer;
import fr.leconsulat.api.ranks.Rank;
import org.bukkit.Bukkit;

public class StaffListCommand extends ConsulatCommand {

    public StaffListCommand() {
        super("stafflist", "/stafflist", 0, Rank.MODPLUS);
        suggest(LiteralArgumentBuilder.literal("stafflist"));
    }

    @Override
    public void onCommand(ConsulatPlayer sender, String[] args){
        sender.sendMessage("§6§uListe du staff en ligne : ");
        Bukkit.getOnlinePlayers().forEach(player -> {
            ConsulatPlayer consulatPlayer = CPlayerManager.getInstance().getConsulatPlayer(player.getUniqueId());
            if(consulatPlayer.hasPower(Rank.BUILDER)){
                Rank rank = consulatPlayer.getRank();
                sender.sendMessage(rank.getRankColor() + "[" + rank.getRankName() + "] " + player.getName());
            }
        });
    }
}
