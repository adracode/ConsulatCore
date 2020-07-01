package fr.amisoz.consulatcore.guis.claims;

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
import java.util.UUID;

public class ManageClaimGui extends DataRelatPagedGui<Claim> {
    
    private static final byte INFO_SLOT = 4;
    private static final byte ADD_SLOT = 6;
    
    public ManageClaimGui(Claim claim){
        super(claim, "<position>", 6,
                IGui.getItem("§eAccès", INFO_SLOT, Material.PAPER),
                IGui.getItem("§eAjouter un joueur", ADD_SLOT, Material.PLAYER_HEAD)
        );
        setDeco(Material.BLACK_STAINED_GLASS_PANE, 9, 10, 11, 12, 13, 14, 15, 16, 17, 18, 26, 27, 35, 36, 44, 46, 47, 48, 49, 50, 51, 52, 53);
        setDeco(Material.RED_STAINED_GLASS_PANE, 0, 1, 2, 3, 5, 7, 8);
        setDynamicItems(19, 20, 21, 22, 23, 24, 25, 28, 29, 30, 31, 32, 33, 34, 37, 38, 39, 40, 41, 42, 43);
    }
    
    @Override
    public void onCreate(){
        applyFather();
        for(UUID uuid : getData().getPlayers()){
            addPlayerToClaim(uuid, Bukkit.getOfflinePlayer(uuid).getName());
        }
    }
    
    @Override
    public void onPageCreated(GuiCreateEvent event, Pageable page){
        Claim claim = getData();
        page.setName((claim.getX() << 4) + " " + (claim.getZ() << 4));
        if(page.getPage() != 0){
            page.setItem(IGui.getItem("§7Précédent", 47, Material.ARROW));
            getPage(page.getPage() - 1).setItem(IGui.getItem("§7Suivant", 51, Material.ARROW));
        }
    }
    
    @Override
    public void onPageRemoved(GuiRemoveEvent event, Pageable page){
        if(page.getPage() != 0){
            getPage(page.getPage() - 1).setDeco(Material.BLACK_STAINED_GLASS_PANE, 51);
        }
    }
    
    @Override
    public void onPageClick(GuiClickEvent event, Pageable page){
        ConsulatPlayer player = event.getPlayer();
        GuiItem clickedItem = page.getItem(event.getSlot());
        switch(event.getSlot()){
            case 47:
                if(clickedItem.getType() == Material.ARROW){
                    getPage(page.getPage() - 1).open(player);
                }
                break;
            case 51:
                if(clickedItem.getType() == Material.ARROW){
                    getPage(page.getPage() + 1).open(player);
                }
                return;
            case ADD_SLOT:
                GuiManager.getInstance().userInput(event.getPlayer().getPlayer(), (input) -> {
                    UUID targetUUID = CPlayerManager.getInstance().getPlayerUUID(input);
                    if(targetUUID == null){
                        player.sendMessage("§cCe joueur n'existe pas.");
                        return;
                    }
                    Zone zone = getData().getOwner();
                    if(zone instanceof City && !((City)zone).isMember(targetUUID)){
                        player.sendMessage("§cCe joueur n'est pas membre de la ville");
                        return;
                    }
                    if(targetUUID.equals(player.getUUID()) || targetUUID.equals(zone.getOwner())){
                        player.sendMessage("§cTu ne peux pas modifier l'accès de ce joueur.");
                        return;
                    }
                    if(!getData().addPlayer(targetUUID)){
                        player.sendMessage("§cCe joueur a déjà accès à ce claim.");
                        return;
                    }
                    player.sendMessage("§aTu as ajouté " + input + " à ce claim");
                }, new String[]{"", "^^^^^^^^^^^^^^", "Entrez le joueur", "à ajouter"}, 0);
                return;
        }
        if(event.getSlot() >= 19 && event.getSlot() <= 44 && clickedItem.getType() == Material.PLAYER_HEAD){
            getChild(clickedItem.getAttachedObject()).open(player);
        }
    }
    
    public void addPlayerToClaim(UUID uuid, String name){
        GuiItem item = IGui.getItem("§e" + name, -1, uuid);
        addItem(item);
        item.setAttachedObject(uuid);
    }
    
    @Override
    public Relationnable createChild(@Nullable Object key){
        if(key instanceof UUID){
            return new AccessPermissionsGui((UUID)key);
        }
        return super.createChild(key);
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
