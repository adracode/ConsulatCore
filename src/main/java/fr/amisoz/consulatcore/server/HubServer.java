package fr.amisoz.consulatcore.server;

import fr.amisoz.consulatcore.ConsulatCore;
import fr.leconsulat.api.ConsulatAPI;
import fr.leconsulat.api.server.Server;
import org.bukkit.plugin.Plugin;

public class HubServer extends Server {
    
    public HubServer(){
        super(ConsulatAPI.getConsulatAPI().isDevelopment() ? "testhub" : "hub", false);
    }
    
    @Override
    public Plugin getPlugin(){
        return ConsulatCore.getInstance();
    }
}
