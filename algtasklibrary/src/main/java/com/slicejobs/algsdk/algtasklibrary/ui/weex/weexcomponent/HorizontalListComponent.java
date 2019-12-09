package com.slicejobs.algsdk.algtasklibrary.ui.weex.weexcomponent;

import android.content.Context;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.widget.ImageView;

import com.slicejobs.algsdk.algtasklibrary.ui.adapter.RequirementPhotosAdapter;
import com.taobao.weex.WXSDKInstance;
import com.taobao.weex.ui.action.BasicComponentData;
import com.taobao.weex.ui.component.WXComponent;
import com.taobao.weex.ui.component.WXComponentProp;
import com.taobao.weex.ui.component.WXVContainer;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by xiaoying on 17/8/9.
 */
public class HorizontalListComponent extends WXComponent<RecyclerView> {

    RequirementPhotosAdapter descRequirementPhotoAdapter;
    RecyclerView rvStepPhotos;
    WXSDKInstance mInstance;

    public HorizontalListComponent(WXSDKInstance instance, WXVContainer parent, BasicComponentData basicComponentData) {
        super(instance, parent, basicComponentData);
        mInstance = instance;
    }


    @Override
    protected RecyclerView initComponentHostView(Context context) {
        rvStepPhotos = new RecyclerView(context);

        rvStepPhotos.setHasFixedSize(true);
        rvStepPhotos.setLayoutManager(new LinearLayoutManager(context, RecyclerView.HORIZONTAL, false));
        descRequirementPhotoAdapter = new RequirementPhotosAdapter(RequirementPhotosAdapter.FROM_LACAL_EXAMINE, rvStepPhotos);
        rvStepPhotos.setAdapter(descRequirementPhotoAdapter);

        rvStepPhotos.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(RecyclerView recyclerView, int newState) {
                super.onScrollStateChanged(recyclerView, newState);
            }

            @Override
            public void onScrolled(RecyclerView recyclerView, int dx, int dy) {

                if (!recyclerView.canScrollHorizontally(1)) {//等于false
                    Map<String, Object> params = new HashMap<>();
                    params.put("changedIndex", -1);

                    //发送全局监听
                    mInstance.fireGlobalEventCallback("exampleComponentEvent", params);
                }

                super.onScrolled(recyclerView, dx, dy);
            }
        });


        descRequirementPhotoAdapter.setItemClickListener(new RequirementPhotosAdapter.ItemClickListener() {
            @Override
            public void onItemClick(String url, int position) {//点击当前照片
                Map<String, Object> params = new HashMap<>();
                params.put("clickImageIndex", position);
                mInstance.fireGlobalEventCallback("exampleComponentEvent", params);
            }

            @Override
            public void onItemLongClick(ImageView imageView) {

            }
        });



        Log.d("------------------", "当前控件的宽"+rvStepPhotos.getWidth()+"------"+rvStepPhotos.getHeight());


        return rvStepPhotos;
    }



    @WXComponentProp(name = "updatephotos")
    public void updatePhotoList(List<String> list) {
        descRequirementPhotoAdapter.setUrls(list);
        rvStepPhotos.scrollToPosition(0);
    }

}
