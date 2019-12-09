package com.slicejobs.algsdk.algtasklibrary.ui.weex.weexcomponent;

import android.content.Context;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.slicejobs.algsdk.algtasklibrary.R;
import com.slicejobs.algsdk.algtasklibrary.R2;
import com.slicejobs.algsdk.algtasklibrary.model.ResultVideo;
import com.slicejobs.algsdk.algtasklibrary.ui.adapter.CacheResultPhotosAdapter;
import com.slicejobs.algsdk.algtasklibrary.utils.DensityUtil;
import com.slicejobs.algsdk.algtasklibrary.utils.StringUtil;
import com.taobao.weex.WXSDKInstance;
import com.taobao.weex.ui.action.BasicComponentData;
import com.taobao.weex.ui.component.WXComponent;
import com.taobao.weex.ui.component.WXComponentProp;
import com.taobao.weex.ui.component.WXVContainer;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


/**
 * Created by keller.zhou on 17/3/3.
 * 核心组件 用于给h5显示本地图片
 */
public class RecyclerComponent extends WXComponent<RecyclerView> {

    private CacheResultPhotosAdapter adapter;
    WXSDKInstance mInstance;

    public RecyclerComponent(WXSDKInstance instance, WXVContainer parent, BasicComponentData basicComponentData) {
        super(instance, parent, basicComponentData);
        mInstance = instance;
    }

    @Override
    protected RecyclerView initComponentHostView(Context context) {
        LayoutInflater inflater = LayoutInflater.from(context);
        RecyclerView view = (RecyclerView) inflater.inflate(R.layout.recycle_view, null);
        view.setLayoutManager(new LinearLayoutManager(context, RecyclerView.HORIZONTAL, false));
        view.setBackgroundResource(R.drawable.bg_bank_item);
        adapter = new CacheResultPhotosAdapter(mInstance.getContext());
        initWidget();
        view.setAdapter(adapter);

        //添加适配器
        return view;
    }


    private void initWidget() {
        adapter.setItemClickListener(new CacheResultPhotosAdapter.ItemClickListener() {

            @Override
            public void onItemClick(String url) {
                //通过全局更新个人信息
                Map<String, Object> params = new HashMap<>();
                params.put("url", url);

                if (url != null && StringUtil.isNotBlank(adapter.getEvidenceType()) && adapter.getEvidenceType().equals("video")) {
                    String videoUrl = adapter.pathUrlVideoPair.get(url).getVideoUrl();
                    params.put("url", videoUrl);
                }

                params.put("count", adapter.getItemCount());
                mInstance.fireGlobalEventCallback("evidenceEvent", params);
            }

            @Override
            public void onDelete(int pos, String url) {//删除一个凭证
                Map<String, Object> paramas = new HashMap<>();
                if (StringUtil.isNotBlank(adapter.getEvidenceType()) && adapter.getEvidenceType().equals("photo")) {
                    paramas.put("updatePhotoPaths", adapter.getNewUrls());
                } else if (StringUtil.isNotBlank(adapter.getEvidenceType()) && adapter.getEvidenceType().equals("video")) {
                    paramas.put("updateVideoPaths", adapter.getNewVideos());
                } else if (StringUtil.isNotBlank(adapter.getEvidenceType()) && adapter.getEvidenceType().equals("record")) {
                    paramas.put("updateRecordPaths", adapter.getNewRecord());
                }

                mInstance.refreshInstance(paramas);
            }

            @Override
            public void onDeleteClick(int pos, String url) {
                android.app.AlertDialog.Builder builer = new  android.app.AlertDialog.Builder(mInstance.getContext(),R.style.Dialog_Fullscreen);
                LayoutInflater layoutInflater = LayoutInflater.from(mInstance.getContext());
                View view = layoutInflater.inflate(R.layout.dialog_base_hint, null);
                TextView tvTitle = (TextView)view.findViewById(R.id.dialog_title);
                tvTitle.setText("蜂宝提示");
                TextView tvMsg = (TextView)view.findViewById(R.id.dialog_msg);
                tvMsg.setText("您确定要删除吗?");
                TextView tvBt = (TextView)view.findViewById(R.id.dialog_bt_hint);
                tvBt.setVisibility(View.GONE);
                LinearLayout linearLayout = (LinearLayout)view.findViewById(R.id.dialog_bt_layout);
                linearLayout.setVisibility(View.VISIBLE);
                Button btCancel = (Button) view.findViewById(R.id.dialog_cancel);
                btCancel.setText("取消");
                Button btDefine = (Button) view.findViewById(R.id.dialog_define);
                btDefine.setText("确定");
                builer.setView(view);
                android.app.AlertDialog dialog = builer.create();
                dialog.show();
                WindowManager.LayoutParams params = dialog.getWindow().getAttributes();
                params.width = (int) (DensityUtil.screenWidthInPix(mInstance.getContext()) * 0.82);
                dialog.getWindow().setAttributes(params);
                dialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
                btCancel.setOnClickListener(v -> {//点击取消按钮,调用接口
                    dialog.dismiss();

                });
                btDefine.setOnClickListener(v -> {//点击确定按钮,调用接口
                    dialog.dismiss();
                    if (adapter != null) {
                        adapter.confirmDelete(pos, url);
                    }
                });
            }

        });
    }


