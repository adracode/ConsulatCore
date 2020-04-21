package fr.amisoz.consulatcore.utils;

import java.lang.reflect.Field;

public class ObjectUtils {
    
    public static String getDeepContent(Object o){
        StringBuilder builder = new StringBuilder("Classes: ");
        for(Class<?> c : ReflectionUtils.getAllSuperClasses(o.getClass())){
            builder.append(c).append(", ");
        }
        builder.deleteCharAt(builder.length() - 1).append("\n");
        for(Field field : ReflectionUtils.getAllDeclaredFields(o.getClass())){
            builder.append(field.getName()).append(":").append(ReflectionUtils.getDeclaredField(o, field)).append("\n");
        }
        return builder.toString();
    }
    
}
