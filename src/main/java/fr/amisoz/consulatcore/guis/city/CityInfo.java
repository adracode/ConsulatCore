package fr.amisoz.consulatcore.guis.city;

import fr.amisoz.consulatcore.zones.cities.City;
import fr.amisoz.consulatcore.zones.cities.CityPlayer;
import fr.leconsulat.api.gui.GuiContainer;
import fr.leconsulat.api.gui.GuiItem;
import fr.leconsulat.api.gui.GuiManager;
import fr.leconsulat.api.gui.event.GuiCreateEvent;
import fr.leconsulat.api.gui.event.GuiRemoveEvent;
import fr.leconsulat.api.gui.gui.IGui;
import fr.leconsulat.api.gui.gui.module.api.Datable;
import fr.leconsulat.api.gui.gui.module.api.Pageable;
import fr.leconsulat.api.gui.gui.template.DataPagedGui;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;

import java.util.Iterator;
import java.util.UUID;

public class CityInfo extends DataPagedGui<City> {
    
    private static final byte CITY_SLOT = 4;
    private static final byte HOME_SLOT = 6;
    
    public CityInfo(City city){
        super(city, "<ville>",6,
                IGui.getItem("§e<ville>", CITY_SLOT, Material.PAPER),
                IGui.getItem("§eHome", HOME_SLOT, Material.COMPASS));
        setDeco(Material.BLACK_STAINED_GLASS_PANE, 9, 10, 11, 12, 13, 14, 15, 16, 17, 18, 26, 27, 35, 36, 44, 45, 46, 47, 48, 49, 50, 51, 52, 53);
        setDeco(Material.RED_STAINED_GLASS_PANE, 0, 1, 2, 3, 5, 7, 8);
        setDynamicItems(19, 20, 21, 22, 23, 24, 25, 28, 29, 30, 31, 32, 33, 34, 37, 38, 39, 40, 41, 42, 43);
    }
    
    @Override
    public void onCreate(){
        for(UUID uuid : getData().getMembers()){
            addPlayer(uuid);
        }
    }
    
    @Override
    public void onPageCreated(GuiCreateEvent event, Pageable pageGui){
        City city = getData();
        setDescription(CITY_SLOT, "§7Propriétaire: §a" + Bukkit.getOfflinePlayer(city.getOwner()).getName());
        updateHome();
        updateName();
        int page = pageGui.getPage();
        if(page != 0){
            setItem(IGui.getItem("§7Précédent", 47, Material.ARROW));
            getPage(page - 1).setItem(IGui.getItem("§7Suivant", 51, Material.ARROW));
        }
    }
    
    @Override
    public void onPageRemoved(GuiRemoveEvent event, Pageable pageGui){
        int page = pageGui.getPage();
        if(page != 0){
            getPage(page - 1).setDeco(Material.BLACK_STAINED_GLASS_PANE, 51);
        }
    }
    
    public void addPlayer(UUID uuid){
        City city = getData();
        CityPlayer player = city.getCityPlayer(uuid);
        GuiItem item = IGui.getItem("§e" + Bukkit.getOfflinePlayer(uuid).getName(), -1, uuid,
                "", "§7Grade: §b" + player.getRank().getRankName());
        addItem(item);
        item.setAttachedObject(player);
    }
    
    public void removePlayer(UUID uuid){
        //PagedGui pour gérer les accès
        for(Iterator<GuiItem> iterator = iterator(); iterator.hasNext(); ){
            GuiItem item = iterator.next();
            if(item != null && ((CityPlayer)item.getAttachedObject()).getUUID().equals(uuid)){
                iterator.remove();
                return;
            }
        }
    }
    
    public void updateName(){
        City city = getData();
        for(Pageable page : getPages()){
            page.setDisplayName(CITY_SLOT, "§e" + city.getName());
            page.setName("§e" + city.getName());
        }
    }
    
    public void updateHome(){
        City city = getData();
            if(city.hasHome()){
                Location home = city.getHome();
                for(Pageable page : getPages())
                    page.setDescription(HOME_SLOT, "",
                        "§7x: " + home.getBlockX(), "§7y: " + home.getBlockY(), "§7z: " + home.getBlockZ());
            } else {
                for(Pageable page : getPages()){
                    page.setDescription(HOME_SLOT, "", "§cAucun");
                }
            }
    }
    
    public void updateRanks(){
        for(GuiItem item : this){
            item.setDescription("", "§7Grade: §b" + ((CityPlayer)item.getAttachedObject()).getRank().getRankName());
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
