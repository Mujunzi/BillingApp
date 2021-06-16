package com.lenovo.billing.views.fragment;

import android.app.Fragment;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.Nullable;
import android.util.Log;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.bumptech.glide.load.DataSource;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.load.engine.GlideException;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.target.Target;
import com.lenovo.billing.GlideApp;
import com.lenovo.billing.R;
import com.lenovo.billing.presenter.Presenter;
import com.lenovo.billing.protocol.BillingConfig;
import com.lenovo.billing.protocol.PayType;
import com.lenovo.billing.views.activity.MainActivity;
import com.lenovo.billing.views.audio.AudioUtil;
import com.lenovo.billing.views.customized.BaseFragment;
import com.lenovo.billing.views.customized.OnMultiClickListener;

import java.lang.ref.WeakReference;

import de.hdodenhof.circleimageview.CircleImageView;

/**
 * You can leave now.
 */
public class FarewellFragment extends BaseFragment {

    private static final String TAG = FarewellFragment.class.getSimpleName();

    private static final int PAY_SUCCESS = 1;
    private static final int SEE_YOU = 2;

    private Presenter presenter;

    // GUI for no shopping
    private FrameLayout flNoShopping;
    private TextView tvNickName;
    private CircleImageView civPortrait;
    private Button btBillError;
    private Button btnLeaveStore;
    private TextView tvLeaveStoreTips, tvRecognizeTips;
    private TextView tvNoshoppingExit;

    // GUI for pay result
    private LinearLayout llPayResult;
    private ImageView ivPayResult;
    private TextView tvPayResult;
    private TextView tvPayAgain;
    private TextView tvCardBalance;
    private TextView tvCardPayFailTipsBottom;

    private RequestListener<Drawable> listener;

    private MyHandler myHandler;

    public FarewellFragment() {
        // Required empty public constructor
    }

    public static FarewellFragment newInstance() {
        return new FarewellFragment();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        myHandler = new MyHandler(this);

        listener = new RequestListener<Drawable>() {
            @Override
            public boolean onLoadFailed(@Nullable GlideException e, Object model,
                                        Target<Drawable> target, boolean isFirstResource) {
                Log.d(TAG, "onException, " + e.toString() +
                        "  model:" + model + " isFirstResource: " + isFirstResource);
                return false;
            }

            @Override
            public boolean onResourceReady(Drawable resource, Object model,
                                           Target<Drawable> target, DataSource dataSource, boolean isFirstResource) {
                Log.d(TAG, "onResourceReady, " + "isFirstResource:" +
                        isFirstResource + "  model:" + model);
                return false;
            }
        };

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_farewell, container, false);

        flNoShopping = view.findViewById(R.id.fl_no_shopping);
        civPortrait = view.findViewById(R.id.civ_portrait);
        tvNickName = view.findViewById(R.id.tv_nick_name);
        btBillError = view.findViewById(R.id.bt_bill_error);
        btnLeaveStore = view.findViewById(R.id.bt_leave_store);
        tvRecognizeTips = view.findViewById(R.id.tv_recognize_again_tips);
        tvLeaveStoreTips = view.findViewById(R.id.tv_leave_store_tips);
        tvNoshoppingExit = view.findViewById(R.id.tv_exit_noshopping);

        //
        // BillingConfig.json
        //      smartEquipment.notToOpenDoorNum3IfNoShopping
        //      smartEquipment.notToOpenDoorNum3IfNoShoppingDisplayOpenDoorButton
        //
        // Cases: (notToOpenDoorNum3IfNoShopping, notToOpenDoorNum3IfNoShoppingDisplayOpenDoorButton)
        //      Case 1: (false, Don't care)
        //      Case 2: (true, false)
        //      Case 3: (true, true)
        //

