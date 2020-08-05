package fr.amisoz.consulatcore.commands.moderation;

import fr.amisoz.consulatcore.ConsulatCore;
import fr.amisoz.consulatcore.chunks.ChunkManager;
import fr.leconsulat.api.commands.ConsulatCommand;
import fr.leconsulat.api.gui.GuiItem;
import fr.leconsulat.api.gui.gui.IGui;
import fr.leconsulat.api.gui.gui.module.api.Pageable;
import fr.leconsulat.api.player.ConsulatPlayer;
import fr.leconsulat.api.ranks.Rank;
import org.bukkit.Bukkit;

public class CDebugCommand extends ConsulatCommand {
    
    public CDebugCommand(){
        super("cdebug", "/cdebug", 0, Rank.ADMIN);
        setPermission("consulat.core.command.cdebug");
        suggest(false);
    }
    
    @Override
    public void onCommand(ConsulatPlayer sender, String[] args){
        if(args.length == 1){
            switch(args[0]){
                case "chunk":
                    sender.sendMessage(ChunkManager.getInstance().getChunk(sender.getPlayer().getChunk()).toString());
                    break;
            }
        }
        if(args.length == 2){
            Bukkit.getScheduler().scheduleSyncDelayedTask(ConsulatCore.getInstance(), () -> {
                IGui open = sender.getCurrentlyOpen();
                if(open instanceof Pageable){
                    for(int i = 0, size = Integer.parseInt(args[1]); i < size; ++i){
                        ((Pageable)open).getMainPage().addItem(new GuiItem("Test", (byte)-1, "adracode", null));
                    }
                }
            }, Integer.parseInt(args[0]));
        }
    }
}
