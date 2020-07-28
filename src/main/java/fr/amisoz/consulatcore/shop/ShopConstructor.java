package fr.amisoz.consulatcore.shop;

@FunctionalInterface
public interface ShopConstructor {
    
    Shop construct(long coords);
    
}
