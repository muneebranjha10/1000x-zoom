package com.s23hype.hyperzoom;
final class ZoomMath {
    static int limit(int n) { return Math.max(1, Math.min(1000,n)); }
    static float fromProgress(int progress,int max) {
        return (float)Math.pow(limit(max), Math.max(0,Math.min(1000,progress))/1000.0);
    }
    static float hardware(float wanted,float max) { return Math.max(1f,Math.min(wanted,max)); }
    static float extra(float wanted,float actual) { return Math.max(1f,wanted/Math.max(1f,actual)); }
    static int crop(int size,float factor) { return Math.max(1,Math.min(size,Math.round(size/Math.max(1f,factor)))); }
}
