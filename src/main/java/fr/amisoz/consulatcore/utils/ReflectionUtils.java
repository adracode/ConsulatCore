package fr.amisoz.consulatcore.utils;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class ReflectionUtils {
    
    public static List<Field> getAllDeclaredFields(Class<?> start){
        List<Field> fields = new ArrayList<>(Arrays.asList(start.getDeclaredFields()));
        Class<?> superClass = start.getSuperclass();
        if(superClass != null && !superClass.equals(Object.class)){
            fields.addAll(getAllDeclaredFields(superClass));
        }
        return fields;
    }
    
    public static List<Class<?>> getAllSuperClasses(Class<?> start){
        List<Class<?>> classes = new ArrayList<>(Collections.singleton(start));
        Class<?> superClass = start.getSuperclass();
        if(superClass != null && !superClass.equals(Object.class)){
            classes.addAll(getAllSuperClasses(superClass));
        }
        return classes;
    }
    
    public static Object getDeclaredField(Object instance, Field field){
        try {
            if(!field.isAccessible()){
                field.setAccessible(true);
            }
            return field.get(instance);
        } catch(IllegalAccessException e){
            e.printStackTrace();
        }
        return new Object();
    }
    
    public static Object getDeclaredField(Object instance, String name){
        try {
            Field field = instance.getClass().getDeclaredField(name);
            if(!field.isAccessible()){
                field.setAccessible(true);
            }
            return field.get(instance);
        } catch(NoSuchFieldException | IllegalAccessException e){
            e.printStackTrace();
        }
        return new Object();
    }
    
    public static Field getDeclaredField(Class<?> c, String name){
        try {
            return c.getDeclaredField(name);
        } catch(NoSuchFieldException e){
            e.printStackTrace();
        }
        return null;
    }
    
    public static Method getDeclaredMethod(Object instance, String name, Class<?>... parameters){
        try {
            return instance.getClass().getDeclaredMethod(name, parameters);
        } catch(NoSuchMethodException e){
            e.printStackTrace();
        }
        return null;
    }
    
    public static Object invoke(Object instance, Method method, Object... args){
        if(!method.isAccessible()){
            method.setAccessible(true);
        }
        try {
            return method.invoke(instance, args);
        } catch(IllegalAccessException | InvocationTargetException e){
            e.printStackTrace();
        }
        return null;
    }
    
    public static void setField(Field field, Object instance, Object value){
        if(Modifier.isFinal(field.getModifiers()) && Modifier.isStatic(field.getModifiers())){
            return;
        }
        if(!field.isAccessible()){
            field.setAccessible(true);
        }
        try {
            field.set(instance, value);
        } catch(IllegalAccessException e){
            e.printStackTrace();
        }
    }
    
    public static boolean isSuper(Class<?> searchedClass, Class<?> c){
        if(c == null){
            return false;
        }
        if(c.equals(searchedClass)){
            return true;
        }
        return isSuper(searchedClass, c.getSuperclass());
    }
    
}
