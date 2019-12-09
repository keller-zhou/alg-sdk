package com.slicejobs.algsdk.algtasklibrary.utils;

import android.content.Context;
import android.content.SharedPreferences;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.slicejobs.algsdk.algtasklibrary.net.ISODateAdapter;

import java.util.Date;
import java.util.HashSet;
import java.util.Set;

/**
 * Created by jgzhu on 5/7/14.
 */
public class PrefUtil {
    public static final String PREFERENCE_NAME = "preference";
    static private String KEY_ROBOT_WELCOME_MSG = "robot_welcome_msg";

    private SharedPreferences mPreferences;
    private SharedPreferences.Editor mEditor;
    private Gson mGson;
    private Context mContext;

    private PrefUtil(Context context, String name) {
        mPreferences = context.getSharedPreferences(PrefUtil.PREFERENCE_NAME, Context.MODE_PRIVATE);
        mEditor = mPreferences.edit();
        mGson = new GsonBuilder().registerTypeAdapter(Date.class, new ISODateAdapter()).create();
        mContext = context;
    }

    public static PrefUtil make(Context context, String name) {
        return new PrefUtil(context, name);
    }

    public String getString(String key, String defaultValue) {
        return mPreferences.getString(key, defaultValue);
    }

    public String getString(String key) {
        return mPreferences.getString(key, "");
    }

    public PrefUtil putString(String key, String value) {
        mEditor.putString(key, value).commit();
        return this;
    }

    public boolean putSaveToken(String key, String value) {
        return mEditor.putString(key, value).commit();
    }

    public PrefUtil putBoolean(String key, Boolean value) {
        mEditor.putBoolean(key, value).commit();
        return this;
    }

    public int getInt(String key, int defaultValue) {
        return mPreferences.getInt(key, defaultValue);
    }

    public PrefUtil putInt(String key, int i) {
        mEditor.putInt(key, i).commit();
        return this;
    }

    public long getLong(String key, long defaultValue) {
        return mPreferences.getLong(key, defaultValue);
    }

    public PrefUtil putLong(String key, long i) {
        mEditor.putLong(key, i).commit();
        return this;
    }

    public Boolean getBoolean(String key, Boolean defaultValue) {
        return mPreferences.getBoolean(key, defaultValue);
    }

    public Boolean getBoolean(String key) {
        return mPreferences.getBoolean(key, false);
    }

    public PrefUtil putStringSet(String key, Set<String> set) {
        mEditor.putStringSet(key, set).commit();
        return this;
    }

    public Set<String> getStringSet(String key) {
        return mPreferences.getStringSet(key, new HashSet<>());
    }

    public PrefUtil putObject(String key, Object object) {
        String json = mGson.toJson(object);
        return putString(key, json);
    }

    public Object getObject(String key, Class<?> clazz) {
        Object object = null;
        if (StringUtil.isNotBlank(getString(key))) {
            object = mGson.fromJson(getString(key), clazz);
        }
        return object;
    }

    public void removeObj(String key) {
        mEditor.remove(key).commit();
    }

    public void clear() {
        mEditor.clear();
        mEditor.commit();
    }
    public void saveRobot(String txt){
        mEditor.putString(KEY_ROBOT_WELCOME_MSG, txt);
        mEditor.commit();
    }

    public String getRobot(){
        return mPreferences.getString(KEY_ROBOT_WELCOME_MSG, "");
    }
}
