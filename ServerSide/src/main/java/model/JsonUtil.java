package model;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

public class JsonUtil {

    private static final String DATE_FORMAT = "yyyy-MM-dd HH:mm:ss";

    public static Gson createGson() {
        return new GsonBuilder()
                .setDateFormat(DATE_FORMAT)
                .create();
    }
}