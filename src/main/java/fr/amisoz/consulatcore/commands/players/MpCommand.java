package fr.amisoz.consulatcore.commands.players;

import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.RequiredArgumentBuilder;
import fr.amisoz.consulatcore.ConsulatCore;
import fr.amisoz.consulatcore.moderation.MuteObject;
import fr.amisoz.consulatcore.players.SurvivalPlayer;
import fr.leconsulat.api.commands.Arguments;
import fr.leconsulat.api.commands.ConsulatCommand;
import fr.leconsulat.api.player.CPlayerManager;
import fr.leconsulat.api.player.ConsulatPlayer;
import fr.leconsulat.api.ranks.Rank;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.ComponentBuilder;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.TextComponent;
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
            sender.sendMessage("§cLe joueur n'existe pas.");
            return;
        }
        SurvivalPlayer survivalSender = (SurvivalPlayer)sender;
        if(survivalSender.isMuted()){
            MuteObject muteInfo = survivalSender.getMute();
            if(muteInfo != null){
                sender.sendMessage("§cTu es actuellement mute.\n§4Raison : §c" + muteInfo.getReason() + "\n§4Jusqu'au : §c" + muteInfo.getEndDate());
                return;
            }
        }
        if(!survivalSender.hasPower(Rank.RESPONSABLE) && (target.isIgnored(sender.getUUID()) || survivalSender.isIgnored(target.getUUID()))){
            sender.sendMessage("§cTu ne peux pas MP ce joueur.");
            return;
        }
        String messageResult = StringUtils.join(args, " ", 1, args.length);
        String messageFormat = "§7[§6MP§7] §6§l" + sender.getName() + "§r§7 >> §6Toi§r§7 : §f" + messageResult;
        TextComponent messageFormatComponent = new TextComponent(messageFormat);
        TextComponent answerComponent = new TextComponent("\n§a[Répondre]");
        answerComponent.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT,
                new ComponentBuilder("§7§oRépondre à : §6" + sender.getName()).create()));
        answerComponent.setClickEvent(new ClickEvent(ClickEvent.Action.SUGGEST_COMMAND, "/msg " + sender.getName() + " "));
        target.sendMessage(messageFormatComponent, answerComponent);
        target.setLastPrivate(sender.getUUID());
        sender.sendMessage("§7[§6MP§7] §r§6Toi §7>> §6§l" + target.getName() + "§r§7 : §f" + messageResult);
        ConsulatCore.getInstance().getSpy().sendMessage("§2(Spy) §a" + sender.getName() + "§7 > §a" + target.getName() + "§7 : " + messageResult);
    }
}
