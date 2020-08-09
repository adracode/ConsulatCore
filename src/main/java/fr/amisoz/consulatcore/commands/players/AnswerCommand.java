package fr.amisoz.consulatcore.commands.players;

import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.RequiredArgumentBuilder;
import fr.amisoz.consulatcore.ConsulatCore;
import fr.amisoz.consulatcore.Text;
import fr.amisoz.consulatcore.players.SurvivalPlayer;
import fr.leconsulat.api.commands.ConsulatCommand;
import fr.leconsulat.api.player.CPlayerManager;
import fr.leconsulat.api.player.ConsulatPlayer;
import fr.leconsulat.api.ranks.Rank;
import org.apache.commons.lang.StringUtils;

public class AnswerCommand extends ConsulatCommand {

    public AnswerCommand() {
        super("consulat.core", "answer", "r", "/r <Message>", 1, Rank.JOUEUR);
        suggest(RequiredArgumentBuilder.argument("message", StringArgumentType.greedyString()));
    }

    @Override
    public void onCommand(ConsulatPlayer sender, String[] args){
        SurvivalPlayer survivalSender = (SurvivalPlayer)sender;
        SurvivalPlayer target = survivalSender.getLastPrivate() == null ?
                null : (SurvivalPlayer)CPlayerManager.getInstance().getConsulatPlayer(survivalSender.getLastPrivate());
        if(target == null){
            sender.sendMessage(Text.PLAYER_NOT_CONNECTED);
            return;
        }
        String rawMessage = StringUtils.join(args, ' ');
        target.sendMessage(Text.MP_FROM(sender.getName(), rawMessage));
        target.setLastPrivate(sender.getUUID());
        sender.sendMessage(Text.MP_TO(target.getName(), rawMessage));
        ConsulatCore.getInstance().getSpy().sendMessage(Text.SPY(sender.getName(), target.getName(), rawMessage));
    }
}
