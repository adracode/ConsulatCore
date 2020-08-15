package fr.amisoz.consulatcore.commands.claims;

import fr.amisoz.consulatcore.ConsulatCore;
import fr.amisoz.consulatcore.zones.claims.ClaimManager;
import fr.leconsulat.api.commands.ConsulatCommand;
import fr.leconsulat.api.player.ConsulatPlayer;
import fr.leconsulat.api.ranks.Rank;
import org.jetbrains.annotations.NotNull;

public class GetkeyCommand extends ConsulatCommand {
    
    public GetkeyCommand(){
        super(ConsulatCore.getInstance(), "getkey");
        setDescription("Te donnes une clé pour mettre un coffre privé").
                setUsage("/getkey - Donne une clé").
                setRank(Rank.RESPONSABLE).
                suggest();
    }
    
    @Override
    public void onCommand(@NotNull ConsulatPlayer sender, @NotNull String[] args){
        sender.getPlayer().getInventory().addItem(ClaimManager.getKey());
    }
}
