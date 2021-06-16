package com.lenovo.billing.model;

public enum BillState {

    CLEARED,
    RECOGNIZING,
    RECOGNIZED,
    GENERATED,
    CHECKED,
    PENDING // noshopping btn clicked, turn the bill state this.
}
