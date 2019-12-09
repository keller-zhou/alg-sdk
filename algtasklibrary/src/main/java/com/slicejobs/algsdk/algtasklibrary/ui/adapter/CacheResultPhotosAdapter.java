package com.slicejobs.algsdk.algtasklibrary.ui.adapter;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.drawable.ColorDrawable;
import android.media.MediaPlayer;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.assist.FailReason;
import com.nostra13.universalimageloader.core.listener.SimpleImageLoadingListener;
import com.slicejobs.algsdk.algtasklibrary.R;
import com.slicejobs.algsdk.algtasklibrary.R2;
import com.slicejobs.algsdk.algtasklibrary.model.ResultVideo;
import com.slicejobs.algsdk.algtasklibrary.model.TaskStep;
import com.slicejobs.algsdk.algtasklibrary.model.TaskVideo;
import com.slicejobs.algsdk.algtasklibrary.utils.StringUtil;
import com.slicejobs.algsdk.algtasklibrary.utils.TimeUtils;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import butterknife.ButterKnife;
import butterknife.BindView;

/**
 * Created by nlmartian on 8/22/15.
 */
public class CacheResultPhotosAdapter extends RecyclerView.Adapter {
    public static final String TAG = CacheResultPhotosAdapter.class.getSimpleName();
    private static final int MAX_UPLOAD_NUM = 2;
    private static BlockingQueue QUEUE;
    private static ThreadPoolExecutor EXECUTOR;
    static {
        QUEUE = new LinkedBlockingQueue();
        EXECUTOR = new ThreadPoolExecutor(2, MAX_UPLOAD_NUM, 3, TimeUnit.SECONDS, QUEUE);
    }

    private ColorDrawable defaultPlaceholder = new ColorDrawable(0xff898989);
    private DisplayImageOptions options = new DisplayImageOptions.Builder()
            .showImageOnFail(defaultPlaceholder)
            .showImageOnLoading(defaultPlaceholder)
            .cacheInMemory(true)
            .cacheOnDisk(true)
            .build();
    private ArrayList<String> paths = new ArrayList<>();
    private ArrayList<String> urls = new ArrayList<>();
    private ItemClickListener itemClickListener;
    public HashMap<String, String> nativeVideo = new HashMap<>();//本地图片，本地视频,只为当前步骤视频的读取
    private RecyclerView recyclerView;
    private boolean canDelete = true;

    private String currEvidenceType = "photo";//默认类型是图片,video视屏  record是录音

    public HashMap<String, String> pathUrlPair = new HashMap<>(); //本地图片，服务器图片

    public HashMap<String, ResultVideo> pathUrlVideoPair = new HashMap<>();//本地缩略图绑定 ，(网络缩略图 和网络视频)

    public HashMap<String, String> pathUrlRecordPair = new HashMap<>();//本地录音，服务器录音
    public HashMap<String, String> pathTimeMediaPair = new HashMap<>();//path，时长

