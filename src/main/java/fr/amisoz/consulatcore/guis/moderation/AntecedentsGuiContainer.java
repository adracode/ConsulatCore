package fr.amisoz.consulatcore.guis.moderation;

import fr.leconsulat.api.gui.GuiContainer;
import fr.leconsulat.api.gui.GuiManager;
import fr.leconsulat.api.gui.gui.module.api.Datable;
import fr.leconsulat.api.player.ConsulatOffline;

public class AntecedentsGuiContainer extends GuiContainer<ConsulatOffline> {
    
    private static AntecedentsGuiContainer instance;
    
    public AntecedentsGuiContainer(){
        if(instance != null){
            throw new IllegalStateException();
        }
        instance = this;
        GuiManager.getInstance().addContainer("antecedents", this);
    }
    
    @Override
    public Datable<ConsulatOffline> createGui(ConsulatOffline player){
        return new AntecedentsGui(player);
    }
}
