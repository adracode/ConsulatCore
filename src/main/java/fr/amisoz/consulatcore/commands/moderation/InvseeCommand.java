package fr.amisoz.consulatcore.commands.moderation;

import fr.amisoz.consulatcore.ConsulatCore;
import fr.amisoz.consulatcore.Text;
import fr.amisoz.consulatcore.players.SurvivalPlayer;
import fr.leconsulat.api.ConsulatAPI;
import fr.leconsulat.api.commands.Arguments;
import fr.leconsulat.api.commands.ConsulatCommand;
import fr.leconsulat.api.player.CPlayerManager;
import fr.leconsulat.api.player.ConsulatPlayer;
import fr.leconsulat.api.ranks.Rank;
import fr.leconsulat.api.utils.InventoryUtils;
import org.bukkit.inventory.Inventory;
import org.jetbrains.annotations.NotNull;

import java.util.UUID;

public class InvseeCommand extends ConsulatCommand {
    
    public InvseeCommand(){
        super(ConsulatCore.getInstance(), "invsee");
        setDescription("Voir l'inventaire d'un joueur").
                setUsage("/invsee <joueur> - Voir l'inventaire d'un joueur").
                setArgsMin(1).
                setRank(Rank.MODO).
                suggest(Arguments.playerList("joueur"));
    }
    
    @Override
    public void onCommand(@NotNull ConsulatPlayer sender, @NotNull String[] args){
        SurvivalPlayer player = (SurvivalPlayer)sender;
        if(!player.isInModeration() && !player.hasPermission(ConsulatAPI.getConsulatAPI().getPermission("bypass-blockinventory"))){
            sender.sendMessage(Text.NEED_STAFF_MODE);
            return;
        }
        ConsulatPlayer target = CPlayerManager.getInstance().getConsulatPlayer(args[0]);
        if(target == null){
            UUID targetUUID = CPlayerManager.getInstance().getPlayerUUID(args[0]);
            if(targetUUID == null){
                sender.sendMessage(Text.PLAYER_DOESNT_EXISTS);
                return;
            }
            Inventory offlineInventory = InventoryUtils.getOfflineInventory(targetUUID);
            if(offlineInventory == null){
                sender.sendMessage(Text.ERROR);
                return;
            }
            sender.getPlayer().openInventory(offlineInventory);
            return;
        }
        sender.getPlayer().openInventory(target.getPlayer().getInventory());
    }
}
