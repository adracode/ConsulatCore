package fr.amisoz.consulatcore.guis.city.members.member.claims.permissions;

import fr.leconsulat.api.gui.GuiItem;
import fr.leconsulat.api.gui.event.GuiClickEvent;
import fr.leconsulat.api.gui.gui.IGui;
import fr.leconsulat.api.gui.gui.template.DataRelatGui;

import java.util.UUID;

public class AccessPermissionsGui extends DataRelatGui<UUID> {
    
    private IGui link;
    
    public IGui getLink(){
        return link;
    }
    
    public void setLink(IGui link){
        this.link = link;
    }
    
    public AccessPermissionsGui(DataRelatGui<UUID> link){
        super(link.getData(), link.getName(), link.getLine());
        setLink(link);
        ((fr.amisoz.consulatcore.guis.claims.permissions.AccessPermissionsGui)link).setLink(this);
    }
    
    @Override
    public void onCreate(){
        for(GuiItem item : link.getItems()){
            if(item != null){
                setItem(item);
            }
        }
    }
    
    @Override
    public void onClick(GuiClickEvent event){
       link.onClick(event);
    }
  
}
