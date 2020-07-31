package fr.amisoz.consulatcore.economy;

import fr.amisoz.consulatcore.ConsulatCore;
import fr.leconsulat.api.ConsulatAPI;
import org.bukkit.Bukkit;
import org.bukkit.event.Listener;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Collections;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.logging.Level;

public class BaltopManager implements Listener {
    
    private static BaltopManager instance;
    
    private SortedSet<MoneyOwner> rank = new TreeSet<>();
    private final int max = 10;
    private long lastUpdate = System.currentTimeMillis();
    private int timeBetweenUpdate = 5 * 60 * 1000;
    
    public BaltopManager(){
        if(instance != null){
            return;
        }
        instance = this;
        Bukkit.getPluginManager().registerEvents(this, ConsulatCore.getInstance());
        Bukkit.getScheduler().runTaskAsynchronously(ConsulatCore.getInstance(), this::updateBaltop);
    }
    
    private void updateBaltop(){
        lastUpdate = System.currentTimeMillis();
        Bukkit.getScheduler().runTaskAsynchronously(ConsulatCore.getInstance(), () -> {
            try {
                PreparedStatement preparedStatement = ConsulatAPI.getDatabase().prepareStatement(
                        "SELECT id, player_name, money FROM players WHERE player_rank != 'Admin' AND player_rank != 'Superviseur' ORDER BY money DESC limit ?;");
                preparedStatement.setInt(1, max);
                preparedStatement.executeQuery();
                ResultSet result = preparedStatement.executeQuery();
                SortedSet<MoneyOwner> rank = new TreeSet<>();
                while(result.next()){
                    String name = result.getString("player_name");
                    if(name == null){
                        ConsulatAPI.getConsulatAPI().log(Level.WARNING, "Player name in null at id " + result.getInt("id"));
                        continue;
                    }
                    rank.add(new MoneyOwner(result.getDouble("money"),
                            result.getString("player_name")));
                }
                preparedStatement.close();
                this.rank = rank;
            } catch(SQLException e){
                e.printStackTrace();
            }
        });
    }
    
    public SortedSet<MoneyOwner> getBaltop(){
        if(System.currentTimeMillis() - lastUpdate > timeBetweenUpdate){
            updateBaltop();
        }
        return Collections.unmodifiableSortedSet(rank);
    }
    
    public static BaltopManager getInstance(){
        return instance;
    }
    
    public static class MoneyOwner implements Comparable<MoneyOwner>{
    
        private final double money;
        private final String name;
    
        private MoneyOwner(double money, String name){
            this.money = money;
            this.name = name;
        }
    
        @Override
        public int compareTo(MoneyOwner o){
            return Double.compare(o.money, money);
        }
    
        @Override
        public boolean equals(Object o){
            if(this == o) return true;
            if(o == null || getClass() != o.getClass()) return false;
            MoneyOwner that = (MoneyOwner)o;
            return name.equals(that.name);
        }
    
        @Override
        public int hashCode(){
            return name.hashCode();
        }
    
        public double getMoney(){
            return money;
        }
    
        public String getName(){
            return name;
        }
    }
    
}
