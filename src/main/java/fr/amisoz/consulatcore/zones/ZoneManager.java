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

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;

public class ZoneManager {
    
    private static ZoneManager instance;
    
    private Map<String, Zone> zonesByName = new HashMap<>();
    private Map<UUID, Zone> zones = new HashMap<>();
    private Map<UUID, Set<City>> invitedPlayers = new HashMap<>();
    
    private ManageClaimGui manageClaimGui;
    private CityGui cityGui;
    private CityInfo cityInfoGui;
    private DisbandGui disbandCityGui;
    
    public ZoneManager(){
        if(instance != null){
            return;
        }
        instance = this;
        SaveManager saveManager = SaveManager.getInstance();
        saveManager.addSaveTask("city-money", new SaveTask<>(
                "UPDATE cities SET money = ? WHERE uuid = ?;",
                (statement, city) -> {
                    statement.setDouble(1, city.getMoney());
                    statement.setString(2, city.getUUID().toString());
                },
                City::getMoney
        ));
        saveManager.addSaveTask("city-name", new SaveTask<>(
                "UPDATE cities SET name = ? WHERE uuid = ?;",
                (statement, city) -> {
                    statement.setString(1, city.getName());
                    statement.setString(2, city.getUUID().toString());
                },
                City::getName
        ));
        saveManager.addSaveTask("city-home", new SaveTask<>(
                "UPDATE cities SET spawn_x = ?, spawn_y = ?, spawn_z = ?, yaw = ?, pitch = ? WHERE uuid = ?;",
                (statement, city) -> {
                    Location home = city.getHome();
                    statement.setDouble(1, home.getX());
                    statement.setDouble(2, home.getY());
                    statement.setDouble(3, home.getZ());
                    statement.setFloat(4, home.getYaw());
                    statement.setFloat(5, home.getPitch());
                    statement.setString(6, city.getUUID().toString());
                },
                City::getHome
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
        }
    }
    
    private void initZones(){
        try {
            PreparedStatement fetchZones = ConsulatAPI.getDatabase().prepareStatement("SELECT * FROM zones;");
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
        } catch(SQLException e){
            e.printStackTrace();
        }
    }
    
    private void initCities() throws SQLException{
        PreparedStatement fetchCities = ConsulatAPI.getDatabase().prepareStatement("SELECT * FROM cities;");
        ResultSet result = fetchCities.executeQuery();
        while(result.next()){
            double x = result.getDouble("spawn_x");
            Location spawn = result.wasNull() ? null : new Location(Bukkit.getWorlds().get(0),
                    x,
                    result.getDouble("spawn_y"),
                    result.getDouble("spawn_z"),
                    result.getFloat("yaw"),
                    result.getFloat("pitch"));
            City city = new City(
                    UUID.fromString(result.getString("uuid")),
                    result.getString("name"),
                    UUID.fromString(result.getString("owner")),
                    result.getDouble("money"),
                    spawn
            );
            city.loadNBT();
            addCity(city);
        }
    }
    
    public void addZone(Zone zone){
        zones.put(zone.getUUID(), zone);
        zonesByName.put(zone.getName().toLowerCase(), zone);
    }
    
    private void addCity(City city){
        cityInfoGui.addGui(cityInfoGui.createGui(city));
        SaveManager.getInstance().addData("city-money", city);
        addZone(city);
    }
    
    public void removeZone(Zone zone){
        zones.remove(zone.getUUID());
        zonesByName.remove(zone.getName().toLowerCase());
    }
    
    public void removeCity(City city){
        SaveManager.getInstance().removeData("city-money", city, false);
        removeZone(city);
    }
    
    public static ZoneManager getInstance(){
        return instance;
    }
    
    public boolean invitePlayer(City city, UUID uuid){
        return invitedPlayers.computeIfAbsent(uuid, (k) -> new HashSet<>()).add(city);
    }
    
    public void renameCity(City city, String oldName, String newName){
        city.rename(newName);
        zonesByName.remove(oldName);
        zonesByName.put(newName.toLowerCase(), city);
        SaveManager.getInstance().saveOnce("city-name", city);
        cityGui.setCityName(city);
    }
    
    public void setHome(City city, Location location){
        city.setHome(location);
        SaveManager.getInstance().saveOnce("city-home", city);
        cityGui.setHome(city, true);
    }
    
    public Set<City> getInvitations(UUID uuid){
        return invitedPlayers.get(uuid);
    }
    
    public Zone getZone(String name){
        return zonesByName.get(name.toLowerCase());
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
                statement.setString(1, city.getUUID().toString());
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
        ClaimManager.getInstance().removeClaim(city);
        cityGui.removeGui(city);
        cityInfoGui.removeGui(city);
        removeCity(city);
        FileUtils.deleteFile(ConsulatAPI.getConsulatAPI().getDataFolder(), "cities/" + city.getUUID() + ".dat");
        Bukkit.getScheduler().runTaskAsynchronously(ConsulatCore.getInstance(), () -> {
            try {
                PreparedStatement statement = ConsulatAPI.getDatabase().prepareStatement("DELETE FROM cities WHERE uuid = ?");
                statement.setString(1, city.getUUID().toString());
                statement.executeUpdate();
                statement.close();
                statement = ConsulatAPI.getDatabase().prepareStatement("DELETE FROM claims WHERE player_uuid = ?");
                statement.setString(1, city.getUUID().toString());
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
}
