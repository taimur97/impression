package com.afollestad.impression.viewer;

import android.annotation.TargetApi;
import android.app.Fragment;
import android.graphics.Bitmap;
import android.graphics.RectF;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.view.ViewCompat;
import android.support.v7.graphics.Palette;
import android.transition.Transition;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;

import com.afollestad.impression.R;
import com.afollestad.impression.api.PhotoEntry;
import com.afollestad.impression.api.VideoEntry;
import com.afollestad.impression.api.base.MediaEntry;
import com.afollestad.impression.utils.Utils;
import com.afollestad.impression.views.ImpressionVideoView;
import com.afollestad.impression.views.ScaleListenerImageView;
import com.bumptech.glide.Glide;
import com.bumptech.glide.Priority;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.load.resource.gif.GifDrawable;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.animation.GlideAnimation;
import com.bumptech.glide.request.target.SimpleTarget;
import com.bumptech.glide.request.target.Target;

import java.io.File;

import uk.co.senab.photoview.PhotoViewAttacher;

/**
 * @author Aidan Follestad (afollestad)
 */
public class ViewerPageFragment extends Fragment {

    public static final short LIGHT_MODE_ON = 2;
    public static final int INIT_DIMEN_NONE = -1;
    public static final String INIT_WIDTH = "width";
    public static final String INIT_HEIGHT = "height";
    public static final String INIT_INDEX = "index";
    public static final String INIT_MEDIA = "media";
    public static final String INIT_MEDIA_PATH = "media_path";
    private static final short LIGHT_MODE_UNLOADED = 0;
    private static final short LIGHT_MODE_LOADING = -1;
    private static final short LIGHT_MODE_OFF = 1;
    public short mLightMode = LIGHT_MODE_UNLOADED;
    private MediaEntry mEntry;
    private String mMediaPath;
    private boolean isVideo;
    private boolean isActive;
    private int mWidth;
    private int mHeight;
    private int mIndex;
    private boolean mImageZoomedUnderToolbar;

    private PhotoViewAttacher mAttacher;
    private ScaleListenerImageView mPhoto;
    private ImpressionVideoView mVideo;
    private Bitmap mBitmap;

    public static ViewerPageFragment create(MediaEntry entry, int index, int width, int height) {
        ViewerPageFragment frag = new ViewerPageFragment();
        Bundle args = new Bundle();
        args.putSerializable(INIT_MEDIA, entry);
        args.putInt(INIT_INDEX, index);
        args.putInt(INIT_WIDTH, width);
        args.putInt(INIT_HEIGHT, height);
        frag.setArguments(args);
        return frag;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mWidth = getArguments().getInt(INIT_WIDTH);
        mHeight = getArguments().getInt(INIT_HEIGHT);

        mIndex = getArguments().getInt(INIT_INDEX);
        if (getArguments().containsKey(INIT_MEDIA)) {
            mEntry = (MediaEntry) getArguments().getSerializable(INIT_MEDIA);
            isVideo = mEntry.isVideo();
        } else if (getArguments().containsKey(INIT_MEDIA_PATH)) {
            mMediaPath = getArguments().getString(INIT_MEDIA_PATH);
            String mime = Utils.getMimeType(Utils.getExtension(mMediaPath));
            isVideo = mime != null && mime.startsWith("video/");
        }
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        final View view;
        if (isVideo) {
            view = inflater.inflate(R.layout.fragment_viewer_video, container, false);
            mVideo = (ImpressionVideoView) view.findViewById(R.id.video);
            ViewCompat.setTransitionName(mVideo, "view_" + mIndex);
        } else {
            view = inflater.inflate(R.layout.fragment_viewer, container, false);
            mPhoto = (ScaleListenerImageView) view.findViewById(R.id.photo);
            ViewCompat.setTransitionName(mPhoto, "view_" + mIndex);
        }
        return view;
    }

    public ViewerPageFragment setIsActive(boolean active) {
        isActive = active;
        if (mVideo != null) {
            if (!isActive) {
                mVideo.pause(false);
            }
        }
        return this;
    }

    private boolean isGif() {
        String ext;
        if (mEntry != null) {
            ext = Utils.getExtension(mEntry.data());
        } else {
            ext = Utils.getExtension(mMediaPath);
        }
        return ext != null && ext.equalsIgnoreCase("gif");
    }

