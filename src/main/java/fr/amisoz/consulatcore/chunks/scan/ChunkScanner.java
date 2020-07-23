package fr.amisoz.consulatcore.chunks.scan;

import fr.leconsulat.api.task.Task;
import org.bukkit.Chunk;
import org.bukkit.ChunkSnapshot;

public class ChunkScanner implements Task {
    
    private Chunk chunk;
    private ChunkSnapshot snapshot;
    private ScanAction action;
    int x = 0, y = 0, z = 0;
    
    public ChunkScanner(Chunk chunk, ScanAction action){
        this.chunk = chunk;
        this.action = action;
    }
    
    @Override
    public void onStart(){
        this.snapshot = chunk.getChunkSnapshot();
    }
    
    @Override
    public void next(){
        action.action(x, y, z, snapshot.getBlockType(x, y, z));
        if(++x >= 16){
            x = 0;
            if(++z >= 16){
                z = 0;
                ++y;
            }
        }
    }
    
    @Override
    public boolean isFinished(){
        return y >= 256;
    }
    
    @Override
    public void onFinish(){
    
    }
}
