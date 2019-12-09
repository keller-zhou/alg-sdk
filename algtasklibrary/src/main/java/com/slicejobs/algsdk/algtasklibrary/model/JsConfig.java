package com.slicejobs.algsdk.algtasklibrary.model;

import java.util.Arrays;

public class JsConfig {
    private String version;
    private JsFileConfig[] list;
    private boolean useStytemCamera;
    private boolean useOldVideo;
    private String cameraKitBlacklist;

    public JsConfig() {
    }

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    public JsFileConfig[] getList() {
        return list;
    }

    public void setList(JsFileConfig[] list) {
        this.list = list;
    }

    public boolean isUseStytemCamera() {
        return useStytemCamera;
    }

    public void setUseStytemCamera(boolean useStytemCamera) {
        this.useStytemCamera = useStytemCamera;
    }

    public boolean isUseOldVideo() {
        return useOldVideo;
    }

    public void setUseOldVideo(boolean useOldVideo) {
        this.useOldVideo = useOldVideo;
    }

    public String getCameraKitBlacklist() {
        return cameraKitBlacklist;
    }

    public void setCameraKitBlacklist(String cameraKitBlacklist) {
        this.cameraKitBlacklist = cameraKitBlacklist;
    }

    @Override
    public String toString() {
        return "JsConfig{" +
                "version='" + version + '\'' +
                ", list=" + Arrays.toString(list) +
                ", useStytemCamera=" + useStytemCamera +
                ", useOldVideo=" + useOldVideo +
                ", cameraKitBlacklist='" + cameraKitBlacklist + '\'' +
                '}';
    }

    public JsConfig(String version, JsFileConfig[] list, boolean useStytemCamera, boolean useOldVideo, String cameraKitBlacklist) {
        this.version = version;
        this.list = list;
        this.useStytemCamera = useStytemCamera;
        this.useOldVideo = useOldVideo;
        this.cameraKitBlacklist = cameraKitBlacklist;
    }
}
