package com.ravenwallet.presenter.activities.settings;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.platform.addressBook.event.OnAddressClickedListener;
import com.ravenwallet.R;
import com.ravenwallet.core.BRCoreAddress;


public class UserAddressesAdapter extends RecyclerView.Adapter<UserAddressesAdapter.ViewHolder> {

    private BRCoreAddress[] addresses;
    private OnAddressClickedListener listener;

    public UserAddressesAdapter(BRCoreAddress[] addresses, OnAddressClickedListener listener) {
        this.addresses = addresses;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_used_address, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull final ViewHolder holder, int position) {
        final BRCoreAddress address = addresses[position];
        holder.addressValue.setText(address.stringify());
        holder.viewContainer.setOnClickListener(
                new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        listener.onAddressClicked(address);
                    }
                }
        );
    }

    @Override
    public int getItemCount() {
        return addresses.length;
    }

    class ViewHolder extends RecyclerView.ViewHolder {
        LinearLayout viewContainer;
        TextView addressValue;

        ViewHolder(View itemView) {
            super(itemView);
            viewContainer = itemView.findViewById(R.id.view_container);
            addressValue = itemView.findViewById(R.id.address_value);
        }
    }
}