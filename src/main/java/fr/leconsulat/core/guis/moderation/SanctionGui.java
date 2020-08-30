package fr.leconsulat.core.guis.moderation;

import fr.leconsulat.api.gui.GuiContainer;
import fr.leconsulat.api.gui.GuiManager;
import fr.leconsulat.api.gui.event.GuiClickEvent;
import fr.leconsulat.api.gui.gui.IGui;
import fr.leconsulat.api.gui.gui.module.api.Datable;
import fr.leconsulat.api.gui.gui.module.api.Relationnable;
import fr.leconsulat.api.gui.gui.template.DataRelatGui;
import fr.leconsulat.api.player.ConsulatOffline;
import org.bukkit.Material;
import org.jetbrains.annotations.Nullable;

public class SanctionGui extends DataRelatGui<ConsulatOffline> {
    
    public static final String BAN = "ban";
    public static final String MUTE = "mute";
    
    public SanctionGui(ConsulatOffline player){
        super(player, "§6§lSanction §7↠ §e" + player.getName(), 3,
                IGui.getItem("§cBannir", 11, Material.REDSTONE_BLOCK),
                IGui.getItem("§6Mute", 15, Material.PAPER));
    }
    
    @Override
    public Relationnable createChild(@Nullable Object key){
        if(!(key instanceof String)){
            throw new IllegalArgumentException();
        }
        switch((String)key){
            case BAN:
                return new BanGui(getData());
            case MUTE:
                return new MuteGui(getData());
        }
        throw new IllegalArgumentException();
    }
    
    @Override
    public void onClick(GuiClickEvent event){
        switch(event.getSlot()){
            case 11:
                getChild(BAN).getGui().open(event.getPlayer());
                break;
            case 15:
                getChild(MUTE).getGui().open(event.getPlayer());
                break;
        }
    }
    
    public static class Container extends GuiContainer<ConsulatOffline> {
        
        private static Container instance;
        
        public Container(){
            if(instance != null){
                throw new IllegalStateException();
            }
            instance = this;
            GuiManager.getInstance().addContainer("sanctions", this);
        }
        
        @Override
        public Datable<ConsulatOffline> createGui(ConsulatOffline player){
            return new SanctionGui(player);
        }
    }
    
    
}
