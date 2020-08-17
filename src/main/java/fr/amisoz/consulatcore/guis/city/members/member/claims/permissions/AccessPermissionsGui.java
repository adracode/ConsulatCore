package fr.amisoz.consulatcore.guis.city.members.member.claims.permissions;

import fr.amisoz.consulatcore.zones.claims.Claim;
import fr.leconsulat.api.gui.GuiItem;
import fr.leconsulat.api.gui.event.GuiClickEvent;
import fr.leconsulat.api.gui.event.GuiOpenEvent;
import fr.leconsulat.api.gui.gui.IGui;
import fr.leconsulat.api.gui.gui.template.DataRelatGui;

import java.util.UUID;

public class AccessPermissionsGui extends DataRelatGui<Claim> {
    
    private IGui link;
    
    public AccessPermissionsGui(DataRelatGui<UUID> link, Claim claim){
        super(claim, (claim.getX() << 4) + " " + (claim.getZ() << 4), link.getLine());
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
    public void onOpened(GuiOpenEvent event){
        link.onOpened(event);
    }
    
    @Override
    public void onClick(GuiClickEvent event){
        link.onClick(event);
    }
    
    public IGui getLink(){
        return link;
    }
    
    public void setLink(IGui link){
        this.link = link;
    }
    
}
