package com.xm.aeclient;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;

public class SelectListAdapter extends RecyclerView.Adapter<SelectListAdapter.ViewHolder> {

    private final ArrayList<String> localDataSet;
    private SelectListListener mListClickListener;

    public interface SelectListListener { void listItemSelected(View v, int position);}

    public static class ViewHolder extends RecyclerView.ViewHolder {
        private final TextView textView;

        public ViewHolder(View view, SelectListListener mOcl) {
            super(view);
            textView = (TextView) view.findViewById(R.id.ilist_text_item);
            view.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    mOcl.listItemSelected(view, getAdapterPosition());
                }
            });
        }
        public TextView getTextView() {
            return textView;
        }
    }

    public SelectListAdapter(ArrayList<String> inDataSet, SelectListListener inListClickListener) {
        localDataSet = inDataSet;
        mListClickListener = inListClickListener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(ViewGroup viewGroup, int viewType) {
        View view = LayoutInflater.from(viewGroup.getContext())
                .inflate(R.layout.ilist_txt_item, viewGroup, false);
        return new ViewHolder(view, mListClickListener);
    }

    @Override
    public void onBindViewHolder(ViewHolder viewHolder, final int position) {
        viewHolder.getTextView().setText(localDataSet.get(position));
    }

    @Override
    public int getItemCount() {
        return localDataSet.size();
    }
}