    @WXComponentProp(name = "addphoto")
    public void addPhoto(String path) {//添加一张照片
        adapter.addPath(path);
        getHostView().scrollToPosition(adapter.getItemCount() - 1);
        Map<String, Object> paramas = new HashMap<>();
        paramas.put("photoPaths", adapter.getNewUrls());
        mInstance.refreshInstance(paramas);

    }


    @WXComponentProp(name = "addrecord")
    public void addRecord(String path) {//添加一个录音
        adapter.addRecordPath(path);
        getHostView().scrollToPosition(adapter.getItemCount() - 1);
        Map<String, Object> paramas = new HashMap<>();
        paramas.put("recordPaths", adapter.getNewRecord());
        mInstance.refreshInstance(paramas);
    }

    @WXComponentProp(name = "addvideo")
    public void addVideo(ResultVideo path) {//添加一个视频
        if(path != null) {
            adapter.addVideoPath(path.getVideoUrl(), path.getThumbUrl());
            getHostView().scrollToPosition(adapter.getItemCount() - 1);
            Map<String, Object> paramas = new HashMap<>();
            paramas.put("videoPaths", adapter.getTaskVideos());
            mInstance.refreshInstance(paramas);
        }
    }




    @WXComponentProp(name = "updatephotos")
    public void updatePhotoList(List<String> list) {//添加一张照片
        if (StringUtil.isNotBlank(adapter.getEvidenceType()) && adapter.getEvidenceType().equals("photo")) {
            adapter.setUrls(list);
            getHostView().scrollToPosition(adapter.getItemCount() - 1);
            Map<String, Object> paramas = new HashMap<>();
            paramas.put("photoPaths", adapter.getNewUrls());
            mInstance.refreshInstance(paramas);
        }
    }



    @WXComponentProp(name = "updaterecords")
    public void updateRecordList(List<String> list) {
        if (StringUtil.isNotBlank(adapter.getEvidenceType()) && adapter.getEvidenceType().equals("record")) {
            adapter.setRecordUrls(list);
            getHostView().scrollToPosition(adapter.getItemCount() - 1);
            Map<String, Object> paramas = new HashMap<>();
            paramas.put("recordPaths", adapter.getNewRecord());
            mInstance.refreshInstance(paramas);
        }

    }

    @WXComponentProp(name = "updatevideos")
    public void updateVideoList(List<ResultVideo> list) {
        if (StringUtil.isNotBlank(adapter.getEvidenceType()) && adapter.getEvidenceType().equals("video")) {

            adapter.setVideoUrls(list);
            getHostView().scrollToPosition(adapter.getItemCount() - 1);
            Map<String, Object> paramas = new HashMap<>();
            paramas.put("videoPaths", adapter.getTaskVideos());
            mInstance.refreshInstance(paramas);
        }
    }

    @WXComponentProp(name = "type")
    public void setEvidenceType(String str) {
        if (StringUtil.isNotBlank(str)) {
            adapter.setEvidenceType(str);
        }
    }

    @WXComponentProp(name = "addphotos")
    public void addPhotoList(List<String> list) {//添加一张照片
        if(list != null) {
            adapter.addPaths((ArrayList<String>) list);
            getHostView().scrollToPosition(adapter.getItemCount() - 1);
            Map<String, Object> paramas = new HashMap<>();
            paramas.put("photoPaths", adapter.getNewUrls());
            mInstance.refreshInstance(paramas);
        }
    }

    @WXComponentProp(name = "addvideos")
    public void addVideoList(List<ResultVideo> list) {//添加一组视频
        if(list != null) {
            adapter.addVideoPaths((ArrayList<ResultVideo>) list);
            getHostView().scrollToPosition(adapter.getItemCount() - 1);
            Map<String, Object> paramas = new HashMap<>();
            paramas.put("videoPaths", adapter.getTaskVideos());
            mInstance.refreshInstance(paramas);
        }
    }

    @WXComponentProp(name= "hide")
    public void hideComponent(boolean hide) {
        if (hide) {
            getHostView().setVisibility(View.VISIBLE);
        } else {
            getHostView().setVisibility(View.INVISIBLE);
        }
    }


    @WXComponentProp(name="interaction")
    public void isDelete(boolean abled) {
        adapter.setDeletable(abled);
    }

}
