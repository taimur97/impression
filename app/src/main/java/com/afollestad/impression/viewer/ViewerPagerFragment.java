package com.afollestad.impression.viewer;

import android.annotation.TargetApi;
import android.app.Fragment;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.Snackbar;
import android.support.v4.view.ViewCompat;
import android.transition.Transition;
import android.util.Log;
import android.util.Pair;
import android.view.GestureDetector;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;

import com.afollestad.impression.BuildConfig;
import com.afollestad.impression.R;
import com.afollestad.impression.api.MediaEntry;
import com.afollestad.impression.utils.Utils;
import com.afollestad.impression.widget.ImpressionVideoView;
import com.bumptech.glide.Glide;
import com.bumptech.glide.Priority;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.animation.GlideAnimation;
import com.bumptech.glide.request.target.SimpleTarget;
import com.bumptech.glide.request.target.Target;
import com.davemorrissey.labs.subscaleview.ImageSource;
import com.davemorrissey.labs.subscaleview.SubsamplingScaleImageView;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;

import pl.droidsonroids.gif.GifDrawable;
import pl.droidsonroids.gif.GifImageView;
import rx.Observable;
import rx.Single;
import rx.SingleSubscriber;
import rx.Subscriber;
import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Action1;
import rx.schedulers.Schedulers;

/**
 * @author Aidan Follestad (afollestad)
 */
public class ViewerPagerFragment extends Fragment {

    public static final String INIT_WIDTH = "width";
    public static final String INIT_HEIGHT = "height";

    public static final String INIT_INDEX = "index";
    public static final String INIT_MEDIA_ENTRY = "media";
    public static final String INIT_MEDIA_PATH = "media_path";

    private MediaEntry mEntry;
    private String mMediaPath;

    private boolean mIsVideo;

    private int mThumbWidth;
    private int mThumbHeight;

    private int mIndex;
    private boolean mIsActive;

    private GifImageView mGifImageView;
    private ImageView mThumbImageView;
    private SubsamplingScaleImageView mImageView;
    private ImpressionVideoView mVideoView;

    private Bitmap mThumbnailBitmap;
    private int mFullWidth = -1;
    private int mFullHeight = -1;
    private Subscription mFullSizeSubscription;

    public static ViewerPagerFragment create(MediaEntry entry, int index, int width, int height) {
        ViewerPagerFragment frag = new ViewerPagerFragment();
        Bundle args = new Bundle();
        args.putSerializable(INIT_MEDIA_ENTRY, entry);
        args.putInt(INIT_INDEX, index);
        args.putInt(INIT_WIDTH, width);
        args.putInt(INIT_HEIGHT, height);
        frag.setArguments(args);
        return frag;
    }

