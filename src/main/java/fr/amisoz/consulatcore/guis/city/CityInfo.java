package fr.amisoz.consulatcore.guis.city;

import fr.amisoz.consulatcore.zones.cities.City;
import fr.leconsulat.api.gui.GuiContainer;
import fr.leconsulat.api.gui.events.PagedGuiCreateEvent;
import org.bukkit.Bukkit;
import org.bukkit.Material;

import java.util.UUID;

public class CityInfo extends GuiContainer<City> {

    public CityInfo(){
        super(6);
        setTemplate("Ville",
                getItem("§eDescription", 0, Material.PAPER)
        );
        setMoveableItems(1, 9);
    }

    @Override
    public void onPageCreate(PagedGuiCreateEvent<City> event){
        City city = event.getData();
        event.getPagedGui().setName(city.getName());
        event.getPagedGui().setDescription(0, "§7" + city.getDescription());
        for(UUID uuid : city.getMembers()){
            event.getGui().addItem(getItem("§e" + Bukkit.getOfflinePlayer(uuid).getName(), -1, uuid));
        }
    }
}
