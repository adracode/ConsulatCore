package fr.amisoz.consulatcore.commands.moderation;

import fr.amisoz.consulatcore.ConsulatCore;
import fr.amisoz.consulatcore.commands.manager.ConsulatCommand;
import fr.amisoz.consulatcore.moderation.ModerationUtils;
import fr.leconsulat.api.ranks.RankEnum;

public class UnbanCommand extends ConsulatCommand {

    private ConsulatCore consulatCore;

    public UnbanCommand(ConsulatCore consulatCore) {
        super("/unban <Pseudo>", 1, RankEnum.ADMIN);
        this.consulatCore = consulatCore;
    }

    @Override
    public void consulatCommand() {
        String playerName = getArgs()[0];
        consulatCore.getModerationDatabase().unban(playerName);
        getPlayer().sendMessage(ModerationUtils.MODERATION_PREFIX + "Si le joueur était banni, il a été dé-banni.");
    }
}
