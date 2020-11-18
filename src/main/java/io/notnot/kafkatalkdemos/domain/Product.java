package io.notnot.kafkatalkdemos.domain;

import com.google.gson.annotations.SerializedName;
import org.immutables.gson.Gson;
import org.immutables.value.Value;

@Gson.TypeAdapters
@Value.Immutable
public interface Product {
    @SerializedName("id")
    public abstract String getId();

    @SerializedName("name")
    public abstract  String getName();
}
