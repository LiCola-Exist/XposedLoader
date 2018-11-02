package com.licola.hook.xposedloader;

import android.app.Activity;
import android.os.Bundle;
import com.licola.llogger.LLogger;
import de.robv.android.xposed.IXposedHookLoadPackage;
import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XposedHelpers;
import de.robv.android.xposed.callbacks.XC_LoadPackage.LoadPackageParam;
import java.lang.reflect.Method;
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
  public void handleLoadPackage(final LoadPackageParam lpparam) throws Throwable {
//    LLogger.d("调用invoke " + lpparam.packageName + " process:" + lpparam.processName + " thread:" + Thread.currentThread());

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
    XposedHelpers.findAndHookMethod("android.app.Activity", lpparam.classLoader, "onCreate",
        Bundle.class,
        new XC_MethodHook() {
          @Override
          protected void afterHookedMethod(MethodHookParam param) throws Throwable {
            Activity activity = (Activity) param.thisObject;
            LLogger.d("捕获到目标Activity的 onCreate事件 :" + activity.toString());
            LLogger.d(param.args);
          }
        });
    XposedHelpers.findAndHookMethod(ClassLoader.class.getName(), lpparam.classLoader, "loadClass",
        String.class,
        new XC_MethodHook() {
          @Override
          protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
            String className = (String) param.args[0];
            LLogger.d(className);
            if (className.contains("XposedBridge")) {
              LLogger.trace("有类尝试加载xopsed");
            }

            if (className.contains("ScanAttack")) {
              LLogger.trace("ScanAttack");
            }
          }

          @Override
          protected void afterHookedMethod(MethodHookParam param) throws Throwable {
            String className = (String) param.args[0];
            LLogger.d(className);
            if (className.contains("XposedBridge")) {
            }

            if (className.contains("ScanAttack")) {
            }
          }
        });

    //目前大概能确定 ScanAttack类负责相关Hook检测
//    final Class<?> aClass = XposedHelpers
//        .findClass("com.alipay.apmobilesecuritysdk.scanattack.common.ScanAttack",
//            lpparam.classLoader);
//    LLogger.d("findClass:" + aClass);
//
//    XposedHelpers.findAndHookMethod(
//        "com.alipay.apmobilesecuritysdk.scanattack.common.ScanAttack",
//        lpparam.classLoader,
//        "xpInstalled",
//        Context.class,
//        new XC_MethodHook() {
//
//          @Override
//          protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
//            LLogger.trace("xpInstalled");
//          }
//
//          @Override
//          protected void afterHookedMethod(MethodHookParam param) throws Throwable {
//            LLogger.d("xpInstalled");
//            param.setResult(false);
//          }
//        });

    //尝试找到阿里相关Aty 会触发XposedHook检测机制
    Method method = XposedHelpers.findMethodBestMatch(
        XposedHelpers.findClass("com.eg.android.AlipayGphone.AlipayLogin", lpparam.classLoader),
        "onCreate", Bundle.class);
    LLogger.d(method);

//    XposedHelpers.findAndHookMethod("com.eg.android.AlipayGphone.AlipayLogin", lpparam.classLoader,
//        "onResume", new XC_MethodHook() {
//          @Override
//          protected void afterHookedMethod(MethodHookParam param) throws Throwable {
//            LLogger.d("onResume", param.method, param.thisObject);
//          }
//        });

//    XposedHelpers
//        .findAndHookMethod(View.class.getName(),
//            lpparam.classLoader, "setOnClickListener", OnClickListener.class, new XC_MethodHook() {
//
//              @Override
//              protected void afterHookedMethod(MethodHookParam param) throws Throwable {
//                LLogger.d("OnClickListener", param.method, param.thisObject);
//                LLogger.d("OnClickListener", param.args);
//              }
//            });

  }
}
