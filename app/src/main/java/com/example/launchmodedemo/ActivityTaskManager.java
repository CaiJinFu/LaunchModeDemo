package com.example.launchmodedemo;

import android.app.Activity;
import java.util.concurrent.CopyOnWriteArrayList;

/** Activity栈管理类，当Activity被创建是压栈，销毁时出栈 */
public class ActivityTaskManager {

  private final CopyOnWriteArrayList<Activity> ACTIVITY_ARRAY = new CopyOnWriteArrayList<>();
  private final CopyOnWriteArrayList<Activity> SINGLE_INSTANCE_ACTIVITY_ARRAY =
      new CopyOnWriteArrayList<>();

  private static final Singleton<ActivityTaskManager> SINGLETON =
      new Singleton<ActivityTaskManager>() {
        @Override
        protected ActivityTaskManager create() {
          return new ActivityTaskManager();
        }
      };

  public static ActivityTaskManager getInstance() {
    return SINGLETON.get();
  }

  public void put(Activity targetActivity) {
    boolean hasActivity = false;
    for (Activity activity : ACTIVITY_ARRAY) {
      if (targetActivity == activity) {
        hasActivity = true;
        break;
      }
    }
    if (!hasActivity) {
      ACTIVITY_ARRAY.add(targetActivity);
    }
  }

  public void remove(Activity targetActivity) {
    for (Activity activity : ACTIVITY_ARRAY) {
      if (targetActivity == activity) {
        ACTIVITY_ARRAY.remove(targetActivity);
        break;
      }
    }
  }

  public void putSingleInstanceActivity(Activity targetActivity) {
    boolean hasActivity = false;
    for (Activity activity : SINGLE_INSTANCE_ACTIVITY_ARRAY) {
      if (targetActivity == activity) {
        hasActivity = true;
        break;
      }
    }
    if (!hasActivity) {
      SINGLE_INSTANCE_ACTIVITY_ARRAY.add(targetActivity);
    }
  }

  public void removeSingleInstanceActivity(Activity targetActivity) {
    SINGLE_INSTANCE_ACTIVITY_ARRAY.remove(targetActivity);
  }

  public CopyOnWriteArrayList<Activity> getSingleInstanceActivityArray() {
    return SINGLE_INSTANCE_ACTIVITY_ARRAY;
  }

  public Activity getTopActivity() {
    if (ACTIVITY_ARRAY.isEmpty()) {
      return null;
    }
    return ACTIVITY_ARRAY.get(0);
  }

  public Activity getLastActivity() {
    if (ACTIVITY_ARRAY.isEmpty()) {
      return null;
    }
    return ACTIVITY_ARRAY.get(ACTIVITY_ARRAY.size() - 1);
  }
}
