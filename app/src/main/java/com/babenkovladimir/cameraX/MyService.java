package com.babenkovladimir.cameraX;

import android.app.Service;
import android.content.Intent;
import android.os.FileObserver;
import android.os.IBinder;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

public class MyService extends Service {

  public static final String ACTION_COMMAND_START = "com.babenkovladimir.intent.ACTION_COMMAND_START";
  public static final String ACTION_COMMAND_STOP = "com.babenkovladimir.intent.ACTION_COMMAND_STOP";
  public static final String EXTRA_FILE_PATH = "com.babenkovladimir.intent.EXTRA_FILE_PATH";
  public static final String ACTION_EVENT = "com.babenkovladimir.intent.ACTION_EVENT";

  private String mObservedPath;
  private FileObserver mFileObserver;

  @Override
  public int onStartCommand(Intent intent, int flags, int startId) {
    if (intent != null) {
      if (ACTION_COMMAND_START.equals(intent.getAction())) {
        stopObserving();
        mObservedPath = intent.getStringExtra(EXTRA_FILE_PATH);
        startObserving();
      } else if (ACTION_COMMAND_STOP.equals(intent.getAction())) {
        stopObserving();
      }
    }

    return START_STICKY;
  }

  @Override
  public IBinder onBind(Intent intent) {
    return null;
  }

  private void startObserving() {
    mFileObserver = new FileObserver(mObservedPath, FileObserver.CLOSE_NOWRITE) {
      @Override
      public void onEvent(int event, String path) {
        sendBroadcast();
      }
    };

    mFileObserver.startWatching();
  }

  private void stopObserving() {
    if (mFileObserver != null) {
      mFileObserver.stopWatching();
      mFileObserver = null;
    }
  }


  private void sendBroadcast() {
    Intent intent = new Intent(ACTION_EVENT);
    LocalBroadcastManager.getInstance(this).sendBroadcast(intent);
    stopObserving();
    stopSelf();
  }

}
