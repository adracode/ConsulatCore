package fr.amisoz.consulatcore.economy;

import fr.amisoz.consulatcore.ConsulatCore;
import fr.amisoz.consulatcore.players.SurvivalOffline;
import fr.leconsulat.api.ConsulatAPI;
import org.bukkit.Bukkit;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;

//TODO: si un joueur passe admin, il sera affiché dans le baltop s'il n'y a pas eu d'update
public class BaltopManager {
    
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
        Bukkit.getScheduler().runTaskAsynchronously(ConsulatCore.getInstance(), this::updateBaltop);
    }
    
    private void updateBaltop(){
        lastUpdate = System.currentTimeMillis();
        Bukkit.getScheduler().runTaskAsynchronously(ConsulatCore.getInstance(), () -> {
            try {
                PreparedStatement preparedStatement = ConsulatAPI.getDatabase().prepareStatement(
                        "SELECT player_name, money FROM players WHERE player_rank != 'Admin' ORDER BY money DESC limit ?;");
                preparedStatement.setInt(1, max);
                preparedStatement.executeQuery();
                ResultSet result = preparedStatement.executeQuery();
                SortedSet<MoneyOwner> rank = new TreeSet<>();
                while(result.next()){
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
    
    public class MoneyOwner implements Comparable<MoneyOwner>{
    
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
