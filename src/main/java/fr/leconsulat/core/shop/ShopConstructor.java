package fr.leconsulat.core.shop;

@FunctionalInterface
public interface ShopConstructor {
    
    Shop construct(long coords);
    
}
