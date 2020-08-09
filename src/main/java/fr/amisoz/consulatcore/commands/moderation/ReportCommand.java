package fr.amisoz.consulatcore.commands.moderation;

import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.RequiredArgumentBuilder;
import fr.amisoz.consulatcore.Text;
import fr.amisoz.consulatcore.players.SurvivalPlayer;
import fr.leconsulat.api.commands.Arguments;
import fr.leconsulat.api.commands.ConsulatCommand;
import fr.leconsulat.api.player.CPlayerManager;
import fr.leconsulat.api.player.ConsulatPlayer;
import fr.leconsulat.api.ranks.Rank;
import net.md_5.bungee.api.chat.TextComponent;

public class ReportCommand extends ConsulatCommand {
    
    public ReportCommand(){
        super("consulat.core", "report", "/report <Joueur> <Raison>", 2, Rank.JOUEUR);
        suggest(Arguments.playerList("joueur")
                        .then(RequiredArgumentBuilder.argument("raison", StringArgumentType.greedyString())));
    }
    
    @Override
    public void onCommand(ConsulatPlayer sender, String[] args){
        SurvivalPlayer target = (SurvivalPlayer)CPlayerManager.getInstance().getConsulatPlayer(args[0]);
        if(target == null){
            sender.sendMessage(Text.PLAYER_NOT_CONNECTED);
            return;
        }
        StringBuilder stringBuilder = new StringBuilder(args[1]);
        for(int i = 2; i < args.length; ++i){
            stringBuilder.append(" ").append(args[i]);
        }
        String reason = stringBuilder.toString();
        TextComponent report = Text.REPORT(target.getName(), sender.getName(), reason);
        for(ConsulatPlayer onlinePlayer : CPlayerManager.getInstance().getConsulatPlayers()){
            if(onlinePlayer.hasPower(Rank.MODO)){
                onlinePlayer.sendMessage(report);
            }
        }
        sender.sendMessage(Text.YOU_REPORTED(target.getName(), reason));
    }
}