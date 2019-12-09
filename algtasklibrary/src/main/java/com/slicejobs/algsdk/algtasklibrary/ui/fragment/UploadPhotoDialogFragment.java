package com.slicejobs.algsdk.algtasklibrary.ui.fragment;


import android.app.Activity;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebView;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;


import com.slicejobs.algsdk.algtasklibrary.R;
import com.slicejobs.algsdk.algtasklibrary.R2;
import com.slicejobs.algsdk.algtasklibrary.model.PhotoRequirement;
import com.slicejobs.algsdk.algtasklibrary.ui.activity.ViewImageActivity;
import com.slicejobs.algsdk.algtasklibrary.ui.adapter.DialogRequirementPhotosAdapter;
import com.slicejobs.algsdk.algtasklibrary.utils.StringUtil;
import com.slicejobs.algsdk.algtasklibrary.utils.TextUtil;

import butterknife.ButterKnife;
import butterknife.BindView;
import butterknife.OnClick;

/**
 * Created by nlmartian on 11/22/15.
 */
public class UploadPhotoDialogFragment extends DialogFragment {
    public static final String ARG_REQUIREMENT = "arg_requirement";
    public static final String ARG_RESULT_DATA = "arg_result_data";

    private PhotoRequirement requirement;
    private DialogRequirementPhotosAdapter photosAdapter;
    private OnTakePhotoListener onTakePhotoListener;
    private String resultData;

    @BindView(R2.id.desc)
    WebView tvDesc;
    @BindView(R2.id.example_photos_divider)
    View examplePhotoDivier;
    @BindView(R2.id.example)
    TextView example;
    @BindView(R2.id.example_photos)
    RecyclerView tvPhotos;
    @BindView(R2.id.btn_upload)
    Button btnUpload;


    public interface OnTakePhotoListener {
        public void onTakeCamera(int takePhotoAuxiliaryLine);
        public void onUploadOrUse(boolean forceCamera, boolean allowReusePhoto, String resultData);
        public void onTakePhoto(boolean forceCamera, boolean allowReusePhoto, String resultData, int takePhotoAuxiliaryLine);

        public void onTakeVideo(boolean forceCamera);//上传视频

        public void onTakeRecord(boolean forceCamera, String stepId);//上传录音
    }

    public static UploadPhotoDialogFragment newInstance(PhotoRequirement requirement,String resultData) {
        UploadPhotoDialogFragment f = new UploadPhotoDialogFragment();
        Bundle bundle = new Bundle();
        bundle.putSerializable(ARG_REQUIREMENT, requirement);
        bundle.putString(ARG_RESULT_DATA, resultData);
        f.setArguments(bundle);
        return f;
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        try {
            this.onTakePhotoListener = (OnTakePhotoListener) activity;
        } catch (ClassCastException e) {
            throw new ClassCastException(activity.toString()
                    + " must implement OnTakePhotoListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        this.onTakePhotoListener = null;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            requirement = (PhotoRequirement) getArguments().getSerializable(ARG_REQUIREMENT);
            resultData = getArguments().getString(ARG_RESULT_DATA);
        }
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_upload_photo, container, false);
        ButterKnife.bind(this, view);
        initWidgets();
        return view;
    }

    @OnClick({R2.id.btn_upload, R2.id.close})
    public void onClick(View view) {
        if (view.getId() == R.id.btn_upload) {
            dismiss();
            if (onTakePhotoListener != null) {
                    if (null != requirement && StringUtil.isNotBlank(requirement.getEvidenceType()) && requirement.getEvidenceType().equals("video")) {
                        onTakePhotoListener.onTakeVideo(requirement.isForceCamera());
                    } else if (null != requirement && StringUtil.isNotBlank(requirement.getEvidenceType()) && requirement.getEvidenceType().equals("record")) {
                        onTakePhotoListener.onTakeRecord(requirement.isForceCamera(),requirement.getStepId());
                    } else {
                        onTakePhotoListener.onTakePhoto(requirement.isForceCamera(), requirement.isAllowReusePhoto(), resultData,requirement.getTakePhotoAuxiliaryLine());
                    }
            }
        } else if (view.getId() == R.id.close) {
            dismiss();
        }
    }

    private void initWidgets() {
        if (StringUtil.isNotBlank(requirement.getExampleText())) {
            String strHtml = TextUtil.html(requirement.getExampleText());
            String head = "<head>" +
                    "<meta name=\"viewport\" content=\"width=device-width, initial-scale=1.0, user-scalable=no\"> " +
                    "<style>img{max-width: 100%; width:auto; height:auto;}</style>" +
                    "</head>";
            String htmlStr = "<html>" + head + "<body>" + strHtml + "</body></html>";
            // 加载并显示HTML代码
            tvDesc.loadDataWithBaseURL(null, htmlStr, "text/html", "utf-8", null);
        }

        /*if (StringUtil.isNotBlank(requirement.getEvidenceType())) {
            if (requirement.getEvidenceType().equals("video")) {
                btnUpload.setText(SliceApp.CONTEXT.getString(R.string.upload_video));
            } else if (requirement.getEvidenceType().equals("record")) {
                btnUpload.setText(SliceApp.CONTEXT.getString(R.string.upload_record));
            } else {
                btnUpload.setText(SliceApp.CONTEXT.getString(R.string.upload_photo));
            }
        }*/

        photosAdapter = new DialogRequirementPhotosAdapter(tvPhotos);
        if (requirement.getExamplePhotos() == null || requirement.getExamplePhotos().isEmpty()) {
            examplePhotoDivier.setVisibility(View.GONE);
            example.setVisibility(View.GONE);
            tvPhotos.setVisibility(View.GONE);
        } else {
            examplePhotoDivier.setVisibility(View.VISIBLE);
            example.setVisibility(View.VISIBLE);
            tvPhotos.setVisibility(View.VISIBLE);
            photosAdapter.setUrls(requirement.getExamplePhotos());

            photosAdapter.setItemClickListener(new DialogRequirementPhotosAdapter.ItemClickListener() {
                @Override
                public void onItemClick(String url, int position) {
                    UploadPhotoDialogFragment.this.startActivity(ViewImageActivity.getIntent(getActivity(), url));
                }

                @Override
                public void onItemLongClick(ImageView imageView) {

                }
            });
            tvPhotos.setLayoutManager(new LinearLayoutManager(getActivity(), RecyclerView.HORIZONTAL, false));
            tvPhotos.setAdapter(photosAdapter);
        }
        if (requirement.getExampleText() == null || requirement.getExampleText().isEmpty()) {
            tvDesc.setVisibility(View.GONE);
        } else {
            tvDesc.setVisibility(View.VISIBLE);
        }
    }
}
