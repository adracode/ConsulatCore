package fr.amisoz.consulatcore.commands.moderation;

import fr.amisoz.consulatcore.ConsulatCore;
import fr.amisoz.consulatcore.commands.manager.ConsulatCommand;
import fr.leconsulat.api.ranks.RankEnum;

public class ToggleChatCommand extends ConsulatCommand {


    public ToggleChatCommand() {
        super("/chat", 0, RankEnum.ADMIN);
    }

    @Override
    public void consulatCommand() {
        if(!ConsulatCore.chat_activated){
            getPlayer().performCommand("annonce Le chat est à nouveau disponible.");
        }else{
            getPlayer().performCommand("annonce Le chat est coupé.");
        }

        ConsulatCore.chat_activated = !ConsulatCore.chat_activated;
    }
}
