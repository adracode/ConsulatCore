package fr.amisoz.consulatcore.guis.city.members;

import fr.amisoz.consulatcore.guis.city.members.member.MemberGui;
import fr.amisoz.consulatcore.players.SurvivalPlayer;
import fr.amisoz.consulatcore.zones.ZoneManager;
import fr.amisoz.consulatcore.zones.cities.City;
import fr.amisoz.consulatcore.zones.cities.CityPlayer;
import fr.leconsulat.api.ConsulatAPI;
import fr.leconsulat.api.gui.GuiItem;
import fr.leconsulat.api.gui.GuiManager;
import fr.leconsulat.api.gui.event.GuiClickEvent;
import fr.leconsulat.api.gui.event.GuiCreateEvent;
import fr.leconsulat.api.gui.event.GuiOpenEvent;
import fr.leconsulat.api.gui.event.GuiRemoveEvent;
import fr.leconsulat.api.gui.gui.IGui;
import fr.leconsulat.api.gui.gui.module.api.Pageable;
import fr.leconsulat.api.gui.gui.module.api.Relationnable;
import fr.leconsulat.api.gui.gui.template.DataRelatPagedGui;
import fr.leconsulat.api.player.CPlayerManager;
import fr.leconsulat.api.player.ConsulatPlayer;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.ComponentBuilder;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Iterator;
import java.util.Objects;
import java.util.UUID;

public class MembersGui extends DataRelatPagedGui<City> {
    
    private static final byte ADD_SLOT = 6;
    private static final byte PUBLIC_PERMISSIONS_SLOT = 13;
    
    public static final String PUBLIC = "city.members.public";
    
    public MembersGui(City city){
        super(city, "Membres", 6,
                IGui.getItem("§eInviter un joueur", ADD_SLOT, Material.PLAYER_HEAD),
                IGui.getItem("§ePermissions publiques", PUBLIC_PERMISSIONS_SLOT, Material.BOOK));
        setDeco(Material.BLACK_STAINED_GLASS_PANE, 9, 10, 11, 12, 14, 15, 16, 17, 18, 26, 27, 35, 36, 44, 46, 47, 48, 49, 50, 51, 52, 53);
        setDeco(Material.RED_STAINED_GLASS_PANE, 0, 1, 2, 3, 4, 5, 7, 8);
        setDynamicItems(19, 20, 21, 22, 23, 24, 25, 28, 29, 30, 31, 32, 33, 34, 37, 38, 39, 40, 41, 42, 43);
    }
    
    @Override
    public void onCreate(){
        for(CityPlayer player : getData().getMembers()){
            String name = Bukkit.getOfflinePlayer(player.getUUID()).getName();
            addPlayer(player.getUUID(), name == null ? "Pseudo" : name);
        }
    }
    
    @Override
    public void onPageOpened(GuiOpenEvent event, Pageable pageGui){
        ConsulatPlayer player = event.getPlayer();
        UUID uuid = player.getUUID();
        UUID ownerUUID = getData().getOwner();
        for(GuiItem item : this){
            if(uuid.equals(item.getAttachedObject()) || ownerUUID.equals(item.getAttachedObject())){
                pageGui.setDescriptionPlayer(item.getSlot(), player, "", "§cVous ne pouvez pas modifier", "§cce membre");
            }
        }
    }
    
    @Override
    public Relationnable createChild(@Nullable Object key){
        if(key instanceof UUID){
            return new MemberGui((UUID)key);
        } else if(PUBLIC.equals(key)){
            return new PublicPermissionsGui(getData());
        }
        return super.createChild(key);
    }
    
    @Override
    public void onPageCreated(GuiCreateEvent event, Pageable page){
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
        City city = getData();
        ConsulatPlayer player = event.getPlayer();
        GuiItem clickedItem = Objects.requireNonNull(page.getItem(event.getSlot()));
        switch(event.getSlot()){
            case PUBLIC_PERMISSIONS_SLOT:
                getChild(PUBLIC).open(player);
                return;
            case 45:
                if(clickedItem.getType() == Material.ARROW){
                    getPage(page.getPage() - 1).open(player);
                }
                break;
            case 53:
                if(clickedItem.getType() == Material.ARROW){
                    getPage(page.getPage() + 1).open(player);
                }
                return;
                //TODO: si n'a pas la perm
            case ADD_SLOT:{
                GuiManager.getInstance().userInput(event.getPlayer(), input -> {
                    UUID targetUUID = CPlayerManager.getInstance().getPlayerUUID(input);
                    if(targetUUID == null){
                        player.sendMessage("§cCe joueur n'existe pas.");
                        return;
                    }
                    SurvivalPlayer target = (SurvivalPlayer)CPlayerManager.getInstance().getConsulatPlayer(targetUUID);
                    if(target == null){
                        player.sendMessage("§cCe joueur n'est pas connecté.");
                        return;
                    }
                    if(target.belongsToCity()){
                        player.sendMessage("§cCe joueur est déjà dans une ville.");
                        return;
                    }
                    if(!ZoneManager.getInstance().invitePlayer(city, targetUUID)){
                        player.sendMessage("§cCe joueur est déjà invité dans la ville.");
                        return;
                    }
                    player.sendMessage("§aTu as invité §7" + target.getName() + " §a à rejoindre la ville §7" + city.getName() + "§a.");
                    city.sendMessage("§a" + player.getName() + "§7 a invité §a" + target.getName() + "§7.");
                    TextComponent message = new TextComponent("§aTu as été invité à rejoindre la ville §7" + city.getName() + "§a par §7" + player.getName() + "§a.");
                    message.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new ComponentBuilder("§aClique pour rejoindre " + city.getName()).create()));
                    message.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/city accept " + city.getName()));
                    target.sendMessage(message);
                }, new String[]{"", "^^^^^^^^^^^^^^", "Entrez le nom", "du joueur"}, 0);
            }
            return;
        }
        if(event.getSlot() >= 19 && event.getSlot() <= 44 && clickedItem.getType() == Material.PLAYER_HEAD){
            if(!ConsulatAPI.getConsulatAPI().isDebug() && (city.getOwner().equals(clickedItem.getAttachedObject()) || player.getUUID().equals(clickedItem.getAttachedObject()))){
                player.sendMessage("§cLes permissions du proriétaire de la ville ne peuvent pas changer.");
                return;
            }
            UUID playerUUID = (UUID)clickedItem.getAttachedObject();
            getChild(playerUUID).open(player);
        }
    }
    
    public void addPlayer(@NotNull UUID uuid, @NotNull String name){
        //Création de la tête du joueur
        GuiItem item = IGui.getItem("§e" + name, -1, uuid);
        //L'UUID du joueur est placé sur l'item
        item.setAttachedObject(uuid);
        //La tête est ajouté au PagedGui répertoriant les membres
        addItem(item);
    }
    
    public void removePlayer(@NotNull UUID uuid){
        removeChild(uuid);
        for(Iterator<GuiItem> iterator = iterator(); iterator.hasNext(); ){
            GuiItem item = iterator.next();
            if(item != null && item.getAttachedObject().equals(uuid)){
                iterator.remove();
                return;
            }
        }
    }
}
