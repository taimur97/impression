package com.afollestad.impression.media;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;

import com.afollestad.impression.R;
import com.afollestad.impression.adapters.MediaAdapter;
import com.afollestad.impression.api.AlbumEntry;
import com.afollestad.impression.cab.MediaCab;
import com.afollestad.impression.providers.SortMemoryProvider;
import com.afollestad.impression.utils.PrefUtils;
import com.afollestad.impression.views.BreadCrumbLayout;

import java.io.File;

import static android.app.Activity.RESULT_OK;

/**
 * @author Aidan Follestad (afollestad)
 */
public class MediaFragment extends LoaderFragment<MediaAdapter.ViewHolder, MediaPresenter>
        implements MediaView {

    public MediaPresenter getPresenter() {
        return mPresenter;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);

        mPresenter.create();
    }

    @Override
    public void onResume() {
        super.onResume();
        mPresenter.resume();
    }

    public void setCrumb(BreadCrumbLayout.Crumb crumb) {
        this.crumb = crumb;
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

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        inflater.inflate(R.menu.fragment, menu);

        if (getActivity() != null) {
            boolean isMain = mPresenter.getAlbumPath() == null || mPresenter.getAlbumPath().equals(AlbumEntry.ALBUM_OVERVIEW);
            boolean isAlbumSelect = ((MainActivity) getActivity()).isSelectAlbumMode();
            menu.findItem(R.id.choose).setVisible(!isMain && isAlbumSelect);
            menu.findItem(R.id.viewMode).setVisible(!isAlbumSelect);
            menu.findItem(R.id.filter).setVisible(!isAlbumSelect);

            sortCache = SortMemoryProvider.remember(this, mPresenter.getAlbumPath());
            switch (sortCache) {
                default:
                    menu.findItem(R.id.sortNameAsc).setChecked(true);
                    break;
                case NAME_DESC:
                    menu.findItem(R.id.sortNameDesc).setChecked(true);
                    break;
                case MODIFIED_DATE_ASC:
                    menu.findItem(R.id.sortModifiedAsc).setChecked(true);
                    break;
                case MODIFIED_DATE_DESC:
                    menu.findItem(R.id.sortModifiedDesc).setChecked(true);
                    break;
            }
            menu.findItem(R.id.sortCurrentDir).setChecked(sortRememberDir);

            MediaAdapter.FileFilterMode filterMode = getFilterMode(getActivity());
            switch (filterMode) {
                default:
                    setStatus(null);
                    menu.findItem(R.id.filterAll).setChecked(true);
                    break;
                case PHOTOS:
                    setStatus(getString(R.string.filtering_photos));
                    menu.findItem(R.id.filterPhotos).setChecked(true);
                    break;
                case VIDEOS:
                    setStatus(getString(R.string.filtering_videos));
                    menu.findItem(R.id.filterVideos).setChecked(true);
                    break;
            }

            switch (mPresenter.getGridWidth()) {
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
        if (mPresenter.getViewMode() == MediaAdapter.ViewMode.GRID) {
            menu.findItem(R.id.viewMode).setIcon(R.drawable.ic_action_view_list);
        } else {
            menu.findItem(R.id.viewMode).setIcon(R.drawable.ic_action_view_grid);
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.choose:
                getActivity().setResult(RESULT_OK, new Intent().setData(Uri.fromFile(new File(mPresenter.getAlbumPath()))));
                getActivity().finish();
                return true;
            case R.id.viewMode:
                if (mPresenter.getViewMode() == MediaAdapter.ViewMode.GRID)
                    setViewMode(MediaAdapter.ViewMode.LIST);
                else setViewMode(MediaAdapter.ViewMode.GRID);
                return true;
            case R.id.viewExplorer:
                setExplorerMode(!PrefUtils.isExplorerMode(getActivity()));
                return true;
            case R.id.filterAll:
                setFilterMode(MediaAdapter.FileFilterMode.ALL);
                return true;
            case R.id.filterPhotos:
                setFilterMode(MediaAdapter.FileFilterMode.PHOTOS);
                return true;
            case R.id.filterVideos:
                setFilterMode(MediaAdapter.FileFilterMode.VIDEOS);
                return true;
            case R.id.sortNameAsc:
                setSortMode(MediaAdapter.SortMode.NAME_ASC, mPresenter.getAlbumPath());
                return true;
            case R.id.sortNameDesc:
                setSortMode(MediaAdapter.SortMode.NAME_DESC, sortRememberDir ? mPresenter.getAlbumPath() : null);
                return true;
            case R.id.sortModifiedAsc:
                setSortMode(MediaAdapter.SortMode.MODIFIED_DATE_ASC, sortRememberDir ? mPresenter.getAlbumPath() : null);
                return true;
            case R.id.sortModifiedDesc:
                setSortMode(MediaAdapter.SortMode.MODIFIED_DATE_DESC, sortRememberDir ? mPresenter.getAlbumPath() : null);
                return true;
            case R.id.sortCurrentDir:
                item.setChecked(!item.isChecked());
                if (item.isChecked()) {
                    sortRememberDir = true;
                    setSortMode(sortCache, mPresenter.getAlbumPath());
                } else {
                    sortRememberDir = false;
                    SortMemoryProvider.forget(getActivity(), mPresenter.getAlbumPath());
                    setSortMode(SortMemoryProvider.remember(getActivity(), null), null);
                }
                return true;
            case R.id.gridSizeOne:
                item.setChecked(!item.isChecked());
                setGridWidth(1);
                break;
            case R.id.gridSizeTwo:
                item.setChecked(!item.isChecked());
                setGridWidth(2);
                break;
            case R.id.gridSizeThree:
                item.setChecked(!item.isChecked());
                setGridWidth(3);
                break;
            case R.id.gridSizeFour:
                item.setChecked(!item.isChecked());
                setGridWidth(4);
                break;
            case R.id.gridSizeFive:
                item.setChecked(!item.isChecked());
                setGridWidth(5);
                break;
            case R.id.gridSizeSix:
                item.setChecked(!item.isChecked());
                setGridWidth(6);
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected MediaPresenter createPresenter() {
        return new MediaPresenter();
    }
}