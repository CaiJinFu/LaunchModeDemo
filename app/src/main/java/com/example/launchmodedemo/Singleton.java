package com.example.launchmodedemo;

/**
 * 单例构建类
 */
public abstract class Singleton<T> {
  private T mInstance;

  protected abstract T create();

  public final T get() {
    synchronized (this) {
      if (mInstance == null) {
        mInstance = create();
      }
      return mInstance;
    }
  }
}
