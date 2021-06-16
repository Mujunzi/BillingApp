package com.lenovo.billing.views.adapter;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.load.resource.bitmap.CircleCrop;
import com.bumptech.glide.request.RequestOptions;
import com.lenovo.billing.GlideApp;
import com.lenovo.billing.R;
import com.lenovo.billing.entity.DebtItem;

import java.util.ArrayList;
import java.util.Locale;

public class DebtAdapter extends RecyclerView.Adapter<DebtAdapter.DebtViewHolder> {

    private ArrayList<DebtItem> data;
    private Context mContext;

    public DebtAdapter(Context context, ArrayList<DebtItem> data) {
        this.data = data;
        mContext = context;
    }

    @Override
    public DebtViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_commodity_list,
                parent,
                false);

        return new DebtViewHolder(view);
    }

    @Override
    public void onBindViewHolder(DebtViewHolder holder, int position) {

        DebtItem item = data.get(position);

        //
        // load goods img logo
        //

//        Glide.with(mContext)
//                .load(item.getImageUrl())
//                .apply(RequestOptions.bitmapTransform(new CircleCrop()))
//                .apply(RequestOptions.skipMemoryCacheOf(true))
//                .apply(RequestOptions.diskCacheStrategyOf(DiskCacheStrategy.NONE))
//                .apply(RequestOptions.placeholderOf(R.mipmap.goods_default_logo))
//                .apply(RequestOptions.errorOf(R.mipmap.goods_default_logo))
//                .into(holder.ivLogo);

        GlideApp.with(mContext)
                .asDrawable()
                .circleCrop()
                .diskCacheStrategy(DiskCacheStrategy.RESOURCE)
                .placeholder(R.mipmap.goods_default_logo)
                .load(item.getImageUrl())
                .into(holder.ivLogo);

        holder.nameTv.setText(item.getName());
        holder.priceTv.setText(String.format("%s", item.getPrice() / 100.00));
        holder.countTv.setText(String.format(Locale.getDefault(), "×%d", item.getQty()));
    }

    @Override
    public int getItemCount() {
        return data == null ? 0 : data.size();
    }

    class DebtViewHolder extends RecyclerView.ViewHolder {

        TextView nameTv, priceTv, countTv;
        ImageView ivLogo;

        DebtViewHolder(View itemView) {
            super(itemView);
            ivLogo = itemView.findViewById(R.id.iv_logo);
            nameTv = itemView.findViewById(R.id.commodity_name_tv);
            priceTv = itemView.findViewById(R.id.price_tv);
            countTv = itemView.findViewById(R.id.count_tv);
        }
    }
}
