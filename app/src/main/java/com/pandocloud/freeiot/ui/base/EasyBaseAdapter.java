
package com.pandocloud.freeiot.ui.base;

import java.util.ArrayList;
import java.util.List;

import android.R.integer;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;



public abstract class EasyBaseAdapter<T> extends BaseAdapter {
    
    protected List<T>              mDataSet;
    
    protected final Context        mContext;
    
    protected final LayoutInflater mInflater;
    
    public EasyBaseAdapter(Context context, List<T> dataset) {
        mDataSet = dataset;
        if (mDataSet == null) {
			mDataSet = new ArrayList<T>();
		}
        mContext = context;
        mInflater = LayoutInflater.from(context);
    }
    
    /**
     * add adapter dataset item
     * @param t
     */
    public void addItems(T t) {
    	mDataSet.add(t);
    	notifyDataSetChanged();
    }
    
    /**
     * add all items 
     * @param items
     */
    public void addAll(List<T> items) {
    	mDataSet.addAll(items);
    	notifyDataSetChanged();
    }
    
    public void addAtPosition(int position, List<T> items) {
    	mDataSet.addAll(position, items);
    	notifyDataSetChanged();
    }
    
    public void removeAtPosition(int position) {
    	if (position < 0 || position >= mDataSet.size()) {
			return;
		}
    	mDataSet.remove(position);
    	notifyDataSetChanged();
    }
    public void removeAll(List<T> items) {
    	mDataSet.removeAll(items);
    	notifyDataSetChanged();
    }
    /**
     * 
     * @param datasets
     */
    public void updateDataSet(List<T> datasets) {
    	mDataSet = datasets;
    	notifyDataSetChanged();
    }
    
    
    /*
     * (non-Javadoc)
     * 
     * @see android.widget.Adapter#getCount()
     */
    @Override
    public int getCount() {
        return mDataSet == null ? 0 : mDataSet.size();
    }
    
    /*
     * (non-Javadoc)
     * 
     * @see android.widget.Adapter#getItem(int)
     */
    @Override
    public Object getItem(int position) {
        return mDataSet.get(position);
    }
    
    /*
     * (non-Javadoc)
     * 
     * @see android.widget.Adapter#getItemId(int)
     */
    @Override
    public long getItemId(int position) {
        return position;
    }
    
    /*
     * (non-Javadoc)
     * 
     * @see android.widget.Adapter#getView(int, android.view.View, android.view.ViewGroup)
     */
    @Override
    public abstract View getView(int position, View convertView, ViewGroup parent);
}
