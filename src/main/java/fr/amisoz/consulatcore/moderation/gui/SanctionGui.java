package fr.amisoz.consulatcore.moderation.gui;

import fr.leconsulat.api.gui.GuiListener;
import fr.leconsulat.api.gui.events.GuiClickEvent;
import fr.leconsulat.api.gui.events.GuiCloseEvent;
import fr.leconsulat.api.gui.events.GuiCreateEvent;
import fr.leconsulat.api.gui.events.GuiOpenEvent;
import fr.leconsulat.api.player.ConsulatOffline;
import org.bukkit.Material;

public class SanctionGui extends GuiListener {
    
    public SanctionGui(){
        super(null, ConsulatOffline.class);
        addGui(null, this, "§6§lSanction §7↠ §e", 3,
                getItem("§cBannir", 11, Material.REDSTONE_BLOCK),
                getItem("§6Mute", 15, Material.PAPER));
        addChild(11, new BanGui(this));
        addChild(15, new MuteGui(this));
        setCreateOnOpen(true);
    }
    
    @Override
    public void onCreate(GuiCreateEvent event){
        if(event.getKey() == null){
            return;
        }
        event.getGui().setName("§6§lSanction §7↠ §e" + (((ConsulatOffline)event.getKey()).getName()));
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
