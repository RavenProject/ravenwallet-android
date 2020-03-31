package com.ravenwallet.presenter.activities.util;

import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.ItemTouchHelper;

import com.ravenwallet.presenter.interfaces.OnItemTouchHelperListener;


public class CustomItemTouchHelperCallback extends ItemTouchHelper.Callback {

    private OnItemTouchHelperListener listener;

    public CustomItemTouchHelperCallback(OnItemTouchHelperListener listener) {
        this.listener = listener;
    }

    @Override
    public int getMovementFlags(RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder) {
        int dragFlags = ItemTouchHelper.UP | ItemTouchHelper.DOWN;
        int swipeFlags = 0;
        return makeMovementFlags(dragFlags, swipeFlags);
    }

    @Override
    public boolean onMove(RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder, RecyclerView.ViewHolder target) {
        listener.onItemMove(viewHolder.getAdapterPosition(),
                target.getAdapterPosition());

        return true;
    }

    @Override
    public void onSwiped(RecyclerView.ViewHolder viewHolder, int direction) {
        // Do nothing here
    }

    @Override
    public boolean isLongPressDragEnabled() {
        return true;
    }
}
