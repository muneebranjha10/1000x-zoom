package com.s23hype.hyperzoom;
import android.Manifest;
import android.content.ContentValues;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.BitmapRegionDecoder;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Rect;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.*;
import android.widget.*;
import androidx.activity.ComponentActivity;
import androidx.camera.core.*;
import androidx.camera.lifecycle.ProcessCameraProvider;
import androidx.camera.view.PreviewView;
import androidx.exifinterface.media.ExifInterface;
import com.google.common.util.concurrent.ListenableFuture;
import java.io.*;
import java.util.concurrent.*;

public class PhotoActivity extends ComponentActivity {
 PreviewView preview; androidx.camera.core.Camera camera; ImageCapture capture; Button shutter; TextView label;
 SeekBar slider; float wanted=1,extra=1; int max, generation; boolean busy,ready; volatile boolean closing; boolean hardwarePending; float lastRequested=Float.NaN;
 final ExecutorService worker=Executors.newSingleThreadExecutor();
 final Executor main=command->runOnUiThread(command);
 int dp(float n){return (int)(n*getResources().getDisplayMetrics().density+.5f);}
 @Override public void onCreate(Bundle b){super.onCreate(b);max=ZoomMath.limit(getIntent().getIntExtra("max",1000));
   LinearLayout root=new LinearLayout(this);root.setOrientation(1);root.setGravity(Gravity.CENTER);root.setBackgroundColor(Color.BLACK);setContentView(root);
   FrameLayout frame=new FrameLayout(this);frame.setClipChildren(true);frame.setClipToPadding(true);
   root.addView(frame,new LinearLayout.LayoutParams(-1,0,1));
   preview=new PreviewView(this);preview.setImplementationMode(PreviewView.ImplementationMode.COMPATIBLE);preview.setScaleType(PreviewView.ScaleType.FILL_CENTER);frame.addView(preview,new FrameLayout.LayoutParams(-1,-1));
   label=new TextView(this);label.setTextColor(Color.WHITE);label.setTextSize(24);label.setGravity(Gravity.CENTER);label.setText("1.0×");root.addView(label);
   slider=new SeekBar(this);slider.setMax(1000);root.addView(slider,new LinearLayout.LayoutParams(-1,dp(56)));
   shutter=new Button(this);shutter.setText("Capture");shutter.setEnabled(false);root.addView(shutter,new LinearLayout.LayoutParams(-1,dp(64)));
   slider.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener(){public void onStartTrackingTouch(SeekBar s){} public void onStopTrackingTouch(SeekBar s){} public void onProgressChanged(SeekBar s,int p,boolean user){if(user){wanted=ZoomMath.fromProgress(p,max);applyZoom();}}});
   shutter.setOnClickListener(v->take());
   if(checkSelfPermission(Manifest.permission.CAMERA)==PackageManager.PERMISSION_GRANTED)preview.post(this::startCamera);else requestPermissions(new String[]{Manifest.permission.CAMERA},1);
 }
 @Override public void onRequestPermissionsResult(int r,String[] p,int[] g){super.onRequestPermissionsResult(r,p,g);if(r==1&&g.length>0&&g[0]==PackageManager.PERMISSION_GRANTED)preview.post(this::startCamera);else finish();}
 void startCamera(){
   ListenableFuture<ProcessCameraProvider> f=ProcessCameraProvider.getInstance(this);
   f.addListener(()->{if(closing)return;try{
     ProcessCameraProvider provider=f.get(); Preview p=new Preview.Builder().setTargetAspectRatio(AspectRatio.RATIO_4_3).build();
     capture=new ImageCapture.Builder().setTargetAspectRatio(AspectRatio.RATIO_4_3).setCaptureMode(ImageCapture.CAPTURE_MODE_MAXIMIZE_QUALITY).build();
     p.setSurfaceProvider(preview.getSurfaceProvider());provider.unbindAll();
     ViewPort vp=preview.getViewPort();
     if(vp==null)throw new IllegalStateException("Preview layout not ready");
     UseCaseGroup group=new UseCaseGroup.Builder().setViewPort(vp).addUseCase(p).addUseCase(capture).build();
     camera=provider.bindToLifecycle(this,CameraSelector.DEFAULT_BACK_CAMERA,group);applyZoom();
   }catch(Exception e){Toast.makeText(this,"Camera unavailable",Toast.LENGTH_LONG).show();}},main);
 }
 void applyZoom(){
   label.setText(String.format(java.util.Locale.US,"%.1f×",wanted)); if(camera==null||busy)return;
   ZoomState state=camera.getCameraInfo().getZoomState().getValue();if(state==null)return;
   int token=++generation;float request=wanted;float hw=ZoomMath.hardware(request,state.getMaxZoomRatio());
   ready=false;shutter.setEnabled(false);
   if(Math.abs(state.getZoomRatio()-hw)<0.00001f && lastRequested==hw && !hardwarePending){
     extra=ZoomMath.extra(request,state.getZoomRatio());preview.setScaleX(extra);preview.setScaleY(extra);ready=true;shutter.setEnabled(!busy);return;
   }
   hardwarePending=true;lastRequested=hw;
   ListenableFuture<Void> f=camera.getCameraControl().setZoomRatio(hw);
   f.addListener(()->{if(closing||token!=generation)return;hardwarePending=false;try{f.get();ZoomState current=camera.getCameraInfo().getZoomState().getValue();float actual=current==null?hw:current.getZoomRatio();extra=ZoomMath.extra(request,actual);
     preview.setScaleX(extra);preview.setScaleY(extra);ready=true;shutter.setEnabled(!busy);
   }catch(Exception e){ready=false;shutter.setEnabled(false);label.setText("Zoom unavailable");}},main);
 }
 void take(){
   if(capture==null||busy||!ready)return;busy=true;shutter.setEnabled(false);slider.setEnabled(false);final float factor=extra;
   File temp=new File(getCacheDir(),"capture-"+System.nanoTime()+".jpg");
   capture.takePicture(new ImageCapture.OutputFileOptions.Builder(temp).build(),worker,new ImageCapture.OnImageSavedCallback(){
     public void onImageSaved(ImageCapture.OutputFileResults result){
       try{save(temp,factor);main.execute(()->done("Saved"));}catch(Exception e){main.execute(()->done("Save failed"));}finally{temp.delete();}
     }
     public void onError(ImageCaptureException e){temp.delete();main.execute(()->done("Capture failed"));}
   });
 }
 void done(String message){if(closing)return;busy=false;slider.setEnabled(true);shutter.setEnabled(ready);Toast.makeText(this,message,Toast.LENGTH_SHORT).show();}
 void save(File input,float factor)throws IOException{
   ContentValues values=new ContentValues();values.put(MediaStore.Images.Media.DISPLAY_NAME,"HyperZoom_"+System.currentTimeMillis()+".jpg");values.put(MediaStore.Images.Media.MIME_TYPE,"image/jpeg");values.put(MediaStore.Images.Media.RELATIVE_PATH,"Pictures/HyperZoom");values.put(MediaStore.Images.Media.IS_PENDING,1);
   Uri uri=getContentResolver().insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI,values);if(uri==null)throw new IOException("MediaStore");
   Bitmap bitmap=null, rotated=null;BitmapRegionDecoder decoder=null;
   try{
     try(OutputStream out=getContentResolver().openOutputStream(uri)){if(out==null)throw new IOException("Output");
       if(factor<=1.0001f){try(InputStream in=new FileInputStream(input)){byte[] buf=new byte[65536];int n;while((n=in.read(buf))!=-1)out.write(buf,0,n);}}
       else{decoder=BitmapRegionDecoder.newInstance(input.getAbsolutePath(),false);int w=decoder.getWidth(),h=decoder.getHeight(),cw=ZoomMath.crop(w,factor),ch=ZoomMath.crop(h,factor);
         BitmapFactory.Options opts=new BitmapFactory.Options();opts.inPreferredConfig=Bitmap.Config.ARGB_8888;
         opts.inSampleSize=1;
         while((long)(cw/opts.inSampleSize)*(ch/opts.inSampleSize)>12000000L)opts.inSampleSize*=2;
         bitmap=decoder.decodeRegion(new Rect((w-cw)/2,(h-ch)/2,(w-cw)/2+cw,(h-ch)/2+ch),opts);
         if(bitmap==null)throw new IOException("Crop");
         ExifInterface exif=new ExifInterface(input.getAbsolutePath()); Matrix m=new Matrix();
         if(exif.isFlipped())m.postScale(-1f,1f);m.postRotate(exif.getRotationDegrees());
         rotated=Bitmap.createBitmap(bitmap,0,0,bitmap.getWidth(),bitmap.getHeight(),m,true);
         if(!rotated.compress(Bitmap.CompressFormat.JPEG,95,out))throw new IOException("JPEG");
       }
     }
     values.clear();values.put(MediaStore.Images.Media.IS_PENDING,0);getContentResolver().update(uri,values,null,null);
   }catch(Exception e){getContentResolver().delete(uri,null,null);throw new IOException(e);}finally{if(decoder!=null)decoder.recycle();if(rotated!=null&&rotated!=bitmap)rotated.recycle();if(bitmap!=null)bitmap.recycle();}
 }
 @Override protected void onDestroy(){closing=true;super.onDestroy();worker.shutdown();}
}
