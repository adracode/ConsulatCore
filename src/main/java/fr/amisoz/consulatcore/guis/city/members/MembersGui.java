package fr.amisoz.consulatcore.guis.city.members;

import fr.amisoz.consulatcore.ConsulatCore;
import fr.amisoz.consulatcore.Text;
import fr.amisoz.consulatcore.guis.city.members.member.MemberGui;
import fr.amisoz.consulatcore.players.SurvivalPlayer;
import fr.amisoz.consulatcore.zones.ZoneManager;
import fr.amisoz.consulatcore.zones.cities.City;
import fr.amisoz.consulatcore.zones.cities.CityPlayer;
import fr.leconsulat.api.gui.GuiHeadItem;
import fr.leconsulat.api.gui.GuiItem;
import fr.leconsulat.api.gui.GuiManager;
import fr.leconsulat.api.gui.event.GuiClickEvent;
import fr.leconsulat.api.gui.event.GuiCreateEvent;
import fr.leconsulat.api.gui.event.GuiOpenEvent;
import fr.leconsulat.api.gui.event.GuiRemoveEvent;
import fr.leconsulat.api.gui.gui.IGui;
import fr.leconsulat.api.gui.gui.module.MainPageGui;
import fr.leconsulat.api.gui.gui.module.api.Pageable;
import fr.leconsulat.api.gui.gui.module.api.Relationnable;
import fr.leconsulat.api.gui.gui.template.DataRelatPagedGui;
import fr.leconsulat.api.player.CPlayerManager;
import fr.leconsulat.api.player.ConsulatPlayer;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Iterator;
import java.util.Objects;
import java.util.UUID;

public class MembersGui extends DataRelatPagedGui<City> {
    
    public static final String PUBLIC = "city.members.public";
    private static final byte ADD_SLOT = 6;
    private static final byte PUBLIC_PERMISSIONS_SLOT = 13;
    
    public MembersGui(City city){
        super(city, "Membres", 6,
                IGui.getItem("§eInviter un joueur", ADD_SLOT, Material.PLAYER_HEAD),
                IGui.getItem("§ePermissions publiques", PUBLIC_PERMISSIONS_SLOT, Material.BOOK, "", "§7Les permissions publiques", "§7définissent ce que les", "§7joueur n'appartenant pas", "§7à la ville peuvent faire"));
        setDeco(Material.BLACK_STAINED_GLASS_PANE, 9, 10, 11, 12, 14, 15, 16, 17, 18, 26, 27, 35, 36, 44, 46, 47, 48, 49, 50, 51, 52, 53);
        setDeco(Material.RED_STAINED_GLASS_PANE, 0, 1, 2, 3, 4, 5, 7, 8);
        setDynamicItems(19, 20, 21, 22, 23, 24, 25, 28, 29, 30, 31, 32, 33, 34, 37, 38, 39, 40, 41, 42, 43);
        setTemplateItems(0, 1, 2, 3, 4, 5, ADD_SLOT, 7, 8, 9, 10, 11, 12, PUBLIC_PERMISSIONS_SLOT, 14, 15, 16, 17, 18, 26, 27, 35, 36, 44, 46, 47, 48, 49, 50, 51, 52, 53);
        setSort((item1, item2) -> {
            if(item1.getAttachedObject() == null || item2.getAttachedObject() == null){
                return 0;
            }
            return ((CityPlayer)item1.getAttachedObject()).compareTo((CityPlayer)item2.getAttachedObject());
        });
    }
    
    @Override
    public void onCreate(){
        for(CityPlayer player : getData().getMembers()){
            addPlayer(player.getUUID());
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
            IGui gui = page.getGui();
            gui.setItem(IGui.getItem("§7Précédent", 47, Material.ARROW));
            getPage(page.getPage() - 1).getGui().setItem(IGui.getItem("§7Suivant", 51, Material.ARROW));
            gui.setDeco(Material.BLACK_STAINED_GLASS_PANE, 51);
        }
    }
    
