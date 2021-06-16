package com.lenovo.billing.model;

import android.util.Log;

import com.lenovo.billing.entity.Bill;
import com.lenovo.billing.entity.CustomerInfo;
import com.lenovo.billing.entity.CustomerStatus;
import com.lenovo.billing.protocol.PayType;

import org.json.JSONArray;
import org.json.JSONException;

//
// structure of BillSub:
//
//      number
//
//      billState
//      
//      theFirstTimeGetFaceId
//      faceId
//      customerInfo:
//           info:
//               customerId
//               customerName
//               portrait
//      mUserIdentified
//      
//      aTidList
//      mItemsRecognized
//      
//      billData:
//           orderId
//           qrCodeText
//           amount
//           totalCount
//           debtAmount
//           itemList
//           customerStatus:
//               customerId
//               oneStepPayment
//               shopping
//               debt
//               debtItems
//               oneStepPaymentFalseCode
//      
//      refreshFrom
//      
//      payType
//      balance
// 

public class BillSub {

    private static final String TAG = BillSub.class.getSimpleName();

    public int number = 0;
    public BillState billState;

    public boolean theFirstTimeGetFaceId;   // get faceId onetime
    public String faceId;                   // faceId
    public CustomerInfo.Data.Info customerInfo;
    public boolean mUserIdentified;

    public JSONArray aTidList;              // tidList
    public boolean mItemsRecognized;

    public Bill.Data billData;

    public String refreshFrom = "";         // refresh source

    public PayType payType;
    public int balance = -101;              // employee card balance

    synchronized
    public void clear() {
        Log.d(TAG, "clear()");
        
        this.number += 1;
        Log.d(TAG, "number := " + this.number);
        
        this.billState = BillState.CLEARED;
        this.theFirstTimeGetFaceId = false;
        this.faceId = "-100";
        this.customerInfo = null;
        this.mUserIdentified = false;   // avoid the bug of "info is null."
        try {
            this.aTidList = new JSONArray("[]");
        } catch (JSONException e) {
            e.printStackTrace();
        }
        this.mItemsRecognized = false;
        this.billData = null;
        this.refreshFrom = "";
        this.payType = PayType.DEFAULT;
        this.balance = -101;
    }

    synchronized
    public CustomerStatus getCustomerStatus() {
        if (billData == null) {
            Log.d(TAG, "getCustomerStatus() Error. billData is null.");
            return null;
        }
        return billData.getCustomerStatus();
    }

    public boolean isShopping() {

        boolean isShopping = false;
        if (getCustomerStatus() != null) {
            isShopping = getCustomerStatus().isShopping();
        }

        return isShopping;
    }

    public boolean isDebt() {

        boolean isDebt = false;
        if (getCustomerStatus() != null) {
            isDebt = getCustomerStatus().isDebt();
        }

        return isDebt;
    }

    public boolean isOneStepPayment() {

        boolean isOneStepPayment = false;
        if (getCustomerStatus() != null) {
            isOneStepPayment = getCustomerStatus().isOneStepPayment();
        }

        return isOneStepPayment;
    }

    public boolean isRealOneStepPayment() {

        return isOneStepPayment() && (getCustomerStatus().getOneStepPaymentFalseCode() == 0);
    }

    public void setNumber(int num){
        number = num;
    }

    public int getNumber(){
        return number;
    }
}
