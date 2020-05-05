package fr.amisoz.consulatcore.commands.moderation;

import fr.amisoz.consulatcore.Text;
import fr.amisoz.consulatcore.moderation.InventorySanction;
import fr.amisoz.consulatcore.players.SurvivalPlayer;
import fr.leconsulat.api.commands.Arguments;
import fr.leconsulat.api.commands.ConsulatCommand;
import fr.leconsulat.api.player.CPlayerManager;
import fr.leconsulat.api.player.ConsulatPlayer;
import fr.leconsulat.api.ranks.Rank;
import org.bukkit.Bukkit;

import java.util.UUID;

public class SanctionCommand extends ConsulatCommand {
    
    public SanctionCommand(){
        super("sanction", "/sanction <Joueur>", 1, Rank.MODO);
        suggest(true, Arguments.player("joueur"));
    }
    
    @Override
    public void onCommand(ConsulatPlayer sender, String[] args){
        String targetName = args[0];
        SurvivalPlayer player = (SurvivalPlayer)sender;
        UUID uuid = CPlayerManager.getInstance().getPlayerUUID(args[0]);
        if(uuid == null){
            player.sendMessage(Text.PREFIX + "§cCe joueur ne s'est jamais connecté.");
            return;
        }
        player.setSanctionTarget(Bukkit.getOfflinePlayer(uuid).getName());
        sender.getPlayer().openInventory(InventorySanction.selectSanctionInventory(targetName));
    }
}