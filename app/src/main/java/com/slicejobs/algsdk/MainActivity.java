package com.slicejobs.algsdk;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import com.slicejobs.algsdk.algtasklibrary.app.SliceApp;
import com.slicejobs.algsdk.algtasklibrary.utils.SignUtil;

import butterknife.BindView;
import butterknife.ButterKnife;

public class MainActivity extends AppCompatActivity {

    @BindView(R.id.text)
    TextView textView;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);
        textView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String appId = "5df87dea9ba77";
                String userId = "12342";
                String mobile = "13142210414";
                String actionTime = System.currentTimeMillis() / 1000 + "";
                String sign = SignUtil.md5("appId=" + appId + "&appKey=Bz68utOQ2R1nIWwZqNw8CFsVJloshWTe0qEMJnALs14HXWsVLsXFWWWrHKxgFVGRSLi5wVu0t7J22ff9o1P2RDy3OVqzJyAsueocRS0fFh3TgWRVuEiojeWF0mTzacmX"
                        +"&userId=" + userId + "&mobile=" + mobile + "&actionTime=" + actionTime);

                SliceApp.getInstance().openAlg(MainActivity.this,appId,userId,mobile,actionTime,sign);
            }
        });
    }
}
