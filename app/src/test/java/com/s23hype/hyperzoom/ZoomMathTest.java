package com.s23hype.hyperzoom;
import org.junit.Test;
import static org.junit.Assert.*;
public class ZoomMathTest {
 @Test public void limits(){ assertEquals(1,ZoomMath.limit(0));assertEquals(1000,ZoomMath.limit(5000));}
 @Test public void maximum(){assertEquals(1000f,ZoomMath.fromProgress(1000,1000),.001f);}
 @Test public void hardwareAndCrop(){assertEquals(30f,ZoomMath.hardware(1000,30),.001f);assertEquals(120,ZoomMath.crop(4000,ZoomMath.extra(1000,30)));}
 @Test public void noZeroPixels(){assertEquals(1,ZoomMath.crop(10,1000));}
}
