package com.lenovo.billing.views.fragment;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.Nullable;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.Spannable;
import android.text.SpannableStringBuilder;
import android.text.style.ForegroundColorSpan;
import android.transition.TransitionInflater;
import android.util.Log;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.bumptech.glide.load.DataSource;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.load.engine.GlideException;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.target.Target;
import com.lenovo.billing.GlideApp;
import com.lenovo.billing.R;
import com.lenovo.billing.entity.BuyItem;
import com.lenovo.billing.entity.DebtItem;
import com.lenovo.billing.presenter.Presenter;
import com.lenovo.billing.protocol.BillingConfig;
import com.lenovo.billing.views.activity.MainActivity;
import com.lenovo.billing.views.audio.AudioUtil;
import com.lenovo.billing.views.adapter.DebtAdapter;
import com.lenovo.billing.views.adapter.BillAdapter;
import com.lenovo.billing.views.customized.BaseFragment;
import com.lenovo.billing.common.QRCodeUtil;
import com.lenovo.billing.views.customized.OnMultiClickListener;

import java.util.ArrayList;
import java.util.Objects;

/**
 * Payment fragment where show qrcode.
 */
public class PaymentFragment extends BaseFragment {

    private static final String TAG = PaymentFragment.class.getSimpleName();

    private RecyclerView billRecyclerView, debtRecyclerView;
    private TextView tvAmount, tvFinalMoney, tvNickName, tvWithoutCodeLabel, tvCheckTips;
    private ImageView ivWe, ivPortrait;
    private Button btRefresh, btConfirm, btOpenDoor3;
    private View debtAbout;
    private TextView tvDebtBillTotalTips, tvDebtSubtotalMoney, tvDebtTitle, tvNewBillTitle;
    private LinearLayout llShopingPage, llQRCodePage, llDebtSubtotal, llCouponLayout;
    private TextView tvOverrun, tvPayHint, tvScanWechatAlipay;
    private Button btnNotMe;
    private TextView tvCardPay;
    private ImageView ivRedArrows;
    private TextView tvCount, tvCoupon, tvTotalMoney, tvNullGoodsTips;
    private Button btnLanguageSwitch;

    private RelativeLayout rlExpectNextMeet;
    private ImageView ivCustomerPortrait;
    private TextView tvCustomerNickName, tvNoshoppingRefreshTips, tvExpectNextMeet;
    private Button btnRefreshNoshopping;

    private TextView tvPayAmount, tvPayCount, tvPayMoney;

    private RecyclerView.Adapter billAdapter, debtAdapter;

    private Presenter presenter;

    private ArrayList<BuyItem> buyItems = new ArrayList<>();
    private ArrayList<DebtItem> debtItems = new ArrayList<>();

    private RequestListener<Drawable> listener;

    // delay to show openDoor3 btn flag. true : need delay; false : step delay
    private boolean delayShowOpenDoor3BtnSwitch = true;

