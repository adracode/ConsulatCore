package fr.amisoz.consulatcore.guis.city.memberpermissions;

import fr.amisoz.consulatcore.guis.city.members.CityPublicPermissions;
import fr.amisoz.consulatcore.guis.city.members.member.MemberGui;
import fr.amisoz.consulatcore.zones.cities.City;
import fr.leconsulat.api.gui.Gui;
import fr.leconsulat.api.gui.GuiItem;
import fr.leconsulat.api.gui.GuiListener;
import fr.leconsulat.api.gui.events.GuiClickEvent;
import fr.leconsulat.api.gui.events.GuiCreateEvent;
import fr.leconsulat.api.gui.events.PagedGuiCreateEvent;
import fr.leconsulat.api.player.ConsulatPlayer;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Iterator;
import java.util.UUID;

public class ListMembersGui extends GuiListener<City> {
    
    private static final byte PUBLIC_PERMISSIONS_SLOT = 13;
    private CityPublicPermissions cityPublicPermissions = new CityPublicPermissions();
    private MemberGui memberGui = new MemberGui();
    
    public ListMembersGui(){
        super(6);
        setTemplate("Membres",
                getItem("§ePermissions publiques", PUBLIC_PERMISSIONS_SLOT, Material.BOOK))
                .setDeco(Material.BLACK_STAINED_GLASS_PANE, 9, 10, 11, 12, 14, 15, 16, 17, 18, 26, 27, 35, 36, 44, 45, 46, 47, 48, 49, 50, 51, 52, 53)
                .setDeco(Material.RED_STAINED_GLASS_PANE, 0, 1, 2, 3, 4, 5, 6, 7, 8);
        setMoveableItems(19, 20, 21, 22, 23, 24, 25, 28, 29, 30, 31, 32, 33, 34, 37, 38, 39, 40, 41, 42, 43);
    }
    
    @Override
    public void onCreate(GuiCreateEvent<City> event){
        City city = event.getData();
        Gui<City> current = event.getGui();
        current.prepareChild(cityPublicPermissions, ()-> cityPublicPermissions.createGui(city, current));
        for(UUID uuid : city.getMembers()){
            String name = Bukkit.getOfflinePlayer(uuid).getName();
            addPlayer(event.getGui(), uuid, name == null ? "Pseudo" : name);
        }
    }
    
    @Override
    public void onPageCreate(PagedGuiCreateEvent<City> event){
        int page = event.getPage();
        if(page != 0){
            event.getPagedGui().setItem(getItem("§7Précédent", 45, Material.ARROW));
            event.getGui().getPage(page - 1).setItem(getItem("§7Suivant", 53, Material.ARROW));
        }
    }
    
    @Override
    public void onClick(GuiClickEvent<City> event){
        City city = event.getData();
        ConsulatPlayer player = event.getPlayer();
        Gui<City> current = event.getGui();
        switch(event.getSlot()){
            case PUBLIC_PERMISSIONS_SLOT:
                current.getChild(cityPublicPermissions).open(player);
                return;
            case 45:
                if(event.getClickedItem().getType() == Material.ARROW){
                    event.getGui().open(player, event.getPage() - 1);
                }
                break;
            case 53:
                if(event.getClickedItem().getType() == Material.ARROW){
                    event.getGui().open(player, event.getPage() + 1);
                }
                return;
        }
        if(event.getSlot() >= 19 && event.getSlot() <= 44 && event.getClickedItem().getType() == Material.PLAYER_HEAD){
            if(city.getOwner().equals(event.getClickedItem().getAttachedObject())){
                player.sendMessage("§cLes permissions du proriétaire de la ville ne peuvent pas changer.");
                return;
            }
            UUID playerUUID = (UUID)event.getClickedItem().getAttachedObject();
            event.getGui().getChild(playerUUID).open(player);
        }
    }
    
    public void addPlayer(@Nullable Gui<City> current, @NotNull UUID uuid, @NotNull String name){
        if(current == null){
            return;
        }
        //Création de la tête du joueur
        GuiItem item = getItem("§e" + name, -1, uuid);
        //L'UUID du joueur est placé sur l'item
        item.setAttachedObject(uuid);
        //La tête est ajouté au PagedGui répertoriant les membres
        current.addItem(item);
        current.prepareChild(uuid, () -> memberGui.createGui(uuid, current));
    }
    
    public void removePlayer(@Nullable Gui<City> pagedGui, @NotNull UUID uuid){
        if(pagedGui == null){
            return;
        }
        pagedGui.removeChild(uuid);
        for(Iterator<GuiItem> iterator = pagedGui.iterator(); iterator.hasNext(); ){
            GuiItem item = iterator.next();
            if(item != null && item.getAttachedObject().equals(uuid)){
                iterator.remove();
                return;
            }
        }
    }
}
