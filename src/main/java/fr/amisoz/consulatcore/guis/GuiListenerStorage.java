package fr.amisoz.consulatcore.guis;

import fr.leconsulat.api.gui.GuiListener;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.Map;

public class GuiListenerStorage {
    
    private static GuiListenerStorage instance;
    
    private final Map<String, GuiListener<?>> listeners = new HashMap<>();
    
    public GuiListenerStorage(){
        if(instance != null){
            throw new IllegalStateException();
        }
        instance = this;
    }
    
    public boolean addListener(@NotNull String id, @NotNull GuiListener<?> listener){
        return listeners.put(id, listener) == null;
    }
    
    public boolean removeListener(@NotNull String id){
        return listeners.remove(id) != null;
    }
    
    @SuppressWarnings("unchecked")
    public @NotNull GuiListener<?> getListener(@NotNull String id){
        return listeners.get(id);
    }

    public static GuiListenerStorage getInstance(){
        return instance;
    }
}
