package com.slicejobs.algsdk.algtasklibrary.ui.activity;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.TextView;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import com.slicejobs.algsdk.algtasklibrary.R;
import com.slicejobs.algsdk.algtasklibrary.R2;
import com.slicejobs.algsdk.algtasklibrary.model.TaskImage;
import com.slicejobs.algsdk.algtasklibrary.model.TaskStepResult;
import com.slicejobs.algsdk.algtasklibrary.net.ISODateAdapter;
import com.slicejobs.algsdk.algtasklibrary.ui.adapter.MyTaskPhotosAdapter;
import com.slicejobs.algsdk.algtasklibrary.ui.base.BaseActivity;
import com.slicejobs.algsdk.algtasklibrary.utils.StringUtil;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;

import butterknife.ButterKnife;
import butterknife.BindView;
import butterknife.OnClick;

public class SelectTaskPhotoActivity extends BaseActivity {

    public static final String RESULR_JSON  = "cache_dir";
    public static final int BROWSER_BIG_IMAGE_SELECT  = 1232;
    @BindView(R2.id.recycler_view)
    RecyclerView cecycleView;
    @BindView(R2.id.action_comfirm)
    TextView selectConfirm;
    MyTaskPhotosAdapter myPhotosAdapter;
    private String resultJson;
    private ArrayList<String> photoList = new ArrayList<>();
    private ArrayList<String> selectPhotoList = new ArrayList<>();

    public static Intent getStartIntent(Context context, String resultData) {
        Intent intent = new Intent(context, SelectTaskPhotoActivity.class);
        intent.putExtra(RESULR_JSON, resultData);
        return intent;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_select_task_photo);
        ButterKnife.bind(this);
        resultJson = getIntent().getStringExtra(RESULR_JSON);
        initWidgets();//初始化照片显示适配器
    }

    @OnClick({R2.id.action_return,R2.id.action_comfirm})
    public void onClick(View view) {
        if (view.getId() == R.id.action_return) {
            finish();
        }else if (view.getId() == R.id.action_comfirm) {
            Intent intent = new Intent();
            intent.putStringArrayListExtra("selectPhotos", selectPhotoList);
            setResult(RESULT_OK,intent);
            this.finish();
        }
    }

    private void initWidgets() {
        cecycleView.setLayoutManager(new GridLayoutManager(this,4));
        myPhotosAdapter = new MyTaskPhotosAdapter();
        cecycleView.setAdapter(myPhotosAdapter);
        cecycleView.setItemAnimator(null);
        if(StringUtil.isNotBlank(resultJson)){
            Gson gson = new GsonBuilder().registerTypeAdapter(Date.class, new ISODateAdapter()).create();
            List<TaskStepResult> resultList = gson.fromJson(
                    resultJson, new TypeToken<List<TaskStepResult>>() {
                    }.getType());
            Collections.reverse(resultList);
            for (int i = 0; i < resultList.size(); i++) {
                TaskStepResult result = resultList.get(i);
                List<String> photos = result.getPhotos();
                if(photos != null && photos.size() != 0){
                    for (String photoPath:photos) {
                        if(!photoPath.contains("_thumbnail") && photoPath.startsWith("/storage/emulated/0/Android/data/com.slicejobs.ailinggong/files/currTaskCache")){
                            if(!photoList.contains(photoPath)) {
                                photoList.add(photoPath);
                                myPhotosAdapter.addImage(new TaskImage(photoPath, false));
                            }
                        }
                    }
                }
            }
        }
        myPhotosAdapter.setItemClickListener(new MyTaskPhotosAdapter.ItemClickListener() {
            @Override
            public void onItemImageClick(TaskImage taskImage, int position) {
                if (StringUtil.isNotBlank(photoList.get(position))) {
                    Intent intent = BigImageSelectActivity.getIntent(SelectTaskPhotoActivity.this, photoList,position,selectPhotoList);
                    intent.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);//如果存在就不创建，直接打开
                    startActivityForResult(intent,BROWSER_BIG_IMAGE_SELECT);
                }
            }

            @Override
            public void onItemSlectClick(TaskImage taskImage, int position) {
                if(taskImage.isIfSelect()){
                    selectPhotoList.add(photoList.get(position));
                }else {
                    if(selectPhotoList.contains(photoList.get(position))) {
                        selectPhotoList.remove(photoList.get(position));
                    }
                }
                if (selectPhotoList.size() != 0) {
                    selectConfirm.setEnabled(true);
                    selectConfirm.setBackgroundResource(R.drawable.selector_button_big_corners);
                } else {
                    selectConfirm.setEnabled(false);
                    selectConfirm.setBackgroundResource(R.drawable.shape_button_diaable_big_corners);
                }
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK && requestCode == BROWSER_BIG_IMAGE_SELECT) {
            if (data.getStringArrayListExtra("selectUrls") != null) {
                Intent intent = new Intent();
                intent.putStringArrayListExtra("selectPhotos", data.getStringArrayListExtra("selectUrls"));
                setResult(RESULT_OK,intent);
                this.finish();
            }
        }
    }
}
