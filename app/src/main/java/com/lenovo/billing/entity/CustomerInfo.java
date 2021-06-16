package com.lenovo.billing.entity;

import java.io.Serializable;

public class CustomerInfo implements Serializable{

    private String status;
    private int code;
    private Data data;
    private String message;

    public void setStatus(String status) {
        this.status = status;
    }

    public String getStatus() {
        return status;
    }

    public void setCode(int code) {
        this.code = code;
    }

    public int getCode() {
        return code;
    }

    public void setData(Data data) {
        this.data = data;
    }

    public Data getData() {
        return data;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getMessage() {
        return message;
    }

    @Override
    public String toString() {
        return "CustomerInfo{" +
                "status='" + status + '\'' +
                ", code=" + code +
                ", data=" + data +
                ", message='" + message + '\'' +
                '}';
    }

    public class Data implements Serializable{

        private Info customerInfo;

        public void setInfo(Info info) {
            this.customerInfo = info;
        }

        public Info getInfo() {
            return customerInfo;
        }

        @Override
        public String toString() {
            return "Data{" +
                    "info=" + customerInfo +
                    '}';
        }

        public class Info implements Serializable{

            private boolean wechatOneStepPayment;
            private String customerId;
            private boolean credit;
            private String portrait;
            private boolean aliOneStepPayment;
            private String customerName;

            public void setWechatOneStepPayment(boolean wechatOneStepPayment) {
                this.wechatOneStepPayment = wechatOneStepPayment;
            }

            public boolean getWechatOneStepPayment() {
                return wechatOneStepPayment;
            }

            public void setCustomerId(String customerId) {
                this.customerId = customerId;
            }

            public String getCustomerId() {
                return customerId;
            }

            public void setCredit(boolean credit) {
                this.credit = credit;
            }

            public boolean getCredit() {
                return credit;
            }

            public void setPortrait(String portrait) {
                this.portrait = portrait;
            }

            public String getPortrait() {
                return portrait;
            }

            public void setAliOneStepPayment(boolean aliOneStepPayment) {
                this.aliOneStepPayment = aliOneStepPayment;
            }

            public boolean getAliOneStepPayment() {
                return aliOneStepPayment;
            }

            public void setCustomerName(String customerName) {
                this.customerName = customerName;
            }

            public String getCustomerName() {
                return customerName;
            }

            @Override
            public String toString() {
                return "Info{" +
                        "wechatOneStepPayment=" + wechatOneStepPayment +
                        ", customerId='" + customerId + '\'' +
                        ", credit=" + credit +
                        ", portrait='" + portrait + '\'' +
                        ", aliOneStepPayment=" + aliOneStepPayment +
                        ", customerName='" + customerName + '\'' +
                        '}';
            }
        }
    }
}
