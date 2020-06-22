package fr.amisoz.consulatcore.guis.moderation;

import fr.leconsulat.api.gui.GuiContainer;
import fr.leconsulat.api.gui.events.GuiClickEvent;
import fr.leconsulat.api.gui.events.PagedGuiCreateEvent;
import fr.leconsulat.api.player.ConsulatOffline;
import org.bukkit.Material;

public class SanctionGui extends GuiContainer<ConsulatOffline> {
    
    private BanGui banGui = new BanGui();
    private MuteGui muteGui = new MuteGui();
    
    public SanctionGui(){
        super(3);
        setTemplate("§6§lSanction §7↠ §e",
                getItem("§cBannir", 11, Material.REDSTONE_BLOCK),
                getItem("§6Mute", 15, Material.PAPER));
    }
    
    @Override
    public void onPageCreate(PagedGuiCreateEvent<ConsulatOffline> event){
        event.getPagedGui().setName("§6§lSanction §7↠ §e" + event.getData().getName());
        event.getGui().prepareChild(banGui, () -> banGui.createGui(event.getData(), event.getGui()));
        event.getGui().prepareChild(muteGui, () -> muteGui.createGui(event.getData(), event.getGui()));
    }
    
    @Override
    public void onClick(GuiClickEvent<ConsulatOffline> event){
        switch(event.getSlot()){
            case 11:
                event.getGui().getChild(banGui).open(event.getPlayer());
                break;
            case 15:
                event.getGui().getChild(muteGui).open(event.getPlayer());
                break;
        }
    }
    
}
