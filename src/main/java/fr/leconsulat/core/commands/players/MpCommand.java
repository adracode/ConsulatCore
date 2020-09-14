package fr.leconsulat.core.commands.players;

import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.RequiredArgumentBuilder;
import fr.leconsulat.api.channel.ChannelManager;
import fr.leconsulat.api.commands.Arguments;
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

public class MpCommand extends ConsulatCommand {
    
    public MpCommand(){
        super(ConsulatCore.getInstance(), "msg");
        setDescription("Envoyer un message privé").
                setUsage("/msg <joueur> <message> - Envoyer un message privé").
                setAliases("mp", "whisper", "tell").
                setArgsMin(2).
                setRank(Rank.JOUEUR).
                suggest(Arguments.playerList("joueur")
                        .then(RequiredArgumentBuilder.argument("message", StringArgumentType.greedyString())));
    }
    
    @Override
    public void onCommand(@NotNull ConsulatPlayer sender, @NotNull String[] args){
        SurvivalPlayer target = (SurvivalPlayer)CPlayerManager.getInstance().getConsulatPlayer(args[0]);
        if(target == null){
            sender.sendMessage(Text.PLAYER_NOT_CONNECTED);
            return;
        }
        SurvivalPlayer survivalSender = (SurvivalPlayer)sender;
        if(survivalSender.isMuted()){
            MutedPlayer muteInfo = survivalSender.getMute();
            if(muteInfo != null){
                sender.sendMessage(Text.YOU_MUTE(muteInfo));
                return;
            }
        }
        if(!survivalSender.hasPower(Rank.RESPONSABLE) && (target.isIgnored(sender.getUUID()) || survivalSender.isIgnored(target.getUUID()))){
            sender.sendMessage(Text.CANT_MP);
            return;
        }
        String rawMessage = StringUtils.join(args, ' ', 1, args.length);
        target.sendMessage(Text.MP_FROM(sender.getName(), rawMessage));
        target.setLastPrivate(sender.getUUID());
        sender.sendMessage(Text.MP_TO(target.getName(), rawMessage));
        ChannelManager.getInstance().getChannel("spy").sendMessage(Text.SPY(sender.getName(), target.getName(), rawMessage));
    }
}
