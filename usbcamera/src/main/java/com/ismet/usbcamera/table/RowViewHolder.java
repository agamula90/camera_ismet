package com.ismet.usbcamera.table;

import android.content.Context;
import android.graphics.Color;
import android.support.v7.widget.RecyclerView;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

class RowViewHolder extends RecyclerView.ViewHolder {
    private LinearLayout linearLayout;

    RowViewHolder(View itemView, int countColumns) {
        super(itemView);
        Context context = itemView.getContext();
        linearLayout = (LinearLayout) itemView;
        linearLayout.removeAllViews();

        for (int i = 0; i < countColumns + 1; i++) {
            addDivider(context);
            TextView textView = new TextView(itemView.getContext());
            textView.setGravity(Gravity.CENTER);
            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT);
            params.weight = 1;
            linearLayout.addView(textView, params);
        }

        addDivider(context);
    }

    private void addDivider(Context context) {
        View view = new View(context);
        view.setBackgroundColor(Color.BLACK);
        linearLayout.addView(view, new LinearLayout.LayoutParams(1, ViewGroup.LayoutParams.MATCH_PARENT));
    }

    void setText(int position, String text) {
        ((TextView)linearLayout.getChildAt(2 * position + 1)).setText(text);
    }
}
