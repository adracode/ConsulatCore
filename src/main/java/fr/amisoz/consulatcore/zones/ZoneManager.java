package fr.amisoz.consulatcore.zones;

import fr.amisoz.consulatcore.ConsulatCore;
import fr.amisoz.consulatcore.guis.city.CityGui;
import fr.amisoz.consulatcore.guis.city.CityInfo;
import fr.amisoz.consulatcore.guis.city.DisbandGui;
import fr.amisoz.consulatcore.zones.cities.City;
import fr.amisoz.consulatcore.zones.claims.ClaimManager;
import fr.leconsulat.api.ConsulatAPI;
import fr.leconsulat.api.database.SaveManager;
import fr.leconsulat.api.database.tasks.SaveTask;
import fr.leconsulat.api.gui.GuiManager;
import fr.leconsulat.api.gui.gui.IGui;
import fr.leconsulat.api.utils.FileUtils;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.util.*;
import java.util.logging.Level;

public class ZoneManager {
    
    private static ZoneManager instance;
    
    private final @NotNull Map<UUID, Zone> zones = new HashMap<>();
    private final @NotNull Map<String, City> citiesByName = new HashMap<>();
    private final @NotNull Map<UUID, Set<City>> invitedPlayers = new HashMap<>();
    
    public ZoneManager(){
        if(instance != null){
            throw new IllegalStateException("ZoneManager is already instantiated");
        }
        instance = this;
        SaveManager saveManager = SaveManager.getInstance();
        saveManager.addSaveTask("city-money", new SaveTask<>(
                "UPDATE cities SET money = ? WHERE uuid = ?",
                (statement, city) -> {
                    statement.setDouble(1, city.getMoney());
                    statement.setString(2, city.getUniqueId().toString());
                },
                City::getMoney
        ));
        saveManager.addSaveTask("city-name", new SaveTask<>(
                "UPDATE cities SET name = ? WHERE uuid = ?",
                (statement, city) -> {
                    statement.setString(1, city.getName());
                    statement.setString(2, city.getUniqueId().toString());
                },
                City::getName
        ));
        new CityGui.Container();
        new CityInfo.Container();
        new DisbandGui.Container();
        try {
            initZones();
            initCities();
        } catch(SQLException e){
            e.printStackTrace();
            Bukkit.shutdown();
        }
    }
    
    private void initZones() throws SQLException{
        PreparedStatement fetchZones = ConsulatAPI.getDatabase().prepareStatement("SELECT * FROM zones");
        ResultSet result = fetchZones.executeQuery();
        while(result.next()){
            Zone zone = new Zone(
                    UUID.fromString(result.getString("uuid")),
                    result.getString("name"),
                    UUID.fromString(result.getString("owner"))
            );
            zone.loadNBT();
            addZone(zone);
        }
    }
    
    private void initCities() throws SQLException{
        PreparedStatement fetchCities = ConsulatAPI.getDatabase().prepareStatement("SELECT * FROM cities");
        ResultSet result = fetchCities.executeQuery();
        while(result.next()){
            City city = new City(
                    UUID.fromString(result.getString("uuid")),
                    result.getString("name"),
                    UUID.fromString(result.getString("owner")),
                    result.getDouble("money")
            );
            city.loadNBT();
            addCity(city);
        }
    }
    
    public void addZone(Zone zone){
        zones.put(zone.getUniqueId(), zone);
    }
    
    private void addCity(City city){
        citiesByName.put(city.getName().toLowerCase(), city);
        SaveManager.getInstance().addData("city-money", city);
        addZone(city);
    }
    
    public void removeZone(Zone zone){
        zones.remove(zone.getUniqueId());
    }
    
    public void removeCity(City city){
        citiesByName.remove(city.getName().toLowerCase());
        SaveManager.getInstance().removeData("city-money", city, false);
        removeZone(city);
    }
    
    public static ZoneManager getInstance(){
        return instance;
    }
    
    public boolean invitePlayer(City city, UUID uuid){
        if(ConsulatAPI.getConsulatAPI().isDebug()){
            ConsulatAPI.getConsulatAPI().log(Level.INFO, "Player " + uuid + " is invited to " + city.getName());
        }
        return invitedPlayers.computeIfAbsent(uuid, (k) -> new HashSet<>()).add(city);
    }
    
