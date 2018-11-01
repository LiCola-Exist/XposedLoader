package com.licola.hook.xposedloader;

import android.app.Activity;
import android.view.View;
import android.view.View.OnClickListener;
import com.licola.llogger.LLogger;
import de.robv.android.xposed.IXposedHookLoadPackage;
import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XposedHelpers;
import de.robv.android.xposed.callbacks.XC_LoadPackage.LoadPackageParam;
import java.util.Map;

/**
 * @author LiCola
 * @date 2018/11/1
 */
public class AliPayHook implements IXposedHookLoadPackage {

  /**
   * 动态装载的包名,方便寻找apk文件
   */
  private final static String LOAD_PACKAGE = "com.licola.hook.xposedloader";

  /**
   * 动态装载的主类名，实际hook逻辑处理类
   */
  private final static String LOAD_CLASS = AliPayHook.class.getName();

  /**
   * 动态装载的主方法，实际hook逻辑处理类的入口方法
   */
  private final static String LOAD_METHOD = "handleLoadPackage";

  private static final String TARGET_PACKAGE = "com.eg.android.AlipayGphone";
  private static final String TARGET_PROCESS = "com.eg.android.AlipayGphone";

  public static void install(Map<String, HookModel> installHook) {
    installHook.put(TARGET_PACKAGE, new HookModel(LOAD_PACKAGE, LOAD_CLASS, LOAD_METHOD));
  }

  public static final String TAG = LOAD_CLASS;

  static {
    LLogger.init(true, TAG);
  }

  @Override
  public void handleLoadPackage(LoadPackageParam lpparam) throws Throwable {
    LLogger.d("调用invoke " + lpparam.packageName + " process:" + lpparam.processName + " thread:"
        + Thread.currentThread());

    if (!lpparam.processName.equals(TARGET_PROCESS)) {
      return;
    }

    XposedHelpers.findAndHookMethod("android.app.Activity", lpparam.classLoader, "onResume",
        new XC_MethodHook() {
          @Override
          protected void afterHookedMethod(MethodHookParam param) throws Throwable {
            Activity activity = (Activity) param.thisObject;
            LLogger.d("捕获到目标Activity的 onResume事件 :" + activity.toString());
          }
        });

//
    XposedHelpers
        .findAndHookMethod(View.class.getName(),
            lpparam.classLoader, "setOnClickListener", OnClickListener.class, new XC_MethodHook() {

              @Override
              protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                LLogger.d("Hook目标:", param.method, param.thisObject);
                LLogger.d(param.args);
              }
            });

  }
}
