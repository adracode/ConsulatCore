package fr.amisoz.consulatcore.commands.moderation;

import fr.amisoz.consulatcore.commands.manager.ConsulatCommand;
import fr.amisoz.consulatcore.moderation.InventorySanction;
import fr.leconsulat.api.ranks.RankEnum;


public class SanctionCommand extends ConsulatCommand {

    public SanctionCommand() {
        super("/sanction <Joueur>", 1, RankEnum.MODO);
    }

    @Override
    public void consulatCommand() {
        String targetName = getArgs()[0];

        getCorePlayer().setSanctionTarget(targetName);
        getPlayer().openInventory(InventorySanction.selectSanctionInventory(targetName));
    }

}