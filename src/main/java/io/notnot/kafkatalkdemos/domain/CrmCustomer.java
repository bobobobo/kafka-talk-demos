package io.notnot.kafkatalkdemos.domain;

import com.google.gson.annotations.SerializedName;
import org.immutables.gson.Gson;
import org.immutables.value.Value;

@Gson.TypeAdapters
@Value.Immutable
public interface CrmCustomer {
    @SerializedName("customer_id")
    public abstract Integer getCustomerId();

    @SerializedName("customer_name")
    public abstract String getName();

    @SerializedName("customer_no")
    public abstract String getCustomerNo();

    @SerializedName("disabled")
    public abstract boolean isDisabled();
}
