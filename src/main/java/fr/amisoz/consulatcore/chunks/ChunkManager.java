package fr.amisoz.consulatcore.chunks;

import fr.amisoz.consulatcore.ConsulatCore;
import fr.amisoz.consulatcore.chunks.scan.ChunkScanner;
import fr.leconsulat.api.ConsulatAPI;
import fr.leconsulat.api.nbt.*;
import fr.leconsulat.api.task.TaskManager;
import fr.leconsulat.api.utils.FileUtils;
import org.bukkit.Bukkit;
import org.bukkit.Chunk;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.world.ChunkLoadEvent;

import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.logging.Level;

//TODO: Nether et End
public class ChunkManager implements Listener {
    
    private static final ChunkManager instance = new ChunkManager();
    private static final int SHIFT_CLAIMS = 5;
    
    private final Map<String, ChunkConstructor> createChunk = new HashMap<>();
    
    private final Map<Long, CChunk> chunks = new HashMap<>();
    private final Map<Material, Integer> limits = new EnumMap<>(Material.class);
    
    private ChunkManager(){
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
            for(File file : FileUtils.getFiles(new File(ConsulatAPI.getConsulatAPI().getDataFolder(), "chunks"))){
                NBTInputStream is = new NBTInputStream(file);
                CompoundTag region = is.read();
                is.close();
                List<CompoundTag> chunks = region.getList("Chunks", CompoundTag.class);
                for(CompoundTag chunkTag : chunks){
                    CChunk chunk = createChunk.get(chunkTag.getString("Type")).construct(chunkTag.getLong("Coords"));
                    chunk.loadNBT(chunkTag);
                    addChunk(chunk);
                    chunk.syncLimits();
                    ++size;
                }
            }
        } catch(IOException e){
            e.printStackTrace();
            Bukkit.shutdown();
        }
        ConsulatAPI.getConsulatAPI().log(Level.INFO, size + " Chunks loaded in " + (System.currentTimeMillis() - start));
    }
    
    public void saveChunks(){
        try {
            Map<Integer, Map<Integer, Set<CChunk>>> orderedChunks = new HashMap<>();
            for(CChunk chunk : chunks.values()){
                orderedChunks.computeIfAbsent(
                        chunk.getX() >> SHIFT_CLAIMS,
                        v -> new HashMap<>()).computeIfAbsent(chunk.getZ() >> SHIFT_CLAIMS,
                        v -> new TreeSet<>()).add(chunk);
            }
            for(Map.Entry<Integer, Map<Integer, Set<CChunk>>> claimX : orderedChunks.entrySet()){
                for(Map.Entry<Integer, Set<CChunk>> chunkZ : claimX.getValue().entrySet()){
                    File file = FileUtils.loadFile(ConsulatAPI.getConsulatAPI().getDataFolder(), "chunks/" + claimX.getKey() + "." + chunkZ.getKey() + ".dat");
                    if(!file.exists()){
                        if(!file.createNewFile()){
                            throw new IOException("Couldn't create file.");
                        }
                    }
                    CompoundTag chunks = new CompoundTag();
                    ListTag<CompoundTag> listChunks = new ListTag<>(NBTType.COMPOUND);
                    for(CChunk chunk : chunkZ.getValue()){
                        listChunks.addTag(chunk.saveNBT());
                    }
                    chunks.put("Chunks", listChunks);
                    NBTOutputStream os = new NBTOutputStream(file, chunks);
                    os.write("Region");
                    os.close();
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
        return getChunk(location.getChunk().getX(), location.getChunk().getZ());
    }
    
    public CChunk getChunk(Chunk chunk){
        return getChunk(chunk.getX(), chunk.getZ());
    }
    
    public CChunk getChunk(int x, int z){
        return getChunk(CChunk.convert(x, z));
    }
    
    public CChunk getChunk(long coords){
        return chunks.get(coords);
    }
   
    public void addChunk(CChunk chunk){
        CChunk previous = chunks.put(chunk.getCoordinates(), chunk);
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
    
    public boolean removeChunk(CChunk chunk, boolean replaceByCChunk){
        if(replaceByCChunk){
            addChunk(new CChunk(chunk));
            return true;
        } else {
            return chunks.remove(chunk.getCoordinates()) != null;
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
    
    public Collection<CChunk> getChunks(){
        return Collections.unmodifiableCollection(chunks.values());
    }
    
    @EventHandler
    public void onLoad(ChunkLoadEvent event){
        CChunk chunk = chunks.get(CChunk.convert(event.getChunk().getX(), event.getChunk().getZ()));
        System.out.println("Loading chunk " + chunk);
        if(event.getWorld() == Bukkit.getWorlds().get(0) && (chunk == null || chunk.isNeedLimitSync())){
            if(chunk == null){
                chunk = new CChunk(event.getChunk().getX(), event.getChunk().getZ());
                addChunk(chunk);
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
