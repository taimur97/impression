package com.afollestad.impression.media;

import android.app.Activity;
import android.app.Fragment;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.Nullable;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.afollestad.impression.R;
import com.afollestad.impression.api.MediaEntry;
import com.afollestad.impression.api.MediaFolderEntry;
import com.afollestad.impression.base.ThemedActivity;
import com.afollestad.impression.utils.PrefUtils;
import com.afollestad.impression.widget.breadcrumbs.Crumb;

import java.io.File;
import java.util.List;

import static android.app.Activity.RESULT_OK;

public class MediaFragment extends Fragment implements MediaView {

    private RecyclerView mRecyclerView;
    private SwipeRefreshLayout mSwipeRefreshLayout;
    private MediaAdapter mAdapter;
    private MediaPresenter mPresenter;
    private View mEmptyView;


    RecyclerView getRecyclerView() {
        return mRecyclerView;
    }

    final void jumpToTop(boolean animateChange) {
        if (animateChange) {
            //stopAnimation();
            mRecyclerView.smoothScrollToPosition(0);
        } else {
            mRecyclerView.scrollToPosition(0);
        }
    }

    public final void setListShown(boolean shown) {
        View v = getView();
        if (v == null || getActivity() == null) {
            return;
        }
        if (shown) {
            mEmptyView.setVisibility(mAdapter.getItemCount() == 0 ? View.VISIBLE : View.GONE);
            mSwipeRefreshLayout.setRefreshing(false);
        } else {
            v.findViewById(R.id.empty).setVisibility(View.GONE);
            mSwipeRefreshLayout.setRefreshing(true);
        }
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        mPresenter.onSaveInstanceState(outState);
        super.onSaveInstanceState(outState);
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
        mEmptyView = view.findViewById(R.id.empty);

        mSwipeRefreshLayout = (SwipeRefreshLayout) view.findViewById(R.id.swipe_refresh_layout);
        mSwipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                mPresenter.onRefresh();
            }
        });
        mSwipeRefreshLayout.setColorSchemeColors(((ThemedActivity) getActivity()).accentColor());

        mPresenter.onViewCreated(savedInstanceState);
    }

    @Override
    public void initializeRecyclerView(boolean gridMode, int size, MediaAdapter adapter) {
        mRecyclerView.setLayoutManager(new GridLayoutManager(getContextCompat(), size));

        mAdapter = adapter;
        mRecyclerView.setAdapter(mAdapter);

        mRecyclerView.setItemAnimator(new DefaultItemAnimator() {
            @Override
            public boolean canReuseUpdatedViewHolder(RecyclerView.ViewHolder viewHolder) {
                return true;
            }
        });

        if (gridMode) {
            mPresenter.setGridModeOn(true);
        }
    }

    public void scrollToTop() {
        mRecyclerView.scrollToPosition(0);
    }

    public void updateGridModeOn(boolean gridMode) {
        int gridSpacing = getResources().getDimensionPixelSize(R.dimen.grid_spacing);
        if (gridMode) {
            mRecyclerView.setPadding(gridSpacing, 0, 0, gridSpacing);
        } else {
            mRecyclerView.setPadding(0, 0, 0, 0);
        }
    }

    @Override
    public void updateGridColumns(int size) {
        final GridLayoutManager layoutManager = (GridLayoutManager) mRecyclerView.getLayoutManager();
        layoutManager.setSpanCount(size);
    }

    @Override
    public MediaAdapter getAdapter() {
        return mAdapter;
    }

    @Override
    public Context getContextCompat() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            return getContext();
        } else {
            return getActivity();
        }
    }

    @Override
    public void saveScrollPositionInto(Crumb crumb) {
        if (crumb == null) {
            return;
        }
        crumb.setScrollPosition(((GridLayoutManager) mRecyclerView.getLayoutManager()).findFirstVisibleItemPosition());
        final View firstChild = mRecyclerView.getChildAt(0);
        if (firstChild != null) {
            crumb.setScrollOffset((int) firstChild.getY());
        }
    }

    @Override
    public void restoreScrollPositionFrom(Crumb crumb) {
        if (crumb == null) {
            return;
        }
        final int scrollY = crumb.getScrollPosition();
        if (scrollY > -1 && scrollY < getAdapter().getItemCount()) {
            ((GridLayoutManager) mRecyclerView.getLayoutManager()).scrollToPositionWithOffset(scrollY, crumb.getScrollOffset());
        }
    }

    private void setFilterMode(@MediaAdapter.FileFilterMode int mode) {
        PrefUtils.setFilterMode(getActivity(), mode);
        getPresenter().reload();
        getActivity().invalidateOptionsMenu();
    }


    public void invalidateEmptyText() {
        @MediaAdapter.FileFilterMode int mode = PrefUtils.getFilterMode(getActivity());
        View v = getView();
        if (v != null) {
            TextView empty = (TextView) v.findViewById(R.id.empty);
            if (empty != null) {
                switch (mode) {
                    default:
                        empty.setText(R.string.no_photosorvideos);
                        break;
                    case MediaAdapter.FILTER_PHOTOS:
                        empty.setText(R.string.no_photos);
                        break;
                    case MediaAdapter.FILTER_VIDEOS:
                        empty.setText(R.string.no_videos);
                        break;
                }
            }
        }
    }


    public void invalidateSubtitle(List<MediaEntry> entries) {
        AppCompatActivity act = (AppCompatActivity) getActivity();
        if (act != null) {
            final boolean toolbarStats = PreferenceManager.getDefaultSharedPreferences(act)
                    .getBoolean("toolbar_album_stats", true);
            if (toolbarStats) {
                if (entries == null || entries.size() == 0) {
                    act.getSupportActionBar().setSubtitle(getString(R.string.empty));
                    return;
                }

                int folderCount = 0;
                int albumCount = 0;
                int videoCount = 0;
                int photoCount = 0;
                for (MediaEntry e : entries) {
                    if (e.isFolder()) {
                        folderCount++;
                    }
                    //else if (e.isAlbum()) albumCount++;
                    else if (e.isVideo()) {
                        videoCount++;
                    } else {
                        photoCount++;
                    }
                }
                final StringBuilder sb = new StringBuilder();
                if (albumCount > 1) {
                    sb.append(getString(R.string.x_albums, albumCount));
                } else if (albumCount == 1) {
                    sb.append(getString(R.string.one_album));
                }
                if (folderCount > 1) {
                    if (sb.length() > 0) {
                        sb.append(", ");
                    }
                    sb.append(getString(R.string.x_folders, folderCount));
                } else if (folderCount == 1) {
                    if (sb.length() > 0) {
                        sb.append(", ");
                    }
                    sb.append(getString(R.string.one_folder));
                }
                if (photoCount > 1) {
                    if (sb.length() > 0) {
                        sb.append(", ");
                    }
                    sb.append(getString(R.string.x_photos, photoCount));
                } else if (photoCount == 1) {
                    if (sb.length() > 0) {
                        sb.append(", ");
                    }
                    sb.append(getString(R.string.one_photo));
                }
                if (videoCount > 1) {
                    if (sb.length() > 0) {
                        sb.append(", ");
                    }
                    sb.append(getString(R.string.x_videos, videoCount));
                } else if (videoCount == 1) {
                    if (sb.length() > 0) {
                        sb.append(", ");
                    }
                    sb.append(getString(R.string.one_video));
                }
                act.getSupportActionBar().setSubtitle(sb.toString());
            } else {
                act.getSupportActionBar().setSubtitle(null);
            }
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        mPresenter.onDestroy();
        mPresenter.detachView();
    }

    public MediaPresenter getPresenter() {
        return mPresenter;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);

        mPresenter = createPresenter();
        mPresenter.attachView(this);
        mPresenter.onCreate(savedInstanceState);
    }

    @Override
    public void onResume() {
        super.onResume();
        mPresenter.onResume();
    }

    @Override
    public void onPause() {
        super.onPause();
        mPresenter.onPause();
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == Activity.RESULT_OK &&
                (requestCode == MediaCab.COPY_REQUEST_CODE || requestCode == MediaCab.MOVE_REQUEST_CODE)) {
            ((MainActivity) getActivity()).getMediaCab().finishCopyMove(new File(data.getData().getPath()), requestCode);
        }
    }

    private void setStatus(String status) {
        ((MainActivity) getActivity()).setStatus(status);
    }

    public void onCreateSortMenuSelection(Menu menu, @MediaAdapter.SortMode int sortMode, boolean sortRememberDir) {
        switch (sortMode) {
            default:
                menu.findItem(R.id.sortNameAsc).setChecked(true);
                break;
            case MediaAdapter.SORT_NAME_DESC:
                menu.findItem(R.id.sortNameDesc).setChecked(true);
                break;
            case MediaAdapter.SORT_TAKEN_DATE_ASC:
                menu.findItem(R.id.sortModifiedAsc).setChecked(true);
                break;
            case MediaAdapter.SORT_TAKEN_DATE_DESC:
                menu.findItem(R.id.sortModifiedDesc).setChecked(true);
                break;
        }
        menu.findItem(R.id.sortCurrentDir).setChecked(sortRememberDir);

    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        inflater.inflate(R.menu.menu_media, menu);
        if (getActivity() != null) {
            boolean isMain = mPresenter.getPath() == null || mPresenter.getPath().equals(MediaFolderEntry.OVERVIEW_PATH);
            boolean isAlbumSelect = ((MainActivity) getActivity()).isSelectAlbumMode();
            menu.findItem(R.id.choose).setVisible(!isMain && isAlbumSelect);
            menu.findItem(R.id.viewMode).setVisible(!isAlbumSelect);
            menu.findItem(R.id.filter).setVisible(!isAlbumSelect);

            //TODO: Transfer menu control to presenter
            mPresenter.onCreateOptionsMenu(menu);

            @MediaAdapter.FileFilterMode int filterMode = PrefUtils.getFilterMode(getActivity());
            switch (filterMode) {
                default:
                    setStatus(null);
                    menu.findItem(R.id.filterAll).setChecked(true);
                    break;
                case MediaAdapter.FILTER_PHOTOS:
                    setStatus(getString(R.string.filtering_photos));
                    menu.findItem(R.id.filterPhotos).setChecked(true);
                    break;
                case MediaAdapter.FILTER_VIDEOS:
                    setStatus(getString(R.string.filtering_videos));
                    menu.findItem(R.id.filterVideos).setChecked(true);
                    break;
            }

            switch (PrefUtils.getGridColumns(getActivity())) {
                default:
                    menu.findItem(R.id.gridSizeOne).setChecked(true);
                    break;
                case 2:
                    menu.findItem(R.id.gridSizeTwo).setChecked(true);
                    break;
                case 3:
                    menu.findItem(R.id.gridSizeThree).setChecked(true);
                    break;
                case 4:
                    menu.findItem(R.id.gridSizeFour).setChecked(true);
                    break;
                case 5:
                    menu.findItem(R.id.gridSizeFive).setChecked(true);
                    break;
                case 6:
                    menu.findItem(R.id.gridSizeSix).setChecked(true);
                    break;
            }
        }

        menu.findItem(R.id.viewExplorer).setChecked(PrefUtils.isExplorerMode(getActivity()));
        if (PrefUtils.isGridMode(getContextCompat())) {
            menu.findItem(R.id.viewMode).setIcon(R.drawable.ic_action_view_list);
        } else {
            menu.findItem(R.id.viewMode).setIcon(R.drawable.ic_action_view_grid);
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.choose:
                getActivity().setResult(RESULT_OK, new Intent().setData(Uri.fromFile(new File(mPresenter.getPath()))));
                getActivity().finish();
                return true;
            case R.id.viewMode:
                mPresenter.setGridModeOn(!PrefUtils.isGridMode(getActivity()));
                return true;
            case R.id.viewExplorer:
                mPresenter.onOptionsItemSelected(item);
                return true;
            case R.id.filterAll:
                setFilterMode(MediaAdapter.FILTER_ALL);
                return true;
            case R.id.filterPhotos:
                setFilterMode(MediaAdapter.FILTER_PHOTOS);
                return true;
            case R.id.filterVideos:
                setFilterMode(MediaAdapter.FILTER_VIDEOS);
                return true;
            case R.id.sortCurrentDir:
            case R.id.sortNameAsc:
            case R.id.sortNameDesc:
            case R.id.sortModifiedAsc:
            case R.id.sortModifiedDesc:
                mPresenter.onOptionsItemSelected(item);
                return true;
            case R.id.gridSizeOne:
                item.setChecked(!item.isChecked());
                mPresenter.setGridColumns(1);
                break;
            case R.id.gridSizeTwo:
                item.setChecked(!item.isChecked());
                mPresenter.setGridColumns(2);
                break;
            case R.id.gridSizeThree:
                item.setChecked(!item.isChecked());
                mPresenter.setGridColumns(3);
                break;
            case R.id.gridSizeFour:
                item.setChecked(!item.isChecked());
                mPresenter.setGridColumns(4);
                break;
            case R.id.gridSizeFive:
                item.setChecked(!item.isChecked());
                mPresenter.setGridColumns(5);
                break;
            case R.id.gridSizeSix:
                item.setChecked(!item.isChecked());
                mPresenter.setGridColumns(6);
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    protected MediaPresenter createPresenter() {
        return new MediaPresenter();
    }
}