package fr.amisoz.consulatcore.guis.moderation;

import fr.leconsulat.api.gui.GuiContainer;
import fr.leconsulat.api.gui.GuiManager;
import fr.leconsulat.api.gui.gui.module.api.Datable;
import fr.leconsulat.api.player.ConsulatOffline;

public class SanctionsGuiContainer extends GuiContainer<ConsulatOffline> {
    
    private static SanctionsGuiContainer instance;
    
    public SanctionsGuiContainer(){
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
