package io.notnot.kafkatalkdemos.domain;

import com.google.gson.annotations.SerializedName;
import org.immutables.gson.Gson;
import org.immutables.value.Value;

@Gson.TypeAdapters
@Value.Immutable
public interface OrderStatusUpdate {
    @SerializedName("orderId")
    public abstract String getOrderId();

    @SerializedName("orderStatus")
    public abstract  OrderStatus getOrderStatus();
}
