package fr.amisoz.consulatcore.shop;

//TODO: à changer
public enum ShopEnum {
    POLAR_BEAR_SPAWN_EGG("POLAR_BEAR_SPAWN_EGG", "POLAR", (byte)0),
    TURTLE_SPAWN_EGG("TURTLE_SPAWN_EGG", "TURTLE", (byte)0),
    PANDA_SPAWN_EGG("PANDA_SPAWN_EGG", "PANDA", (byte)0),
    CHICKEN_SPAWN_EGG("CHICKEN_SPAWN_EGG", "CHICKEN", (byte)0),
    COW_SPAWN_EGG("COW_SPAWN_EGG", "COW", (byte)0),
    ENCHANTED_GOLDEN_APPLE("ENCHANTED_GOLDEN_APPLE", "CHEAT_APPLE", (byte)0),
    WITHER_SKELETON_SKULL("WITHER_SKELETON_SKULL", "WITHER_SKULL", (byte)0),
    EXPERIENCE_BOTTLE("EXPERIENCE_BOTTLE", "XP_BOTTLE", (byte)0),
    PRISMARINE_BRICKS("PRISMARINE_BRICKS", "PRISMARINE_B", (byte)0);

    private String material, materialNew;
    private byte metadata;
    
    ShopEnum(final String material, final String materialNew, byte metadata) {
        this.material = material;
        this.materialNew = materialNew;
        this.metadata = metadata;
    }
    
    public String getNewMaterialName() 
    {
        return this.materialNew;
    }
    
    public String getOldMaterialName() 
    {
        return this.material;
    }
    
    public byte getMetadata() 
    {
        return this.metadata;
    }
}
