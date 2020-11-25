package io.notnot.kafkatalkdemos.domain;

import com.google.gson.annotations.SerializedName;
import org.immutables.gson.Gson;
import org.immutables.value.Value;

@Gson.TypeAdapters
@Value.Immutable
public interface OrderCount {
    @SerializedName("customerId")
    public abstract String getCustomerId();

    @SerializedName("count")
    public abstract  Long getCount();
}
