package org.wonday.aliyun.push;

import android.content.Context;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

public class SystemProperty {
  private final Context mContext;

  public SystemProperty(Context mContext) {
    this.mContext = mContext;
  }

  public String getOrThrow(String key) throws Exception {
    try {
      ClassLoader classLoader = mContext.getClassLoader();
      Class SystemProperties = classLoader.loadClass("android.os.SystemProperties");
      Method methodGet = SystemProperties.getMethod("get", String.class);
      return (String) methodGet.invoke(SystemProperties, key);
    } catch (Exception e) {
      throw new Exception(e);
    }
  }

  public String get(String key) {
    try {
      return getOrThrow(key);
    } catch (Exception e) {
      return null;
    }
  }

}
