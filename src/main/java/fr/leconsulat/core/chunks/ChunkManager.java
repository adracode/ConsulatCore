package fr.leconsulat.core.chunks;

import fr.leconsulat.api.ConsulatAPI;
import fr.leconsulat.api.nbt.*;
import fr.leconsulat.api.task.TaskManager;
import fr.leconsulat.api.utils.FileUtils;
import fr.leconsulat.core.ConsulatCore;
import fr.leconsulat.core.chunks.scan.ChunkScanner;
import fr.leconsulat.core.zones.claims.Claim;
import it.unimi.dsi.fastutil.longs.Long2ObjectMap;
import it.unimi.dsi.fastutil.longs.Long2ObjectOpenHashMap;
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.world.ChunkLoadEvent;

import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.logging.Level;

@SuppressWarnings("UnusedReturnValue")
public class ChunkManager implements Listener {
    
    private static final int SHIFT_CLAIMS = 5;
    private static ChunkManager instance;
    
    static{
        ChunkManager chunkManager = new ChunkManager();
        chunkManager.register(CChunk.TYPE, CChunk::new);
        chunkManager.register(Claim.TYPE, Claim::new);
    }
    
    private final Map<String, ChunkConstructor> createChunk = new HashMap<>(2);
    
    private final Map<UUID, Long2ObjectMap<CChunk>> chunks = new HashMap<>();
    private final Map<Material, Integer> limits = new EnumMap<>(Material.class);
    
    private ChunkManager(){
        if(instance == null){
            instance = this;
        }
        for(World world : Bukkit.getWorlds()){
            chunks.put(world.getUID(), new Long2ObjectOpenHashMap<>());
        }
        FileConfiguration config = ConsulatCore.getInstance().getConfig();
        ConfigurationSection blockLimits = config.getConfigurationSection("block-limits");
        if(blockLimits == null){
            ConsulatAPI.getConsulatAPI().log(Level.WARNING, "No 'block-limits' section found in config.yml, then no limits are applied on chunks.");
        } else {
            for(Map.Entry<String, Object> limit : blockLimits.getValues(false).entrySet()){
                Material material;
                try {
                    material = Material.valueOf(limit.getKey());
                } catch(IllegalArgumentException | NullPointerException e){
                    ConsulatAPI.getConsulatAPI().log(Level.WARNING, "Invalid block in limit config (" + limit + ")");
                    continue;
                }
                limits.put(material, (int)limit.getValue());
            }
        }
        ConsulatCore.getInstance().getServer().getPluginManager().registerEvents(this, ConsulatCore.getInstance());
    }
    
    public void register(String type, ChunkConstructor create){
        this.createChunk.put(type, create);
    }
    
    public void loadChunks(){
        ConsulatAPI.getConsulatAPI().log(Level.INFO, "Loading chunks...");
        long start = System.currentTimeMillis();
        int size = 0;
        boolean hasCrashed = ConsulatAPI.getConsulatAPI().hasCrashed();
        try {
            for(File worldDir : FileUtils.getFiles(new File(ConsulatAPI.getConsulatAPI().getDataFolder(), "chunks"))){
                UUID worldUUID = UUID.fromString(worldDir.getName());
                World world = Bukkit.getWorld(worldUUID);
                if(world == null){
                    ConsulatAPI.getConsulatAPI().log(Level.WARNING, "World " + worldUUID + " not found, skipping loading chunks for this world");
                    continue;
                }
                for(File regionFile : FileUtils.getFiles(worldDir)){
                    NBTInputStream is = new NBTInputStream(regionFile);
                    CompoundTag region;
                    try {
                        region = is.read();
                    } catch(IOException e){
                        e.printStackTrace();
                        System.out.println("regionFile = " + regionFile);
                        continue;
                    }
                    is.close();
                    List<CompoundTag> chunks = region.getList("Chunks", NBTType.COMPOUND);
                    for(CompoundTag chunkTag : chunks){
                        CChunk chunk = createChunk.get(chunkTag.getString("Type")).construct(chunkTag.getLong("Coords"));
                        chunk.loadNBT(chunkTag);
                        addChunk(world, chunk);
                        chunk.syncLimits();
                        if(hasCrashed){
                            chunk.setNeedLimitSync(true);
                        }
                        ++size;
                    }
                }
            }
        } catch(IOException e){
            e.printStackTrace();
            Bukkit.shutdown();
        }
        for(World world : Bukkit.getWorlds()){
            for(Chunk spawnChunk : world.getLoadedChunks()){
                Bukkit.getPluginManager().callEvent(new ChunkLoadEvent(spawnChunk, false));
            }
        }
        ConsulatAPI.getConsulatAPI().log(Level.INFO, size + " Chunks loaded in " + (System.currentTimeMillis() - start) + " ms");
    }
    
