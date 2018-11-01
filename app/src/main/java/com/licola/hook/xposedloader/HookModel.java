package com.licola.hook.xposedloader;

/**
 * @author LiCola
 * @date 2018/11/1
 */
public class HookModel {

  private String packageName;
  private String loadClassName;
  private String loadMethodName;

  public HookModel(String packageName, String loadClassName, String loadMethodName) {
    this.packageName = packageName;
    this.loadClassName = loadClassName;
    this.loadMethodName = loadMethodName;
  }

  public String getPackageName() {
    return packageName;
  }

  public String getLoadClassName() {
    return loadClassName;
  }

  public String getLoadMethodName() {
    return loadMethodName;
  }

  @Override
  public String toString() {
    final StringBuilder sb = new StringBuilder("HookModel{");
    sb.append("packageName='").append(packageName).append('\'');
    sb.append(", loadClassName='").append(loadClassName).append('\'');
    sb.append(", loadMethodName='").append(loadMethodName).append('\'');
    sb.append('}');
    return sb.toString();
  }
}
