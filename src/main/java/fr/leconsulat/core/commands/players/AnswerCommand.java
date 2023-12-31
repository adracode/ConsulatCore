package fr.leconsulat.core.commands.players;

import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.RequiredArgumentBuilder;
import fr.leconsulat.api.channel.ChannelManager;
import fr.leconsulat.api.commands.ConsulatCommand;
import fr.leconsulat.api.moderation.MutedPlayer;
import fr.leconsulat.api.player.CPlayerManager;
import fr.leconsulat.api.player.ConsulatPlayer;
import fr.leconsulat.api.ranks.Rank;
import fr.leconsulat.core.ConsulatCore;
import fr.leconsulat.core.Text;
import fr.leconsulat.core.players.SurvivalPlayer;
import org.apache.commons.lang.StringUtils;
import org.jetbrains.annotations.NotNull;

public class AnswerCommand extends ConsulatCommand {
    
    public AnswerCommand(){
        super(ConsulatCore.getInstance(), "answer");
        setDescription("Répondre à un message privé").
                setUsage("/r <message> - Répondre à un MP").
                setAliases("r").
                setArgsMin(1).
                setRank(Rank.JOUEUR).
                suggest(RequiredArgumentBuilder.argument("message", StringArgumentType.greedyString()));
    }
    
    @Override
    public void onCommand(@NotNull ConsulatPlayer sender, @NotNull String[] args){
        SurvivalPlayer survivalSender = (SurvivalPlayer)sender;
        SurvivalPlayer target = survivalSender.getLastPrivate() == null ?
                null : (SurvivalPlayer)CPlayerManager.getInstance().getConsulatPlayer(survivalSender.getLastPrivate());
        if(target == null){
            sender.sendMessage(Text.NO_ANSWER);
            return;
        }

        if(survivalSender.isMuted()){
            MutedPlayer muteInfo = survivalSender.getMute();
            if(muteInfo != null){
                sender.sendMessage(Text.YOU_MUTE(muteInfo));
                return;
            }
        }

        String rawMessage = StringUtils.join(args, ' ');
        target.sendMessage(Text.MP_FROM(sender.getName(), rawMessage));
        target.setLastPrivate(sender.getUUID());
        sender.sendMessage(Text.MP_TO(target.getName(), rawMessage));
        ChannelManager.getInstance().getChannel("spy").sendMessage(Text.SPY(sender.getName(), target.getName(), rawMessage));
    }
}
