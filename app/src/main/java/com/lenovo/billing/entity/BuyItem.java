package com.lenovo.billing.entity;

import java.io.Serializable;

public class BuyItem implements Serializable {

    private String barcode;
    private int price;
    private String itemName;
    private int qty;
    private String imageUrl;


    public String getBarcode() {
        return barcode;
    }

    public void setBarcode(String barcode) {
        this.barcode = barcode;
    }

    public int getPrice() {
        return price;
    }

    public void setPrice(int price) {
        this.price = price;
    }

    public String getItemName() {
        return itemName;
    }

    public void setItemName(String itemName) {
        this.itemName = itemName;
    }

    public int getQty() {
        return qty;
    }

    public void setQty(int qty) {
        this.qty = qty;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    @Override
    public String toString() {
        return "BuyItem{" +
                "barcode='" + barcode + '\'' +
                ", price=" + price +
                ", itemName='" + itemName + '\'' +
                ", qty=" + qty +
                ", imageUrl=" + imageUrl +
                '}';
    }
}

