package fr.amisoz.consulatcore.commands.moderation;

import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.builder.RequiredArgumentBuilder;
import fr.leconsulat.api.commands.ConsulatCommand;
import fr.leconsulat.api.player.CPlayerManager;
import fr.leconsulat.api.player.ConsulatPlayer;
import fr.leconsulat.api.ranks.Rank;
import org.apache.commons.lang.StringUtils;
import org.bukkit.Bukkit;

import java.util.Collections;

public class StaffChatCommand extends ConsulatCommand {
    
    public StaffChatCommand() {
        super("staffchat", Collections.singletonList("sc"), "/sc <Message>", 1, Rank.MODO);
        suggest(LiteralArgumentBuilder.literal("staffchat")
                .then(RequiredArgumentBuilder.argument("message", StringArgumentType.greedyString())));
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
