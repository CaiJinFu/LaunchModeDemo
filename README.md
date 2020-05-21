# LaunchModeDemo
启动singleInstance模式遇到的一些问题

## 前言

在实际应用中，使用singleinstance启动模式时，会遇到一些奇奇怪怪的问题。Android有四种启动模式，分别是standard，singleTop，singleTask，singleInstance。下面分别简单的介绍下这四种启动模式的作用。

### standard

Android 默认的一种启动模式。不需要为activity设置launchMode。这种启动模式简单的来说就是当你startActivity的时候，他就创建一个。

### singleTop

这种模式模式从字面意思就能看得出来，就是当前的activity处于栈顶的时候，当你startActivity当前的activity的时候，它不会创建新的activity，而是会复用之前的activity。举个例子，startActivity了一个ActivityA，ActivityA又startActivity了ActivityB，当在ActivityB再次startActivity一个ActivityB的时候，它不会创建一个新的ActivityB，而是复用之前的ActivityB。
这里需要注意的是，只有当前的activity处于栈顶的时候才管用。举个例子：startActivity了一个ActivityA，ActivityA又startActivity了ActivityB，ActivityB又startActivity了ActivityA，那么ActivityA还是会重新创建，而不是复用之前的ActivityA。

### singleTask

单一任务。意思就是说当前的activity只有一个实例，无论在任何地方startActivity出来这个activity，它都只存在一个实例。并且，它会将在他之上的所有activity都销毁。通常这个activity都是用来作为MainActivity。因为主页只需要存在一个，然后回到主页的时候可以将所有的activity都销毁起到退出应用的作用。举个例子，startActivity了一个ActivityA，ActivityA的启动模式为singleTask，那么在ActivityA里startActivity了一个ActivityB，在ActivityB里startActivity了一个ActivityC。此时在当前的任务栈中的顺序是，ActivityA->ActivityB->ActivityC。然后在ActivityC里重新startActivity了一个ActivityA，此时ActivityA会将存在于它之上的所有activity都销毁。所以此时任务栈中就只剩下ActivityA了。

### singleInstance

这个模式才是重点，也是比较容易入坑的一种启动模式。字面上理解为单一实例。它具备所有singleTask的特点，唯一不同的是，它是存在于另一个任务栈中。上面的三种模式都存在于同一个任务栈中，而这种模式则是存在于另一个任务栈中。举个例子，上面的启动模式都存在于地球上，而这种模式存在于火星上。整个Android系统就是个宇宙。下面来详细介绍一下singleInstance的坑。

#### singleInstance之一坑

此时有三个activity，ActivityA，ActivityB，ActivityC，除了ActivityB的启动模式为singleInstance，其他的启动模式都为默认的。startActivity了一个ActivityA，在ActivityA里startActivity了一个ActivityB，在ActivityB里startActivity了一个ActivityC。此时在当前的任务栈中的顺序是，ActivityA->ActivityB->ActivityC。照理来说在当前ActivityC页面按返回键，finish当前界面后应当回到ActivityB界面。但是事与愿违，奇迹出现了，页面直接回到了ActivityA。这是为什么呢？其实想想就能明白了，上面已经说过，singleInstance模式是存在于另一个任务栈中的。也就是说ActivityA和ActivityC是处于同一个任务栈中的，ActivityB则是存在另个栈中。所以当关闭了ActivityC的时候，它自然就会去找当前任务栈存在的activity。当前的activity都关闭了之后，才会去找另一个任务栈中的activity。也就是说当在ActivityC中finish之后，会回到ActivityA的界面，在ActivityA里finish之后会回到ActivityB界面。如果还想回到ActivityB的页面怎么办呢？

#### singleInstance之二坑

此时有两个个activity，ActivityA，ActivityB，ActivityA的启动模式为默认的，ActivityB的启动模式为singleInstance。当在ActivityA里startActivity了ActivityB，当前页面为ActivityB。按下home键。应用退到后台。此时再点击图标进入APP，按照天理来说，此时的界面应该是ActivityB，可是奇迹又出现了，当前显示的界面是ActivityA。这是因为当重新启动的时候，系统会先去找主栈（我是这么叫的）里的activity，也就是APP中LAUNCHER的activity所处在的栈。查看是否有存在的activity。没有的话则会重新启动LAUNCHER。这个又要怎么解决呢？

## 解决方案

代码在GitHub上[LaunchModeDemo
](https://github.com/CaiJinFu/LaunchModeDemo)

首先先将每个创建的activity用一个单例类保存下来，接着再用这个单例类保存启动了singleInstance模式的activity。在oncreate()时put,在onDestroy和onBackPressed时remove。为什么要在这两个地方都删除，待会会说明，已经在remove方法里处理了重复删除的问题。
先贴上管理activity的类，也就是添加删除activity的单例类ActivityTaskManager

```java
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

```
Singleton

```java
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

```

偷个懒，没什么注释，但是各位那么聪明应该看得懂。

然后贴上我的BaseActivity，基本上都是在这里处理的了

```java
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

```
在代码的注释已经解释了我的思路了。我简要说明一下，在onStart中判断，如果当前的activity跟添加进去的最后一个activity不是同一个的话，那么这种哦情况就有可能是最后一个activity的启动模式是SingleInstance，所以这时候就要遍历添加进去的SingleInstanceActivityArray，看是否有存在，有的话并且跟最后一个添加进去的activity是同一个的话就跳转。大家可以看看代码，在注释上写的很清楚的。最后附上代码地址，代码在GitHub上[LaunchModeDemo
](https://github.com/CaiJinFu/LaunchModeDemo)。

### 总结

Android的启动模式如果利用的好，还是可以解决很多问题的。启动模式还是值得好好的研究一下的。欢迎各位指教出错误，共同学习。如果有不对的地方，请大家指出，一起快乐的改bug。

