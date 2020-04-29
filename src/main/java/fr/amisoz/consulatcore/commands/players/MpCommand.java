package fr.amisoz.consulatcore.commands.players;

import fr.amisoz.consulatcore.moderation.MuteObject;
import fr.amisoz.consulatcore.players.SurvivalPlayer;
import fr.leconsulat.api.commands.ConsulatCommand;
import fr.leconsulat.api.player.CPlayerManager;
import fr.leconsulat.api.player.ConsulatPlayer;
import fr.leconsulat.api.ranks.Rank;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.ComponentBuilder;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.TextComponent;
import org.apache.commons.lang.StringUtils;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

public class MpCommand extends ConsulatCommand {

    public MpCommand() {
        super("msg", "/msg <Joueur> <Message>", 2, Rank.JOUEUR);
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
            if(muteInfo != null) {
                sender.sendMessage("§cTu es actuellement mute.\n§4Raison : §c" + muteInfo.getReason() +"\n§4Jusqu'au : §c" + muteInfo.getEndDate());
                return;
            }
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
        Bukkit.getOnlinePlayers().forEach(player -> {
            SurvivalPlayer survivalEach = (SurvivalPlayer)CPlayerManager.getInstance().getConsulatPlayer(player.getUniqueId());
            if(survivalEach.isSpying() && !survivalEach.equals(sender) && player != target){
                player.sendMessage("§2(Spy) §a" + sender.getName() + "§7 > §a" + target.getName() +"§7 : " + messageResult);
            }
        });
    }
}