    public void renameCity(City city, String newName){
        if(ConsulatAPI.getConsulatAPI().isDebug()){
            ConsulatAPI.getConsulatAPI().log(Level.INFO, "Renaming city " + city.getName() + " to " + newName);
        }
        city.removeMoney(City.RENAME_TAX);
        citiesByName.remove(city.getName().toLowerCase());
        city.rename(newName);
        citiesByName.put(newName.toLowerCase(), city);
        SaveManager.getInstance().saveOnce("city-name", city);
        IGui cityGui = GuiManager.getInstance().getContainer("city").getGui(false, city);
        if(cityGui != null){
            ((CityGui)cityGui).updateName();
        }
        IGui cityInfoGui = GuiManager.getInstance().getContainer("city-info").getGui(false, city);
        if(cityInfoGui != null){
            ((CityInfo)cityInfoGui).updateName();
        }
    }
    
    public void setHome(City city, Location location){
        city.setHome(location);
        IGui cityGui = GuiManager.getInstance().getContainer("city").getGui(false, city);
        if(cityGui != null){
            ((CityGui)cityGui).updateHome();
        }
        IGui cityInfoGui = GuiManager.getInstance().getContainer("city-info").getGui(false, city);
        if(cityInfoGui != null){
            ((CityInfo)cityInfoGui).updateHome();
        }
    }
    
    public Set<City> getInvitations(UUID uuid){
        return invitedPlayers.get(uuid);
    }
    
    public @Nullable City getCity(String name){
        return citiesByName.get(name.toLowerCase());
    }
    
    public Zone getZone(UUID uuid){
        return zones.get(uuid);
    }
    
    public void removeInvitation(UUID uuid){
        invitedPlayers.remove(uuid);
    }
    
    public City createCity(String name, UUID owner){
        City city = new City(UUID.randomUUID(), name, owner);
        addCity(city);
        addCityDatabase(city);
        return city;
    }
    
    private void addCityDatabase(City city){
        Bukkit.getScheduler().runTaskAsynchronously(ConsulatCore.getInstance(), () -> {
            try {
                PreparedStatement statement = ConsulatAPI.getDatabase().prepareStatement("INSERT INTO cities (uuid, name, owner) VALUES (?, ?, ?)");
                statement.setString(1, city.getUniqueId().toString());
                statement.setString(2, city.getName());
                statement.setString(3, city.getOwner().toString());
                statement.executeUpdate();
                statement.close();
            } catch(SQLException e){
                e.printStackTrace();
            }
        });
    }
    
    public void deleteCity(City city){
        if(ConsulatAPI.getConsulatAPI().isDebug()){
            ConsulatAPI.getConsulatAPI().log(Level.INFO, "Deleting city " + city.getName());
        }
        city.disband();
        removeCity(city);
        ClaimManager.getInstance().removeClaim(city);
        GuiManager.getInstance().getContainer("city").removeGui(city);
        GuiManager.getInstance().getContainer("city-info").removeGui(city);
        FileUtils.deleteFile(ConsulatAPI.getConsulatAPI().getDataFolder(), "cities/" + city.getUniqueId() + ".dat");
        Bukkit.getScheduler().runTaskAsynchronously(ConsulatCore.getInstance(), () -> {
            try {
                PreparedStatement statement = ConsulatAPI.getDatabase().prepareStatement("DELETE FROM cities WHERE uuid = ?");
                statement.setString(1, city.getUniqueId().toString());
                statement.executeUpdate();
                statement.close();
                statement = ConsulatAPI.getDatabase().prepareStatement("DELETE FROM claims WHERE player_uuid = ?");
                statement.setString(1, city.getUniqueId().toString());
                statement.executeUpdate();
                statement.close();
            } catch(SQLException e){
                e.printStackTrace();
            }
        });
    }
    
    public void saveZones(){
        for(Zone zone : zones.values()){
            zone.saveNBT();
        }
    }
    
    public void setPlayerCity(@NotNull UUID uuid, @Nullable City city){
        Bukkit.getScheduler().runTaskAsynchronously(ConsulatCore.getInstance(), () -> {
            try {
                PreparedStatement statement = ConsulatAPI.getDatabase().prepareStatement("UPDATE players SET city = ? WHERE player_uuid = ?");
                if(city == null){
                    statement.setNull(1, Types.CHAR);
                } else {
                    statement.setString(1, city.getUniqueId().toString());
                }
                statement.setString(2, uuid.toString());
                statement.executeUpdate();
                statement.close();
            } catch(SQLException e){
                e.printStackTrace();
            }
        });
    }
}
