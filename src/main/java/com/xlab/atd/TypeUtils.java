package com.xlab.atd;

import java.lang.reflect.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class TypeUtils {

    public static Type getFieldType(Method method, Field field){
        Type fieldType;
        if (method != null) {
            Class<?>[] types;
            if ((types = method.getParameterTypes()).length == 1) {
                fieldType = method.getGenericParameterTypes()[0];
            } else if (types.length == 2 && types[0] == String.class && types[1] == Object.class) {
                fieldType = types[0];
            } else {
                fieldType = method.getGenericReturnType();
            }
        } else {
            fieldType = field.getGenericType();
        }

        // TODO 暂时不处理范型
//        if (clazz != null && fieldClass == Object.class && fieldType instanceof TypeVariable) {
//            TypeVariable<?> tv = (TypeVariable<?>) fieldType;
//            Type genericFieldType = getInheritGenericType(clazz, type, tv);
//            if (genericFieldType != null) {
//                this.fieldClass = TypeUtils.getClass(genericFieldType);
//                this.fieldType = genericFieldType;
//
//                isEnum = fieldClass.isEnum();
//                return;
//            }
//        }

        return fieldType;
    }

    public static Field getField(Class<?> clazz, String fieldName, Field[] declaredFields){
        for(Field field : declaredFields){
            String itemName = field.getName();
            if(fieldName.equals(itemName)){
                return field;
            }

            char c0, c1;
            if (fieldName.length() > 2
                    && (c0 = fieldName.charAt(0)) >= 'a' && c0 <= 'z'
                    && (c1 = fieldName.charAt(1)) >= 'A' && c1 <= 'Z'
                    && fieldName.equalsIgnoreCase(itemName)) {
                return field;
            }
        }
        Class<?> superClass = clazz.getSuperclass();
        if(superClass != null && superClass != Object.class){
            return getField(superClass, fieldName, superClass.getDeclaredFields());
        }
        return null;
    }
    public static String decapitalize(String name){
        if(name == null || name.length() == 0){
            return name;
        }
        if(name.length() > 1 && Character.isUpperCase(name.charAt(1)) && Character.isUpperCase(name.charAt(0))){
            return name;
        }
        char[] chars = name.toCharArray();
        chars[0] = Character.toLowerCase(chars[0]);
        return new String(chars);
    }
    private static boolean stringFilter(String str) {
        String regex = "[ `~!@#%^&*()+=|{}':;',\\[\\]<>/?~！ @#￥%……&*（）——+|{}【】‘；：”“’。，、？]|\n|\r|\t";
        Pattern pattern = Pattern.compile(regex );
        Matcher matcher = pattern.matcher(str);
        return matcher.find();
    }
    public static String getPureName(String name){
        name = name.replace("/",".").replace("[","");
        if(name.startsWith("L") && name.endsWith(";")){
            name = name.substring(1, name.length()-1);
        }
        return name;
    }
}