    private ArrayList<TaskVideo> taskVideos = new ArrayList<>();//当前步骤的video
    private Context context;
    public CacheResultPhotosAdapter(Context context) {
        this.context = context;
    }




    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_result_photo, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        if (currEvidenceType.equals(TaskStep.EVIDENCETYPE_PHPTO)) {
            ViewHolder itemHolder = ((ViewHolder) holder);
            itemHolder.mediaTime.setVisibility(View.GONE);
            if (position < paths.size()) {
                final String url = paths.get(position);
                itemHolder.imageUrl = url;
                itemHolder.progressbar.setVisibility(View.VISIBLE);
                itemHolder.delete.setVisibility(View.VISIBLE);
                //本地的直接显示，网络图片显示阿里云缩略图
                String urlStr;
                if (url.startsWith("/")) {
                    if (new File(url.substring(0, url.length() - 5) + "_thumbnail.jpeg").exists()) {//有缩略图
                        urlStr = "file://" + url.substring(0, url.length() - 5) + "_thumbnail.jpeg";

                    } else {//显示原图
                        urlStr = "file://" + url;
                    }

                } else {
                    urlStr = url + "?x-oss-process=image/resize,m_fixed,h_200,w_200";
                }

                ImageLoader.getInstance().displayImage(
                        urlStr,
                        itemHolder.imageView,
                        options,
                        new SimpleImageLoadingListener() {
                            @Override
                            public void onLoadingFailed(String imageUri, View view, FailReason failReason) {
                                itemHolder.progressbar.setVisibility(View.GONE);
                            }

                            @Override
                            public void onLoadingComplete(String imageUri, View view, Bitmap loadedImage) {
                                itemHolder.progressbar.setVisibility(View.GONE);
                            }
                        });

                if (itemClickListener != null) {
                    holder.itemView.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            itemClickListener.onItemClick(url);
                        }
                    });
                    itemHolder.delete.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            if (canDelete) {
                                itemClickListener.onDeleteClick(position, url);
                            }
                        }
                    });
                }
            } else {
                itemHolder.imageUrl = null;
                itemHolder.tvStatus.setVisibility(View.GONE);
                itemHolder.progressbar.setVisibility(View.GONE);
                itemHolder.delete.setVisibility(View.GONE);
                itemHolder.imageView.setImageResource(R.drawable.icon_add_photo);
                holder.itemView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if (itemClickListener != null) {
                            itemClickListener.onItemClick(null);
                        }
                    }
                });
            }

            if (this.currEvidenceType != null && this.currEvidenceType.equals(TaskStep.EVIDENCETYPE_VIDEO) && paths.size() > position) {
                itemHolder.videoIcon.setVisibility(View.VISIBLE);
            } else {
                itemHolder.videoIcon.setVisibility(View.GONE);
            }
        } else if (currEvidenceType.equals(TaskStep.EVIDENCETYPE_VIDEO)) {
            ViewHolder itemHolder = ((ViewHolder) holder);
            if (position < paths.size()) {
                final String url = paths.get(position);
                ResultVideo resultVideo = pathUrlVideoPair.get(url);
                if(resultVideo != null){
                    String mediaTime = pathTimeMediaPair.get(resultVideo.getVideoUrl());
                    if (StringUtil.isNotBlank(mediaTime)) {
                        itemHolder.mediaTime.setVisibility(View.VISIBLE);
                        itemHolder.mediaTime.setText(mediaTime);
                    } else {
                        itemHolder.mediaTime.setVisibility(View.GONE);
                    }
                } else {
                    itemHolder.mediaTime.setVisibility(View.GONE);
                }
                itemHolder.imageUrl = url;
                itemHolder.progressbar.setVisibility(View.VISIBLE);
                itemHolder.delete.setVisibility(View.VISIBLE);
                //本地的直接显示，网络图片显示阿里云缩略图
                String urlStr;
                if (url.startsWith("/")) {
                    urlStr = "file://" + url;
                } else {

                    urlStr = url + "?x-oss-process=image/resize,m_fixed,h_200,w_200";
                }

                ImageLoader.getInstance().displayImage(
                        urlStr,
                        itemHolder.imageView,
                        options,
                        new SimpleImageLoadingListener() {
                            @Override
                            public void onLoadingFailed(String imageUri, View view, FailReason failReason) {
                                itemHolder.progressbar.setVisibility(View.GONE);
                            }

                            @Override
                            public void onLoadingComplete(String imageUri, View view, Bitmap loadedImage) {
                                itemHolder.progressbar.setVisibility(View.GONE);
                            }
                        });

                if (itemClickListener != null) {
                    holder.itemView.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            itemClickListener.onItemClick(url);
                        }
                    });
                    itemHolder.delete.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            if (canDelete) {
                                itemClickListener.onDeleteClick(position, url);
                            }
                        }
                    });
                }
            } else {
                itemHolder.imageUrl = null;
                itemHolder.tvStatus.setVisibility(View.GONE);
                itemHolder.progressbar.setVisibility(View.GONE);
                itemHolder.delete.setVisibility(View.GONE);
                itemHolder.imageView.setImageResource(R.drawable.icon_add_video);
                itemHolder.mediaTime.setVisibility(View.GONE);
                holder.itemView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if (itemClickListener != null) {
                            itemClickListener.onItemClick(null);
                        }
                    }
                });
            }

            if (this.currEvidenceType != null && this.currEvidenceType.equals(TaskStep.EVIDENCETYPE_VIDEO) && paths.size() > position) {
                itemHolder.videoIcon.setVisibility(View.VISIBLE);
            } else {
                itemHolder.videoIcon.setVisibility(View.GONE);
            }
        } else {//这是一个音频文件
            ViewHolder itemHolder = ((ViewHolder) holder);
            if (position < paths.size()) {
                final String url = paths.get(position);
                String mediaTime = pathTimeMediaPair.get(url);
                if (StringUtil.isNotBlank(mediaTime)) {
                    itemHolder.mediaTime.setVisibility(View.VISIBLE);
                    itemHolder.mediaTime.setText(mediaTime);
                } else {
                    itemHolder.mediaTime.setVisibility(View.GONE);
                }
                itemHolder.imageUrl = url;
                itemHolder.imageView.setImageResource(R.drawable.ic_record_image);//音频默认设置设个图标
                itemHolder.progressbar.setVisibility(View.GONE);
                itemHolder.delete.setVisibility(View.VISIBLE);
                if (itemClickListener != null) {
                    holder.itemView.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            itemClickListener.onItemClick(url);
                        }
                    });
                    itemHolder.delete.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            if (canDelete) {
                                itemClickListener.onDeleteClick(position, url);
                            }
                        }
                    });
                }
            } else {
                itemHolder.imageUrl = null;
                itemHolder.tvStatus.setVisibility(View.GONE);
                itemHolder.progressbar.setVisibility(View.GONE);
                itemHolder.delete.setVisibility(View.GONE);
                itemHolder.imageView.setImageResource(R.drawable.icon_add_record);
                itemHolder.mediaTime.setVisibility(View.GONE);
                holder.itemView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if (itemClickListener != null) {
                            itemClickListener.onItemClick(null);
                        }
                    }
                });


            }

            if (this.currEvidenceType != null && this.currEvidenceType.equals(TaskStep.EVIDENCETYPE_VIDEO) && paths.size() > position) {
                itemHolder.videoIcon.setVisibility(View.VISIBLE);
            } else {
                itemHolder.videoIcon.setVisibility(View.GONE);
            }
        }


    }


    @Override
    public int getItemCount() {
        return paths.size() + 1;
    }

    @Override
    public int getItemViewType(int position) {
        return position;
    }

    public void setEvidenceType(String evidenceType) {
        this.currEvidenceType = evidenceType;
    }

    public String getEvidenceType() {
        return this.currEvidenceType;
    }


    /**
     * 初始化步骤照片,都是本地地址
     * @param urls
     */
    public void setUrls(List<String> urls) {
        if (urls == null) {
            return;
        }
        this.urls.clear();
        this.urls.addAll(urls);
        this.paths.clear();
        this.paths.addAll(urls);
        this.pathUrlPair.clear();
        this.nativeVideo.clear();
        this.pathUrlVideoPair.clear();
        this.pathUrlRecordPair.clear();
        notifyDataSetChanged();
    }

    /**
     * 初始化步骤视频
     * @param videoUrls
     */
    public void setVideoUrls(List<ResultVideo> videoUrls) {
        if (videoUrls == null) {
            return;
        }
        this.urls.clear();
        this.paths.clear();
        this.pathUrlPair.clear();
        this.nativeVideo.clear();
        this.pathUrlVideoPair.clear();
        this.pathUrlRecordPair.clear();
        this.taskVideos.clear();
        for (ResultVideo resultVideo :videoUrls) {
            this.urls.add(resultVideo.getThumbUrl());
            this.paths.add(resultVideo.getThumbUrl());
            pathUrlVideoPair.put(resultVideo.getThumbUrl(), resultVideo);
            this.taskVideos.add(new TaskVideo(resultVideo.getVideoUrl(),resultVideo.getThumbUrl()));

            if(resultVideo.getVideoUrl().startsWith("http")) {
                String[] strArray = resultVideo.getVideoUrl().split("_");
                for(String subStr:strArray){
                    if(subStr.startsWith("dur")){
                        String durationStr = subStr.substring(3);
                        String totalTime = TimeUtils.getTime(Integer.parseInt(durationStr) * 1000);
                        pathTimeMediaPair.put(resultVideo.getVideoUrl(), totalTime);
                    }
                }
            } else {
                MediaPlayer mediaPlayer = new MediaPlayer();
                try {
                    mediaPlayer.setDataSource(resultVideo.getVideoUrl());
                    mediaPlayer.prepare();
                    int duration = mediaPlayer.getDuration();
                    if (0 != duration) {
                        String totalTime = TimeUtils.getTime(duration);
                        pathTimeMediaPair.put(resultVideo.getVideoUrl(), totalTime);
                        //记得释放资源
                        mediaPlayer.release();
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        notifyDataSetChanged();
    }

    /**
     * 初始化录音
     * @param urls
     */
    public void setRecordUrls(List<String> urls) {
        if (urls == null) {
            return;
        }
        this.urls.clear();
        this.urls.addAll(urls);
        this.paths.clear();
        this.paths.addAll(urls);
        this.pathUrlPair.clear();
        this.nativeVideo.clear();
        this.pathUrlVideoPair.clear();
        this.pathUrlRecordPair.clear();
        pathTimeMediaPair.clear();
        for (String str : urls) {
            pathUrlRecordPair.put(str, str);
            if(str.startsWith("http")) {
                String[] strArray = str.split("_");
                for(String subStr:strArray){
                    if(subStr.startsWith("dur")){
                        String durationStr = subStr.substring(3);
                        String totalTime = TimeUtils.getTime(Integer.parseInt(durationStr) * 1000);
                        pathTimeMediaPair.put(str, totalTime);
                    }
                }
            } else {
                MediaPlayer mediaPlayer = new MediaPlayer();
                try {
                    mediaPlayer.setDataSource(str);
                    mediaPlayer.prepare();
                    int duration = mediaPlayer.getDuration();
                    if (0 != duration) {
                        String totalTime = TimeUtils.getTime(duration);
                        pathTimeMediaPair.put(str, totalTime);
                        //记得释放资源
                        mediaPlayer.release();
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        notifyDataSetChanged();
    }



    /**
     * 上传图片
     * @param path
     */
    public void addPath(String path) {
        this.paths.add(path);
        this.pathUrlPair.put(path, path);
        notifyDataSetChanged();
    }

    /**
     * 添加多张照片
     */
    public void addPaths(ArrayList<String> list) {
        for (String str : list) {
            this.paths.add(str);
            this.pathUrlPair.put(str, str);
        }
        notifyDataSetChanged();

    }

    /**
     *上传视频
     */
    public void addVideoPath(String videoPath, String thumbnailPath) {
        this.paths.add(thumbnailPath);
        this.nativeVideo.put(thumbnailPath, videoPath);

        ResultVideo resultVideo = new ResultVideo();
        resultVideo.setThumbUrl(thumbnailPath);
        resultVideo.setVideoUrl(videoPath);
        this.pathUrlVideoPair.put(thumbnailPath, resultVideo);
        this.taskVideos.add(new TaskVideo(videoPath,thumbnailPath));

        MediaPlayer mediaPlayer = new MediaPlayer();
        try {
            mediaPlayer.setDataSource(videoPath);
            mediaPlayer.prepare();
            int duration = mediaPlayer.getDuration();
            if (0 != duration) {
                String totalTime = TimeUtils.getTime(duration);
                pathTimeMediaPair.put(videoPath, totalTime);
                //记得释放资源
                mediaPlayer.release();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        notifyItemInserted(paths.size() - 1);
    }

    /**
     *上传视频
     */
    public void addVideoPaths(ArrayList<ResultVideo> list) {
        for(ResultVideo resultVideo:list) {
            this.paths.add(resultVideo.getThumbUrl());
            this.nativeVideo.put(resultVideo.getThumbUrl(), resultVideo.getVideoUrl());

            this.pathUrlVideoPair.put(resultVideo.getThumbUrl(), resultVideo);
            this.taskVideos.add(new TaskVideo(resultVideo.getVideoUrl(),resultVideo.getThumbUrl()));
            notifyItemInserted(paths.size() - 1);
        }
    }

    /**
     * 上传音频
     * @param recordPath
     */
    public void addRecordPath(String recordPath){
        this.paths.add(recordPath);
        this.pathUrlRecordPair.put(recordPath, recordPath);
        MediaPlayer mediaPlayer = new MediaPlayer();
        try {
            mediaPlayer.setDataSource(recordPath);
            mediaPlayer.prepare();
            int duration = mediaPlayer.getDuration();
            if (0 != duration) {
                String totalTime = TimeUtils.getTime(duration);
                pathTimeMediaPair.put(recordPath, totalTime);
                //记得释放资源
                mediaPlayer.release();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        notifyItemInserted(paths.size() - 1);
    }


    /**
     * 当前步骤的结果图片，
     */
    public List<String> getCurrStepsPath(){
        return paths;
    }



    public void setItemClickListener(ItemClickListener itemClickListener) {
        this.itemClickListener = itemClickListener;
    }


    /**
     * 获得当前图片结果
     * @return
     */
    public List<String> getNewUrls() {
        ArrayList<String> newUrls = new ArrayList<>();
        ArrayList<String> listTemp = new ArrayList();
        listTemp.addAll(paths);
        for (int i = 0; i < listTemp.size(); i++) {
            boolean onOff = true;
            for (int j = 0; j < i; j++) {
                if (listTemp.get(i).equals(listTemp.get(j))) {//如果出现相同的,筛选
                    onOff = false;
                }
            }

            if (!listTemp.get(i).startsWith("http")) {
                File file = new File(listTemp.get(i));
                if (!file.exists()) {//本地文件不存在
                    onOff = false;
                    Toast.makeText(context, "一张本地照片保存失败或已丢失，请删除后重新拍摄", Toast.LENGTH_SHORT).show();
                }
            }

            if (onOff) {
                newUrls.add(listTemp.get(i));
            }
        }
        return newUrls;
    }


    public void clearUrls() {
        this.urls.clear();
        this.paths.clear();
        this.pathUrlPair.clear();
        this.nativeVideo.clear();
        this.pathUrlVideoPair.clear();
        this.pathUrlRecordPair.clear();
        pathTimeMediaPair.clear();
        notifyDataSetChanged();
    }



    /**
     * 获得当前步骤的视频结果
     * @return
     */
    public List<ResultVideo> getNewVideos() {
        List<ResultVideo> newVideoUrls = new ArrayList<>();
        Collection<ResultVideo> collection= pathUrlVideoPair.values();
        newVideoUrls.addAll(collection);
        return newVideoUrls;
    }

    /**
     * 获得当前步骤的视频结果
     * @return
     */
    public List<TaskVideo> getTaskVideos() {
        return taskVideos;
    }

    /**
     * 获得当前音频结果
     * @return
     */
    public List<String> getNewRecord() {
        List<String> newRecordUrls = new ArrayList<>();
        newRecordUrls.addAll(pathUrlRecordPair.values());
        return newRecordUrls;
    }




    public boolean hasUrls() {
        return (urls.size()) + (pathUrlPair.size())!= 0;
    }

    public boolean hasVideoUrls() {
        return (urls.size() + pathUrlVideoPair.values().size()) != 0;
    }

    public boolean hasRecordUrls() {
        return (urls.size() + pathUrlRecordPair.values().size()) != 0;
    }

    public void setDeletable(boolean canDelete) {
        this.canDelete = canDelete;
    }

    public void confirmDelete(int position, String url) {

        if (position < paths.size()) {
            String path = paths.get(position);
            pathUrlPair.remove(path);
            pathUrlVideoPair.remove(path);
            pathUrlRecordPair.remove(path);
            pathTimeMediaPair.remove(path);
            urls.remove(path);
            paths.remove(path);
        }
        CacheResultPhotosAdapter.this.notifyDataSetChanged();
        itemClickListener.onDelete(position, url);
    }



    static class ViewHolder extends RecyclerView.ViewHolder {

        @BindView(R2.id.image)
        ImageView imageView;
        @BindView(R2.id.delete)
        View delete;
        @BindView(R2.id.status)
        TextView tvStatus;
        @BindView(R2.id.progress_bar)
        View progressbar;
        @BindView(R2.id.videoicon)
        ImageView videoIcon;
        @BindView(R2.id.media_time)
        TextView mediaTime;
        String imageUrl;

        public ViewHolder(View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
            itemView.setTag(this);
        }
    }

    public interface ItemClickListener {
        public void onItemClick(String url);

        public void onDeleteClick(int pos, String url);

        public void onDelete(int pos, String url);
    }
}
