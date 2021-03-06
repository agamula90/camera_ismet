package com.ismet.usbcamera.table;

import android.content.Context;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.RecyclerView;
import android.view.ViewGroup;
import android.widget.LinearLayout;

import com.ismet.usbcamera.R;

import java.util.List;

public class GridAdapter extends RecyclerView.Adapter<RowViewHolder> {
    private final List<RowItem> rowItems;
    private int countColumns;
    private final Context context;
    private int currentRow;
    private final RecyclerView recyclerView;

    public GridAdapter(List<RowItem> rowItems, Context context, RecyclerView recyclerView) {
        this.rowItems = rowItems;
        countColumns = rowItems.get(0).getColumnValues().length;
        this.context = context;
        this.recyclerView = recyclerView;
        currentRow = 0;
    }

    @Override
    public RowViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        LinearLayout layout = new LinearLayout(parent.getContext());
        layout.setOrientation(LinearLayout.HORIZONTAL);
        layout.setLayoutParams(new RecyclerView.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
        return new RowViewHolder(layout, countColumns);
    }

    public void selectNext() {
        currentRow++;
        currentRow = currentRow % rowItems.size();
        notifyDataSetChanged();
        recyclerView.scrollToPosition(currentRow);
    }

    public void selectPrevious() {
        currentRow--;
        if(currentRow < 0) {
            currentRow += rowItems.size();
        }
        notifyDataSetChanged();
        recyclerView.scrollToPosition(currentRow);
    }

    public int getCurrentRow() {
        return currentRow;
    }

    @Override
    public void onBindViewHolder(RowViewHolder holder, int position) {
        holder.itemView.setBackgroundColor(ContextCompat.getColor(context,
                currentRow == position ? R.color.colorAccent : android.R.color.white));
        RowItem rowItem = rowItems.get(position);

        holder.setText(0, String.valueOf(rowItem.row));
        for (int i = 0; i < countColumns; i++) {
            holder.setText(i + 1, rowItems.get(position).getColumnValues()[i]);
        }
    }

    @Override
    public int getItemCount() {
        return rowItems.size();
    }
}