    @Override
    public void onPageClick(GuiClickEvent event, Pageable page){
        City city = getData();
        ConsulatPlayer player = event.getPlayer();
        GuiItem clickedItem = Objects.requireNonNull(page.getGui().getItem(event.getSlot()));
        switch(event.getSlot()){
            case PUBLIC_PERMISSIONS_SLOT:
                getChild(PUBLIC).getGui().open(player);
                return;
            case 47:
                if(clickedItem.getType() == Material.ARROW){
                    getPage(page.getPage() - 1).getGui().open(player);
                }
                return;
            case 51:
                if(clickedItem.getType() == Material.ARROW){
                    getPage(page.getPage() + 1).getGui().open(player);
                }
                return;
            case ADD_SLOT:{
                if(!city.canInvite(player.getUUID())){
                    return;
                }
                GuiManager.getInstance().userInput(event.getPlayer(), input -> {
                    UUID targetUUID = CPlayerManager.getInstance().getPlayerUUID(input);
                    if(targetUUID == null){
                        player.sendMessage(Text.PLAYER_DOESNT_EXISTS);
                        return;
                    }
                    SurvivalPlayer target = (SurvivalPlayer)CPlayerManager.getInstance().getConsulatPlayer(targetUUID);
                    if(target == null){
                        player.sendMessage(Text.PLAYER_NOT_CONNECTED);
                        return;
                    }
                    if(target.belongsToCity()){
                        player.sendMessage(Text.PLAYER_ALREADY_BELONGS_CITY);
                        return;
                    }
                    if(!ZoneManager.getInstance().invitePlayer(city, targetUUID)){
                        player.sendMessage(Text.ALREADY_INVITED_CITY);
                        return;
                    }
                    player.sendMessage(Text.YOU_INVITED_PLAYER_TO_CITY(target.getName(), city.getName()));
                    city.sendMessage(Text.HAS_INVITED_PLAYER_TO_CITY(city, player.getName(), target.getName()));
                    target.sendMessage(Text.YOU_BEEN_INVITED_TO_CITY(city.getName(), player.getName()));
                    Bukkit.getScheduler().runTask(ConsulatCore.getInstance(), () -> {
                        open(player);
                    });
                }, new String[]{"", "^^^^^^^^^^^^^^", "Entre le nom", "du joueur"}, 0);
            }
            return;
        }
        if(event.getSlot() >= 19 && event.getSlot() <= 44 && clickedItem.getType() == Material.PLAYER_HEAD){
            if(!city.isOwner(player.getUUID()) && !player.getUUID().equals(((CityPlayer)clickedItem.getAttachedObject()).getUUID())){
                player.sendActionBar(Text.CANT_CHANGE_PERMISSION);
                return;
            }
            UUID playerUUID = ((CityPlayer)clickedItem.getAttachedObject()).getUUID();
            getChild(playerUUID).getGui().open(player);
        }
    }
    
    @Override
    public void onPageOpened(GuiOpenEvent event, Pageable pageGui){
        update(event.getPlayer(), pageGui);
    }
    
    @Override
    public void onPageRemoved(GuiRemoveEvent event, Pageable page){
        if(page.getPage() != 0){
            getPage(page.getPage() - 1).getGui().setDeco(Material.BLACK_STAINED_GLASS_PANE, 51);
        }
    }
    
    public void update(ConsulatPlayer player, Pageable pageGui){
        UUID uuid = player.getUUID();
        City city = getData();
        IGui gui = pageGui.getGui();
        if(!city.canInvite(uuid)){
            gui.setDescriptionPlayer(ADD_SLOT, player, "", "§cTu ne peux pas", "§cinviter un joueur");
        } else {
            gui.removeFakeItem(ADD_SLOT, player);
        }
        for(GuiItem item : this){
            if(!city.isOwner(player.getUUID()) && !player.getUUID().equals(((CityPlayer)item.getAttachedObject()).getUUID())){
                gui.setDescriptionPlayer(item.getSlot(), player, GuiItem.getDescription(item, "", "§cTu ne peux pas modifier", "§cce membre"));
            } else {
                gui.removeFakeItem(item.getSlot(), player);
            }
        }
    }
    
    public void updateRanks(){
        for(MainPageGui<?>.GuiIterator iterator = (MainPageGui<?>.GuiIterator)this.iterator(); iterator.hasNext(); ){
            GuiItem item = iterator.next();
            getPage(iterator.getPage()).getGui().setDescription(item.getSlot(), "", "§7Grade: §b" + ((CityPlayer)item.getAttachedObject()).getRank().getRankName());
        }
        refreshItems();
    }
    
    public void addPlayer(@NotNull UUID uuid){
        CityPlayer player = getData().getCityPlayer(uuid);
        //Création de la tête du joueur
        GuiHeadItem item = IGui.getItem(this, "§e%s", -1, uuid,
                "", "§7Grade: §b" + player.getRank().getRankName());
        item.onUpdate(head -> refresh());
        //L'UUID du joueur est placé sur l'item
        item.setAttachedObject(player);
        //La tête est ajouté au PagedGui répertoriant les membres
        addItem(item);
    }
    
    public void removePlayer(@NotNull UUID uuid){
        removeChild(uuid);
        for(Iterator<GuiItem> iterator = iterator(); iterator.hasNext(); ){
            GuiItem item = iterator.next();
            if(item != null && ((CityPlayer)item.getAttachedObject()).getUUID().equals(uuid)){
                iterator.remove();
                return;
            }
        }
    }
}
