package com.ravencoin.tools.adapter;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.platform.assets.Asset;
import com.platform.assets.AssetsRepository;
import com.ravencoin.BuildConfig;
import com.ravencoin.R;
import com.ravencoin.presenter.customviews.BRToast;
import com.ravencoin.presenter.interfaces.OnItemTouchHelperListener;

import java.util.Collections;
import java.util.List;


public class ManageAssetsAdapter extends RecyclerView.Adapter<ManageAssetsAdapter.ViewHolder> implements OnItemTouchHelperListener {

    private Context context;
    private List<Asset> assets;

    public ManageAssetsAdapter(Context context, List<Asset> assets) {
        this.context = context;
        this.assets = assets;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_manage_asset, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull final ViewHolder holder, final int position) {
        final Asset asset = assets.get(position);
        holder.assetName.setText(asset.getName());
        final String showText = context.getString(R.string.show);
        final String hideText = context.getString(R.string.hide);

        // Setting the text of the show/hide button
        if (asset.getIsVisible() == 1) {
            setHideButtonStyle(holder.visibilityButton, hideText);
        } else {
            asset.setIsVisible(1);
            setShowButtonStyle(holder.visibilityButton, showText);
        }

        // Setting the show/hide actions
        holder.visibilityButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (holder.visibilityButton.getText().equals(showText)) {
                    asset.setIsVisible(1);
                    AssetsRepository repository = AssetsRepository.getInstance(context);
                    repository.updateAssetVisibility(asset);
                } else {
                    asset.setIsVisible(0);
                    AssetsRepository repository = AssetsRepository.getInstance(context);
                    repository.updateAssetVisibility(asset);
                }
                notifyItemChanged(holder.getAdapterPosition());
            }
        });
    }

    private void setShowButtonStyle(Button button, String text) {
        button.setText(text);
        button.setBackground(ContextCompat.getDrawable(context, R.drawable.show_button_shape));
    }

    private void setHideButtonStyle(Button button, String text) {
        button.setText(text);
        button.setBackground(ContextCompat.getDrawable(context, R.drawable.hide_button_shape));
    }

    @Override
    public int getItemCount() {
        return assets.size();
    }

    @Override
    public void onItemMove(int fromPosition, int toPosition) {
        AssetsRepository repository = AssetsRepository.getInstance(context);
        if (fromPosition < toPosition) {
            for (int i = fromPosition; i < toPosition; i++) {
                int currentAssetItemPriority = assets.get(i).getSortPriority();
                int nextAssetItemPriority = assets.get(i + 1).getSortPriority();

                // Swapping priorities
                assets.get(i).setSortPriority(nextAssetItemPriority);
                assets.get(i + 1).setSortPriority(currentAssetItemPriority);

                // Updating priorities swapping in database
                repository.updateAssetPriority(assets.get(i));
                repository.updateAssetPriority(assets.get(i + 1));

                // Swapping items on the adapter
                Collections.swap(assets, i, i + 1);
            }
        } else {
            for (int i = fromPosition; i > toPosition; i--) {
                int currentAssetItemPriority = assets.get(i).getSortPriority();
                int previousAssetItemPriority = assets.get(i - 1).getSortPriority();

                // Swapping priorities
                assets.get(i).setSortPriority(previousAssetItemPriority);
                assets.get(i - 1).setSortPriority(currentAssetItemPriority);

                // Updating priorities swapping in database
                repository.updateAssetPriority(assets.get(i));
                repository.updateAssetPriority(assets.get(i - 1));

                // Swapping items on the adapter
                Collections.swap(assets, i, i - 1);
            }
        }
        notifyItemMoved(fromPosition, toPosition);
    }

    class ViewHolder extends RecyclerView.ViewHolder {
        RelativeLayout viewContainer;
        TextView assetName;
        Button visibilityButton;

        ViewHolder(View itemView) {
            super(itemView);
            viewContainer = itemView.findViewById(R.id.view_container);
            assetName = itemView.findViewById(R.id.asset_name);
            visibilityButton = itemView.findViewById(R.id.item_visibility_action);
        }
    }
}