    public static InputStream openStream(Context context, Uri uri) throws FileNotFoundException {
        if (uri == null) {
            return null;
        }
        if (uri.getScheme() == null || uri.getScheme().equalsIgnoreCase("file")) {
            return new FileInputStream(uri.getPath());
        } else {
            return context.getContentResolver().openInputStream(uri);
        }
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mThumbWidth = getArguments().getInt(INIT_WIDTH);
        mThumbHeight = getArguments().getInt(INIT_HEIGHT);

        mIndex = getArguments().getInt(INIT_INDEX);
        if (getArguments().containsKey(INIT_MEDIA_ENTRY)) {
            mEntry = (MediaEntry) getArguments().getSerializable(INIT_MEDIA_ENTRY);
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
            mVideoView = (ImpressionVideoView) view.findViewById(R.id.video);
            ViewCompat.setTransitionName(mVideoView, "view_" + mIndex);
        } else {
            view = inflater.inflate(R.layout.fragment_viewer, container, false);

            mImageView = (SubsamplingScaleImageView) view.findViewById(R.id.photo);
            mThumbImageView = (ImageView) view.findViewById(R.id.thumb);
            mGifImageView = (GifImageView) view.findViewById(R.id.gif);

            final GestureDetector detector = new GestureDetector(getActivity(), new GestureDetector.SimpleOnGestureListener() {
                @Override
                public boolean onDown(MotionEvent e) {
                    return true;
                }

                @Override
                public boolean onSingleTapConfirmed(MotionEvent e) {
                    invokeToolbar();
                    return true;
                }
            });
            View.OnTouchListener onTouch = new View.OnTouchListener() {
                @Override
                public boolean onTouch(View v, MotionEvent event) {
                    return detector.onTouchEvent(event);
                }
            };

            mThumbImageView.setOnTouchListener(onTouch);
            mGifImageView.setOnTouchListener(onTouch);

            mImageView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    invokeToolbar();
                }
            });

            if (BuildConfig.DEBUG) {
                mImageView.setDebug(true);
            }

            ViewCompat.setTransitionName(mThumbImageView, "view_" + mIndex);
        }
        return view;
    }

    public void setIsActive(boolean active) {
        boolean wasActive = mIsActive;
        mIsActive = active;

        if (!mIsActive) {
            if (mIsVideo) {
                if (mVideoView != null) {
                    mVideoView.pause(false);
                }
            } else if (wasActive && isAdded()) {
                loadThumbAndFullIfCurrent();
            }
        } else {
            if (!mIsVideo && !wasActive && isAdded()) {
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
            /*if (mEntry instanceof PhotoEntry) {
                if (((PhotoEntry) mEntry).originalUri != null)
                    uri = Uri.parse(((PhotoEntry) mEntry).originalUri);
            } else if (((VideoEntry) mEntry).originalUri != null) {
                uri = Uri.parse(((VideoEntry) mEntry).originalUri);
            }*/
            if (uri == null) {
                uri = Uri.fromFile(new File(mEntry.data()));
            }
        } else {
            uri = Uri.fromFile(new File(mMediaPath));
        }
        return uri;
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        if (mIsVideo) {
            if ((mEntry == null || mEntry.data() == null) && mMediaPath == null) {
                return;
            }
            mVideoView.setVideoURI(getUri());
            View playFrame = view.findViewById(R.id.playFrame);
            View seekFrame = view.findViewById(R.id.seekerFrame);
            FrameLayout.LayoutParams p = (FrameLayout.LayoutParams) seekFrame.getLayoutParams();
            p.rightMargin = ((ViewerActivity) getActivity()).getNavigationBarHeight(false, true);
            p.bottomMargin = ((ViewerActivity) getActivity()).getNavigationBarHeight(true, false);
            mVideoView.hookViews(this, playFrame);
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
        recycleFullImageShowThumbnail();
    }

    private BitmapFactory.Options getBitmapOptions(String uri) throws IOException {
        InputStream is;
        BitmapFactory.Options options;
        is = openStream(getActivity(), Uri.parse(uri));
        options = new BitmapFactory.Options();
        //Only the properties, so we can get the width/height
        options.inJustDecodeBounds = true;
        BitmapFactory.decodeStream(is, null, options);
        is.close();
        return options;
    }

    private void loadThumbAndFullIfCurrent() {
        if ((mEntry == null || mEntry.data() == null || mEntry.data().trim().isEmpty()) &&
                (mMediaPath == null || mMediaPath.trim().isEmpty())) {
            Utils.showErrorDialog(getActivity(), new Exception(getString(R.string.invalid_file_path_error)));
            return;
        }


        // Sets the initial cached thumbnail while the rest of loading takes place
        Glide.with(this)
                .load(getUri().toString())
                .asBitmap()
                .diskCacheStrategy(DiskCacheStrategy.RESULT)
                .priority(Priority.IMMEDIATE)
                .dontAnimate()
                .override(mThumbWidth, mThumbHeight)
                .transform(new KeepRatio(getActivity()))
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
                        final ViewerActivity activity = (ViewerActivity) getActivity();

                        recycleFullImageShowThumbnail();

                        mThumbnailBitmap = resource;

                        mThumbImageView.setImageBitmap(resource);
                        Log.e("HI", "mThumbImageView set imagebitmap" + mThumbImageView.toString());

                        //Lollipop+: start transition
                        activity.invalidateTransition();

                        if (mIsActive) {
                            loadFullImage();
                        }
                    }
                });
    }

    private void recycleFullImageShowThumbnail() {
        if (mFullSizeSubscription != null) {
            mFullSizeSubscription.unsubscribe();
            mFullSizeSubscription = null;
        }

        if (mImageView != null) {
            mImageView.setOnImageEventListener(null);
            mImageView.recycle();
            mImageView.setVisibility(View.INVISIBLE);
        }

        if (mGifImageView != null) {
            mGifImageView.setVisibility(View.INVISIBLE);
        }

        if (mThumbImageView != null) {
            mThumbImageView.setVisibility(View.VISIBLE);
        }
    }

    private void loadFullImage() {
        final ViewerActivity act = (ViewerActivity) getActivity();

        // If the activity transition didn't finish yet, wait for it to do so
        // So that the photo view attacher attaches correctly.
        if (!act.isFinishedTransition() && Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
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
                    if (act == null) {
                        return;
                    }
                    act.getWindow().getSharedElementEnterTransition().removeListener(this);
                    act.setFinishedTransition(true);

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
            mImageView.setVisibility(View.INVISIBLE);
            mGifImageView.postDelayed(new Runnable() {
                @Override
                public void run() {
                    if (!mIsActive) {
                        return;
                    }

                    mGifImageView.setImageURI(getUri());
                    mGifImageView.setVisibility(View.VISIBLE);

                    Observable.create(new Observable.OnSubscribe<Object>() {
                        @Override
                        public void call(Subscriber<? super Object> subscriber) {
                            while (!((GifDrawable) mGifImageView.getDrawable()).isRunning()) {
                                try {
                                    Thread.sleep(16);
                                } catch (InterruptedException e) {
                                    e.printStackTrace();
                                }
                            }
                            subscriber.onNext(null);
                        }
                    }).subscribeOn(Schedulers.io())
                            .observeOn(AndroidSchedulers.mainThread())
                            .subscribe(new Action1<Object>() {
                                @Override
                                public void call(Object o) {
                                    mThumbImageView.setVisibility(View.INVISIBLE);
                                }
                            });

                }
            }, 150);
        } else {
            if (mFullWidth == -1 || mFullHeight == -1) {
                mFullSizeSubscription = Single.create(new Single.OnSubscribe<Pair<Integer, Integer>>() {
                    @Override
                    public void call(SingleSubscriber<? super Pair<Integer, Integer>> singleSubscriber) {
                        BitmapFactory.Options options;
                        try {
                            options = getBitmapOptions(getUri().toString());
                            singleSubscriber.onSuccess(new Pair<>(options.outWidth, options.outHeight));
                        } catch (IOException e) {
                            singleSubscriber.onError(e);
                        }
                    }
                }).subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe(new Subscriber<Pair<Integer, Integer>>() {
                            @Override
                            public void onCompleted() {

                            }

                            @Override
                            public void onError(Throwable e) {
                                Snackbar.make(mThumbImageView, e.getMessage(), Snackbar.LENGTH_SHORT);
                            }

                            @Override
                            public void onNext(Pair<Integer, Integer> size) {
                                mFullWidth = size.first;
                                mFullHeight = size.second;
                                mFullSizeSubscription = null;
                                onFullImageSizeKnown();
                            }
                        });
            } else {
                onFullImageSizeKnown();
            }
        }
    }

    private void onFullImageSizeKnown() {
        // Load the full size image into the view from the file
        mImageView.setOnImageEventListener(new SubsamplingScaleImageView.DefaultOnImageEventListener() {

            private void onError(Throwable e) {
                Snackbar.make(mImageView, e.getLocalizedMessage(), Snackbar.LENGTH_SHORT);
            }

            @Override
            public void onImageLoadError(Throwable e) {
                super.onImageLoadError(e);
                onError(e);
            }

            @Override
            public void onTileLoadError(Throwable e) {
                super.onTileLoadError(e);
                onError(e);
            }

            @Override
            public void onPreviewLoadError(Throwable e) {
                super.onPreviewLoadError(e);
                onError(e);
            }

            @Override
            public void onImageLoaded() {
                final ViewerActivity activity = (ViewerActivity) getActivity();
                if (activity != null) {
                    activity.invalidateTransition();
                }
            }

            @Override
            public void onPreviewLoaded() {
                Log.e("HI", "Preview loaded, attempt hide thumb" + mThumbImageView.toString());
                //TODO: Figure out why just setting visibility doesn't work?
                mThumbImageView.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        if (mIsActive) {
                            mThumbImageView.setVisibility(View.INVISIBLE);
                        }
                    }
                }, 150);
            }
        });

        mImageView.setVisibility(View.VISIBLE);
        mImageView.setImage(ImageSource.uri(getUri().toString()).dimensions(mFullWidth, mFullHeight), ImageSource.cachedBitmap(mThumbnailBitmap));
    }

    public void finish() {
        recycleFullImageShowThumbnail();
        if (getActivity() != null) {
            ViewerActivity act = (ViewerActivity) getActivity();
            act.uiTapped(false, null);
        }
    }

    private void loadVideo() {
        if ((mEntry == null || mEntry.data() == null || mEntry.data().trim().isEmpty()) &&
                (mMediaPath == null || mMediaPath.trim().isEmpty())) {
            Utils.showErrorDialog(getActivity(), new Exception(getString(R.string.invalid_file_path_error)));
            return;
        }

        ViewerActivity act = (ViewerActivity) getActivity();
        if (act == null) {
            return;
        } else if (!act.isFinishedTransition() && mIsActive && Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
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
                    if (act == null) {
                        return;
                    }
                    act.getWindow().getEnterTransition().removeListener(this);
                    act.setFinishedTransition(true);
                    if (isAdded()) {
                        loadVideo();
                    }
                }
            });
            return;
        }

        mVideoView.start();
        mVideoView.pause();
    }

    private void invokeToolbar() {
        invokeToolbar(null);
    }

    public void invokeToolbar(ViewerActivity.ToolbarFadeListener callback) {
        if (getActivity() != null) {
            ViewerActivity act = (ViewerActivity) getActivity();
            act.uiTapped(true, callback);
        }
    }

    @Nullable
    public View getSharedElement() {
        if (mIsVideo) {
            return mVideoView;
        } else {
            return mThumbImageView;
        }
    }
}