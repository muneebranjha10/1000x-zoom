package com.s23hype.hyperzoom;
import android.hardware.camera2.CaptureRequest;
import android.graphics.Rect;
import android.os.SystemClock;
import de.robv.android.xposed.*;
import de.robv.android.xposed.callbacks.XC_LoadPackage;
public final class ZoomHook implements IXposedHookLoadPackage {
 private long uiLast,requestLast;
 @Override public void handleLoadPackage(XC_LoadPackage.LoadPackageParam p){
   if(!"com.sec.android.app.camera".equals(p.packageName)||!p.packageName.equals(p.processName))return;
   try{XposedHelpers.findAndHookMethod("com.sec.android.app.camera.layer.keyscreen.zoom.ZoomView",p.classLoader,"setZoomValue",int.class,new XC_MethodHook(){
     @Override protected void afterHookedMethod(MethodHookParam param){long now=SystemClock.elapsedRealtime();if(now-uiLast>500){uiLast=now;XposedBridge.log("HyperZoom: ZoomView raw="+param.args[0]);}}
   });XposedBridge.log("HyperZoom: Samsung zoom diagnostic attached");}catch(Throwable t){XposedBridge.log("HyperZoom: ZoomView unavailable");}
   try{XposedHelpers.findAndHookMethod(CaptureRequest.Builder.class,"set",CaptureRequest.Key.class,Object.class,new XC_MethodHook(){
     @Override protected void afterHookedMethod(MethodHookParam param){Object key=param.args[0];if(!CaptureRequest.CONTROL_ZOOM_RATIO.equals(key)&&!CaptureRequest.SCALER_CROP_REGION.equals(key))return;
       long now=SystemClock.elapsedRealtime();if(now-requestLast>500){requestLast=now;XposedBridge.log("HyperZoom: "+((CaptureRequest.Key<?>)key).getName()+"="+param.args[1]);}}
   });}catch(Throwable t){XposedBridge.log("HyperZoom: Camera2 diagnostic unavailable");}
 }
}
