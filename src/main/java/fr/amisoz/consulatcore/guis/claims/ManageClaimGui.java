package fr.amisoz.consulatcore.guis.claims;

import fr.amisoz.consulatcore.Text;
import fr.amisoz.consulatcore.guis.city.CityGui;
import fr.amisoz.consulatcore.guis.city.claimlist.ClaimsGui;
import fr.amisoz.consulatcore.guis.claims.permissions.AccessPermissionsGui;
import fr.amisoz.consulatcore.zones.Zone;
import fr.amisoz.consulatcore.zones.cities.City;
import fr.amisoz.consulatcore.zones.claims.Claim;
import fr.leconsulat.api.gui.GuiContainer;
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
import fr.leconsulat.api.player.CPlayerManager;
import fr.leconsulat.api.player.ConsulatPlayer;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.jetbrains.annotations.Nullable;

import java.util.Iterator;
import java.util.Objects;
import java.util.UUID;

public class ManageClaimGui extends DataRelatPagedGui<Claim> {
    
    private static final byte MANAGE_INTERACT_SLOT = 2;
    private static final byte INFO_SLOT = 4;
    private static final byte ADD_SLOT = 6;
    
    public ManageClaimGui(Claim claim){
        super(claim, "<position>", 6,
                IGui.getItem("Interaction", MANAGE_INTERACT_SLOT, Material.LAVA_BUCKET),
                IGui.getItem("§eAccès", INFO_SLOT, Material.PAPER),
                IGui.getItem("§eAjouter un joueur", ADD_SLOT, Material.PLAYER_HEAD)
        );
        setDeco(Material.BLACK_STAINED_GLASS_PANE, 9, 10, 11, 12, 13, 14, 15, 16, 17, 18, 26, 27, 35, 36, 44, 46, 47, 48, 49, 50, 51, 52, 53);
        setDeco(Material.RED_STAINED_GLASS_PANE, 0, 1, 3, 5, 7, 8);
        setDynamicItems(19, 20, 21, 22, 23, 24, 25, 28, 29, 30, 31, 32, 33, 34, 37, 38, 39, 40, 41, 42, 43);
        setTemplateItems(0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16, 17, 18, 26, 27, 35, 36, 44, 46, 47, 48, 49, 50, 51, 52, 53);
    }
    
    @Override
    public void onCreate(){
        applyFather();
        Claim claim = getData();
        setManageInteractSlot(claim.isInteractSurrounding());
        for(UUID uuid : getData().getPlayers()){
            addPlayerToClaim(uuid, Bukkit.getOfflinePlayer(uuid).getName());
        }
    }
    
    @Override
    public void onPageCreated(GuiCreateEvent event, Pageable page){
        Claim claim = getData();
        IGui gui = page.getGui();
        gui.setName((claim.getX() << 4) + " " + (claim.getZ() << 4));
        if(page.getPage() != 0){
            gui.setItem(IGui.getItem("§7Précédent", 47, Material.ARROW));
            getPage(page.getPage() - 1).getGui().setItem(IGui.getItem("§7Suivant", 51, Material.ARROW));
            gui.setDeco(Material.BLACK_STAINED_GLASS_PANE, 51);
        }
    }
    
    @Override
    public void onPageClick(GuiClickEvent event, Pageable page){
        ConsulatPlayer player = event.getPlayer();
        GuiItem clickedItem = Objects.requireNonNull(page.getGui().getItem(event.getSlot()));
        switch(event.getSlot()){
            case 47:
                if(clickedItem.getType() == Material.ARROW){
                    getPage(page.getPage() - 1).getGui().open(player);
                }
                break;
            case 51:
                if(clickedItem.getType() == Material.ARROW){
                    getPage(page.getPage() + 1).getGui().open(player);
                }
                return;
            case MANAGE_INTERACT_SLOT:{
                Claim claim = getData();
                if(!claim.getOwner().isOwner(player.getUUID())){
                    return;
                }
                claim.setInteractSurrounding(!claim.isInteractSurrounding());
                setManageInteractSlot(claim.isInteractSurrounding());
            }
            return;
            case ADD_SLOT:{
                Zone zone = getData().getOwner();
                if(zone instanceof City){
                    if(!((City)zone).canManageAccesses(player.getUUID())){
                        return;
                    }
                } else if(!zone.isOwner(player.getUUID())){
                    return;
                }
                GuiManager.getInstance().userInput(event.getPlayer(), (input) -> {
                    UUID targetUUID = CPlayerManager.getInstance().getPlayerUUID(input);
                    if(targetUUID == null){
                        player.sendMessage(Text.PLAYER_DOESNT_EXISTS);
                        return;
                    }
                    if(zone instanceof City && !((City)zone).isMember(targetUUID)){
                        player.sendMessage(Text.PLAYER_DOESNT_BELONGS_CITY);
                        return;
                    }
                    if((targetUUID.equals(player.getUUID()) || targetUUID.equals(zone.getOwner()))){
                        player.sendMessage(Text.CANT_MANAGE_ACCESS_PLAYER);
                        return;
                    }
                    if(!getData().addPlayer(targetUUID)){
                        player.sendMessage(Text.PLAYER_ALREADY_ACCESS_CLAIM);
                        return;
                    }
                    player.sendMessage(Text.ADD_PLAYER_CLAIM(input));
                }, new String[]{"", "^^^^^^^^^^^^^^", "Entre le joueur", "à ajouter"}, 0);
            }
            return;
        }
        if(event.getSlot() >= 19 && event.getSlot() <= 44 && clickedItem.getType() == Material.PLAYER_HEAD){
            UUID uuid = player.getUUID();
            Zone zone = getData().getOwner();
            if((zone instanceof City ? !((City)zone).canManageAccesses(uuid) : !zone.isOwner(uuid)) &&
                    !uuid.equals(clickedItem.getAttachedObject())){
                return;
            }
            getChild(clickedItem.getAttachedObject()).getGui().open(player);
        }
    }
    
