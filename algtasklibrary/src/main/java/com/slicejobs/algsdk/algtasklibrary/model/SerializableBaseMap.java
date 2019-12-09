package com.slicejobs.algsdk.algtasklibrary.model;

import java.io.Serializable;
import java.util.Map;

/**
 * Created by keller.zhou on 17/2/10.
 */
public class SerializableBaseMap implements Serializable {

    private Map<String,Object> map;

    public Map<String,Object> getMap() {
        return map;
    }

    public void setMap(Map<String,Object> map) {
        this.map = map;
    }
}
