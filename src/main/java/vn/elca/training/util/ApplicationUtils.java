package vn.elca.training.util;

import org.springframework.stereotype.Component;

@Component
public class ApplicationUtils {
    public static boolean isLong(String value){
        try{
            Long.valueOf(value);
            return true;
        } catch (NumberFormatException exception){
            return false;
        }
    }
}
