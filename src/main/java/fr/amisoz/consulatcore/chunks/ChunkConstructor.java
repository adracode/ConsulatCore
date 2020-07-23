package fr.amisoz.consulatcore.chunks;

@FunctionalInterface
public interface ChunkConstructor {
    
    CChunk construct(long coords);
    
}
