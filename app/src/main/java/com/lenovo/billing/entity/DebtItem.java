package com.lenovo.billing.entity;

public class DebtItem {

    private String name;
    private int price;
    private int qty;
    private String imageUrl;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getPrice() {
        return price;
    }

    public void setPrice(int price) {
        this.price = price;
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
        return "DebtItem{" +
                "name='" + name + '\'' +
                ", price=" + price +
                ", qty=" + qty +
                ", imageUrl=" + imageUrl +
                '}';
    }
}
