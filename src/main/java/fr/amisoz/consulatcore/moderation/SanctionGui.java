package fr.amisoz.consulatcore.moderation;

import fr.leconsulat.api.gui.GuiListener;
import fr.leconsulat.api.gui.events.GuiClickEvent;
import fr.leconsulat.api.gui.events.GuiCloseEvent;
import fr.leconsulat.api.gui.events.GuiCreateEvent;
import fr.leconsulat.api.gui.events.GuiOpenEvent;
import org.bukkit.Material;

public class SanctionGui extends GuiListener {
    
    public SanctionGui(){
        super(null, String.class);
        addGui(null, this, "§6§lSanction §7↠ §e", 3,
                getItem("§cBannir", 11, Material.REDSTONE_BLOCK),
                getItem("§6Mute", 15, Material.PAPER));
        setCreateOnOpen(true);
    }
    
    @Override
    public void onCreate(GuiCreateEvent event){
        if(event.getKey() == null){
            return;
        }
        event.getGui().setName("§6§lSanction §7↠ §e" + (event.getKey()));
    }
    
    @Override
    public void onOpen(GuiOpenEvent event){
    
    }
    
    @Override
    public void onClose(GuiCloseEvent event){
    
    }
    
    @Override
    public void onClick(GuiClickEvent event){
        switch(event.getSlot()){
            case 11:
            case 15:
                getChild(event.getSlot()).open(event.getPlayer(), event.getGui().getKey());
        }
    }
    
}
