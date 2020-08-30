package fr.leconsulat.core.guis.city;

import fr.leconsulat.api.gui.GuiContainer;
import fr.leconsulat.api.gui.GuiItem;
import fr.leconsulat.api.gui.GuiManager;
import fr.leconsulat.api.gui.event.GuiClickEvent;
import fr.leconsulat.api.gui.event.GuiCreateEvent;
import fr.leconsulat.api.gui.event.GuiRemoveEvent;
import fr.leconsulat.api.gui.gui.IGui;
import fr.leconsulat.api.gui.gui.module.MainPageGui;
import fr.leconsulat.api.gui.gui.module.api.Datable;
import fr.leconsulat.api.gui.gui.module.api.Pageable;
import fr.leconsulat.api.gui.gui.template.DataPagedGui;
import fr.leconsulat.core.zones.cities.City;
import fr.leconsulat.core.zones.cities.CityPlayer;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;

import java.util.Iterator;
import java.util.Objects;
import java.util.UUID;

public class CityInfo extends DataPagedGui<City> {
    
    private static final byte CITY_SLOT = 4;
    private static final byte HOME_SLOT = 6;
    
    public CityInfo(City city){
        super(city, "<ville>", 6,
                IGui.getItem("<ville>", CITY_SLOT, Material.PAPER),
                IGui.getItem("§eHome", HOME_SLOT, Material.COMPASS));
        setDeco(Material.BLACK_STAINED_GLASS_PANE, 9, 10, 11, 12, 13, 14, 15, 16, 17, 18, 26, 27, 35, 36, 44, 45, 46, 47, 48, 49, 50, 51, 52, 53);
        setDeco(Material.RED_STAINED_GLASS_PANE, 0, 1, 2, 3, 5, 7, 8);
        setDynamicItems(19, 20, 21, 22, 23, 24, 25, 28, 29, 30, 31, 32, 33, 34, 37, 38, 39, 40, 41, 42, 43);
        setTemplateItems(0, 1, 2, 3, CITY_SLOT, 5, HOME_SLOT, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16, 17, 18, 26, 27, 35, 36, 44, 45, 46, 47, 48, 49, 50, 51, 52, 53);
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
    public void onPageCreated(GuiCreateEvent event, Pageable pageGui){
        updateOwner(pageGui.getGui());
        updateHome(pageGui.getGui());
        updateName(pageGui.getGui());
        int page = pageGui.getPage();
        if(page != 0){
            pageGui.getGui().setItem(IGui.getItem("§7Précédent", 47, Material.ARROW));
            getPage(page - 1).getGui().setItem(IGui.getItem("§7Suivant", 51, Material.ARROW));
            pageGui.getGui().setDeco(Material.BLACK_STAINED_GLASS_PANE, 51);
        }
    }
    
    @Override
    public void onPageRemoved(GuiRemoveEvent event, Pageable pageGui){
        int page = pageGui.getPage();
        if(page != 0){
            getPage(page - 1).getGui().setDeco(Material.BLACK_STAINED_GLASS_PANE, 51);
        }
    }
    
    @Override
    public void onPageClick(GuiClickEvent event, Pageable page){
        switch(event.getSlot()){
            case 47:{
                GuiItem clickedItem = Objects.requireNonNull(page.getGui().getItem(event.getSlot()));
                if(clickedItem.getType() == Material.ARROW){
                    getPage(page.getPage() - 1).getGui().open(event.getPlayer());
                }
            }
            break;
            case 51:{
                GuiItem clickedItem = Objects.requireNonNull(page.getGui().getItem(event.getSlot()));
                if(clickedItem.getType() == Material.ARROW){
                    getPage(page.getPage() + 1).getGui().open(event.getPlayer());
                }
            }
            break;
        }
    }
    
    public void addPlayer(UUID uuid){
        City city = getData();
        CityPlayer player = city.getCityPlayer(uuid);
        GuiItem item = IGui.getItem(this, "§e%s", -1, uuid,
                "", "§7Grade: §b" + player.getRank().getRankName());
        item.setAttachedObject(player);
        addItem(item);
    }
    
    public void removePlayer(UUID uuid){
        for(Iterator<GuiItem> iterator = iterator(); iterator.hasNext(); ){
            GuiItem item = iterator.next();
            if(item != null && ((CityPlayer)item.getAttachedObject()).getUUID().equals(uuid)){
                iterator.remove();
                return;
            }
        }
    }
    
    public void updateName(IGui page){
        City city = getData();
        if(page == null){
            for(Pageable pageable : getPages()){
                IGui gui = pageable.getGui();
                gui.setDisplayName(CITY_SLOT, "§e" + city.getName());
                gui.setName(city.getName());
            }
        } else {
            page.setDisplayName(CITY_SLOT, "§e" + city.getName());
            page.setName(city.getName());
        }
    }
    
    public void updateHome(IGui page){
        City city = getData();
        if(city.hasHome()){
            Location home = city.getHome();
            if(page == null){
                for(Pageable pageable : getPages())
                    pageable.getGui().setDescription(HOME_SLOT, "",
                            "§7x: " + home.getBlockX(), "§7y: " + home.getBlockY(), "§7z: " + home.getBlockZ());
            } else {
                page.setDescription(HOME_SLOT, "",
                        "§7x: " + home.getBlockX(), "§7y: " + home.getBlockY(), "§7z: " + home.getBlockZ());
            }
        } else {
            if(page == null){
                for(Pageable pageable : getPages()){
                    pageable.getGui().setDescription(HOME_SLOT, "", "§cAucun");
                }
            } else {
                page.setDescription(HOME_SLOT, "", "§cAucun");
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
    
    public void updateOwner(IGui page){
        if(page == null){
            setDescriptionPages(CITY_SLOT, "§7Propriétaire: §a" + Bukkit.getOfflinePlayer(getData().getOwner()).getName());
        } else {
            setDescription(CITY_SLOT, "§7Propriétaire: §a" + Bukkit.getOfflinePlayer(getData().getOwner()).getName());
        }
    }
    
    public static class Container extends GuiContainer<City> {
        
        private static Container instance;
        
        public Container(){
            if(instance != null){
                throw new IllegalStateException();
            }
            instance = this;
            GuiManager.getInstance().addContainer("city-info", this);
        }
        
        @Override
        public Datable<City> createGui(City city){
            return new CityInfo(city);
        }
    }
    
}
