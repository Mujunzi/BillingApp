package com.lenovo.billing.views.fragment;

import android.os.Bundle;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.lenovo.billing.R;
import com.lenovo.billing.presenter.Presenter;
import com.lenovo.billing.views.customized.BaseFragment;

/**
 * Default fragment when app started.
 */
public class WelcomeFragment extends BaseFragment{

    private static final String TAG = WelcomeFragment.class.getSimpleName();

    private Presenter presenter;

    public WelcomeFragment() {
        // Required empty public constructor
    }

    public static WelcomeFragment newInstance() {
        return new WelcomeFragment();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_welcome, container, false);
    }

    @Override
    public void onResume() {
        super.onResume();
        Log.d(TAG, "onResume()");
    }

    @Override
    public void onHiddenChanged(boolean hidden) {
        super.onHiddenChanged(hidden);
        Log.d(TAG, "onHiddenChanged() hidden = " + hidden);        
    }

    public void setPresenter(Presenter presenter) {
        this.presenter = presenter;
    }
}
