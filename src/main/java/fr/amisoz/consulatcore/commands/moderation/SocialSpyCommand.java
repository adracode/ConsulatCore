package fr.amisoz.consulatcore.commands.moderation;

import fr.amisoz.consulatcore.ConsulatCore;
import fr.amisoz.consulatcore.commands.manager.ConsulatCommand;
import fr.leconsulat.api.ranks.RankEnum;

public class SocialSpyCommand extends ConsulatCommand {

    public SocialSpyCommand() {
        super("/socialspy", 0, RankEnum.RESPONSABLE);
    }

    @Override
    public void consulatCommand() {
        if(getCorePlayer().isSpy){
            getPlayer().sendMessage(ConsulatCore.PREFIX + "Tu ne vois plus les messages.");
        }else{
            getPlayer().sendMessage(ConsulatCore.PREFIX + "Tu vois désormais les messages.");
        }

        getCorePlayer().isSpy = !getCorePlayer().isSpy;
    }
}
