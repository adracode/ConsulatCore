package fr.leconsulat.core.commands.moderation;

import fr.leconsulat.api.commands.ConsulatCommand;
import fr.leconsulat.api.player.ConsulatPlayer;
import fr.leconsulat.api.ranks.Rank;
import fr.leconsulat.core.ConsulatCore;
import fr.leconsulat.core.Text;
import org.bukkit.Bukkit;
import org.jetbrains.annotations.NotNull;

public class ToggleChatCommand extends ConsulatCommand {
    
    public ToggleChatCommand(){
        super(ConsulatCore.getInstance(), "togglechat");
        setDescription("Activer ou désactiver le chat").
                setUsage("/chat - Switcher le chat").
                setAliases("chat").
                setRank(Rank.RESPONSABLE).
                suggest();
    }
    
    @Override
    public void onCommand(@NotNull ConsulatPlayer sender, @NotNull String[] args){
        ConsulatCore core = ConsulatCore.getInstance();
        core.setChat(!core.isChatActivated());
        if(core.isChatActivated()){
            Bukkit.broadcastMessage(Text.BRODCAST(sender.getName(), "Le chat est à nouveau disponible."));
        } else {
            Bukkit.broadcastMessage(Text.BRODCAST(sender.getName(), "Le chat est coupé."));
        }
    }
}
