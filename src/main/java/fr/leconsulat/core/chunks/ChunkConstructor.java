package fr.leconsulat.core.chunks;

@FunctionalInterface
public interface ChunkConstructor {
    
    CChunk construct(long coords);
    
}
