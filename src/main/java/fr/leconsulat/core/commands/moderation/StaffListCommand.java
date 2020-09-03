package fr.leconsulat.core.commands.moderation;

import fr.leconsulat.api.commands.ConsulatCommand;
import fr.leconsulat.api.player.ConsulatPlayer;
import fr.leconsulat.api.ranks.Rank;
import fr.leconsulat.core.ConsulatCore;
import fr.leconsulat.core.Text;
import org.jetbrains.annotations.NotNull;

public class StaffListCommand extends ConsulatCommand {
    
    public StaffListCommand(){
        super(ConsulatCore.getInstance(), "stafflist");
        setDescription("Voir les staffs connectés").
                setUsage("/stafflist - Voir les staffs").
                setRank(Rank.MODPLUS).
                suggest();
    }
    
    @Override
    public void onCommand(@NotNull ConsulatPlayer sender, @NotNull String[] args){
        sender.sendMessage(Text.STAFF_LIST(ConsulatCore.getInstance().getStaffChannel()));
    }
}
