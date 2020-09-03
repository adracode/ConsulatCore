package fr.leconsulat.core.channel;

import fr.leconsulat.api.channel.Channel;
import fr.leconsulat.api.channel.Speakable;
import fr.leconsulat.api.player.ConsulatPlayer;
import org.jetbrains.annotations.NotNull;

public class StaffChannel extends Channel implements Speakable {
    
    public StaffChannel(){
        super("staff");
    }
    
    @Override
    public @NotNull String speak(ConsulatPlayer sender, @NotNull String message){
        return "§2(Staff)§a " + sender.getName() + "§7: " + message;
    }
}
