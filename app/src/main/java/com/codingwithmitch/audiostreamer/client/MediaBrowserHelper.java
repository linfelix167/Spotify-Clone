package com.codingwithmitch.audiostreamer.client;

import android.content.ComponentName;
import android.content.Context;
import android.os.Bundle;
import android.os.RemoteException;
import android.support.annotation.NonNull;
import android.support.v4.media.MediaBrowserCompat;
import android.support.v4.media.MediaBrowserServiceCompat;
import android.support.v4.media.session.MediaControllerCompat;
import android.util.Log;

import java.util.List;

public class MediaBrowserHelper {

    private static final String TAG = "MediaBrowserHelper";

    private final Context mContext;
    private final Class<? extends MediaBrowserServiceCompat> mMediaBrowserServiceClass;

    private MediaBrowserCompat mMediaBrowser;
    private MediaControllerCompat mMediaController;

    private MediaBrowserConnectionCallback mMediaBrowserConnectionCallback;
    private final MediaBrowserSubscriptionCallback mMediaBrowserSubscriptionCallback;

    public MediaBrowserHelper(Context context, Class<? extends MediaBrowserServiceCompat> serviceClass) {
        mContext = context;
        mMediaBrowserServiceClass = serviceClass;

        mMediaBrowserConnectionCallback = new MediaBrowserConnectionCallback();
        mMediaBrowserSubscriptionCallback = new MediaBrowserSubscriptionCallback();
    }

    public void onStart() {
        if (mMediaBrowser == null) {

            mMediaBrowser =
                    new MediaBrowserCompat(
                            mContext,
                            new ComponentName(mContext, mMediaBrowserServiceClass),
                            mMediaBrowserConnectionCallback,
                            null);
            mMediaBrowser.connect();
        }
        Log.d(TAG, "onStart: CALLED: Creating MediaBrowser, and connecting");
    }

    public void onStop() {
        if (mMediaController != null) {
            mMediaController = null;
        }
        if (mMediaBrowser != null && mMediaBrowser.isConnected()) {
            mMediaBrowser.disconnect();
            mMediaBrowser = null;
        }
        Log.d(TAG, "onStop: CALLED: Releasing MediaController, Disconnecting from MediaBrowser");
    }

    // Receives callbacks from the MediaBrowser when it has successfully connected to the
    // MediaBrowserService (MusicService).
    private class MediaBrowserConnectionCallback extends MediaBrowserCompat.ConnectionCallback {

        // Happens as a result of onStart().
        @Override
        public void onConnected() {
            Log.d(TAG, "onConnected: CALLED");
            try {
                // Get a MediaController for the MediaSession.
                mMediaController =
                        new MediaControllerCompat(mContext, mMediaBrowser.getSessionToken());

            } catch (RemoteException e) {
                Log.d(TAG, String.format("onConnected: Problem: %s", e.toString()));
                throw new RuntimeException(e);
            }

            mMediaBrowser.subscribe(mMediaBrowser.getRoot(), mMediaBrowserSubscriptionCallback);
            Log.d(TAG, "onConnected: CALLED: subscribing to: " + mMediaBrowser.getRoot());

        }
    }


    // Receives callbacks from the MediaBrowser when the MediaBrowserService has loaded new media
    // that is ready for playback.
    public class MediaBrowserSubscriptionCallback extends MediaBrowserCompat.SubscriptionCallback {

        @Override
        public void onChildrenLoaded(@NonNull String parentId,
                                     @NonNull List<MediaBrowserCompat.MediaItem> children) {
            Log.d(TAG, "onChildrenLoaded: CALLED: " + parentId + ", " + children.toString());

            for (final MediaBrowserCompat.MediaItem mediaItem : children) {
                Log.d(TAG, "onChildrenLoaded: CALLED: queue item: " + mediaItem.getMediaId());
                mMediaController.addQueueItem(mediaItem.getDescription());
            }
        }
    }

    public MediaControllerCompat.TransportControls getTransportControls() {
        if (mMediaController == null) {
            Log.d(TAG, "getTransportControls: MediaController is null!");
            throw new IllegalStateException("MediaController is null!");
        }
        return mMediaController.getTransportControls();
    }

}

























