package io.notnot.kafkatalkdemos.domain;

import com.google.gson.annotations.SerializedName;
import org.immutables.gson.Gson;
import org.immutables.value.Value;

import java.util.List;

@Gson.TypeAdapters
@Value.Immutable
public interface Order {
    @SerializedName("id")
    public abstract String getId();

    @SerializedName("customerId")
    public abstract String getCustomerId();

    @SerializedName("productIds")
    public abstract List<String> getProductIds();
}
