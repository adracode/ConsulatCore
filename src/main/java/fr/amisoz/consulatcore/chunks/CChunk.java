package fr.amisoz.consulatcore.chunks;

import fr.leconsulat.api.ConsulatAPI;
import fr.leconsulat.api.nbt.CompoundTag;
import fr.leconsulat.api.nbt.ListTag;
import fr.leconsulat.api.nbt.NBTType;
import org.bukkit.Material;
import org.jetbrains.annotations.NotNull;

import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.logging.Level;

@SuppressWarnings({"unused", "CopyConstructorMissesField"})
public class CChunk implements Comparable<CChunk> {
    
    public static final String TYPE = "CHUNK";
    private static final int SHIFT = 25 - 4; //Max coordonnées MC - Taille du chunk
    private static final int LIMIT_X = 1 << SHIFT; //2 097 152 > 30 000 000 / 16 = 1 875 000
    private static final int LIMIT_Z = 1 << SHIFT; //2 097 152 > 30 000 000 / 16 = 1 875 000
    private static final int CONVERT = (1 << SHIFT + 1) - 1; //1111111111111111111111
    
    private final long coords;
    private Map<Material, AtomicInteger> limits = new EnumMap<>(Material.class);
    private boolean needLimitSync = false;
    
    public CChunk(int x, int z){
        this(convert(x, z));
    }
    
    public CChunk(long coords){
        this.coords = coords;
    }
    
    public CChunk(CChunk chunk){
        this.coords = chunk.coords;
        this.limits = chunk.limits;
    }
    
    public void addLimit(Material type){
        limits.put(type, new AtomicInteger(0));
    }
    
    public boolean hasLimit(Material type){
        return limits.containsKey(type);
    }
    
    public void changeLimit(Material type, int amount){
        limits.get(type).addAndGet(amount);
    }
    
    public int getLimit(Material type){
        return limits.get(type).get();
    }
    
    public void loadNBT(CompoundTag chunk){
        if(chunk.has("LimitedBlocks")){
            List<CompoundTag> limits = chunk.getList("LimitedBlocks", NBTType.COMPOUND);
            for(CompoundTag limited : limits){
                this.limits.put(
                        Material.valueOf(limited.getString("Id")),
                        new AtomicInteger(limited.getShort("Limit")));
            }
        }
        if(chunk.has("NeedSync")){
            setNeedLimitSync(true);
        }
    }
    
    public CompoundTag saveNBT(){
        CompoundTag chunk = new CompoundTag();
        chunk.putString("Type", getType());
        chunk.putLong("Coords", coords);
        if(!limits.isEmpty()){
            ListTag<CompoundTag> limits = new ListTag<>();
            for(Map.Entry<Material, AtomicInteger> limitedBlock : this.limits.entrySet()){
                CompoundTag limited = new CompoundTag();
                limited.putString("Id", limitedBlock.getKey().toString());
                limited.putShort("Limit", limitedBlock.getValue().shortValue());
                limits.addTag(limited);
            }
            chunk.put("LimitedBlocks", limits);
        }
        if(needLimitSync){
            chunk.putByte("NeedSync", (byte)1);
        }
        return chunk;
    }
    
    public void set(CChunk chunk){
        this.limits = chunk.limits;
    }
    
    public boolean incrementLimit(Material type){
        AtomicInteger limit = limits.get(type);
        if(limit == null){
            return true;
        }
        if(ChunkManager.getInstance().getMaxLimit(type) > limit.get()){
            limit.incrementAndGet();
            return true;
        }
        return false;
    }
    
    public void decrementLimit(Material type){
        AtomicInteger limit = limits.get(type);
        if(limit != null){
            if(limit.decrementAndGet() < 0){
                limit.set(0);
                ConsulatAPI.getConsulatAPI().log(Level.WARNING, "Attempt to decrement limit below 0 at chunk " + this);
            }
        }
    }
    
    public void syncLimits(){
        boolean needSync = false;
        Map<Material, Integer> config = ChunkManager.getInstance().getLimitedBlocks();
        for(Iterator<Map.Entry<Material, AtomicInteger>> iterator = this.limits.entrySet().iterator(); iterator.hasNext(); ){
            Map.Entry<Material, AtomicInteger> currentLimit = iterator.next();
            Integer limitedBlock = config.get(currentLimit.getKey());
            if(limitedBlock == null){
                iterator.remove();
            } else if(currentLimit.getValue().get() <= limitedBlock){
                continue;
            }
            needSync = true;
        }
        if(config.keySet().size() > this.limits.keySet().size()){
            needSync = true;
            Set<Material> missing = new HashSet<>(config.keySet());
            missing.removeAll(this.limits.keySet());
            for(Material missingType : missing){
                this.limits.put(missingType, new AtomicInteger(0));
            }
        }
        if(!this.needLimitSync){
            setNeedLimitSync(needSync);
        }
    }
    
    public int getLimitSize(){
        return limits.size();
    }
    
    public long getCoordinates(){
        return coords;
    }
    
    public int getX(){
        return (int)((coords & CONVERT) - LIMIT_X);
    }
    
    public int getZ(){
        return (int)((coords >> SHIFT + 1) - LIMIT_Z);
    }
    
    public String getType(){
        return TYPE;
    }
    
    public boolean isNeedLimitSync(){
        return needLimitSync;
    }
    
    public void setNeedLimitSync(boolean needLimitSync){
        if(needLimitSync){
            for(AtomicInteger limit : limits.values()){
                limit.set(0);
            }
        }
        this.needLimitSync = needLimitSync;
    }
    
    public static long convert(int x, int z){
        return (((long)z + LIMIT_Z) << SHIFT + 1) | (x + LIMIT_X);
    }
    
    @Override
    public int hashCode(){
        return Long.hashCode(coords);
    }
    
    @Override
    public boolean equals(Object o){
        if(this == o){
            return true;
        }
        if(!(o instanceof CChunk)){
            return false;
        }
        CChunk cChunk = (CChunk)o;
        return coords == cChunk.coords;
    }
    
    @Override
    public String toString(){
        return "CChunk{" +
                "coords=" + coords +
                ", limits=" + limits +
                ", needLimitSync=" + needLimitSync +
                '}';
    }
    
    @Override
    public int compareTo(@NotNull CChunk o){
        return Long.compare(this.coords, o.coords);
    }
}
