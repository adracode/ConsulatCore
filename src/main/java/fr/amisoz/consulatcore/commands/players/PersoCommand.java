package fr.amisoz.consulatcore.commands.players;

import fr.amisoz.consulatcore.ConsulatCore;
import fr.amisoz.consulatcore.Text;
import fr.amisoz.consulatcore.players.SurvivalPlayer;
import fr.amisoz.consulatcore.utils.CustomEnum;
import fr.leconsulat.api.commands.ConsulatCommand;
import fr.leconsulat.api.player.ConsulatPlayer;
import fr.leconsulat.api.ranks.Rank;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;

import java.sql.SQLException;


public class PersoCommand extends ConsulatCommand {
    
    public PersoCommand(){
        super("consulat.core", "perso", "/perso", 0, Rank.JOUEUR);
        suggest((listener) -> {
                    ConsulatPlayer player = getConsulatPlayer(listener);
                    return player != null && player.hasCustomRank();
                }
        );
    }
    
    @Override
    public void onCommand(ConsulatPlayer sender, String[] args){
        SurvivalPlayer survivalPlayer = (SurvivalPlayer)sender;
        if(!survivalPlayer.hasCustomRank()){
            sender.sendMessage(Text.NO_CUSTOM_RANK);
            return;
        }
        if(args.length == 1 && args[0].equalsIgnoreCase("reset")){
            Bukkit.getScheduler().runTaskAsynchronously(ConsulatCore.getInstance(), () -> {
                try {
                    survivalPlayer.resetCustomRank();
                    sender.sendMessage(Text.CUSTOM_RANK_RESET);
                } catch(SQLException e){
                    e.printStackTrace();
                    sender.sendMessage(Text.ERROR);
                }
            });
            return;
        }
        switch(survivalPlayer.getPersoState()){
            case START:
                survivalPlayer.setPersoState(CustomEnum.PREFIX_COLOR);
                sender.sendMessage(Text.CHOOSE_CUSTOM_RANK_COLOR);
                TextComponent[] textComponents = ConsulatCore.getInstance().getTextPerso().toArray(new TextComponent[0]);
                sender.sendMessage(textComponents);
                break;
            case PREFIX_COLOR:
                if(args.length != 1){
                    return;
                }
                ChatColor color = ChatColor.getByChar(args[0]);
                survivalPlayer.setColorPrefix(color);
                survivalPlayer.setPersoState(CustomEnum.PREFIX);
                sender.sendMessage(Text.CUSTOM_RANK_COLOR_CHOSEN(color));
                break;
            case NAME_COLOR:
                if(args.length != 1){
                    return;
                }
                survivalPlayer.setColorName(ChatColor.getByChar(args[0]));
                sender.sendMessage(Text.NEW_CUSTOM_RANK(survivalPlayer));
                try {
                    survivalPlayer.applyCustomRank();
                } catch(SQLException e){
                    sender.sendMessage(Text.ERROR);
                    e.printStackTrace();
                }
                survivalPlayer.setPersoState(CustomEnum.START);
                break;
        }
    }
}
