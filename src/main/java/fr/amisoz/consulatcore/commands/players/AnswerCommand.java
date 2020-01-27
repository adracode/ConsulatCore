package fr.amisoz.consulatcore.commands.players;

import fr.amisoz.consulatcore.commands.manager.ConsulatCommand;
import fr.leconsulat.api.ranks.RankEnum;
import org.apache.commons.lang.StringUtils;

public class AnswerCommand extends ConsulatCommand {

    public AnswerCommand() {
        super("/r <Message>", 1, RankEnum.JOUEUR);
    }

    @Override
    public void consulatCommand() {
        if(getCorePlayer().lastPrivate == null || !getCorePlayer().lastPrivate.isOnline()){
            getPlayer().sendMessage("§cLe joueur est introuvable");
            return;
        }

        getPlayer().performCommand("mp " + getCorePlayer().lastPrivate.getName() + " " + StringUtils.join(getArgs(), " "));
    }
}
