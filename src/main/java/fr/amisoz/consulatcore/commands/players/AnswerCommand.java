package fr.amisoz.consulatcore.commands.players;

import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.builder.RequiredArgumentBuilder;
import fr.amisoz.consulatcore.players.SPlayerManager;
import fr.amisoz.consulatcore.players.SurvivalPlayer;
import fr.leconsulat.api.commands.ConsulatCommand;
import fr.leconsulat.api.player.CPlayerManager;
import fr.leconsulat.api.player.ConsulatPlayer;
import fr.leconsulat.api.ranks.Rank;
import org.apache.commons.lang.StringUtils;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.util.Collections;

public class AnswerCommand extends ConsulatCommand {

    public AnswerCommand() {
        super("answer", Collections.singletonList("r"), "/r <Message>", 1, Rank.JOUEUR);
        suggest(LiteralArgumentBuilder.literal("r")
                .then(RequiredArgumentBuilder.argument("message", StringArgumentType.greedyString())));
    }

    @Override
    public void onCommand(ConsulatPlayer sender, String[] args){
        SurvivalPlayer survivalSender = (SurvivalPlayer)sender;
        SurvivalPlayer target = survivalSender.getLastPrivate() == null ?
                null : (SurvivalPlayer)CPlayerManager.getInstance().getConsulatPlayer(survivalSender.getLastPrivate());
        if(target == null){
            sender.sendMessage("§cLe joueur est introuvable");
            return;
        }
        sender.getPlayer().performCommand("msg " + target.getName() + " " + StringUtils.join(args, " "));
    }
}
