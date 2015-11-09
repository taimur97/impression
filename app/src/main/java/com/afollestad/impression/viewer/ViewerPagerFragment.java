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
import android.transition.Transition;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;

import com.afollestad.impression.R;
import com.afollestad.impression.api.PhotoEntry;
import com.afollestad.impression.api.VideoEntry;
import com.afollestad.impression.api.base.MediaEntry;
import com.afollestad.impression.utils.Utils;
import com.afollestad.impression.widget.ImpressionVideoView;
import com.afollestad.impression.widget.ScaleListenerImageView;
import com.bumptech.glide.Glide;
import com.bumptech.glide.Priority;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.load.engine.bitmap_recycle.BitmapPool;
import com.bumptech.glide.load.resource.bitmap.FitCenter;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.animation.GlideAnimation;
import com.bumptech.glide.request.target.SimpleTarget;
import com.bumptech.glide.request.target.Target;
import com.davemorrissey.labs.subscaleview.ImageSource;
import com.davemorrissey.labs.subscaleview.SubsamplingScaleImageView;

import java.io.File;

import uk.co.senab.photoview.PhotoViewAttacher;

/**
 * @author Aidan Follestad (afollestad)
 */
public class ViewerPagerFragment extends Fragment {

    public static final short LIGHT_MODE_ON = 2;

    public static final String INIT_WIDTH = "width";
    public static final String INIT_HEIGHT = "height";

    public static final String INIT_INDEX = "index";
    public static final String INIT_MEDIA = "media";
    public static final String INIT_MEDIA_PATH = "media_path";

    private static final short LIGHT_MODE_UNLOADED = 0;
    private static final short LIGHT_MODE_LOADING = -1;
    private static final short LIGHT_MODE_OFF = 1;
    private short mLightMode = LIGHT_MODE_UNLOADED;

    private MediaEntry mEntry;
    private String mMediaPath;

    private boolean mIsVideo;

    private int mThumbWidth;
    private int mThumbHeight;

    private boolean mImageZoomedUnderToolbar;

    private int mIndex;
    private boolean mIsActive;

    private SimpleTarget<Bitmap> mFullImageTarget;

    private PhotoViewAttacher mAttacher;
    private ScaleListenerImageView mThumb;
    private SubsamplingScaleImageView mImageView;
    private ImpressionVideoView mVideo;

    private Bitmap mThumbnailBitmap;
    private int mFullWidth;
    private int mFullHeight;

    public static ViewerPagerFragment create(MediaEntry entry, int index, int width, int height) {
        ViewerPagerFragment frag = new ViewerPagerFragment();
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

        mThumbWidth = getArguments().getInt(INIT_WIDTH);
        mThumbHeight = getArguments().getInt(INIT_HEIGHT);

        mIndex = getArguments().getInt(INIT_INDEX);
        if (getArguments().containsKey(INIT_MEDIA)) {
            mEntry = (MediaEntry) getArguments().getSerializable(INIT_MEDIA);
            mIsVideo = mEntry.isVideo();
        } else if (getArguments().containsKey(INIT_MEDIA_PATH)) {
            mMediaPath = getArguments().getString(INIT_MEDIA_PATH);
            String mime = Utils.getMimeType(Utils.getExtension(mMediaPath));
            mIsVideo = mime != null && mime.startsWith("video/");
        }
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        final View view;
        if (mIsVideo) {
            view = inflater.inflate(R.layout.fragment_viewer_video, container, false);
            mVideo = (ImpressionVideoView) view.findViewById(R.id.video);
            ViewCompat.setTransitionName(mVideo, "view_" + mIndex);
        } else {
            view = inflater.inflate(R.layout.fragment_viewer, container, false);
            mImageView = (SubsamplingScaleImageView) view.findViewById(R.id.photo);

            mThumb = (ScaleListenerImageView) view.findViewById(R.id.thumb);

            ViewCompat.setTransitionName(mThumb, "view_" + mIndex);
        }
        return view;
    }

    public void setIsActive(boolean active) {
        boolean old = mIsActive;
        mIsActive = active;
        if (!mIsActive) {
            if (mVideo != null) {
                mVideo.pause(false);
            }
            if (old != mIsActive && isAdded()) {
                loadThumbAndFullIfCurrent();
            }
        } else {
            if (!old && isAdded()) {
                loadFullImage();
            }
        }
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
        if (mIsVideo) {
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
            loadThumbAndFullIfCurrent();
        }

        // Might need the progress view later, e.g. for cloud images?
        view.findViewById(android.R.id.progress).setVisibility(View.GONE);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        /*if (mAttacher != null)
            mAttacher.cleanup();*/
    }

