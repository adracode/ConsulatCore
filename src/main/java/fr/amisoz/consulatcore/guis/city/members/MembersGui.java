package fr.amisoz.consulatcore.guis.city.members;

import fr.amisoz.consulatcore.guis.GuiListenerStorage;
import fr.amisoz.consulatcore.guis.city.members.member.MemberGui;
import fr.amisoz.consulatcore.zones.cities.City;
import fr.leconsulat.api.gui.Gui;
import fr.leconsulat.api.gui.GuiItem;
import fr.leconsulat.api.gui.GuiListener;
import fr.leconsulat.api.gui.events.GuiClickEvent;
import fr.leconsulat.api.gui.events.GuiCreateEvent;
import fr.leconsulat.api.gui.events.PagedGuiCreateEvent;
import fr.leconsulat.api.gui.events.PagedGuiRemoveEvent;
import fr.leconsulat.api.player.ConsulatPlayer;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Iterator;
import java.util.UUID;

public class MembersGui extends GuiListener<City> {
    
    private static final byte PUBLIC_PERMISSIONS_SLOT = 13;
    
    public static final String PUBLIC = "city.members.public";
    public static final String MEMBER = "city.members.member";
    
    private PublicPermissionsGui publicMember = new PublicPermissionsGui();
    private MemberGui member = new MemberGui();
    
    public MembersGui(){
        super(6);
        setTemplate("Membres",
                getItem("§ePermissions publiques", PUBLIC_PERMISSIONS_SLOT, Material.BOOK))
                .setDeco(Material.BLACK_STAINED_GLASS_PANE, 9, 10, 11, 12, 14, 15, 16, 17, 18, 26, 27, 35, 36, 44, 45, 46, 47, 48, 49, 50, 51, 52, 53)
                .setDeco(Material.RED_STAINED_GLASS_PANE, 0, 1, 2, 3, 4, 5, 6, 7, 8);
        setMoveableItems(19, 20, 21, 22, 23, 24, 25, 28, 29, 30, 31, 32, 33, 34, 37, 38, 39, 40, 41, 42, 43);
        GuiListenerStorage storage = GuiListenerStorage.getInstance();
        storage.addListener(PUBLIC, publicMember);
        storage.addListener(MEMBER, member);
    }
    
    @Override
    public void onCreate(GuiCreateEvent<City> event){
        City city = event.getData();
        Gui<City> current = event.getGui();
        current.prepareChild(PUBLIC, ()-> publicMember.createGui(city, current));
        for(UUID uuid : city.getMembers()){
            String name = Bukkit.getOfflinePlayer(uuid).getName();
            addPlayer(event.getGui(), uuid, name == null ? "Pseudo" : name);
        }
    }
    
    @Override
    public void onPageCreate(PagedGuiCreateEvent<City> event){
        int page = event.getPage();
        if(page != 0){
            event.getPagedGui().setItem(getItem("§7Précédent", 47, Material.ARROW));
            event.getGui().getPage(page - 1).setItem(getItem("§7Suivant", 51, Material.ARROW));
        }
    }
    
    @Override
    public void onRemove(PagedGuiRemoveEvent<City> event){
        int page = event.getPage();
        if(page != 0){
            event.getGui().getPage(page - 1).setDeco(Material.BLACK_STAINED_GLASS_PANE, 51);
        }
    }
    
    @Override
    public void onClick(GuiClickEvent<City> event){
        City city = event.getData();
        ConsulatPlayer player = event.getPlayer();
        Gui<City> current = event.getGui();
        switch(event.getSlot()){
            case PUBLIC_PERMISSIONS_SLOT:
                current.getChild(PUBLIC).open(player);
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
        current.prepareChild(uuid, () -> member.createGui(uuid, current));
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
