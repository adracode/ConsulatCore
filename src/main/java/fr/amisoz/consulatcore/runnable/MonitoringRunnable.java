package fr.amisoz.consulatcore.runnable;


import fr.amisoz.consulatcore.ConsulatCore;
import org.bukkit.Bukkit;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.text.DateFormat;
import java.util.Date;
import java.util.Locale;

public class MonitoringRunnable implements Runnable {
    
    private ConsulatCore consulatCore;
    
    public MonitoringRunnable(ConsulatCore consulatCore){
        this.consulatCore = consulatCore;
    }
    
    @Override
    public void run(){
        try {
            PreparedStatement preparedStatement = consulatCore.getDatabaseConnection().prepareStatement("INSERT INTO monitoring(players, insert_date) VALUES(?, ?)");
            preparedStatement.setInt(1, Bukkit.getOnlinePlayers().size());
            DateFormat shortDateFormat = DateFormat.getDateTimeInstance(
                    DateFormat.SHORT,
                    DateFormat.SHORT, new Locale("FR", "fr"));
            preparedStatement.setString(2, shortDateFormat.format(new Date()));
            
            preparedStatement.executeUpdate();
            preparedStatement.close();
        } catch(SQLException e){
            e.printStackTrace();
        }
    }
}

