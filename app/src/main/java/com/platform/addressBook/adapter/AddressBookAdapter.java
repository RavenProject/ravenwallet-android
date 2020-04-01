package com.platform.addressBook.adapter;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.platform.addressBook.AddressBookItem;
import com.platform.addressBook.event.OnAddressClickedListener;
import com.ravenwallet.R;

import java.util.List;


public class AddressBookAdapter extends RecyclerView.Adapter<AddressBookAdapter.ViewHolder> {

    private List<AddressBookItem> addresses;
    private OnAddressClickedListener listener;

    public AddressBookAdapter(List<AddressBookItem> addresses, OnAddressClickedListener listener) {
        this.addresses = addresses;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_address_book, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull final ViewHolder holder, int position) {
        final AddressBookItem address = addresses.get(position);
        holder.addressLabel.setText(address.getName());
        holder.addressValue.setText(address.getAddress());
        holder.viewContainer.setOnClickListener(
                new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        listener.onAddressClicked(addresses.get(holder.getAdapterPosition()));
                    }
                }
        );
    }

    @Override
    public int getItemCount() {
        return addresses.size();
    }

    class ViewHolder extends RecyclerView.ViewHolder {
        LinearLayout viewContainer;
        TextView addressLabel;
        TextView addressValue;

        ViewHolder(View itemView) {
            super(itemView);
            viewContainer = itemView.findViewById(R.id.view_container);
            addressLabel = itemView.findViewById(R.id.address_label);
            addressValue = itemView.findViewById(R.id.address_value);
        }
    }
}
