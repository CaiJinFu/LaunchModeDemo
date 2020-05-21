package com.example.launchmodedemo;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import androidx.constraintlayout.widget.ConstraintLayout;

public class MainActivityB extends BaseActivity implements View.OnClickListener {

  private TextView mTv;
  private ConstraintLayout mConstraintLayout;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_main);
    initView();
  }

  @Override
  protected void onStart() {
    super.onStart();
    ActivityTaskManager.getInstance().putSingleInstanceActivity(this);
  }

  @Override
  protected void onDestroy() {
    super.onDestroy();
    ActivityTaskManager.getInstance().removeSingleInstanceActivity(this);
  }

  private void initView() {
    mTv = (TextView) findViewById(R.id.tv);
    mTv.setText("B");
    mConstraintLayout = (ConstraintLayout) findViewById(R.id.constraintLayout);
    mConstraintLayout.setOnClickListener(this);
  }

  @Override
  public void onClick(View v) {
    switch (v.getId()) {
      case R.id.constraintLayout:
        startActivity(new Intent(this, MainActivityC.class));
        overridePendingTransition(0, 0);
        break;
      default:
        break;
    }
  }
}
