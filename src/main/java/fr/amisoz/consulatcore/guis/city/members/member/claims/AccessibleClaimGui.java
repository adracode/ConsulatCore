package fr.amisoz.consulatcore.guis.city.members.member.claims;

import fr.amisoz.consulatcore.guis.city.members.member.claims.permissions.AccessPermissionsGui;
import fr.amisoz.consulatcore.zones.cities.City;
import fr.amisoz.consulatcore.zones.claims.Claim;
import fr.leconsulat.api.gui.GuiItem;
import fr.leconsulat.api.gui.GuiManager;
import fr.leconsulat.api.gui.event.GuiClickEvent;
import fr.leconsulat.api.gui.event.GuiCreateEvent;
import fr.leconsulat.api.gui.event.GuiOpenEvent;
import fr.leconsulat.api.gui.event.GuiRemoveEvent;
import fr.leconsulat.api.gui.gui.IGui;
import fr.leconsulat.api.gui.gui.module.api.Datable;
import fr.leconsulat.api.gui.gui.module.api.Pageable;
import fr.leconsulat.api.gui.gui.module.api.Relationnable;
import fr.leconsulat.api.gui.gui.template.DataRelatPagedGui;
import fr.leconsulat.api.player.ConsulatPlayer;
import org.bukkit.Material;
import org.jetbrains.annotations.Nullable;

import java.util.Iterator;
import java.util.Objects;
import java.util.UUID;

public class AccessibleClaimGui extends DataRelatPagedGui<UUID> {
    
    private static final byte PREVIOUS = 47;
    private static final byte NEXT = 51;
    private static final byte GIVE_ALL_SLOT = 3;
    private static final byte REMOVE_ALL_SLOT = 5;
    
    public AccessibleClaimGui(UUID uuid){
        super(uuid, "Claims accessible", 6,
                IGui.getItem("§eAccès", GIVE_ALL_SLOT, Material.END_CRYSTAL, "§7Donner l'accès à tous les claims"),
                IGui.getItem("§eAccès", REMOVE_ALL_SLOT, Material.BARRIER, "§7Retirer l'accès de tous les claims"));
        setDeco(Material.BLACK_STAINED_GLASS_PANE, 0, 8, 9, 10, 11, 12, 13, 14, 15, 16, 17, 18, 26, 27, 35, 36, 44, 46, 47, 48, 49, 50, 51, 52, 53);
        setDynamicItems(19, 20, 21, 22, 23, 24, 25, 28, 29, 30, 31, 32, 33, 34, 37, 38, 39, 40, 41, 42, 43);
        setTemplateItems(0, 3, 5, 8, 9, 10, 11, 12, 13, 14, 15, 16, 17, 18, 26, 27, 35, 36, 44, 46, 47, 48, 49, 50, 51, 52, 53);
    }
    
    @Override
    public void onCreate(){
        City city = getPlayerCity();
        for(Claim claim : city.getZoneClaims()){
            if(claim.hasAccess(getData())){
                addItemClaim(claim);
            }
        }
    }
    
    @Override
    public void onPageCreated(GuiCreateEvent event, Pageable page){
        if(page.getPage() != 0){
            IGui gui = page.getGui();
            gui.setItem(IGui.getItem("§7Précédent", PREVIOUS, Material.ARROW));
            getPage(page.getPage() - 1).getGui().setItem(IGui.getItem("§7Suivant", NEXT, Material.ARROW));
            gui.setDeco(Material.BLACK_STAINED_GLASS_PANE, NEXT);
        }
    }
    
    @Override
    public void onPageClick(GuiClickEvent event, Pageable page){
        City city = getPlayerCity();
        GuiItem clickedItem = page.getGui().getItem(event.getSlot());
        switch(event.getSlot()){
            case PREVIOUS:
                if(clickedItem.getType() == Material.ARROW){
                    getPage(page.getPage() - 1).getGui().open(event.getPlayer());
                }
                break;
            case NEXT:
                if(clickedItem.getType() == Material.ARROW){
                    getPage(page.getPage() + 1).getGui().open(event.getPlayer());
                }
                break;
            case GIVE_ALL_SLOT:
                if(!canSetPermission(event.getPlayer())){
                    return;
                }
                city.addAccess(getData());
                return;
            case REMOVE_ALL_SLOT:
                if(!canSetPermission(event.getPlayer())){
                    return;
                }
                city.removeAccess(getData());
                return;
        }
        if(event.getSlot() >= 19 && event.getSlot() <= 43 && clickedItem.getType() == Material.GRASS_BLOCK){
            Claim claim = (Claim)clickedItem.getAttachedObject();
            getChild(claim).getGui().open(event.getPlayer());
        }
    }
    
    @Override
    public void onPageOpened(GuiOpenEvent event, Pageable page){
        update(event.getPlayer(), canSetPermission(event.getPlayer()), GIVE_ALL_SLOT);
        update(event.getPlayer(), canSetPermission(event.getPlayer()), REMOVE_ALL_SLOT);
    }
    
    @Override
    public void onPageRemoved(GuiRemoveEvent event, Pageable page){
        if(page.getPage() != 0){
            getPage(page.getPage() - 1).getGui().setDeco(Material.BLACK_STAINED_GLASS_PANE, NEXT);
        }
    }
    
    @Override
    public Relationnable createChild(@Nullable Object key){
        if(key instanceof Claim){
            Claim claim = (Claim)key;
            return new AccessPermissionsGui(
                    (fr.amisoz.consulatcore.guis.claims.permissions.AccessPermissionsGui)
                            Objects.requireNonNull(GuiManager.getInstance().getContainer("claim").getGui(true, claim, getData())), claim
            );
        }
        return super.createChild(key);
    }
    
    public void update(ConsulatPlayer player, boolean allow, int slot){
        if(allow){
            setFakeItem(slot, null, player);
        } else {
            setDescriptionPlayer(slot, player, "", "§cTu ne peux pas", "§cfaire cette action");
        }
    }
    
    public void addItemClaim(Claim claim){
        GuiItem item = IGui.getItem("§e" + (claim.getX() << 4) + " " + (claim.getZ() << 4), -1, Material.GRASS_BLOCK);
        item.setAttachedObject(claim);
        addItem(item);
    }
    
    public void removeItemClaim(Claim claim){
        for(Iterator<GuiItem> iterator = iterator(); iterator.hasNext(); ){
            GuiItem item = iterator.next();
            if(item != null && item.getAttachedObject().equals(claim)){
                iterator.remove();
                return;
            }
        }
    }
    
    @SuppressWarnings("unchecked")
    private City getPlayerCity(){
        return ((Datable<City>)getFather().getFather()).getData();
    }
    
    private boolean canSetPermission(ConsulatPlayer player){
        return !getData().equals(player.getUUID());
    }
    
}
