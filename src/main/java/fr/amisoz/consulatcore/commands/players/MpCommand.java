package fr.amisoz.consulatcore.commands.players;

import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.RequiredArgumentBuilder;
import fr.amisoz.consulatcore.ConsulatCore;
import fr.amisoz.consulatcore.Text;
import fr.amisoz.consulatcore.moderation.MuteObject;
import fr.amisoz.consulatcore.players.SurvivalPlayer;
import fr.leconsulat.api.commands.Arguments;
import fr.leconsulat.api.commands.ConsulatCommand;
import fr.leconsulat.api.player.CPlayerManager;
import fr.leconsulat.api.player.ConsulatPlayer;
import fr.leconsulat.api.ranks.Rank;
import org.apache.commons.lang.StringUtils;

import java.util.Arrays;

public class MpCommand extends ConsulatCommand {
    
    public MpCommand(){
        super("consulat.core", "msg",
                Arrays.asList("mp", "whisper", "tell"),
                "/msg <Joueur> <Message>", 2, Rank.JOUEUR);
        suggest(Arguments.playerList("joueur")
                .then(RequiredArgumentBuilder.argument("message", StringArgumentType.greedyString()))
        );
    }
    
    @Override
    public void onCommand(ConsulatPlayer sender, String[] args){
        SurvivalPlayer target = (SurvivalPlayer)CPlayerManager.getInstance().getConsulatPlayer(args[0]);
        if(target == null){
            sender.sendMessage(Text.PLAYER_NOT_CONNECTED);
            return;
        }
        SurvivalPlayer survivalSender = (SurvivalPlayer)sender;
        if(survivalSender.isMuted()){
            MuteObject muteInfo = survivalSender.getMute();
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
        ConsulatCore.getInstance().getSpy().sendMessage(Text.SPY(sender.getName(), target.getName(), rawMessage));
    }
}
