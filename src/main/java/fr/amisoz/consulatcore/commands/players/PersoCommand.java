package fr.amisoz.consulatcore.commands.players;

import fr.amisoz.consulatcore.ConsulatCore;
import fr.amisoz.consulatcore.commands.manager.ConsulatCommand;
import fr.amisoz.consulatcore.utils.CustomEnum;
import fr.leconsulat.api.custom.CustomDatabase;
import fr.leconsulat.api.ranks.RankEnum;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.ComponentBuilder;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;

import java.sql.SQLException;


public class PersoCommand extends ConsulatCommand {

    public PersoCommand() {
        super("/perso", 0, RankEnum.JOUEUR);
    }

    @Override
    public void consulatCommand() {
        if(!getConsulatPlayer().isPerso()){
            getPlayer().sendMessage("§cTu n'as pas de grade personnalisé.");
            return;
        }

        if(getArgs().length == 1){
            if(getArgs()[0].equalsIgnoreCase("reset")){
                Bukkit.getScheduler().runTaskAsynchronously(ConsulatCore.INSTANCE, () -> {
                    try {
                        CustomDatabase.setPrefix(getPlayer(), null);
                        getConsulatPlayer().setPersoPrefix(null);
                        getPlayer().sendMessage(ChatColor.GREEN + "Ton grade personnalisé a été réinitialisé.");
                    } catch (SQLException e) {
                        getPlayer().sendMessage(ChatColor.RED + "Erreur avec la base de données.");
                    }
                });
                return;
            }
        }
        if(getCorePlayer().persoState == CustomEnum.START){
            getCorePlayer().persoState = CustomEnum.PREFIX_COLOR;
            getPlayer().sendMessage("§6Choisis la couleur de ton grade : ");
            TextComponent[] textComponents = ConsulatCore.textPerso.toArray(new TextComponent[0]);
            getPlayer().spigot().sendMessage(textComponents);
        }else if(getCorePlayer().persoState == CustomEnum.PREFIX_COLOR){
            if(getArgs().length != 1) return;
            getCorePlayer().persoNick += "&" + getArgs()[0] +"[";
            getCorePlayer().persoState = CustomEnum.PREFIX;
            getPlayer().sendMessage("§7Tu as choisi §" + getArgs()[0] + "cette couleur !");
            getPlayer().sendMessage("§6Écris dans le chat le nom de ton grade : §o(10 caractères maximum, celui-ci aura des crochets par défaut)");
        }else if(getCorePlayer().persoState == CustomEnum.NAME_COLOR){
            if(getArgs().length != 1) return;
            getCorePlayer().persoNick += "&" + getArgs()[0] + " ";
            getPlayer().sendMessage("§6Voilà ton nouveau grade : " + ChatColor.translateAlternateColorCodes('&', getCorePlayer().persoNick) + getPlayer().getName());
            getConsulatPlayer().setPersoPrefix(getCorePlayer().persoNick);
            try {
                CustomDatabase.setPrefix(getPlayer(), getCorePlayer().persoNick);
            } catch (SQLException e) {
                getPlayer().sendMessage("§cErreur lors de la sauvegarde de ton grade !");
                e.printStackTrace();
            }

            getCorePlayer().persoNick = "";
            getCorePlayer().persoState = CustomEnum.START;
        }
    }
}