    @Override
    public void onPageOpened(GuiOpenEvent event, Pageable page){
        ConsulatPlayer player = event.getPlayer();
        UUID uuid = player.getUUID();
        Zone zone = getData().getOwner();
        updatePermissions(player, page);
        updateInteract(player, page, zone.isOwner(uuid));
        if(zone instanceof City){
            City city = (City)zone;
            updateAccess(player, page, city.canManageAccesses(uuid));
        } else {
            updateAccess(player, page, zone.isOwner(uuid));
        }
    }
    
    @Override
    public void onPageRemoved(GuiRemoveEvent event, Pageable page){
        if(page.getPage() != 0){
            getPage(page.getPage() - 1).getGui().setDeco(Material.BLACK_STAINED_GLASS_PANE, 51);
        }
    }
    
    @Override
    public Relationnable createChild(@Nullable Object key){
        if(key instanceof UUID){
            return new AccessPermissionsGui((UUID)key);
        }
        return super.createChild(key);
    }
    
    public void updatePermissions(ConsulatPlayer player, Pageable page){
        UUID uuid = player.getUUID();
        Zone zone = getData().getOwner();
        boolean allow = zone instanceof City ? ((City)zone).canManageAccesses(uuid) : zone.isOwner(uuid);
        for(GuiItem item : page){
            if(allow || uuid.equals(item.getAttachedObject())){
                setFakeItem(item.getSlot(), null, player);
            } else {
                setDescriptionPlayer(item.getSlot(), player, "", "§cTu ne peux pas", "§cgérer les permissions", "§cde ce joueur");
            }
        }
    }
    
    public void updateInteract(ConsulatPlayer player, Pageable page, boolean allow){
        if(allow){
            page.getGui().setFakeItem(MANAGE_INTERACT_SLOT, null, player);
        } else {
            setDescriptionPlayer(MANAGE_INTERACT_SLOT, player, GuiItem.getDescription(
                    page.getGui().getItem(MANAGE_INTERACT_SLOT),
                    "", "§cTu ne peux pas", "§cchanger l'interaction"));
        }
    }
    
    public void updateAccess(ConsulatPlayer player, Pageable page, boolean allow){
        if(allow){
            page.getGui().setFakeItem(ADD_SLOT, null, player);
        } else {
            setDescriptionPlayer(ADD_SLOT, player, "", "§cTu ne peux pas", "§cajouter un joueur");
        }
    }
    
    public void addPlayerToClaim(UUID uuid, String name){
        GuiItem item = IGui.getItem("§e" + name, -1, uuid);
        addItem(item);
        item.setAttachedObject(uuid);
    }
    
    public void removePlayerFromClaim(UUID uuid){
        removeChild(uuid);
        for(Iterator<GuiItem> iterator = iterator(); iterator.hasNext(); ){
            GuiItem item = iterator.next();
            if(item != null && item.getAttachedObject().equals(uuid)){
                iterator.remove();
                return;
            }
        }
    }
    
    public void applyFather(){
        City owner = getData().getOwner() instanceof City ? (City)getData().getOwner() : null;
        if(owner != null){
            IGui iClaimsGui = GuiManager.getInstance().getContainer("city").getGui(true, owner, CityGui.CLAIMS);
            if(iClaimsGui != null){
                setFather((ClaimsGui)iClaimsGui);
            }
            setDescription(INFO_SLOT, "", "§7Membres de la ville", "§7ayant accès au claim");
        } else {
            setFather(null);
            setDeco(Material.BLACK_STAINED_GLASS_PANE, 45);
            setDescription(INFO_SLOT, "", "§7Joueurs ayant accès", "§7à ce claim");
        }
    }
    
    private void setManageInteractSlot(boolean interaction){
        if(interaction){
            setTypePages(MANAGE_INTERACT_SLOT, Material.WATER_BUCKET);
            setDisplayNamePages(MANAGE_INTERACT_SLOT, "§aInteraction");
            setDescriptionPages(MANAGE_INTERACT_SLOT, "",
                    "§7Interdire les claims dont " + (getData().getOwner() instanceof City ? "la ville est" : "le joueur est"),
                    "§7propriétaire autour de ce claim",
                    "§7à interagir avec celui ci",
                    "§7§oExemple: écoulement d'eau", "", "§7Statut: §aActivé");
        } else {
            setTypePages(MANAGE_INTERACT_SLOT, Material.LAVA_BUCKET);
            setDisplayNamePages(MANAGE_INTERACT_SLOT, "§cInteraction");
            setDescriptionPages(MANAGE_INTERACT_SLOT, "",
                    "§7Autoriser les claims dont " + (getData().getOwner() instanceof City ? "la ville est" : "le joueur est"),
                    "§7propriétaire autour de ce claim",
                    "§7à interagir avec celui ci",
                    "§7§oExemple: écoulement d'eau", "", "§7Statut: §cDésactivé");
        }
    }
    
    public static class Container extends GuiContainer<Claim> {
        
        private static Container instance;
        
        public Container(){
            if(instance != null){
                throw new IllegalStateException();
            }
            instance = this;
            GuiManager.getInstance().addContainer("claim", this);
        }
        
        @Override
        public Datable<Claim> createGui(Claim claim){
            return new ManageClaimGui(claim);
        }
    }
    
}
