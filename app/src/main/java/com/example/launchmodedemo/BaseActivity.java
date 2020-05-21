package com.example.launchmodedemo;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import androidx.appcompat.app.AppCompatActivity;

public class BaseActivity extends AppCompatActivity {

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
    // 如果不在这里移除当前activity的话，在启动另一个界面的onStart的时候，判断处于栈顶的activity，
    // 也就是ActivityTaskManager.getInstance().getLastActivity()的时候，就会出现错误。
    // 原因是，当一个activity的onPause之后就会启动另一个activity，还没经历过onDestroy，
    // 而removeActivity();只放在onDestroy中的话就会在启动的activity的onStart
    // 获取ActivityTaskManager.getInstance().getLastActivity()返回的是错误。
    // 注意事项：应该在所有主动关闭activity，就是调用finish的时候要调用此代码，比如使用toolbar的返回键的时候，也应该调用removeActivity
    removeActivity();
  }

  private void checkActivityJump() {
    if (ActivityTaskManager.getInstance().getLastActivity() != null) {
      Log.i(
          TAG,
          "onStart: " + ActivityTaskManager.getInstance().getLastActivity().getClass().getName());
      // 如果当前的activity跟添加进去的最后一个activity不是同一个的话，那么这种哦情况就有可能是最后一个activity的启动模式是SingleInstance，
      // 所以这时候就要遍历添加进去的SingleInstanceActivityArray，看是否有存在，有的话并且跟最后一个添加进去的activity是同一个的话就跳转
      // 这里设置了跳转动画，是因为单例模式的跳转动画跟其他的模式不一样，看起来很难受，设置后看起来舒服些，也可以设置别的动画，
      // 退到后台再进来会一闪，十分明显，添加跳转动画看起来也会舒服些
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
