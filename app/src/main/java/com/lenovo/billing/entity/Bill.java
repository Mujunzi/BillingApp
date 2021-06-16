package com.lenovo.billing.entity;

import java.io.Serializable;
import java.util.ArrayList;

public class Bill implements Serializable {

    private String status;
    private int code;
    private Data data;
    private String message;

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public int getCode() {
        return code;
    }

    public void setCode(int code) {
        this.code = code;
    }

    public Data getData() {
        return data;
    }

    public void setData(Data data) {
        this.data = data;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    @Override
    public String toString() {
        return "Bill{" +
                "status='" + status + '\'' +
                ", code=" + code +
                ", data=" + data +
                ", message='" + message + '\'' +
                '}';
    }

    public class Data implements Serializable {

        private String orderId;
        private String qrCodeText;
        private int amount;
        private int totalCount;
        private int debtAmount;
        private CustomerStatus customerStatus;
        private ArrayList<BuyItem> itemList;

        private ArrayList<String> unknownItemList;
        private ArrayList<String> soldItemList;

        private int finalAmount;
        private int totalAmount;
        private int shoppingAmount;
        private int couponAmount;

        public int getFinalAmount() {
            return finalAmount;
        }

        public void setFinalAmount(int finalAmount) {
            this.finalAmount = finalAmount;
        }

        public int getTotalAmount() {
            return totalAmount;
        }

        public void setTotalAmount(int totalAmount) {
            this.totalAmount = totalAmount;
        }

        public int getShoppingAmount() {
            return shoppingAmount;
        }

        public void setShoppingAmount(int shoppingAmount) {
            this.shoppingAmount = shoppingAmount;
        }

        public int getCouponAmount() {
            return couponAmount;
        }

        public void setCouponAmount(int couponAmount) {
            this.couponAmount = couponAmount;
        }

        public String getOrderId() {
            return orderId;
        }

        public void setOrderId(String orderId) {
            this.orderId = orderId;
        }

        public String getQrCodeText() {
            return qrCodeText;
        }

        public void setQrCodeText(String qrCodeText) {
            this.qrCodeText = qrCodeText;
        }

        public int getAmount() {
            return amount;
        }

        public void setAmount(int amount) {
            this.amount = amount;
        }

        public int getTotalCount() {
            return totalCount;
        }

        public void setTotalCount(int totalCount) {
            this.totalCount = totalCount;
        }

        public int getDebtAmount() {
            return debtAmount;
        }

        public void setDebtAmount(int debtAmount) {
            this.debtAmount = debtAmount;
        }

        public CustomerStatus getCustomerStatus() {
            return customerStatus;
        }

        public void setCustomerStatus(CustomerStatus customerStatus) {
            this.customerStatus = customerStatus;
        }

        public ArrayList<BuyItem> getItemList() {
            return itemList;
        }

        public void setItemList(ArrayList<BuyItem> buyItemList) {
            this.itemList = itemList;
        }

        public ArrayList<String> getUnknownItemList() {
            return unknownItemList;
        }

        public void setUnknownItemList(ArrayList<String> unknownItemList) {
            this.unknownItemList = unknownItemList;
        }

        public ArrayList<String> getSoldItemList() {
            return soldItemList;
        }

        public void setSoldItemList(ArrayList<String> soldItemList) {
            this.soldItemList = soldItemList;
        }

        @Override
        public String toString() {
            return "Data{" +
                    "orderId='" + orderId + '\'' +
                    ", qrCodeText='" + qrCodeText + '\'' +
                    ", amount=" + amount +
                    ", debtAmount=" + debtAmount +
                    ", totalCount=" + totalCount +
                    ", customerStatus=" + customerStatus +
                    ", finalAmount=" + finalAmount +
                    ", totalAmount=" + totalAmount +
                    ", shoppingAmount=" + shoppingAmount +
                    ", couponAmount=" + couponAmount +
                    ", buyItemList=" + itemList +
                    ", unknownItemList=" + unknownItemList +
                    ", soldItemList=" + soldItemList +
                    '}';
        }

        public boolean isUseable() {
            return !(orderId == null || "".equals(orderId) || orderId.isEmpty())
                    &&
                    amount > 0
                    &&
                    itemList != null
                    &&
                    itemList.size() > 0
                    &&
                    customerStatus != null;
        }
    }
}