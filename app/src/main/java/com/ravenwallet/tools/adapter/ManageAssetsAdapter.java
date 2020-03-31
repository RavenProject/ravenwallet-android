package com.ravenwallet.tools.adapter;

import android.content.Context;
import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.platform.assets.Asset;
import com.platform.assets.AssetsRepository;
import com.ravenwallet.R;
import com.ravenwallet.presenter.interfaces.OnItemTouchHelperListener;

import java.util.Collections;
import java.util.List;

import static com.platform.assets.AssetsValidation.SUB_NAME_DELIMITER;
import static com.platform.assets.AssetsValidation.UNIQUE_TAG_DELIMITER;
import static com.platform.assets.adapter.AssetsAdapter.replaceLast;


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

        String name = asset.getName();
        if (!name.contains(SUB_NAME_DELIMITER) && !name.contains(UNIQUE_TAG_DELIMITER)) {
            holder.assetName.setText(name);
            holder.rootAssetName.setVisibility(View.GONE);
        } else {
            holder.rootAssetName.setVisibility(View.VISIBLE);
            if (name.contains(UNIQUE_TAG_DELIMITER)) {
                String[] names = name.split(UNIQUE_TAG_DELIMITER);
                String subName = names[names.length - 1];
                String rootName = replaceLast(name, subName, "");
                holder.assetName.setText(subName);
                holder.rootAssetName.setText(rootName);
            } else {
                String[] names = name.split(SUB_NAME_DELIMITER);
                String subName = names[names.length - 1];
                String rootName = replaceLast(name, subName, "");
                holder.assetName.setText(subName);
                holder.rootAssetName.setText(rootName);
            }
        }
        final String showText = context.getString(R.string.show);
        final String hideText = context.getString(R.string.hide);

        // Setting the text of the show/hide button
        if (asset.getIsVisible() == 1) {
            setHideButtonStyle(holder.visibilityButton, hideText);
        } else {
//            asset.setIsVisible(1);
            setShowButtonStyle(holder.visibilityButton, showText);
        }

        // Setting the show/hide actions
        holder.visibilityButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (asset.getIsVisible() == 0/*holder.visibilityButton.getText().equals(showText)*/) {
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
        TextView rootAssetName;
        Button visibilityButton;

        ViewHolder(View itemView) {
            super(itemView);
            viewContainer = itemView.findViewById(R.id.view_container);
            rootAssetName = itemView.findViewById(R.id.root_asset_name);
            assetName = itemView.findViewById(R.id.asset_name);
            visibilityButton = itemView.findViewById(R.id.item_visibility_action);
        }
    }
}
