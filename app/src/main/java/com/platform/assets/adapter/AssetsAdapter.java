package com.platform.assets.adapter;

import android.app.Activity;
import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.platform.assets.Asset;
import com.platform.assets.Utils;
import com.ravencoin.R;
import com.ravencoin.tools.animation.BRAnimator;
import com.ravencoin.wallet.WalletsMaster;
import com.ravencoin.wallet.abstracts.BaseWalletManager;

import java.math.BigDecimal;
import java.util.List;


public class AssetsAdapter extends RecyclerView.Adapter<AssetsAdapter.ViewHolder> {

    private Context context;
    private List<Asset> assets;
    private BaseWalletManager wallet;

    public AssetsAdapter(Context context, List<Asset> assets) {
        this.context = context;
        this.assets = assets;
        this.wallet = WalletsMaster.getInstance(context).getCurrentWallet(context);
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_asset, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull final ViewHolder holder, int position) {
        final Asset asset = assets.get(position);
        holder.assetName.setText(asset.getName());
        double assetAmount = wallet.getCryptoForSmallestCrypto(context, new BigDecimal(asset.getAmount())).doubleValue();
        holder.assetAmount.setText(Utils.formatAssetAmount(assetAmount, asset.getUnits()));

        if (asset.getOwnership() == 1) {
            holder.viewContainer.setBackground(ContextCompat.getDrawable(context, R.drawable.shape_owned_asset));
            holder.ownershipImage.setVisibility(View.VISIBLE);
        } else {
            holder.viewContainer.setBackground(ContextCompat.getDrawable(context, R.drawable.shape_non_owned_asset));
            holder.ownershipImage.setVisibility(View.GONE);
        }
        holder.viewContainer.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                BRAnimator.showAssetMenuFragment((Activity) context, asset);
            }
        });
    }

    @Override
    public int getItemCount() {
        return assets.size();
    }

    class ViewHolder extends RecyclerView.ViewHolder {
        RelativeLayout viewContainer;
        ImageView ownershipImage;
        TextView assetName;
        TextView assetAmount;

        ViewHolder(View itemView) {
            super(itemView);
            viewContainer = itemView.findViewById(R.id.view_container);
            ownershipImage = itemView.findViewById(R.id.asset_ownership_image);
            assetName = itemView.findViewById(R.id.asset_name);
            assetAmount = itemView.findViewById(R.id.asset_amount);
        }
    }
}