        if (BillingConfig.SE_NOT_TO_OPEN_DOOR3_IF_NO_SHOPPING) {

            //
            // Case 3: (true, true)
            //

            if (BillingConfig.SE_NOT_TO_OPEN_DOOR3_IF_NO_SHOPPING_DISPLAY_OPEN_DOOR_BUTTON) {
                btnLeaveStore.setVisibility(View.VISIBLE);
                btnLeaveStore.setOnClickListener(new OnMultiClickListener() {
                    @Override
                    public void onMultiClick(View v) {
                        Log.d(TAG, "Leave store button clicked : open door #3.");
                        presenter.leaveStore(v.getId()); // open door num 3
                    }
                });
                tvNoshoppingExit.setVisibility(View.GONE);

                //
                // Case 2: (true, false)
                //

            } else {
                btnLeaveStore.setVisibility(View.GONE);
                tvNoshoppingExit.setVisibility(View.VISIBLE);
            }

            //
            // Case 1: (false, Don't care)
            //

        } else {
            btnLeaveStore.setVisibility(View.GONE);
            tvNoshoppingExit.setVisibility(View.GONE);
        }

        //
        // Support animation on btnLeaveStore.
        //

        if (BillingConfig.SE_FAREWELL_BUTTON_ANIMATION) {
            btBillError.startAnimation(AnimationUtils.loadAnimation(getActivity(), R.anim.scale_recognize_btn_anim));
            if (btnLeaveStore.getVisibility() == View.VISIBLE) {
                btnLeaveStore.startAnimation(AnimationUtils.loadAnimation(getActivity(), R.anim.scale_recognize_btn_anim));
            }
        }

        llPayResult = view.findViewById(R.id.ll_pay_result);
        ivPayResult = view.findViewById(R.id.iv_pay_result);
        tvPayResult = view.findViewById(R.id.tv_pay_result);
        tvPayAgain = view.findViewById(R.id.tv_pay_again);
        tvCardBalance = view.findViewById(R.id.tv_card_balance);
        tvCardPayFailTipsBottom = view.findViewById(R.id.tv_pay_fail_tips_en);

