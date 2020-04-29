package fr.amisoz.consulatcore.commands.players;

import fr.amisoz.consulatcore.ConsulatCore;
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
        super("perso", "/perso", 0, Rank.JOUEUR);
    }
    
    @Override
    public void onCommand(ConsulatPlayer sender, String[] args){
        SurvivalPlayer survivalPlayer = (SurvivalPlayer)sender;
        if(!survivalPlayer.hasCustomRank()){
            sender.sendMessage("§cTu n'as pas de grade personnalisé.");
            return;
        }
        if(args.length == 1 && args[0].equalsIgnoreCase("reset")){
            Bukkit.getScheduler().runTaskAsynchronously(ConsulatCore.getInstance(), () -> {
                try {
                    survivalPlayer.resetCustomRank();
                    sender.sendMessage("§aTon grade personnalisé a été réinitialisé.");
                } catch(SQLException e){
                    sender.sendMessage("§cUne erreur interne est survenue.");
                }
            });
            return;
        }
        switch(survivalPlayer.getPersoState()){
            case START:
                survivalPlayer.setPersoState(CustomEnum.PREFIX_COLOR);
                sender.sendMessage("§6Choisis la couleur de ton grade : ");
                TextComponent[] textComponents = ConsulatCore.getInstance().getTextPerso().toArray(new TextComponent[0]);
                sender.sendMessage(textComponents);
                break;
            case PREFIX_COLOR:
                if(args.length != 1){
                    return;
                }
                survivalPlayer.setColorPrefix(ChatColor.getByChar(args[0]));
                survivalPlayer.setPersoState(CustomEnum.PREFIX);
                sender.sendMessage("§7Tu as choisi §" + args[0] + "cette couleur !");
                sender.sendMessage("§6Écris dans le chat le nom de ton grade : §o(10 caractères maximum, celui-ci aura des crochets par défaut)");
                break;
            case NAME_COLOR:
                if(args.length != 1){
                    return;
                }
                survivalPlayer.setColorName(ChatColor.getByChar(args[0]));
                sender.sendMessage("§6Voilà ton nouveau grade : " + ChatColor.translateAlternateColorCodes('&', survivalPlayer.getCustomRank()) + sender.getName());
                try {
                    survivalPlayer.applyCustomRank();
                } catch(SQLException e){
                    sender.sendMessage("§cErreur lors de la sauvegarde de ton grade !");
                    e.printStackTrace();
                }
                survivalPlayer.setPersoState(CustomEnum.START);
                break;
        }
    }
}
