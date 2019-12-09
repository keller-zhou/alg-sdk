package com.slicejobs.algsdk.algtasklibrary.model;


import java.io.Serializable;
import java.util.List;
import java.util.Map;

/**
 * Created by nlmartian on 11/16/15.
 */
public class TaskStep implements Serializable {
    public static final String TYPE_INFO = "info";
    public static final String TYPE_TEXT = "text";//文本类型
    public static final String TYPE_OPTIONS = "options";//选择题单选
    public static final String TYPE_OPTIONS_MULTIPLE = "moptions";//多选题
    public static final String TYPE_MTEXT = "mtext";//多项文字题

    public static final String INPUT_TYPE_TEXT = "text";
    public static final String INPUT_TYPE_PRICE = "numberInt";//数字numberInt
    public static final String INPUT_TYPE_NUMBER_FLOAT = "numberFloat";//浮点型

    public static final String EVIDENCETYPE_VIDEO ="video";
    public static final String EVIDENCETYPE_PHPTO = "photo";
    public static final String EVIDENCETYPE_RECORD = "record";

    private String stepId;
    private String title;
    private String desc;
    private List<String> images; // 描述图片
    private String type;
    private boolean needPhoto;
    private boolean forceCamera;
    private List<String> examplePhotos; // 拍照示例
    private String exampleText;
    private List<Option> options;//单选
    private List<MOption> moptions;//多选
    private String evidenceType = EVIDENCETYPE_PHPTO; //photo   video    record
    private String delay;//延迟/s
    private String evidenceQuality = EvidenceRequest.PHOTO_QUALITY_MIDDLE;//图片质量 1低 2 中  3高
    private Map<String, String> conditional_jump;//选择题，选项跳转,"选项Id":"分布Id"
    private String default_jump;//默认跳转
    private List<TabTask> mtext;//多个填空脦p
    private String inputType;//处理文字体类型
    private boolean allowReusePhoto;
    private int takePhotoAuxiliaryLine;

    public int getTakePhotoAuxiliaryLine() {
        return takePhotoAuxiliaryLine;
    }

    public void setTakePhotoAuxiliaryLine(int takePhotoAuxiliaryLine) {
        this.takePhotoAuxiliaryLine = takePhotoAuxiliaryLine;
    }

    public boolean isAllowReusePhoto() {
        return allowReusePhoto;
    }

    public void setAllowReusePhoto(boolean allowReusePhoto) {
        this.allowReusePhoto = allowReusePhoto;
    }

    public String getStepId() {
        return stepId;
    }

    public void setStepId(String stepId) {
        this.stepId = stepId;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDesc() {
        return desc;
    }

    public void setDesc(String desc) {
        this.desc = desc;
    }

    public List<String> getImages() {
        return images;
    }

    public void setImages(List<String> images) {
        this.images = images;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public boolean isNeedPhoto() {
        return needPhoto;
    }

    public void setNeedPhoto(boolean needPhoto) {
        this.needPhoto = needPhoto;
    }

    public List<String> getExamplePhotos() {
        return examplePhotos;
    }

    public void setExamplePhotos(List<String> examplePhotos) {
        this.examplePhotos = examplePhotos;
    }

    public String getExampleText() {
        return exampleText;
    }

    public void setExampleText(String exampleText) {
        this.exampleText = exampleText;
    }

    public List<Option> getOptions() {
        return options;
    }

    public void setOptions(List<Option> options) {
        this.options = options;
    }

    public boolean isForceCamera() {
        return forceCamera;
    }

    public void setForceCamera(boolean forceCamera) {
        this.forceCamera = forceCamera;
    }

    public String getEvidenceType() {
        return evidenceType;
    }

    public void setEvidenceType(String evidenceType) {
        this.evidenceType = evidenceType;
    }

    public String getDelay() {
        return delay;
    }

    public void setDelay(String delay) {
        this.delay = delay;
    }

    public List<MOption> getMoptions() {
        return moptions;
    }

    public void setMoptions(List<MOption> moptions) {
        this.moptions = moptions;
    }

    public String getEvidenceQuality() {
        return evidenceQuality;
    }

    public void setEvidenceQuality(String evidenceQuality) {
        this.evidenceQuality = evidenceQuality;
    }

    public String getDefault_jump() {
        return default_jump;
    }

    public void setDefault_jump(String default_jump) {
        this.default_jump = default_jump;
    }

    public Map<String, String> getConditional_jump() {
        return conditional_jump;
    }

    public void setConditional_jump(Map<String, String> conditional_jump) {
        this.conditional_jump = conditional_jump;
    }

    public List<TabTask> getMtext() {
        return mtext;
    }

    public void setMtext(List<TabTask> mtext) {
        this.mtext = mtext;
    }

    public String getInputType() {
        return inputType;
    }

    public void setInputType(String inputType) {
        this.inputType = inputType;
    }

}