        return view;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        btBillError.setOnClickListener(new OnMultiClickListener() {
            @Override
            public void onMultiClick(View v) {
                billError();
            }
        });
    }

    @Override
    public void onResume() {
        super.onResume();
        Log.d(TAG, "onResume()");

        handleView();
    }

    @Override
    public void onHiddenChanged(boolean hidden) {
        super.onHiddenChanged(hidden);
        Log.d(TAG, "onHiddenChanged():hidden=" + hidden);

        if (!hidden) {
            handleView();
        }
    }

    public void setPresenter(Presenter presenter) {
        this.presenter = presenter;
    }

    private void billError() {

        presenter.refreshOrder("noShopping");
    }

    private void handleView() {

        //
        // if mainActivity.languageFlag == true : Chinese
        //

        if (((MainActivity) getActivity()).languageFlag) {
            if (btnLeaveStore.getVisibility() == View.VISIBLE) {
                btnLeaveStore.setText(R.string.leave_store);
                tvLeaveStoreTips.setText(R.string.leave_store_en);
                tvLeaveStoreTips.setVisibility(View.VISIBLE);
            }
            btBillError.setText(R.string.recognize_again);
            tvRecognizeTips.setText(R.string.recognize_again_en);
            tvRecognizeTips.setVisibility(View.VISIBLE);

            //
            // else mainActivity.languageFlag == false : English
            //

        } else {
            if (btnLeaveStore.getVisibility() == View.VISIBLE) {
                btnLeaveStore.setText(R.string.leave_store_en);
                tvLeaveStoreTips.setText(R.string.leave_store_tips);
                tvLeaveStoreTips.setVisibility(View.VISIBLE);
            }
            btBillError.setText(R.string.recognize_again_en);
            tvRecognizeTips.setText(R.string.recognize_again_tips);
            tvRecognizeTips.setVisibility(View.VISIBLE);
        }

        if (!presenter.isShopping() && !presenter.isDebt()) {
            myHandler.sendEmptyMessageDelayed(SEE_YOU, 500);

            GlideApp.with(this)
                    .asDrawable()
                    .diskCacheStrategy(DiskCacheStrategy.RESOURCE)
                    .listener(listener)
                    .load(presenter.getPortrait())
                    .into(civPortrait);

            if (presenter.getName().equals("MysteryCustomer")) {
                tvNickName.setText(getResources().getText(R.string.mystery_customer));
            } else {
                tvNickName.setText(presenter.getName());
            }

            llPayResult.setVisibility(View.GONE);
            flNoShopping.setVisibility(View.VISIBLE);

            //
            // Play audio of refreshing-when-no-shoping when the user is [1,2,3]
            //

            if (BillingConfig.SE_NOT_TO_OPEN_DOOR3_IF_NO_SHOPPING && BillingConfig.SE_FAREWELL_NO_SHOPPING_PLAY_AUDIO) {
                AudioUtil.playAudio(AudioUtil.PLEASE_CLICK_RE_RECOGNIZE);
            }

        } else {

            //
            // Hide the tips : english language pay failed UI tips bottom.
            // The view will be shown when the language is English and the pay result is failed.
            //

            tvCardPayFailTipsBottom.setVisibility(View.GONE);

            if (presenter.isPaySuccess()) {

                myHandler.sendEmptyMessageDelayed(PAY_SUCCESS, 500);

                ivPayResult.setImageResource(R.mipmap.ic_pay_success);

                String balanceStr;

                //
                // if mainActivity.languageFlag == true : Chinese
                //

                if (((MainActivity) getActivity()).languageFlag) {

                    tvPayResult.setText(R.string.pay_success);
                    tvPayResult.setTextSize(TypedValue.COMPLEX_UNIT_SP, 120);
                    tvPayAgain.setText(R.string.welcome_next_time);
                    tvPayAgain.setTextSize(TypedValue.COMPLEX_UNIT_SP, 102);

                    balanceStr = getResources().getString(R.string.card_balance);

                    //
                    // else mainActivity.languageFlag == false : English
                    //

                } else {
                    tvPayResult.setText(R.string.pay_success_en);
                    tvPayResult.setTextSize(TypedValue.COMPLEX_UNIT_SP, 75);
                    tvPayAgain.setText(R.string.welcome_next_time_en);
                    tvPayAgain.setTextSize(TypedValue.COMPLEX_UNIT_SP, 55);

                    balanceStr = getResources().getString(R.string.card_balance_en);
                }


                if (presenter.isCardPay() && presenter.getBalance() != -101) {

                    tvCardBalance.setVisibility(View.VISIBLE);
                    tvCardBalance.setText(String.format(balanceStr, presenter.getBalance() / 100.00));

                } else {

                    tvCardBalance.setVisibility(View.INVISIBLE);
                }

                presenter.setPaySuccess(false);
                presenter.setPayType(PayType.DEFAULT); // called only for employee card payment.
                presenter.setBalance(-101);

            } else {

                ivPayResult.setImageResource(R.mipmap.ic_pay_failure);

                //
                // if mainActivity.languageFlag == true : Chinese
                //

                if (((MainActivity) getActivity()).languageFlag) {

                    tvPayResult.setText(R.string.pay_failure);
                    tvPayResult.setTextSize(TypedValue.COMPLEX_UNIT_SP, 120);
                    tvPayAgain.setText(R.string.pay_again);
                    tvPayAgain.setTextSize(TypedValue.COMPLEX_UNIT_SP, 102);

                    //
                    // else mainActivity.languageFlag == false : English
                    //

                } else {
                    tvPayResult.setText(R.string.pay_failure_en);
                    tvPayResult.setTextSize(TypedValue.COMPLEX_UNIT_SP, 75);
                    tvPayAgain.setText(R.string.pay_again_en);
                    tvPayAgain.setTextSize(TypedValue.COMPLEX_UNIT_SP, 55);

                    //
                    // Show the tips : english language pay failed UI tips bottom.
                    // The view will be shown when the language is English and the pay result is failed.
                    //

                    tvCardPayFailTipsBottom.setVisibility(View.VISIBLE);
                }

                presenter.refreshOrder("recheck");
            }

            flNoShopping.setVisibility(View.GONE);
            llPayResult.setVisibility(View.VISIBLE);

        }
    }

    public static class MyHandler extends Handler {
        private final WeakReference<Fragment> fragmentReference;

        MyHandler(Fragment farewellFragment) {
            this.fragmentReference = new WeakReference<>(farewellFragment);
        }

        @Override
        public void handleMessage(Message msg) {
            FarewellFragment fragment = (FarewellFragment) fragmentReference.get();
            switch (msg.what) {
                case PAY_SUCCESS:
                    AudioUtil.playAudio(AudioUtil.PAY_SUCCESS_SEE_YOU);
                    break;
                case SEE_YOU:
                    AudioUtil.playAudio(AudioUtil.SEE_YOU);
                    break;
            }
        }
    }
}
