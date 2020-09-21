package fr.leconsulat.core.zones;

import fr.leconsulat.api.ConsulatAPI;
import fr.leconsulat.api.database.SaveManager;
import fr.leconsulat.api.database.tasks.SaveTask;
import fr.leconsulat.api.gui.GuiManager;
import fr.leconsulat.api.gui.gui.IGui;
import fr.leconsulat.api.nbt.CompoundTag;
import fr.leconsulat.api.nbt.NBTInputStream;
import fr.leconsulat.api.nbt.NBTOutputStream;
import fr.leconsulat.api.utils.FileUtils;
import fr.leconsulat.core.ConsulatCore;
import fr.leconsulat.core.guis.city.CityGui;
import fr.leconsulat.core.guis.city.CityInfo;
import fr.leconsulat.core.guis.city.DisbandGui;
import fr.leconsulat.core.players.SPlayerManager;
import fr.leconsulat.core.zones.cities.City;
import fr.leconsulat.core.zones.claims.ClaimManager;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
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
    
    public void addZone(Zone zone){
        zones.put(zone.getUniqueId(), zone);
    }
    
    public void removeZone(Zone zone){
        zones.remove(zone.getUniqueId());
    }
    
    public void removeCity(City city){
        citiesByName.remove(city.getName().toLowerCase());
        SaveManager.getInstance().removeData("city-money", city, false);
        removeZone(city);
    }
    
    public boolean invitePlayer(City city, UUID uuid){
        if(ConsulatAPI.getConsulatAPI().isDebug()){
            ConsulatAPI.getConsulatAPI().log(Level.INFO, "Player " + uuid + " is invited to " + city.getName());
        }
        boolean invite = invitedPlayers.computeIfAbsent(uuid, (k) -> new HashSet<>()).add(city);
        if(invite){
            Bukkit.getScheduler().scheduleSyncDelayedTask(ConsulatCore.getInstance(), () -> {
                Set<City> cities = invitedPlayers.get(uuid);
                if(cities == null){
                    return;
                }
                cities.remove(city);
            }, 5 * 60 * 20);
        }
        return invite;
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
            ((CityInfo)cityInfoGui).updateName(null);
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
            ((CityInfo)cityInfoGui).updateHome(null);
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
    
    public void updateOwner(City city){
        Bukkit.getScheduler().runTaskAsynchronously(ConsulatCore.getInstance(), () -> {
            try {
                PreparedStatement statement = ConsulatAPI.getDatabase().prepareStatement("UPDATE cities SET owner = ? WHERE uuid = ?");
                statement.setString(1, city.getOwner().toString());
                statement.setString(2, city.getUniqueId().toString());
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
    
    public synchronized void saveZones(){
        for(Zone zone : zones.values()){
            try {
                File file = FileUtils.loadFile(ConsulatAPI.getConsulatAPI().getDataFolder(), "cities/" + zone.getUniqueId() + ".dat");
                if(!file.exists()){
                    if(!file.createNewFile()){
                        throw new IOException("Couldn't create file.");
                    }
                }
                NBTOutputStream os = new NBTOutputStream(file, zone.saveNBT());
                os.write("Zone");
                os.close();
            } catch(IOException e){
                e.printStackTrace();
            }
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
    
    public Collection<Zone> getZones(){
        return Collections.unmodifiableCollection(zones.values());
    }
    
    public Collection<City> getCities(){
        return Collections.unmodifiableCollection(citiesByName.values());
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
            try {
                File file = FileUtils.loadFile(ConsulatAPI.getConsulatAPI().getDataFolder(), "zones/" + zone.getUniqueId() + ".dat");
                if(!file.exists()){
                    continue;
                }
                NBTInputStream is = new NBTInputStream(new FileInputStream(file));
                CompoundTag city = is.read();
                is.close();
                zone.loadNBT(city);
                addZone(zone);
            } catch(IOException e){
                e.printStackTrace();
            }
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
            File file = FileUtils.loadFile(ConsulatAPI.getConsulatAPI().getDataFolder(), "cities/" + city.getUniqueId() + ".dat");
            if(!file.exists()){
                ConsulatAPI.getConsulatAPI().log(Level.WARNING, "City " + city.getName() + ", owner " + city.getOwner() + ", money " + city.getMoney() + " doesn't have file, deleting...");
                //Refund player
                SPlayerManager.getInstance().addMoney(city.getOwner(), City.CREATE_TAX);
                SPlayerManager.getInstance().addMoney(city.getOwner(), city.getMoney());
                deleteCity(city);
            }
            try {
                NBTInputStream is = new NBTInputStream(new FileInputStream(file));
                CompoundTag cityTag = is.read();
                is.close();
                city.loadNBT(cityTag);
            } catch(Exception e){
                e.printStackTrace();
                continue;
            }
            addCity(city);
        }
    }
    
    private void addCity(City city){
        citiesByName.put(city.getName().toLowerCase(), city);
        SaveManager.getInstance().addData("city-money", city);
        addZone(city);
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
    
    public static ZoneManager getInstance(){
        return instance;
    }
}
