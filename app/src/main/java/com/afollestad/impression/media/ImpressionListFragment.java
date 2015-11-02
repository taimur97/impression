package com.afollestad.impression.media;

import android.app.Fragment;
import android.content.Context;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.afollestad.impression.R;
import com.afollestad.impression.adapters.SpacesItemDecoration;

/**
 * @author Aidan Follestad (afollestad)
 */
public abstract class ImpressionListFragment<P extends ImpressionListPresenter> extends Fragment implements ImpressionListView {

    protected RecyclerView mRecyclerView;
    protected RecyclerView.Adapter mAdapter;

    protected P mPresenter;

    public RecyclerView.Adapter getAdapter() {
        return mAdapter;
    }

    public RecyclerView getRecyclerView() {
        return mRecyclerView;
    }

    final void setListShown(boolean shown) {
        View v = getView();
        if (v == null || getActivity() == null) return;
        if (shown) {
            v.findViewById(R.id.list).setVisibility(mAdapter.getItemCount() > 0 ? View.VISIBLE : View.GONE);
            v.findViewById(R.id.empty).setVisibility(mAdapter.getItemCount() == 0 ? View.VISIBLE : View.GONE);
            v.findViewById(R.id.progress).setVisibility(View.GONE);
            mAdapter.notifyDataSetChanged();
        } else {
            v.findViewById(R.id.list).setVisibility(View.GONE);
            v.findViewById(R.id.empty).setVisibility(View.GONE);
            v.findViewById(R.id.progress).setVisibility(View.VISIBLE);
        }
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mPresenter = createPresenter();
        mPresenter.attachView(this);
    }

    @Nullable
    @Override
    public final View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_list, container, false);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        ((TextView) view.findViewById(R.id.empty)).setText(mPresenter.getEmptyText());
        mRecyclerView = (RecyclerView) view.findViewById(R.id.list);
        mRecyclerView.setScrollBarStyle(View.SCROLLBARS_INSIDE_OVERLAY);

        invalidateLayoutManagerAndAdapter();
    }

    //TODO: Don't recreate layout manager
    public void invalidateLayoutManagerAndAdapter() {
        mRecyclerView.setLayoutManager(mPresenter.createLayoutManager());
        int spacing = getResources().getDimensionPixelSize(R.dimen.grid_spacing);
        mRecyclerView.addItemDecoration(new SpacesItemDecoration(spacing));
        mAdapter = mPresenter.createAdapter();
        mRecyclerView.setAdapter(mAdapter);
    }

    @Override
    public Context getContextCompat() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            return getContext();
        } else {
            return getActivity();
        }
    }

    protected abstract P createPresenter();
}