    private void loadThumbAndFullIfCurrent() {
        if ((mEntry == null || mEntry.data() == null || mEntry.data().trim().isEmpty()) &&
                (mMediaPath == null || mMediaPath.trim().isEmpty())) {
            Utils.showErrorDialog(getActivity(), new Exception(getString(R.string.invalid_file_path_error)));
            attachPhotoView();
            return;
        }

        if (!isGif()) {
            mThumb.setVisibility(View.VISIBLE);
            mImageView.recycle();
            mImageView.setVisibility(View.INVISIBLE);
            // Sets the initial cached thumbnail while the rest of loading takes place
            Glide.with(this)
                    /*.using(new StreamModelLoader<String>() {
                        @Override
                        public DataFetcher<InputStream> getResourceFetcher(final String model, int i, int i1) {
                            return new DataFetcher<InputStream>() {
                                @Override
                                public InputStream loadData(Priority priority) throws Exception {
                                    throw new IOException();
                                }

                                @Override
                                public void cleanup() {

                                }

                                @Override
                                public String getId() {
                                    return model;
                                }

                                @Override
                                public void cancel() {

                                }
                            };
                        }
                    })*/
                    .load(mEntry.data())
                    .asBitmap()
                    .diskCacheStrategy(DiskCacheStrategy.RESULT)
                    .priority(Priority.IMMEDIATE)
                    .dontAnimate()
                    .override(mThumbWidth, mThumbHeight)
                    .transform(new FitCenter(getActivity()) {
                        @Override
                        protected Bitmap transform(BitmapPool pool, Bitmap toTransform, int outWidth, int outHeight) {
                            if (toTransform.getWidth() > toTransform.getHeight()) {
                                outHeight = outHeight;
                                outWidth = (int) (((float) toTransform.getHeight() / outHeight) * toTransform.getWidth());
                            }

                            return super.transform(pool, toTransform, outWidth, outHeight);
                        }

                        @Override
                        public String getId() {
                            return "Octopus";
                        }
                    })
                    .listener(new RequestListener<String, Bitmap>() {
                        @Override
                        public boolean onException(Exception e, String model, Target<Bitmap> target, boolean isFirstResource) {
                            return false;
                        }

                        @Override
                        public boolean onResourceReady(Bitmap resource, String model, Target<Bitmap> target, boolean isFromMemoryCache, boolean isFirstResource) {
                            if (!isFromMemoryCache) {
                                Log.e("ViewerPager", "Image not from cache:" + model + " " + target.toString());
                            } else {
                                Log.e("ViewerPager", "Image from cache:" + model + " " + target.toString());
                            }
                            return false;
                        }
                    })
                    .into(new SimpleTarget<Bitmap>() {
                        @Override
                        public void onResourceReady(Bitmap resource, GlideAnimation<? super Bitmap> glideAnimation) {
                            ((ViewerActivity) getActivity()).invalidateTransition();

                            /*mThumbnailBitmap = resource;*/

                            mThumb.setImageBitmap(resource);
                            mThumb.setScaleType(ImageView.ScaleType.FIT_CENTER);
                        }
                    });
        } else {
            ((ViewerActivity) getActivity()).invalidateTransition();
        }

        if (mIsActive) {
            loadFullImage();
        }
    }

    private void loadFullImage() {
        final ViewerActivity act = (ViewerActivity) getActivity();

        // If the activity transition didn't finish yet, wait for it to do so
        // So that the photo view attacher attaches correctly.
        if (!act.mFinishedTransition && Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
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
                    act.getWindow().getSharedElementEnterTransition().removeListener(this);
                    act.mFinishedTransition = true;

                    if (isAdded()) {
                        loadFullImage();
                    }
                }
            });
            return;
        }