    private final int ENABLE_OPENDOOR3_BUTTON = 123;
    @SuppressLint("HandlerLeak")
    private Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what) {
                case ENABLE_OPENDOOR3_BUTTON:
                    btOpenDoor3.setClickable(true);
                    btOpenDoor3.setVisibility(View.VISIBLE);
                    delayShowOpenDoor3BtnSwitch = false;
                    break;
                default:
                    break;
            }
        }
    };

    public PaymentFragment() {
        // Required empty public constructor
    }

    public static PaymentFragment newInstance() {
        return new PaymentFragment();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d(TAG, "onCreate()");

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            setSharedElementEnterTransition(
                    TransitionInflater.from(getActivity())
                            .inflateTransition(android.R.transition.move));
        }

        listener = new RequestListener<Drawable>() {
            @Override
            public boolean onLoadFailed(@Nullable GlideException e, Object model,
                                        Target<Drawable> target, boolean isFirstResource) {
                Log.d(TAG, "onException() " + e.toString());
                Log.d(TAG, "model = " + model + ", isFirstResource = " + isFirstResource);
                return false;
            }

            @Override
            public boolean onResourceReady(Drawable resource, Object model, Target<Drawable> target,
                                           DataSource dataSource, boolean isFirstResource) {
                Log.d(TAG, "onResourceReady() isFromMemoryCache = " +
                        ", model = " + model +
                        ", isFirstResource = " + isFirstResource);
                return false;
            }
        };

    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        Log.d(TAG, "onAttach()");
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        Log.d(TAG, "onCreateView()");

        View view = inflater.inflate(R.layout.fragment_payment, container, false);

        llShopingPage = view.findViewById(R.id.ll_bill_list);
        llQRCodePage = view.findViewById(R.id.ll_bill_qrcode);

        rlExpectNextMeet = view.findViewById(R.id.rl_expect_next_meet);
        ivCustomerPortrait = view.findViewById(R.id.iv_customer_portrait);
        tvCustomerNickName = view.findViewById(R.id.tv_customer_nickname);
        tvNoshoppingRefreshTips = view.findViewById(R.id.tv_noshopping_tips);
        btnRefreshNoshopping = view.findViewById(R.id.btn_refresh_noshopping);
        tvExpectNextMeet = view.findViewById(R.id.tv_expect_next_meet);


        tvAmount = view.findViewById(R.id.tv_amount);
        tvFinalMoney = view.findViewById(R.id.tv_final_money);

        llCouponLayout = view.findViewById(R.id.ll_coupon_layout);
        tvCoupon = view.findViewById(R.id.tv_coupon);
        tvTotalMoney = view.findViewById(R.id.tv_total_money);
        tvNullGoodsTips = view.findViewById(R.id.tv_null_goods_tips);

        btRefresh = view.findViewById(R.id.pay_refresh_btn);
        btConfirm = view.findViewById(R.id.confirm_btn);
        btOpenDoor3 = view.findViewById(R.id.btn_zero_goods_open_door3);

        ivWe = view.findViewById(R.id.qr_code_iv);

        billRecyclerView = view.findViewById(R.id.shopping_list_rv);

        tvNickName = view.findViewById(R.id.name_tv);
        ivPortrait = view.findViewById(R.id.photo_iv);
        tvWithoutCodeLabel = view.findViewById(R.id.tv_without_code_label);

        tvCheckTips = view.findViewById(R.id.check_tv);

        debtAbout = view.findViewById(R.id.debt_about);
        debtRecyclerView = view.findViewById(R.id.rv_debt);
        tvDebtBillTotalTips = view.findViewById(R.id.tv_subtotal_label);
        tvDebtSubtotalMoney = view.findViewById(R.id.tv_subtotal_money_debt);
        tvDebtTitle = view.findViewById(R.id.tv_debt_label);
        tvNewBillTitle = view.findViewById(R.id.tv_this_time_label);

        llDebtSubtotal = view.findViewById(R.id.ll_subtotal_debt);

        tvOverrun = view.findViewById(R.id.tv_overrun);
        tvPayHint = view.findViewById(R.id.tv_pay_hint);
        tvScanWechatAlipay = view.findViewById(R.id.tv_scan_wechat_alipay);

        btnNotMe = view.findViewById(R.id.btn_not_me);

        tvCardPay = view.findViewById(R.id.tv_card_pay);
        ivRedArrows = view.findViewById(R.id.iv_red_arrows);

        tvCount = view.findViewById(R.id.tv_count);

        btnLanguageSwitch = view.findViewById(R.id.btn_change_language);

        tvPayAmount = view.findViewById(R.id.tv_pay_amount);
        tvPayCount = view.findViewById(R.id.tv_pay_count);
        tvPayMoney = view.findViewById(R.id.tv_pay_money);

        return view;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        Log.d(TAG, "onActivityCreated()");

        btnLanguageSwitch.setOnClickListener(new OnMultiClickListener() {
            @Override
            public void onMultiClick(View v) {
                switchLanguage();
            }
        });

        btRefresh.setOnClickListener(new OnMultiClickListener() {
            @Override
            public void onMultiClick(View v) {
                payRefresh("refresh");
            }
        });

        btConfirm.setOnClickListener(new OnMultiClickListener() {
            @Override
            public void onMultiClick(View v) {
                // stop reader -> Billing.generateBillCallBack() -> gen-bill to born QRCode
                presenter.stopReader(true);
                handleView(true);
                Log.e("cpb", "PaymentFragment: onMultiClick: 点击了按钮停止扫描");
            }
        });

        btOpenDoor3.setOnClickListener(new OnMultiClickListener() {
            @Override
            public void onMultiClick(View v) {
                // stop reader -> openDoor3 -> showRefreshItems page
                noShoppingOpenDoor3(v.getId());
            }
        });

        btnRefreshNoshopping.setOnClickListener(new OnMultiClickListener() {
            @Override
            public void onMultiClick(View v) {

                payRefresh("refreshAndNoShopping");
            }
        });

        btnNotMe.setOnClickListener(new OnMultiClickListener() {
            @Override
            public void onMultiClick(View v) {
                whoAmI();
            }
        });

        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(getActivity(),
                LinearLayoutManager.VERTICAL,
                false);
        layoutManager.setAutoMeasureEnabled(true);

        RecyclerView.LayoutManager debtLayoutManager = new LinearLayoutManager(getActivity(),
                LinearLayoutManager.VERTICAL,
                false);
        debtLayoutManager.setAutoMeasureEnabled(true);

        DividerItemDecoration divider = new DividerItemDecoration(this.getActivity(), DividerItemDecoration.VERTICAL);
        divider.setDrawable(Objects.requireNonNull(ContextCompat.getDrawable(this.getActivity(),
                R.drawable.imaginary_line)));

        debtRecyclerView.setLayoutManager(debtLayoutManager);
        // debtRecyclerView.addItemDecoration(divider);     // Tht english UI version have no decor

        billRecyclerView.setLayoutManager(layoutManager);
        // billRecyclerView.addItemDecoration(divider);     // Tht english UI version have no decor

        delayShowOpenDoor3Btn();

        handleView(true);
    }

    private void delayShowOpenDoor3Btn() {
        //
        // openDoor3 button set enable false, timer 2s set true.
        //

        handler.removeMessages(ENABLE_OPENDOOR3_BUTTON);
        btOpenDoor3.setClickable(false);
        btOpenDoor3.setVisibility(View.INVISIBLE);
//        handler.sendEmptyMessageDelayed(ENABLE_OPENDOOR3_BUTTON, 2000);
        handler.sendEmptyMessage(ENABLE_OPENDOOR3_BUTTON);
    }

    @Override
    public void onHiddenChanged(boolean hidden) {
        super.onHiddenChanged(hidden);
        Log.d(TAG, "onHiddenChanged():hidden = " + hidden);

        if (delayShowOpenDoor3BtnSwitch) {
            delayShowOpenDoor3Btn();
        }

        if (!hidden) {
            handleView(true);
        }
    }

    public void handleView(boolean canPlayAudio) {

        //
        // mainActivity.languageFlag == true : chinese
        //

        if (((MainActivity) getActivity()).languageFlag) {

            //
            // only show shopping list without QRCode
            //

            if (presenter.getQrCodeText().isEmpty()) {
                handleListPageForChinese(canPlayAudio);
            }

            //
            // show QRCode page
            //

            else {
                handleQRCodePageChinese(canPlayAudio);
            }
            Log.d("cpb", "presenter.getQrCodeText(): " + presenter.getQrCodeText());
        }

        //
        // mainActivity.languageFlag == false : english
        //

        else {
            //
            // only show shopping list without QRCode
            //

            if (presenter.getQrCodeText().isEmpty()) {
                handleListPageForEnglish(canPlayAudio);
            }

            //
            // show QRCode page
            //

            else {
                handleQRCodePageEnglish(canPlayAudio);
            }
        }

    }

    public void setCardPayResult(int returnCode) {

        ivRedArrows.setVisibility(View.INVISIBLE);

        if (returnCode == 8) {
            if (((MainActivity) getActivity()).languageFlag) {
                tvCardPay.setText(R.string.not_sufficient_funds);
            } else {
                tvCardPay.setText(R.string.not_sufficient_funds_en);
            }
        } else {
            if (((MainActivity) getActivity()).languageFlag) {
                tvCardPay.setText(R.string.card_pay_failure);
            } else {
                tvCardPay.setText(R.string.card_pay_failure_en);
            }
        }

        tvCardPay.setTextColor(getResources().getColor(R.color.col_ff7800));
    }

    public void posCantUse() {

        ivRedArrows.setVisibility(View.INVISIBLE);
        if (((MainActivity) getActivity()).languageFlag) {
            tvCardPay.setText(R.string.pos_cant_use);
        } else {
            tvCardPay.setText(R.string.pos_cant_use_en);
        }
        tvCardPay.setTextColor(getResources().getColor(R.color.col_ff7800));
    }

    public void whoAmI() {
        presenter.refreshOrder("notMe");
        tvWithoutCodeLabel.setVisibility(View.INVISIBLE);
    }

    public void payRefresh(String source) {
        presenter.refreshOrder(source);
    }

    public void switchLanguage() {

        ((MainActivity) getActivity()).languageFlag = !((MainActivity) getActivity()).languageFlag;
        handleView(false);
    }

    public void changeDelayOpenDoor3Flag() {
        delayShowOpenDoor3BtnSwitch = true;
    }

    private void handleListPageForEnglish(boolean canPlayAudio) {
        llShopingPage.setVisibility(View.VISIBLE);
        llQRCodePage.setVisibility(View.GONE);

        if (presenter.isShopping()) {

            handler.removeMessages(ENABLE_OPENDOOR3_BUTTON);
            btOpenDoor3.setVisibility(View.GONE);

            if (canPlayAudio) {
                AudioUtil.playAudio(AudioUtil.PLEASE_CONFIRM_ORDER);
            }

            buyItems.clear();
            buyItems.addAll(presenter.getItems());

            if (billAdapter == null) {
                billAdapter = new BillAdapter(getActivity(), buyItems);
            } else {
                billAdapter.notifyDataSetChanged();
            }
            billRecyclerView.setAdapter(billAdapter);
            billRecyclerView.setVisibility(View.VISIBLE); // show it

            btConfirm.setVisibility(View.VISIBLE);
            tvAmount.setVisibility(View.VISIBLE);
            tvCount.setVisibility(View.VISIBLE);
            tvCoupon.setVisibility(View.VISIBLE);
            tvTotalMoney.setVisibility(View.VISIBLE);

        } else {

            billRecyclerView.setVisibility(View.GONE); // hide it

            if (delayShowOpenDoor3BtnSwitch) {
                delayShowOpenDoor3Btn();
            } else {
                btOpenDoor3.setClickable(true);
                btOpenDoor3.setVisibility(View.VISIBLE);
            }
            btConfirm.setVisibility(View.GONE);
            tvAmount.setVisibility(View.INVISIBLE);
            tvCount.setVisibility(View.INVISIBLE);
            tvCoupon.setVisibility(View.INVISIBLE);
            tvTotalMoney.setVisibility(View.INVISIBLE);

            if (canPlayAudio) {
                AudioUtil.playAudio(AudioUtil.IF_NOTHING_RECOGNIZED_PUT_GOODS_ON_THE_TABLE_01);
            }
        }

        //Debt is true.
        if (presenter.isDebt()) {


            tvDebtTitle.setText(R.string.debt_label_en);
            tvDebtBillTotalTips.setText(R.string.subtotal_label_en);
            tvDebtSubtotalMoney.setText(String.format("%s", presenter.getDebtAmount() / 100.00));

            if (!presenter.isShopping()) {

                AudioUtil.playAudio(AudioUtil.PLEASE_CHECK_YOUR_DEBT_BILL);

                llDebtSubtotal.setVisibility(View.GONE);

            } else {

                llDebtSubtotal.setVisibility(View.VISIBLE);
                tvNewBillTitle.setText(R.string.this_time_label_en);
            }

            debtAbout.setVisibility(View.VISIBLE);

            debtItems.clear();
            debtItems.addAll(presenter.getDebtItems());
            Log.d(TAG, debtItems.toString());

            if (debtAdapter == null) {
                debtAdapter = new DebtAdapter(getActivity(), debtItems);
            } else {
                debtAdapter.notifyDataSetChanged();
            }

            debtRecyclerView.setAdapter(debtAdapter);
            debtRecyclerView.setVisibility(View.VISIBLE); // show it.

        } else {

            debtAbout.setVisibility(View.GONE); // hide it.
        }

        String url = presenter.getPortrait();
        if (BillingConfig.TE_FORCE_WECHAT_PORTRAIT_FAIL) {
            url = "http://www.abc.com/xxx";
        }

        GlideApp.with(this)
                .asDrawable()
                .diskCacheStrategy(DiskCacheStrategy.RESOURCE)
                .listener(listener)
                .placeholder(R.mipmap.default_portrait)
                .load(url)
                .into(ivPortrait);

        tvAmount.setText(R.string.total_en);
        tvFinalMoney.setText(String.format("%s", presenter.getFinalAmount() / 100.00));

        //
        // Show the coupon layout if the user have coupon.
        //

        if (presenter.getCouponAmount() > 0) {
            llCouponLayout.setVisibility(View.VISIBLE);
            tvCoupon.setText(String.format(getResources().getString(R.string.coupon_en), presenter.getCouponAmount() / 100.00));
            tvTotalMoney.setText(String.format("%s", presenter.getTotalAmount() / 100.00));
        } else {
            llCouponLayout.setVisibility(View.INVISIBLE);
        }

        if (presenter.isShopping() || presenter.isDebt()) {
            if (presenter.getTotalCount() != 0) {

                tvCount.setVisibility(View.VISIBLE);
                tvCount.setText(String.format(getResources().getString(R.string.count_en), presenter.getTotalCount()));

            } else {

                tvCount.setVisibility(View.INVISIBLE);
            }
        }

        if (presenter.getCustomerId().equals(getString(R.string.default_customer_id))) {
            btnNotMe.setVisibility(View.GONE);
            tvNickName.setText(R.string.default_name_en);

            tvCheckTips.setVisibility(View.VISIBLE);
            tvCheckTips.setText(R.string.check_the_bill_en);

        } else {
            tvCheckTips.setVisibility(View.GONE);
            btnNotMe.setVisibility(View.VISIBLE);
            btnNotMe.setText(R.string.not_me_en);

            String name = presenter.getName().trim();
            if (name.isEmpty()) {
                name = getString(R.string.default_name_en);
            }
            tvNickName.setText(name);
        }

        if (presenter.getOneStepPaymentFalseCode() != 5) {

            tvWithoutCodeLabel.setVisibility(View.VISIBLE);
            tvWithoutCodeLabel.setText(R.string.pay_without_code_opened_en);
            tvWithoutCodeLabel.setTextSize(TypedValue.COMPLEX_UNIT_SP, 16);

        } else {                        // one-step payment is false

            tvWithoutCodeLabel.setVisibility(View.INVISIBLE);
        }

        btnLanguageSwitch.setText(R.string.language_zh);
        btConfirm.setText(R.string.confirm_goods_list_en);
        btOpenDoor3.setText(R.string.zero_goods_open_door3_en);
        tvNullGoodsTips.setText(R.string.recognized_null_tips_en);
    }

    private void handleListPageForChinese(boolean canPlayAudio) {
        llShopingPage.setVisibility(View.VISIBLE);
        llQRCodePage.setVisibility(View.GONE);

        if (presenter.isShopping()) {

            handler.removeMessages(ENABLE_OPENDOOR3_BUTTON);
            btOpenDoor3.setVisibility(View.GONE);

            if (canPlayAudio) {
                AudioUtil.playAudio(AudioUtil.PLEASE_CONFIRM_ORDER);
            }

            buyItems.clear();
            buyItems.addAll(presenter.getItems());

            if (billAdapter == null) {
                billAdapter = new BillAdapter(getActivity(), buyItems);
            } else {
                billAdapter.notifyDataSetChanged();
            }
            billRecyclerView.setAdapter(billAdapter);
            billRecyclerView.setVisibility(View.VISIBLE); // show it

            btConfirm.setVisibility(View.VISIBLE);
            tvAmount.setVisibility(View.VISIBLE);
            tvCount.setVisibility(View.VISIBLE);
            tvCoupon.setVisibility(View.VISIBLE);
            tvTotalMoney.setVisibility(View.VISIBLE);

        } else {

            billRecyclerView.setVisibility(View.GONE); // hide it

            if (delayShowOpenDoor3BtnSwitch) {
                delayShowOpenDoor3Btn();
            } else {
                btOpenDoor3.setClickable(true);
                btOpenDoor3.setVisibility(View.VISIBLE);
            }
            btConfirm.setVisibility(View.GONE);
            tvAmount.setVisibility(View.INVISIBLE);
            tvCount.setVisibility(View.INVISIBLE);
            tvCoupon.setVisibility(View.INVISIBLE);
            tvTotalMoney.setVisibility(View.INVISIBLE);

            if (canPlayAudio) {
                AudioUtil.playAudio(AudioUtil.IF_NOTHING_RECOGNIZED_PUT_GOODS_ON_THE_TABLE_01);
            }
        }

        //Debt is true.
        if (presenter.isDebt()) {

            tvDebtTitle.setText(R.string.debt_label);
            tvDebtBillTotalTips.setText(R.string.subtotal_label);
            tvDebtSubtotalMoney.setText(String.format("%s", presenter.getDebtAmount() / 100.00));

            if (!presenter.isShopping()) {

                AudioUtil.playAudio(AudioUtil.PLEASE_CHECK_YOUR_DEBT_BILL);

                llDebtSubtotal.setVisibility(View.GONE);

            } else {

                llDebtSubtotal.setVisibility(View.VISIBLE);
                tvNewBillTitle.setText(R.string.this_time_label);
            }

            debtAbout.setVisibility(View.VISIBLE);

            debtItems.clear();
            debtItems.addAll(presenter.getDebtItems());
            Log.d(TAG, debtItems.toString());

            if (debtAdapter == null) {
                debtAdapter = new DebtAdapter(getActivity(), debtItems);
            } else {
                debtAdapter.notifyDataSetChanged();
            }

            debtRecyclerView.setAdapter(debtAdapter);
            debtRecyclerView.setVisibility(View.VISIBLE); // show it.

        } else {

            debtAbout.setVisibility(View.GONE); // hide it.
        }

        String url = presenter.getPortrait();
        if (BillingConfig.TE_FORCE_WECHAT_PORTRAIT_FAIL) {
            url = "http://www.abc.com/xxx";
        }

        GlideApp.with(this)
                .asDrawable()
                .diskCacheStrategy(DiskCacheStrategy.RESOURCE)
                .listener(listener)
                .placeholder(R.mipmap.default_portrait)
                .load(url)
                .into(ivPortrait);

        tvAmount.setText(R.string.total);
        tvFinalMoney.setText(String.format("%s", presenter.getFinalAmount() / 100.00));

        //
        // Show the coupon layout if the user have coupon.
        //

        if (presenter.getCouponAmount() > 0) {
            llCouponLayout.setVisibility(View.VISIBLE);
            tvCoupon.setText(String.format(getResources().getString(R.string.coupon), presenter.getCouponAmount() / 100.00));
            tvTotalMoney.setText(String.format("%s", presenter.getTotalAmount() / 100.00));
        } else {
            llCouponLayout.setVisibility(View.INVISIBLE);
        }

        if (presenter.isShopping() || presenter.isDebt()) {
            if (presenter.getTotalCount() != 0) {

                tvCount.setVisibility(View.VISIBLE);
                tvCount.setText(String.format(getResources().getString(R.string.count), presenter.getTotalCount()));

            } else {

                tvCount.setVisibility(View.INVISIBLE);
            }
        }

        if (presenter.getCustomerId().equals(getString(R.string.default_customer_id))) {
            btnNotMe.setVisibility(View.GONE);
            tvNickName.setText(R.string.default_name);

            tvCheckTips.setVisibility(View.VISIBLE);
            tvCheckTips.setText(R.string.check_the_bill);

        } else {
            tvCheckTips.setVisibility(View.GONE);
            btnNotMe.setVisibility(View.VISIBLE);
            btnNotMe.setText(R.string.not_me);

            String name = presenter.getName().trim();
            if (name.isEmpty()) {
                name = getString(R.string.default_name);
            }
            tvNickName.setText(name);
        }

        if (presenter.getOneStepPaymentFalseCode() != 5) {

            tvWithoutCodeLabel.setVisibility(View.VISIBLE);
            tvWithoutCodeLabel.setText(R.string.pay_without_code_opened);
            tvWithoutCodeLabel.setTextSize(TypedValue.COMPLEX_UNIT_SP, 24);

        } else {                        // one-step payment is false

            tvWithoutCodeLabel.setVisibility(View.INVISIBLE);
        }

        btnLanguageSwitch.setText(R.string.language_en);
        btConfirm.setText(R.string.confirm_goods_list);
        btOpenDoor3.setText(R.string.zero_goods_open_door3);
        tvNullGoodsTips.setText(R.string.recognized_null_tips);
    }

    private void handleQRCodePageEnglish(boolean canPlayAudio) {
        llShopingPage.setVisibility(View.GONE);
        llQRCodePage.setVisibility(View.VISIBLE);
        if (presenter.isShopping() || presenter.isDebt()) {
            ivWe.setImageBitmap(QRCodeUtil.getRQcode(presenter.getQrCodeText()));
            if (BillingConfig.EC_ENABLE) {
                ivRedArrows.setVisibility(View.VISIBLE);
                tvCardPay.setVisibility(View.VISIBLE);

                SpannableStringBuilder builder = new SpannableStringBuilder();
                String pre = getString(R.string.job_card_to_pay_en1);
                builder.append(pre);
                builder.append(getString(R.string.job_card_to_pay_en2));
                ForegroundColorSpan colorSpan = new ForegroundColorSpan(getResources().getColor(R.color.col_ff7800));
                builder.setSpan(colorSpan, pre.length(), builder.length(), Spannable.SPAN_EXCLUSIVE_INCLUSIVE);

                tvCardPay.setText(builder);
                tvCardPay.setTextColor(getResources().getColor(R.color.font_1));
            } else {
                ivRedArrows.setVisibility(View.INVISIBLE);
                tvCardPay.setVisibility(View.INVISIBLE);
                tvCardPay.setText(R.string.nonsupport_card_en);
                tvCardPay.setTextColor(getResources().getColor(R.color.col_ff7800));
            }
        }

        if (presenter.getOneStepPaymentFalseCode() != 5) {

            tvOverrun.setVisibility(View.GONE);
            tvPayHint.setVisibility(View.VISIBLE);
            tvPayHint.setLineSpacing(17, 1);

            switch (presenter.getOneStepPaymentFalseCode()) {
                case 0:                 // one-step payment is true
                    tvPayHint.setText(R.string.auto_checked_en);
                    break;
                case 1:                 // one-step payment times over
                    tvPayHint.setText(R.string.times_over_can_not_auto_checked_en);
                    break;
                case 2:                 // one-step payment bigger money
                    tvPayHint.setText(R.string.bigger_money_can_not_auto_checked_en);
                    break;
                case 3:                 // one-step payment not fount in inStoreTable.
                    tvPayHint.setText(R.string.not_fount_instore_can_not_auto_checked_en);
                    break;
                default:
                    Log.d(TAG, "handleView() Error. falseCode = " + presenter.getOneStepPaymentFalseCode());
                    tvPayHint.setText(R.string.auto_checked_en);
                    break;
            }

        } else {                        // one-step payment is false

            tvOverrun.setVisibility(View.INVISIBLE);
            tvPayHint.setVisibility(View.INVISIBLE);
        }

        tvScanWechatAlipay.setText(R.string.sweep_the_code_to_pay_en);
        btRefresh.setText(R.string.refresh_the_bill_en);
        tvPayMoney.setText(String.format("%s", presenter.getFinalAmount() / 100.00));
        tvPayCount.setText(String.format(getResources().getString(R.string.count_qrcode_page_en), presenter.getTotalCount()));
        tvPayAmount.setText(R.string.total_qrcode_page_en);
    }

    private void handleQRCodePageChinese(boolean canPlayAudio) {
        llShopingPage.setVisibility(View.GONE);
        llQRCodePage.setVisibility(View.VISIBLE);

        if (presenter.isShopping() || presenter.isDebt()) {
            ivWe.setImageBitmap(QRCodeUtil.getRQcode(presenter.getQrCodeText()));
            if (BillingConfig.EC_ENABLE) {
                ivRedArrows.setVisibility(View.VISIBLE);
                tvCardPay.setVisibility(View.VISIBLE);

                SpannableStringBuilder builder = new SpannableStringBuilder();
                String pre = getString(R.string.job_card_to_pay1);
                builder.append(pre);
                builder.append(getString(R.string.job_card_to_pay2));
                ForegroundColorSpan colorSpan = new ForegroundColorSpan(getResources().getColor(R.color.col_ff7800));
                builder.setSpan(colorSpan, pre.length(), builder.length(), Spannable.SPAN_EXCLUSIVE_INCLUSIVE);

                tvCardPay.setText(builder);
                tvCardPay.setTextColor(getResources().getColor(R.color.font_1));
            } else {
                ivRedArrows.setVisibility(View.INVISIBLE);
                tvCardPay.setVisibility(View.INVISIBLE);
                tvCardPay.setText(R.string.nonsupport_card);
                tvCardPay.setTextColor(getResources().getColor(R.color.col_ff7800));
            }

        }

        if (presenter.getOneStepPaymentFalseCode() != 5) {

            tvOverrun.setVisibility(View.VISIBLE);
            tvPayHint.setVisibility(View.VISIBLE);
            tvPayHint.setLineSpacing(8, 1);

            switch (presenter.getOneStepPaymentFalseCode()) {
                case 0:                 // one-step payment is true
                    tvOverrun.setText(R.string.auto_checked);
                    tvPayHint.setText(R.string.pay_with_out_code);
                    break;
                case 1:                 // one-step payment times over
                    tvOverrun.setText(R.string.times_over_can_not_auto_checked);
                    tvPayHint.setText(R.string.use_other_payment);
                    break;
                case 2:                 // one-step payment bigger money
                    tvOverrun.setText(R.string.bigger_money_can_not_auto_checked);
                    tvPayHint.setText(R.string.use_other_payment);
                    break;
                case 3:                 // one-step payment not fount in inStoreTable.
                    tvOverrun.setText(R.string.not_fount_instore_can_not_auto_checked);
                    tvPayHint.setText(R.string.use_other_payment);
                    break;
                default:
                    Log.d(TAG, "handleView() Error. falseCode = " + presenter.getOneStepPaymentFalseCode());
                    tvOverrun.setText(R.string.auto_checked);
                    tvPayHint.setText(R.string.use_other_payment);
                    break;
            }

        } else {                        // one-step payment is false

            tvOverrun.setVisibility(View.INVISIBLE);
            tvPayHint.setVisibility(View.INVISIBLE);
        }

        tvScanWechatAlipay.setText(R.string.sweep_the_code_to_pay);
        btRefresh.setText(R.string.refresh_the_bill);
        tvPayMoney.setText(String.format("%s", presenter.getFinalAmount() / 100.00));
        tvPayCount.setText(String.format(getResources().getString(R.string.count_qrcode_page), presenter.getTotalCount()));
        tvPayAmount.setText(R.string.total_qrcode_page);
    }

    private void noShoppingOpenDoor3(int rId) {
        Log.d(TAG, "Leave store button clicked : open door #3.");
        presenter.stopReader(false);
        presenter.openDoor3(rId); // open door num 3

        rlExpectNextMeet.setVisibility(View.VISIBLE);
        llQRCodePage.setVisibility(View.GONE);
        llShopingPage.setVisibility(View.GONE);

        tvCustomerNickName.setVisibility(View.GONE);
        String url = presenter.getPortrait();
        if (url.contains("default.png")) {
            url = "";
        }

        GlideApp.with(this)
                .asDrawable()
                .diskCacheStrategy(DiskCacheStrategy.RESOURCE)
                .listener(listener)
                .circleCrop()
                .error(R.mipmap.ring_default_src)
                .load(url)
                .into(ivCustomerPortrait);

        String name = presenter.getName().trim();
        if (name.isEmpty()) {
            name = getString(R.string.default_name_en);
        }
        tvCustomerNickName.setText(name);

        //
        // mainActivity.languageFlag == true : chinese
        //

        if (((MainActivity) getActivity()).languageFlag) {

            tvNoshoppingRefreshTips.setText(getString(R.string.if_taking_goods));
            btnRefreshNoshopping.setText(getString(R.string.refresh_the_bill));
            tvExpectNextMeet.setText(getString(R.string.welcome_next_time));

        } else {

            tvNoshoppingRefreshTips.setText(getString(R.string.if_taking_goods_en));
            btnRefreshNoshopping.setText(getString(R.string.refresh_the_bill_en));
            tvExpectNextMeet.setText(getString(R.string.welcome_next_time_en));
        }

    }

    public void setPresenter(Presenter presenter) {
        this.presenter = presenter;
    }
}
