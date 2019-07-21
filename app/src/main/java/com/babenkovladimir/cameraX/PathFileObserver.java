package com.babenkovladimir.cameraX;

import android.os.FileObserver;
import java.util.concurrent.Callable;

public class PathFileObserver extends FileObserver {

  private final Callable<String> callable;

  public PathFileObserver(String root, Callable<String> callable) {
    super(root, FileObserver.CLOSE_NOWRITE);
    this.callable = callable;
  }

  @Override
  public void onEvent(int i, String s) {
    try {
      callable.call();
    } catch (Exception e) {
      e.printStackTrace();
    }
  }
}