    private Uri getUri() {
        Uri uri = null;
        if (mEntry != null) {
            if (mEntry instanceof PhotoEntry) {
                if (((PhotoEntry) mEntry).originalUri != null)
                    uri = Uri.parse(((PhotoEntry) mEntry).originalUri);
            } else if (((VideoEntry) mEntry).originalUri != null) {
                uri = Uri.parse(((VideoEntry) mEntry).originalUri);
            }
            if (uri == null)
                uri = Uri.fromFile(new File(mEntry.data()));
        } else {
            uri = Uri.fromFile(new File(mMediaPath));
        }
        return uri;
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        if (isVideo) {
            if ((mEntry == null || mEntry.data() == null) && mMediaPath == null) return;
            mVideo.setVideoURI(getUri());
            View playFrame = view.findViewById(R.id.playFrame);
            View seekFrame = view.findViewById(R.id.seekerFrame);
            FrameLayout.LayoutParams p = (FrameLayout.LayoutParams) seekFrame.getLayoutParams();
            p.rightMargin = ((ViewerActivity) getActivity()).getNavigationBarHeight(false, true);
            p.bottomMargin = ((ViewerActivity) getActivity()).getNavigationBarHeight(true, false);
            mVideo.hookViews(this, playFrame);
            loadVideo();
            ((ViewerActivity) getActivity()).invalidateTransition();
        } else {
            loadImage();
        }

        // Might need the progress view later, e.g. for cloud images?
        view.findViewById(android.R.id.progress).setVisibility(View.GONE);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        if (mAttacher != null)
            mAttacher.cleanup();
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    private void loadImage() {
        if ((mEntry == null || mEntry.data() == null || mEntry.data().trim().isEmpty()) &&
                (mMediaPath == null || mMediaPath.trim().isEmpty())) {
            Utils.showErrorDialog(getActivity(), new Exception(getString(R.string.invalid_file_path_error)));
            attachPhotoView();
            return;
        }

        if (mWidth != INIT_DIMEN_NONE && mHeight != INIT_DIMEN_NONE && !isGif()) {
            // Sets the initial cached thumbnail while the rest of loading takes place
            Glide.with(this)
                    .load(mEntry.data())
                    .asBitmap()
                    .skipMemoryCache(true)
                    .diskCacheStrategy(DiskCacheStrategy.SOURCE)
                    .priority(Priority.IMMEDIATE)
                    .dontAnimate()
                    .override(mWidth, mHeight)
                    .listener(new RequestListener<String, Bitmap>() {
                        @Override
                        public boolean onException(Exception e, String model, Target<Bitmap> target, boolean isFirstResource) {
                            return false;
                        }

                        @Override
                        public boolean onResourceReady(Bitmap resource, String model, Target<Bitmap> target, boolean isFromMemoryCache, boolean isFirstResource) {
                            ((ViewerActivity) getActivity()).invalidateTransition();
                            return false;
                        }
                    })
                    .into(mPhoto);
        } else {
            ((ViewerActivity) getActivity()).invalidateTransition();
        }

//       String ext = Utils.getExtension(loadFile.getName());
//        if (ext != null && !ext.equalsIgnoreCase("gif")) {
//            // Using deep zoom with GIFs causes it to not be loaded
//            ion.deepZoom();
//        }

        ViewerActivity act = (ViewerActivity) getActivity();
        if (act == null)
            return;
        else if (!act.mFinishedTransition && isActive) {
            // If the activity transition didn't finish yet, wait for it to do so
            // So that the photo view attacher attaches correctly.
            act.getWindow().getSharedElementEnterTransition().addListener(new Transition.TransitionListener() {
                @Override
                public void onTransitionStart(Transition transition) {
                }

                @Override
                public void onTransitionCancel(Transition transition) {
                }

                @Override
                public void onTransitionPause(Transition transition) {
                }

                @Override
                public void onTransitionResume(Transition transition) {
                }

                @TargetApi(Build.VERSION_CODES.LOLLIPOP)
                @Override
                public void onTransitionEnd(Transition transition) {
                    ViewerActivity act = (ViewerActivity) getActivity();
                    if (act == null)
                        return;
                    act.getWindow().getEnterTransition().removeListener(this);
                    act.mFinishedTransition = true;
                    if (isAdded())
                        loadImage();
                }
            });
            return;
        }

//        if (getView() != null) {
//            getView().findViewById(android.R.id.progress).setVisibility(View.VISIBLE);
//        }

        if (isGif()) {
            // GIFs can't be loaded as a Bitmap
            mLightMode = LIGHT_MODE_OFF;
            Glide.with(getActivity())
                    .load(getUri().toString())
                    .asGif()
                    .listener(new RequestListener<String, GifDrawable>() {
                        @Override
                        public boolean onException(Exception e, String model, Target<GifDrawable> target, boolean isFirstResource) {
                            Utils.showErrorDialog(getActivity(), e);
                            attachPhotoView();
                            ((ViewerActivity) getActivity()).invalidateTransition();
                            return false;
                        }

                        @Override
                        public boolean onResourceReady(GifDrawable resource, String model, Target<GifDrawable> target, boolean isFromMemoryCache, boolean isFirstResource) {
                            if (!isAdded()) {
                                return false;
                            }
                            attachPhotoView();
                            ((ViewerActivity) getActivity()).invalidateTransition();
                            return false;
                        }
                    })
                    .into(mPhoto);
        } else {
            // Load the full size image into the view from the file
            //TODO
            Glide.with(getActivity())
                    .load(getUri().toString())
                    .asBitmap()
                    .into(new SimpleTarget<Bitmap>() {
                        @Override
                        public void onResourceReady(Bitmap resource, GlideAnimation<? super Bitmap> glideAnimation) {
                            if (!isAdded()) {
                                return;
                            }/* else if (e != null) {
                                Utils.showErrorDialog(getActivity(), e);
                                attachPhotoView();
                                ((ViewerActivity) getActivity()).invalidateTransition();
                                return;
                            }*/

                            mBitmap = resource;
                            if (mLightMode != LIGHT_MODE_LOADING) {
                                mLightMode = LIGHT_MODE_LOADING;
                                new Palette.Builder(mBitmap)
                                        .generate(new Palette.PaletteAsyncListener() {
                                            @Override
                                            public void onGenerated(Palette palette) {
                                                if (palette.getSwatches().size() > 0) {
                                                    float total = 0f;
                                                    for (Palette.Swatch s : palette.getSwatches()) {
                                                        total += s.getHsl()[2];
                                                    }
                                                    total /= palette.getSwatches().size();
                                                    mLightMode = total > 0.5f ? LIGHT_MODE_ON : LIGHT_MODE_OFF;
                                                } else {
                                                    mLightMode = LIGHT_MODE_OFF;
                                                }
                                                ((ViewerActivity) getActivity()).invalidateLightMode(
                                                        mImageZoomedUnderToolbar && mLightMode == LIGHT_MODE_ON);
                                            }
                                        });
                            }

                            mPhoto.setImageBitmap(resource);
                            attachPhotoView();

//                        if (getView() != null) {
//                            getView().findViewById(android.R.id.progress)
//                                    .setVisibility(View.GONE);
//                        }
                            // If no cached image was loaded, finish the transition now that there is an image displayed
                            ((ViewerActivity) getActivity()).invalidateTransition();
                        }
                    });
        }
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    private void loadVideo() {
        if ((mEntry == null || mEntry.data() == null || mEntry.data().trim().isEmpty()) &&
                (mMediaPath == null || mMediaPath.trim().isEmpty())) {
            Utils.showErrorDialog(getActivity(), new Exception(getString(R.string.invalid_file_path_error)));
            return;
        }

        ViewerActivity act = (ViewerActivity) getActivity();
        if (act == null)
            return;
        else if (!act.mFinishedTransition && isActive) {
            // If the activity transition didn't finish yet, wait for it to do so
            // So that the photo view attacher attaches correctly.
            act.getWindow().getSharedElementEnterTransition().addListener(new Transition.TransitionListener() {
                @Override
                public void onTransitionStart(Transition transition) {
                }

                @Override
                public void onTransitionCancel(Transition transition) {
                }

                @Override
                public void onTransitionPause(Transition transition) {
                }

                @Override
                public void onTransitionResume(Transition transition) {
                }

                @TargetApi(Build.VERSION_CODES.LOLLIPOP)
                @Override
                public void onTransitionEnd(Transition transition) {
                    ViewerActivity act = (ViewerActivity) getActivity();
                    if (act == null)
                        return;
                    act.getWindow().getEnterTransition().removeListener(this);
                    act.mFinishedTransition = true;
                    if (isAdded())
                        loadVideo();
                }
            });
            return;
        }

        mVideo.start();
        mVideo.pause();
    }

    private void invalidateUnderToolbar(RectF rectF) {
        if (getActivity() == null) return;
        final ViewerActivity act = (ViewerActivity) getActivity();
        mImageZoomedUnderToolbar = rectF.top <= act.mToolbar.getBottom() - (act.mToolbar.getHeight() / 2);
    }

    private void attachPhotoView() {
        mAttacher = mPhoto.setPhotoAttacher();
        mAttacher.setOnMatrixChangeListener(new PhotoViewAttacher.OnMatrixChangedListener() {
            @Override
            public void onMatrixChanged(RectF rectF) {
                final ViewerActivity act = (ViewerActivity) getActivity();
                if (act == null) return;
                invalidateUnderToolbar(rectF);
                if (mImageZoomedUnderToolbar) {
                    // Use detected value
                    if (mLightMode == LIGHT_MODE_LOADING)
                        act.invalidateLightMode(false);
                    else
                        act.invalidateLightMode(mLightMode == LIGHT_MODE_ON);
                } else {
                    // Force dark mode for black space above image
                    act.invalidateLightMode(false);
                }
            }
        });
        invalidateUnderToolbar(mAttacher.getDisplayRect());
        mAttacher.setOnViewTapListener(new PhotoViewAttacher.OnViewTapListener() {
            @Override
            public void onViewTap(View view, float v, float v2) {
                invokeToolbar();
            }
        });
        mAttacher.setOnPhotoTapListener(new PhotoViewAttacher.OnPhotoTapListener() {
            @Override
            public void onPhotoTap(View view, float v, float v2) {
                invokeToolbar();
            }
        });
    }

    private void invokeToolbar() {
        invokeToolbar(null);
    }

    public void invokeToolbar(ViewerActivity.ToolbarFadeListener callback) {
        if (getActivity() != null) {
            ViewerActivity act = (ViewerActivity) getActivity();
            act.invokeToolbar(true, callback);
            act.systemUIFocusChange();
        }
    }

    @Nullable
    public View getSharedElement() {
        if (isVideo) {
            return mVideo;
        } else {
            return mPhoto;
        }
    }
}