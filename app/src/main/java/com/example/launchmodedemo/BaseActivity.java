package com.example.launchmodedemo;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import androidx.appcompat.app.AppCompatActivity;

public class BaseActivity extends AppCompatActivity {

  private boolean mIsRemove = false;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    ActivityTaskManager.getInstance().put(this);
  }

  private static final String TAG = "BaseActivity";

  @Override
  protected void onStart() {
    super.onStart();
    Log.i(
        TAG,
        "onStart: " + ActivityTaskManager.getInstance().getLastActivity().getClass().getName());
    checkActivityJump();
  }

  @Override
  public void onBackPressed() {
    super.onBackPressed();
    removeActivity();
    checkActivityJump();
  }

  private void checkActivityJump() {
    if (ActivityTaskManager.getInstance().getLastActivity() != null) {
      Log.i(
          TAG,
          "onStart: " + ActivityTaskManager.getInstance().getLastActivity().getClass().getName());
      if (!ActivityTaskManager.getInstance()
          .getLastActivity()
          .getClass()
          .getName()
          .equals(this.getClass().getName())) {
        if (ActivityTaskManager.getInstance().getSingleInstanceActivityArray().size() > 0) {
          for (Activity activity :
              ActivityTaskManager.getInstance().getSingleInstanceActivityArray()) {
            if (activity
                .getClass()
                .getName()
                .equals(ActivityTaskManager.getInstance().getLastActivity().getClass().getName())) {
              ActivityTaskManager.getInstance().removeSingleInstanceActivity(activity);
              startActivity(
                  new Intent(this, ActivityTaskManager.getInstance().getLastActivity().getClass()));
              overridePendingTransition(0, 0);
              break;
            }
          }
        }
      }
    }
  }

  @Override
  protected void onStop() {
    super.onStop();
    Log.i(TAG, "onStop: " + this.getClass().getName());
  }

  @Override
  protected void onDestroy() {
    super.onDestroy();
    removeActivity();
  }

  /** 释放资源 */
  private void removeActivity() {
    ActivityTaskManager.getInstance().remove(this);
  }
}
