package br.com.gasi.bastogeofone;

import android.content.Context;
import android.support.annotation.NonNull;
import android.view.LayoutInflater;
import android.widget.ArrayAdapter;

import java.util.ArrayList;

public class InspecaoListAdapter extends ArrayAdapter<String> {

    private LayoutInflater mLayoutInflater;
    private ArrayList<String> mInspecoes;
    private int mViewResourceId;

    public InspecaoListAdapter(@NonNull Context context, int resource) {
        super(context, resource);
        mLayoutInflater = (LayoutInflater)context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }

    @Override
    public void notifyDataSetChanged() {
        super.notifyDataSetChanged();
    }


}
