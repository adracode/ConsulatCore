package fr.amisoz.consulatcore.guis.city;

import fr.amisoz.consulatcore.zones.cities.City;
import fr.amisoz.consulatcore.zones.cities.CityPlayer;
import fr.leconsulat.api.gui.Gui;
import fr.leconsulat.api.gui.GuiContainer;
import fr.leconsulat.api.gui.PagedGui;
import fr.leconsulat.api.gui.events.GuiCreateEvent;
import fr.leconsulat.api.gui.events.PagedGuiCreateEvent;
import fr.leconsulat.api.gui.events.PagedGuiRemoveEvent;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;

import java.util.UUID;

public class CityInfo extends GuiContainer<City> {
    
    private static final byte CITY_SLOT = 4;
    private static final byte HOME_SLOT = 6;
    
    public CityInfo(){
        super(6);
        setTemplate("<ville>",
                getItem("§e<ville>", CITY_SLOT, Material.PAPER),
                getItem("§eHome", HOME_SLOT, Material.COMPASS)
        )
                .setDeco(Material.BLACK_STAINED_GLASS_PANE, 9, 10, 11, 12, 13, 14, 15, 16, 17, 18, 26, 27, 35, 36, 44, 45, 46, 47, 48, 49, 50, 51, 52, 53)
                .setDeco(Material.RED_STAINED_GLASS_PANE, 0, 1, 2, 3, 5, 7, 8);
        setMoveableItems(19, 20, 21, 22, 23, 24, 25, 28, 29, 30, 31, 32, 33, 34, 37, 38, 39, 40, 41, 42, 43);
    }
    
    @Override
    public void onCreate(GuiCreateEvent<City> event){
        City city = event.getData();
        for(UUID uuid : city.getMembers()){
            CityPlayer player = city.getCityPlayer(uuid);
            event.getGui().addItem(getItem("§e" + Bukkit.getOfflinePlayer(uuid).getName(), -1, uuid,
                    "", "§7Grade: §b" + (player.getRank() == null ? "§cAucun" : player.getRank().getRankName())));
        }
    }
    
    @Override
    public void onPageCreate(PagedGuiCreateEvent<City> event){
        City city = event.getData();
        event.getPagedGui().setDescription(CITY_SLOT, "§7Propriétaire: §a" + Bukkit.getOfflinePlayer(city.getOwner()).getName());
        updateHome(event.getPagedGui());
        updateName(event.getPagedGui());
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
    
    public void updateName(City city){
        Gui<City> gui = getGui(city);
        for(PagedGui<City> pagedGui : gui.getPagedGuis()){
            updateName(pagedGui);
        }
    }
    
    private void updateName(PagedGui<City> current){
        City city = current.getGui().getData();
        current.setDisplayName(CITY_SLOT, city.getName());
        current.setName("§e" + city.getName());
    }
    
    public void updateHome(City city){
        Gui<City> gui = getGui(city);
        for(PagedGui<City> pagedGui : gui.getPagedGuis()){
            updateHome(pagedGui);
        }
    }
    
    private void updateHome(PagedGui<City> current){
        City city = current.getGui().getData();
        if(city.hasHome()){
            Location home = city.getHome();
            current.setDescription(HOME_SLOT, "",
                    "§7x: " + home.getBlockX(), "§7y: " + home.getBlockY(), "§7z: " + home.getBlockZ());
        } else {
            current.setDescription(HOME_SLOT, "", "§cAucun");
        }
    }
    
}
