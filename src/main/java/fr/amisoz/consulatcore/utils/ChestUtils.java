package fr.amisoz.consulatcore.utils;

import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.data.type.Chest;
import org.jetbrains.annotations.Nullable;

public final class ChestUtils {
    
    public static boolean isChest(Material type){
        switch(type){
            case CHEST:
            case TRAPPED_CHEST:
                return true;
        }
        return false;
    }
    
    public static @Nullable Block getNextChest(Block chest){
        Chest side = (Chest)chest.getBlockData();
        if(side.getType() == Chest.Type.SINGLE){
            switch(side.getFacing()){
                case NORTH:
                case SOUTH:{
                    Block nextChest = chest.getRelative(BlockFace.EAST);
                    if(isChest(nextChest.getType()) &&
                            !isDoubleChest((org.bukkit.block.Chest)nextChest.getState()) &&
                            ((Chest)nextChest.getBlockData()).getFacing() == side.getFacing()){
                        return nextChest;
                    }
                    nextChest = chest.getRelative(BlockFace.WEST);
                    if(isChest(nextChest.getType()) &&
                            !isDoubleChest((org.bukkit.block.Chest)nextChest.getState()) &&
                            ((Chest)nextChest.getBlockData()).getFacing() == side.getFacing()){
                        return nextChest;
                    }
                    return null;
                }
                case EAST:
                case WEST:
                    Block nextChest = chest.getRelative(BlockFace.NORTH);
                    if(isChest(nextChest.getType()) &&
                            !isDoubleChest((org.bukkit.block.Chest)nextChest.getState()) &&
                            ((Chest)nextChest.getBlockData()).getFacing() == side.getFacing()){
                        return nextChest;
                    }
                    nextChest = chest.getRelative(BlockFace.SOUTH);
                    if(isChest(nextChest.getType()) &&
                            !isDoubleChest((org.bukkit.block.Chest)nextChest.getState()) &&
                            ((Chest)nextChest.getBlockData()).getFacing() == side.getFacing()){
                        return nextChest;
                    }
                    return null;
            }
        } else {
            BlockFace next = getNextChest(side);
            if(next == null){
                return null;
            }
            return chest.getRelative(next);
        }
        return null;
    }
    
    public static @Nullable BlockFace getNextChest(Chest side){
        switch(side.getFacing()){
            case NORTH:
                return side.getType() != Chest.Type.RIGHT ? BlockFace.EAST : BlockFace.WEST;
            case SOUTH:
                return side.getType() != Chest.Type.RIGHT ? BlockFace.WEST : BlockFace.EAST;
            case EAST:
                return side.getType() != Chest.Type.RIGHT ? BlockFace.SOUTH : BlockFace.NORTH;
            case WEST:
                return side.getType() != Chest.Type.RIGHT ? BlockFace.NORTH : BlockFace.SOUTH;
        }
        return null;
    }
    
    public static void setChestsSingle(Block chest, Block otherChest){
        Chest chestData = (Chest)chest.getBlockData();
        Chest otherChestData = ((Chest)otherChest.getBlockData());
        chestData.setType(Chest.Type.SINGLE);
        otherChestData.setType(Chest.Type.SINGLE);
        chest.setBlockData(chestData);
        otherChest.setBlockData(otherChestData);
    }
    
    public static boolean isDoubleChest(org.bukkit.block.Chest chest){
        return ((Chest)chest.getBlock().getBlockData()).getType() != Chest.Type.SINGLE;
    }
    
    public static void setChestDouble(Block chest, Block otherChest){
        Chest chestData = (Chest)chest.getBlockData();
        Chest otherChestData = ((Chest)otherChest.getBlockData());
        switch(chestData.getFacing()){
            case NORTH:
                if(chest.getLocation().getBlockX() - otherChest.getLocation().getBlockX() == -1){
                    chestData.setType(Chest.Type.LEFT);
                    otherChestData.setType(Chest.Type.RIGHT);
                } else {
                    chestData.setType(Chest.Type.RIGHT);
                    otherChestData.setType(Chest.Type.LEFT);
                }
                break;
            case SOUTH:
                if(chest.getLocation().getBlockX() - otherChest.getLocation().getBlockX() == -1){
                    chestData.setType(Chest.Type.RIGHT);
                    otherChestData.setType(Chest.Type.LEFT);
                } else {
                    chestData.setType(Chest.Type.LEFT);
                    otherChestData.setType(Chest.Type.RIGHT);
                }
                break;
            case EAST:
                if(chest.getLocation().getBlockZ() - otherChest.getLocation().getBlockZ() == -1){
                    chestData.setType(Chest.Type.LEFT);
                    otherChestData.setType(Chest.Type.RIGHT);
                } else {
                    chestData.setType(Chest.Type.RIGHT);
                    otherChestData.setType(Chest.Type.LEFT);
                }
                break;
            case WEST:
                if(chest.getLocation().getBlockZ() - otherChest.getLocation().getBlockZ() == -1){
                    chestData.setType(Chest.Type.RIGHT);
                    otherChestData.setType(Chest.Type.LEFT);
                } else {
                    chestData.setType(Chest.Type.LEFT);
                    otherChestData.setType(Chest.Type.RIGHT);
                }
                break;
        }
        
        chest.setBlockData(chestData);
        otherChest.setBlockData(otherChestData);
    }
}
