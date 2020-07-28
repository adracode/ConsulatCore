package fr.amisoz.consulatcore.chunks;

import fr.amisoz.consulatcore.ConsulatCore;
import fr.amisoz.consulatcore.chunks.scan.ChunkScanner;
import fr.leconsulat.api.ConsulatAPI;
import fr.leconsulat.api.nbt.*;
import fr.leconsulat.api.task.TaskManager;
import fr.leconsulat.api.utils.FileUtils;
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.world.ChunkLoadEvent;

import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.logging.Level;

public class ChunkManager implements Listener {
    
    private static ChunkManager instance;
    static {
         new ChunkManager();
    }
    private static final int SHIFT_CLAIMS = 5;
    
    private final Map<String, ChunkConstructor> createChunk = new HashMap<>();
    
    private final Map<UUID, Map<Long, CChunk>> chunks = new HashMap<>();
    private final Map<Material, Integer> limits = new EnumMap<>(Material.class);
    
    private ChunkManager(){
        if(instance == null){
            instance = this;
        }
        Bukkit.getWorlds().stream()
                .sorted(Comparator.comparingInt(world -> world.getEnvironment().ordinal()))
                .forEach((world -> chunks.put(world.getUID() ,new HashMap<>())));
        FileConfiguration config = ConsulatCore.getInstance().getConfig();
        for(Map.Entry<String, Object> limit : config.getConfigurationSection("block-limits").getValues(false).entrySet()){
            Material material;
            try {
                material = Material.valueOf(limit.getKey());
            } catch(IllegalArgumentException | NullPointerException e){
                ConsulatAPI.getConsulatAPI().log(Level.WARNING, "Invalid block in limit config (" + limit + ")");
                continue;
            }
            limits.put(material, (int)limit.getValue());
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
                    CompoundTag region = is.read();
                    is.close();
                    List<CompoundTag> chunks = region.getList("Chunks", NBTType.COMPOUND);
                    for(CompoundTag chunkTag : chunks){
                        CChunk chunk = createChunk.get(chunkTag.getString("Type")).construct(chunkTag.getLong("Coords"));
                        chunk.loadNBT(chunkTag);
                        addChunk(world, chunk);
                        chunk.syncLimits();
                        ++size;
                    }
                }
            }
        } catch(IOException e){
            e.printStackTrace();
            Bukkit.shutdown();
        }
        for(World world : Bukkit.getWorlds()){
            Map<Long, CChunk> worldChunks = chunks.get(world.getUID());
            for(Chunk spawnChunk : world.getLoadedChunks()){
                if(!worldChunks.containsKey(CChunk.convert(spawnChunk.getX(), spawnChunk.getZ()))){
                    Bukkit.getPluginManager().callEvent(new ChunkLoadEvent(spawnChunk, false));
                }
            }
        }
        ConsulatAPI.getConsulatAPI().log(Level.INFO, size + " Chunks loaded in " + (System.currentTimeMillis() - start) + " ms");
    }
    
    public void saveChunks(){
        try {
            File chunkDir = FileUtils.loadFile(ConsulatAPI.getConsulatAPI().getDataFolder(), "chunks");
            if(!chunkDir.exists()){
                if(!chunkDir.mkdir()){
                    throw new IOException("Couldn't create file.");
                }
            }
            for(Map.Entry<UUID, Map<Long, CChunk>> worldChunks : chunks.entrySet()){
                Map<Long, CChunk> world = worldChunks.getValue();
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
    
    public Map<Material, Integer> getLimitedBlocks(){
        return Collections.unmodifiableMap(limits);
    }
    
    public int getMaxLimit(Material material){
        Integer max = limits.get(material);
        return max == null ? -1 : max;
    }
    
    public static ChunkManager getInstance(){
        return instance;
    }
    
    public Collection<CChunk> getChunks(World world){
        return Collections.unmodifiableCollection(chunks.get(world.getUID()).values());
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
}
