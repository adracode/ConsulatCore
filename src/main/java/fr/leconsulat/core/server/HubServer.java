package fr.leconsulat.core.server;

import fr.leconsulat.api.ConsulatAPI;
import fr.leconsulat.api.server.Server;
import fr.leconsulat.core.ConsulatCore;
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
