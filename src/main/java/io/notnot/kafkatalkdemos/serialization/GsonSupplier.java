package io.notnot.kafkatalkdemos.serialization;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.TypeAdapterFactory;

import java.util.ServiceLoader;

public class GsonSupplier {
    private static Gson gson;

    public static Gson get(){
        if (gson == null) {
            GsonBuilder gsonBuilder = new GsonBuilder();
            for (TypeAdapterFactory factory : ServiceLoader.load(TypeAdapterFactory.class)) {
                gsonBuilder.registerTypeAdapterFactory(factory);
            }
            gson = gsonBuilder.create();
        }
        return gson;

    }
}
