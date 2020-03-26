package fr.amisoz.consulatcore.commands.players;

import fr.amisoz.consulatcore.ConsulatCore;
import fr.amisoz.consulatcore.commands.manager.ConsulatCommand;
import fr.amisoz.consulatcore.moderation.MuteObject;
import fr.leconsulat.api.ranks.RankEnum;
import org.apache.commons.lang.StringUtils;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;

import java.util.Calendar;

public class AdvertCommand extends ConsulatCommand {

    public AdvertCommand() {
        super("/advert <Annonce>", 1, RankEnum.FINANCEUR);
    }

    @Override
    public void consulatCommand() {
        String message = StringUtils.join(getArgs(), " ");
        if(((System.currentTimeMillis() - getCorePlayer().advertDelay) >= 1000*60*60*3)){

            if(getCorePlayer().isMuted){
                MuteObject muteInfo = getCorePlayer().getMute();
                if(muteInfo != null) {
                    getPlayer().sendMessage("§cTu es actuellement mute.\n§4Raison : §c" + muteInfo.getReason() +"\n§4Jusqu'au : §c" + muteInfo.getEndDate());
                    return;
                }
            }
            Bukkit.broadcastMessage("§e[Annonce] §6"+ getPlayer().getName() + ChatColor.GRAY + " : §r" + message);
            getCorePlayer().advertDelay = System.currentTimeMillis();
        }else{
            getPlayer().sendMessage("§cTu dois attendre pour refaire cette commande.");
        }

    }
}
