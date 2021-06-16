package com.lenovo.billing.entity;

import java.util.ArrayList;

public class CustomerStatus {

    private boolean oneStepPayment;
    private String customerId;
    private ArrayList<DebtItem> debtItems;
    private boolean debt;
    private boolean shopping;
    private int oneStepPaymentAmountPerTime;
    private int oneStepPaymentFalseCode;
    private int oneStepPaymentCountPerDay;

    public boolean isOneStepPayment() {
        return oneStepPayment;
    }

    public void setOneStepPayment(boolean oneStepPayment) {
        this.oneStepPayment = oneStepPayment;
    }

    public String getCustomerId() {
        return customerId;
    }

    public void setCustomerId(String customerId) {
        this.customerId = customerId;
    }

    public ArrayList<DebtItem> getDebtItems() {
        return debtItems;
    }

    public void setDebtItems(ArrayList<DebtItem> debtItems) {
        this.debtItems = debtItems;
    }

    public boolean isDebt() {
        return debt;
    }

    public void setDebt(boolean debt) {
        this.debt = debt;
    }

    public boolean isShopping() {
        return shopping;
    }

    public void setShopping(boolean shopping) {
        this.shopping = shopping;
    }

    public int getOneStepPaymentAmountPerTime() {
        return oneStepPaymentAmountPerTime;
    }

    public void setOneStepPaymentAmountPerTime(int oneStepPaymentAmountPerTime) {
        this.oneStepPaymentAmountPerTime = oneStepPaymentAmountPerTime;
    }

    public int getOneStepPaymentFalseCode() {
        return oneStepPaymentFalseCode;
    }

    public void setOneStepPaymentFalseCode(int oneStepPaymentFalseCode) {
        this.oneStepPaymentFalseCode = oneStepPaymentFalseCode;
    }

    public int getOneStepPaymentCountPerDay() {
        return oneStepPaymentCountPerDay;
    }

    public void setOneStepPaymentCountPerDay(int oneStepPaymentCountPerDay) {
        this.oneStepPaymentCountPerDay = oneStepPaymentCountPerDay;
    }

    @Override
    public String toString() {
        return "CustomerStatus{" +
                "oneStepPayment=" + oneStepPayment +
                ", customerId='" + customerId + '\'' +
                ", debtItems=" + debtItems +
                ", debt=" + debt +
                ", shopping=" + shopping +
                ", oneStepPaymentAmountPerTime=" + oneStepPaymentAmountPerTime +
                ", oneStepPaymentFalseCode=" + oneStepPaymentFalseCode +
                ", oneStepPaymentCountPerDay=" + oneStepPaymentCountPerDay +
                '}';
    }
}
