package br.com.gasi.bastogeofone;

import android.app.Activity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;

import java.util.List;

/**
 * Created by Resende on 30/01/2018.
 */

public class PoIListAdapter extends BaseAdapter {

    private final List<PointOfInterest> pointOfInterestList;
    private final Activity activity;

    public PoIListAdapter(List<PointOfInterest> pois, Activity act){
        this.pointOfInterestList = pois;
        this.activity = act;
    }

    @Override
    public int getCount() {
        return pointOfInterestList.size();
    }

    @Override
    public Object getItem(int i) {
        return pointOfInterestList.get(i);
    }

    @Override
    public long getItemId(int i) {
        return 0;
    }

    @Override
    public View getView(int i, View view, ViewGroup viewGroup) {
        return null;
    }
}
