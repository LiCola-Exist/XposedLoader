package com.licola.hook.xposedloader;

import android.app.Application;
import android.content.Context;
import android.content.pm.PackageManager;
import android.util.Log;
import com.licola.llogger.LLogger;
import dalvik.system.PathClassLoader;
import de.robv.android.xposed.IXposedHookLoadPackage;
import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XposedHelpers;
import de.robv.android.xposed.callbacks.XC_LoadPackage;
import de.robv.android.xposed.callbacks.XC_LoadPackage.LoadPackageParam;
import java.io.File;
import java.io.FileNotFoundException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.HashMap;

/**
 * Created by LiCola on 2018/03/29. 这种方案建议只在开发调试的时候使用，因为这将损耗一些性能(需要额外加载apk文件)，调试没问题后，直接修改xposed_init文件为正确的类即可
 * 可以实现免重启，由于存在缓存，需要杀死宿主程序以后才能生效 这种免重启的方式针对某些特殊情况的hook无效 例如我们需要implements
 * IXposedHookZygoteInit,并将自己的一个服务注册为系统服务，这种就必须重启了
 *
 * 特殊注意：因为涉及到apk的加载，不能通过开启Instant-Run功能编译项目，因为会导致项目编译后apk的拆分（壳子base-apk，模块apk-1等），
 */
public class HookLoader implements IXposedHookLoadPackage {

  private static final String TAG = "XposedLoader";

  private static final HashMap<String, HookModel> INSTALL_HOOK = new HashMap<>();

  static {
    AliPayHook.install(INSTALL_HOOK);
  }

  @Override
  public void handleLoadPackage(final LoadPackageParam loadPackageParam) throws Throwable {
    String keyLoadPackage = loadPackageParam.packageName;
    final HookModel hookModel = INSTALL_HOOK.get(keyLoadPackage);
    if (hookModel != null) {
      Log.i(TAG, "Hook目标进程的Application启动过程，开始安装模块，并开始实际的Hook操作");
      XposedHelpers.findAndHookMethod(Application.class, "attach", Context.class,
          new XC_MethodHook() {
            @Override
            protected void afterHookedMethod(MethodHookParam param) throws Throwable {
              Context context = (Context) param.args[0];
              loadPackageParam.classLoader = context.getClassLoader();
              invokeHandleHookMethod(context,
                  hookModel.getPackageName(),
                  hookModel.getLoadClassName(),
                  hookModel.getLoadMethodName(),
                  loadPackageParam);
            }
          });
    }
  }

  /**
   * 安装app以后，系统会在/data/app/下备份了一份.apk文件，通过动态加载这个apk文件，调用相应的方法 这样就可以实现，只需要第一次重启，以后修改hook代码就不用重启了
   *
   * @param context context参数
   * @param modulePackageName 当前模块的packageName
   * @param handleHookClass 指定由哪一个类处理相关的hook逻辑
   * @param loadPackageParam 传入XC_LoadPackage.LoadPackageParam参数
   * @throws Throwable 抛出各种异常,包括具体hook逻辑的异常,寻找apk文件异常,反射加载Class异常等
   */
  private void invokeHandleHookMethod(Context context, String modulePackageName,
      String handleHookClass, String handleHookMethod,
      XC_LoadPackage.LoadPackageParam loadPackageParam)
      throws FileNotFoundException, ClassNotFoundException, IllegalAccessException, InstantiationException, NoSuchMethodException, InvocationTargetException {
    File apkFile = findApkFile(context, modulePackageName);
    if (apkFile == null) {
      throw new FileNotFoundException("寻找模块apk文件失败");
    }

    Log.i(TAG, "找到Hook模块 apk:"+apkFile.getAbsolutePath()+" 反射进入Hook模块");

    //加载指定的hook具体处理类
    PathClassLoader pathClassLoader = new PathClassLoader(apkFile.getAbsolutePath(),
        ClassLoader.getSystemClassLoader());
    Class<?> classHook = Class.forName(handleHookClass, true, pathClassLoader);
    //调用具体hook类的处理入口方法
    Object instance = classHook.newInstance();
    Method method = classHook.getDeclaredMethod(handleHookMethod, LoadPackageParam.class);
    method.invoke(instance, loadPackageParam);//把实际的hook参数传入


  }

  /**
   * 根据包名构建目标Context,并调用getPackageCodePath()来定位apk
   *
   * @param context context参数
   * @param modulePackageName 当前模块包名
   * @return return apk file
   */
  private File findApkFile(Context context, String modulePackageName) {
    if (context == null) {
      return null;
    }
    try {
      Context moduleContext = context.createPackageContext(modulePackageName,
          Context.CONTEXT_INCLUDE_CODE | Context.CONTEXT_IGNORE_SECURITY);
      String apkPath = moduleContext.getPackageCodePath();
      return new File(apkPath);
    } catch (PackageManager.NameNotFoundException e) {
      e.printStackTrace();
    }
    return null;
  }
}
