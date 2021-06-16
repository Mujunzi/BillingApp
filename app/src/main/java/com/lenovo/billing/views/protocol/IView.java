package com.lenovo.billing.views.protocol;

public interface IView {
    void setDefaultFragment();

    void showRecognitionFragment();

    void refreshRecognitionFragment(String source); // source: "noShopping", "refresh"

    void showPaymentFragment ();

    void showFarewellFragment ();

    void toWelcomeFragment();

    void toMalfunctionFragment(int errorCode);

    void toRecoverState();
}
