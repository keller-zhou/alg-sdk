package com.slicejobs.algsdk.algtasklibrary.ui.weex.weexcomponent;

import android.content.Context;

import com.slicejobs.algsdk.algtasklibrary.ui.widget.AudioPlayerView;
import com.taobao.weex.WXSDKInstance;
import com.taobao.weex.annotation.Component;
import com.taobao.weex.ui.action.BasicComponentData;
import com.taobao.weex.ui.component.WXComponent;
import com.taobao.weex.ui.component.WXComponentProp;
import com.taobao.weex.ui.component.WXVContainer;

import java.util.HashMap;
import java.util.Map;

@Component(lazyload = true)
public class AudioPlayerComponent extends WXComponent<AudioPlayerView> {
    WXSDKInstance mInstance;
    private AudioPlayerView audioPlayerView;

    public AudioPlayerComponent(WXSDKInstance instance, WXVContainer parent, BasicComponentData basicComponentData) {
        super(instance, parent, basicComponentData);
        mInstance = instance;
    }


    @Override
    protected AudioPlayerView initComponentHostView(Context context) {
        audioPlayerView = new AudioPlayerView(context);
        return audioPlayerView;
    }


    @WXComponentProp(name = "guideAudio")
    public void showAudioPlayer(Map<String, Object> params) {
        if (params.get("src") != null) {
            String audioUrl = params.get("src").toString();
            audioPlayerView.setMediaDataSource(audioUrl);
        }
    }

    @WXComponentProp(name = "pauseAudio")
    public void pauseAudioPlayer(boolean isPauseGuideAudio) {
        if (isPauseGuideAudio) {
            audioPlayerView.pausePlay();
            Map<String, Object> paramas = new HashMap<>();
            paramas.put("isPauseGuideAudio", false);
            mInstance.refreshInstance(paramas);
        }
    }

    @Override
    public void destroy() {
        super.destroy();
        audioPlayerView.exitPlay();
    }

    @WXComponentProp(name = "audioPlayEnble")
    public void setAudioPlayerEnable(boolean audioPlayEnable) {
        audioPlayerView.setAudioPlayEnable(audioPlayEnable);
    }
}
