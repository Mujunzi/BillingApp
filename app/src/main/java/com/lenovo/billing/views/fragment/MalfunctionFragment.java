package com.lenovo.billing.views.fragment;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.text.Spannable;
import android.text.SpannableStringBuilder;
import android.text.style.ForegroundColorSpan;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.lenovo.billing.R;
import com.lenovo.billing.presenter.Presenter;
import com.lenovo.billing.protocol.BillingConfig;
import com.lenovo.billing.protocol.Breakdown;
import com.lenovo.billing.views.customized.BaseFragment;

public class MalfunctionFragment extends BaseFragment {

    private LinearLayout viewLayout;

    private TextView tvCode, tvContactUs;

    private int errorCode = -1;

    private static final String TAG = MalfunctionFragment.class.getSimpleName();

    private Presenter presenter;

    public static MalfunctionFragment newInstance() {

        return new MalfunctionFragment();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_malfunction, container, false);

        viewLayout = view.findViewById(R.id.ll_layout);

        tvCode = view.findViewById(R.id.tv_show_code);
        tvContactUs = view.findViewById(R.id.tv_contact_us);

        return view;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        handleView();
    }

    @Override
    public void onResume() {
        super.onResume();
        Log.d(TAG, "onResume()");
    }

    @Override
    public void onHiddenChanged(boolean hidden) {
        super.onHiddenChanged(hidden);
        Log.d(TAG, "onHiddenChanged()");

        if (!hidden) {

            handleView();
        }
    }

    public void setPresenter(Presenter presenter) {
        this.presenter = presenter;
    }

    public void setErrorCode(int errorCode) {
        this.errorCode = errorCode;
    }

    public int getErrorCode() {
        return this.errorCode;
    }

    private void handleView() {

        if (BillingConfig.BA_CONTACT_US.isEmpty()) {

            tvContactUs.setVisibility(View.GONE);

        } else {

            SpannableStringBuilder builder = new SpannableStringBuilder();
            String pre = getString(R.string.contact_us);
            builder.append(pre);
            builder.append(BillingConfig.BA_CONTACT_US);
            ForegroundColorSpan colorSpan = new ForegroundColorSpan(getResources().getColor(R.color.col_67767c));
            builder.setSpan(colorSpan, pre.length(), builder.length(), Spannable.SPAN_EXCLUSIVE_INCLUSIVE);
            tvContactUs.setText(builder);
            tvContactUs.setVisibility(View.VISIBLE);
        }


        //
        // Emergency
        //
        if (errorCode == Breakdown.EMERGENCY_CLOSE_STORE.getCode()) {

            viewLayout.setBackgroundResource(R.mipmap.emergency_bg);
            tvCode.setVisibility(View.INVISIBLE);

            //
            // Malfunction
            //
        } else {

            viewLayout.setBackgroundResource(R.mipmap.malfunction_bg);
            tvCode.setText(String.valueOf(errorCode));
            tvCode.setVisibility(View.VISIBLE);

        }

    }
}