//        if (getView() != null) {
//            getView().findViewById(android.R.id.progress).setVisibility(View.VISIBLE);
//        }

        if (isGif()) {
            /*// GIFs can't be loaded as a Bitmap
            mLightMode = LIGHT_MODE_OFF;
            Glide.with(this)
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
                    .into(mImageView);*/
        } else {
            // Load the full size image into the view from the file
            mImageView.setVisibility(View.VISIBLE);
            mImageView.setImage(ImageSource.uri(mEntry.data())/*.dimensions(300, 400), ImageSource.bitmap(mThumbnailBitmap)*/);
            mImageView.setOnImageEventListener(new SubsamplingScaleImageView.OnImageEventListener() {
                @Override
                public void onReady() {

                }

                @Override
                public void onImageLoaded() {
                    final ViewerActivity activity = (ViewerActivity) getActivity();
                    if (activity != null)
                        activity.invalidateTransition();
                    mThumb.setVisibility(View.INVISIBLE);
                }

                @Override
                public void onPreviewLoadError(Exception e) {

                }

                @Override
                public void onImageLoadError(Exception e) {

                }

                @Override
                public void onTileLoadError(Exception e) {

                }
            });

            mImageView.setDebug(true);
            mImageView.setOnTouchListener(new View.OnTouchListener() {
                @Override
                public boolean onTouch(View v, MotionEvent event) {
                    return false;
                }
            });

            /*mFullImageTarget = Glide.with(this)
                    .load(getUri().toString())
                    .asBitmap()
                    .format(DecodeFormat.PREFER_ARGB_8888)
                    .listener(new RequestListener<String, Bitmap>() {
                        @Override
                        public boolean onException(Exception e, String model, Target<Bitmap> target, boolean isFirstResource) {
                            Utils.showErrorDialog(getActivity(), e);
                            attachPhotoView();
                            ((ViewerActivity) getActivity()).invalidateTransition();

                            return false;
                        }

                        @Override
                        public boolean onResourceReady(Bitmap resource, String model, Target<Bitmap> target, boolean isFromMemoryCache, boolean isFirstResource) {
                            return false;
                        }
                    })
                    .into(new SimpleTarget<Bitmap>() {
                        @Override
                        public void onResourceReady(Bitmap resource, GlideAnimation<? super Bitmap> glideAnimation) {
                            if (!isAdded()) {
                                return;
                            }

                            if (mLightMode != LIGHT_MODE_LOADING) {
                                mLightMode = LIGHT_MODE_LOADING;
                                new Palette.Builder(resource)
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
                                                ((ViewerActivity) getActivity()).setLightMode(
                                                        mImageZoomedUnderToolbar && mLightMode == LIGHT_MODE_ON);
                                            }
                                        });
                            }

                            mImageView.setImageBitmap(resource);

                            attachPhotoView();

                            mFullImageTarget = null;

//                        if (getView() != null) {
//                            getView().findViewById(android.R.id.progress)
//                                    .setVisibility(View.GONE);
//                        }
                            // If no cached image was loaded, finish the transition now that there is an image displayed
                            ((ViewerActivity) getActivity()).invalidateTransition();
                        }
                    });*/
        }
    }

    public void finish() {
        /*if (mFullImageTarget != null) {
            Glide.clear(mFullImageTarget);
        }*/
        mImageView.recycle();
        mImageView.setVisibility(View.INVISIBLE);
        mThumb.setVisibility(View.VISIBLE);
    }

    private void loadVideo() {
        if ((mEntry == null || mEntry.data() == null || mEntry.data().trim().isEmpty()) &&
                (mMediaPath == null || mMediaPath.trim().isEmpty())) {
            Utils.showErrorDialog(getActivity(), new Exception(getString(R.string.invalid_file_path_error)));
            return;
        }

        ViewerActivity act = (ViewerActivity) getActivity();
        if (act == null)
            return;
        else if (!act.mFinishedTransition && mIsActive && Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
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
        /*mAttacher = mImageView.attachPhotoView();
        mAttacher.setOnMatrixChangeListener(new PhotoViewAttacher.OnMatrixChangedListener() {
            @Override
            public void onMatrixChanged(RectF rectF) {
                final ViewerActivity act = (ViewerActivity) getActivity();
                if (act == null) return;
                invalidateUnderToolbar(rectF);
                if (mImageZoomedUnderToolbar) {
                    // Use detected value
                    if (mLightMode == LIGHT_MODE_LOADING)
                        act.setLightMode(false);
                    else
                        act.setLightMode(mLightMode == LIGHT_MODE_ON);
                } else {
                    // Force dark mode for black space above image
                    act.setLightMode(false);
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
        });*/
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
        if (mIsVideo) {
            return mVideo;
        } else {
            return mThumb;
        }
    }
}