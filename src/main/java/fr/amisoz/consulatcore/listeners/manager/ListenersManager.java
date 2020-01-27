package fr.amisoz.consulatcore.listeners.manager;

import fr.amisoz.consulatcore.ConsulatCore;
import fr.amisoz.consulatcore.listeners.entity.MobListeners;
import fr.amisoz.consulatcore.listeners.entity.player.*;
import fr.amisoz.consulatcore.listeners.world.SignListener;
import fr.amisoz.consulatcore.listeners.world.WeatherListener;
import org.bukkit.Bukkit;

public class ListenersManager {

    public ListenersManager(ConsulatCore consulatCore) {
        Bukkit.getPluginManager().registerEvents(new ConnectionListeners(consulatCore), consulatCore);
        Bukkit.getPluginManager().registerEvents(new ChatListeners(), consulatCore);
        Bukkit.getPluginManager().registerEvents(new InventoryListeners(consulatCore), consulatCore);
        Bukkit.getPluginManager().registerEvents(new InteractListener(consulatCore), consulatCore);
        Bukkit.getPluginManager().registerEvents(new DamageListener(), consulatCore);
        Bukkit.getPluginManager().registerEvents(new MobListeners(), consulatCore);
        Bukkit.getPluginManager().registerEvents(new MoveListeners(), consulatCore);
        Bukkit.getPluginManager().registerEvents(new ExperienceListener(), consulatCore);
        Bukkit.getPluginManager().registerEvents(new FoodListener(), consulatCore);
        Bukkit.getPluginManager().registerEvents(new SignListener(), consulatCore);
        Bukkit.getPluginManager().registerEvents(new WeatherListener(), consulatCore);
    }
}