    public synchronized void saveChunks(){
        try {
            File chunkDir = FileUtils.loadFile(ConsulatAPI.getConsulatAPI().getDataFolder(), "chunks");
            if(!chunkDir.exists()){
                if(!chunkDir.mkdir()){
                    throw new IOException("Couldn't create file.");
                }
            }
            for(Map.Entry<UUID, Long2ObjectMap<CChunk>> worldChunks : chunks.entrySet()){
                Long2ObjectMap<CChunk> world = worldChunks.getValue();
                File worldDir = FileUtils.loadFile(chunkDir, worldChunks.getKey().toString());
                if(!worldDir.exists()){
                    if(!worldDir.mkdir()){
                        throw new IOException("Couldn't create file.");
                    }
                }
                Map<Integer, Map<Integer, Set<CChunk>>> orderedChunks = new HashMap<>();
                for(CChunk chunk : world.values()){
                    orderedChunks.computeIfAbsent(
                            chunk.getX() >> SHIFT_CLAIMS,
                            v -> new HashMap<>()).computeIfAbsent(chunk.getZ() >> SHIFT_CLAIMS,
                            v -> new TreeSet<>()).add(chunk);
                }
                for(Map.Entry<Integer, Map<Integer, Set<CChunk>>> claimX : orderedChunks.entrySet()){
                    for(Map.Entry<Integer, Set<CChunk>> chunkZ : claimX.getValue().entrySet()){
                        CompoundTag chunks = new CompoundTag();
                        ListTag<CompoundTag> listChunks = new ListTag<>(NBTType.COMPOUND);
                        for(CChunk chunk : chunkZ.getValue()){
                            listChunks.addTag(chunk.saveNBT());
                        }
                        chunks.put("Chunks", listChunks);
                        NBTOutputStream os = new NBTOutputStream(new File(worldDir, claimX.getKey() + "." + chunkZ.getKey() + ".dat"), chunks);
                        os.write("Region");
                        os.close();
                    }
                }
            }
        } catch(IOException e){
            e.printStackTrace();
        }
    }
    
    public CChunk getChunk(Block block){
        return getChunk(block.getLocation());
    }
    
    public CChunk getChunk(Location location){
        return getChunk(location.getWorld(), location.getChunk().getX(), location.getChunk().getZ());
    }
    
    public CChunk getChunk(Chunk chunk){
        return getChunk(chunk.getWorld(), chunk.getX(), chunk.getZ());
    }
    
    public CChunk getChunk(World world, int x, int z){
        return getChunk(world, CChunk.convert(x, z));
    }
    
    public CChunk getChunk(World world, long coords){
        return chunks.get(world.getUID()).get(coords);
    }
    
    public void addChunk(World world, CChunk chunk){
        CChunk previous = chunks.get(world.getUID()).put(chunk.getCoordinates(), chunk);
        if(previous != null){
            chunk.set(previous);
        }
        if(chunk.getLimitSize() == 0){
            for(Material material : limits.keySet()){
                chunk.addLimit(material);
            }
            chunk.setNeedLimitSync(true);
        }
    }
    
    public boolean removeChunk(World world, CChunk chunk, boolean replaceByCChunk){
        if(replaceByCChunk){
            addChunk(world, new CChunk(chunk));
            return true;
        } else {
            return chunks.get(world.getUID()).remove(chunk.getCoordinates()) != null;
        }
    }
    
    public int getMaxLimit(Material material){
        Integer max = limits.get(material);
        return max == null ? -1 : max;
    }
    
    @EventHandler
    public void onLoad(ChunkLoadEvent event){
        CChunk chunk = chunks.get(event.getWorld().getUID()).get(CChunk.convert(event.getChunk().getX(), event.getChunk().getZ()));
        if(chunk == null || chunk.isNeedLimitSync()){
            if(chunk == null){
                chunk = new CChunk(event.getChunk().getX(), event.getChunk().getZ());
                addChunk(event.getWorld(), chunk);
            }
            scanLimitedBlock(chunk, event.getChunk());
        }
    }
    
    public void scanLimitedBlock(CChunk consulatChunk, Chunk chunk){
        consulatChunk.setNeedLimitSync(false);
        ChunkScanner scanner = new ChunkScanner(chunk, (x, y, z, type) -> {
            if(consulatChunk.hasLimit(type)){
                if(consulatChunk.getLimit(type) < limits.get(type)){
                    consulatChunk.incrementLimit(type);
                } else {
                    chunk.getBlock(x, y, z).setType(Material.AIR, false);
                }
            }
        });
        TaskManager.getInstance().addTask(scanner);
    }
    
    public Set<Map.Entry<UUID, Long2ObjectMap<CChunk>>> getChunks(){
        return Collections.unmodifiableSet(chunks.entrySet());
        
    }
    
    public Map<Material, Integer> getLimitedBlocks(){
        return Collections.unmodifiableMap(limits);
    }
    
    public static ChunkManager getInstance(){
        return instance;
    }
}
