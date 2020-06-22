package fr.amisoz.consulatcore.zones;

import fr.amisoz.consulatcore.ConsulatCore;
import fr.amisoz.consulatcore.guis.city.CityGui;
import fr.amisoz.consulatcore.guis.city.CityInfo;
import fr.amisoz.consulatcore.guis.city.DisbandGui;
import fr.amisoz.consulatcore.guis.city.claimlist.claims.ManageClaimGui;
import fr.amisoz.consulatcore.zones.cities.City;
import fr.amisoz.consulatcore.zones.claims.ClaimManager;
import fr.leconsulat.api.ConsulatAPI;
import fr.leconsulat.api.database.SaveManager;
import fr.leconsulat.api.database.tasks.SaveTask;
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

public class ZoneManager {
    
    private static ZoneManager instance;
    
    private final @NotNull Map<UUID, Zone> zones = new HashMap<>();
    private final @NotNull Map<String, City> citiesByName = new HashMap<>();
    private final @NotNull Map<UUID, Set<City>> invitedPlayers = new HashMap<>();
    
    private final @NotNull ManageClaimGui manageClaimGui;
    private final @NotNull CityGui cityGui;
    private final @NotNull CityInfo cityInfoGui;
    private final @NotNull DisbandGui disbandCityGui;
    
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
        manageClaimGui = new ManageClaimGui();
        cityGui = new CityGui();
        cityInfoGui = new CityInfo();
        disbandCityGui = new DisbandGui();
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
        cityInfoGui.addGui(cityInfoGui.createGui(city));
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
        return invitedPlayers.computeIfAbsent(uuid, (k) -> new HashSet<>()).add(city);
    }
    
    public void renameCity(City city, String newName){
        citiesByName.remove(city.getName().toLowerCase());
        city.rename(newName);
        citiesByName.put(newName.toLowerCase(), city);
        SaveManager.getInstance().saveOnce("city-name", city);
        cityGui.updateName(city);
        cityInfoGui.updateName(city);
    }
    
    public void setHome(City city, Location location){
        city.setHome(location);
        cityGui.updateHome(city, true);
        cityInfoGui.updateHome(city);
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
        city.disband();
        removeCity(city);
        ClaimManager.getInstance().removeClaim(city);
        cityGui.removeGui(city);
        cityInfoGui.removeGui(city);
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
    
    public CityGui getCityGui(){
        return cityGui;
    }
    
    public CityInfo getCityInfoGui(){
        return cityInfoGui;
    }
    
    public DisbandGui getDisbandCityGui(){
        return disbandCityGui;
    }
    
    public ManageClaimGui getManageClaimGui(){
        return manageClaimGui;
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
