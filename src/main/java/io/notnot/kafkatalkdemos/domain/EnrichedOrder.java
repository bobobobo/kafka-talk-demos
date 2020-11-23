package io.notnot.kafkatalkdemos.domain;

import com.google.gson.annotations.SerializedName;
import org.immutables.gson.Gson;
import org.immutables.value.Value;

import java.util.List;

@Gson.TypeAdapters
@Value.Immutable
public interface EnrichedOrder {
    @SerializedName("id")
    public abstract String getId();

    @SerializedName("customer")
    public abstract Customer getCustomer();

    @SerializedName("products")
    public abstract List<Product> getProduct();

    @SerializedName("status")
    @Value.Default default OrderStatus getStatus() {
        return OrderStatus.CREATED;
    }
}
