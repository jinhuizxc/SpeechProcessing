package com.jh.speechprocessing;

/**
 * Email: 1004260403@qq.com
 * Created by jinhui on 2019/1/10.
 *
 * https://github.com/tkzic/audiograph/blob/master/smbPitchShift.m
 */
public class AudioProcessor {

    private static native void process(float ratio, byte[] in, byte[] out,
                                       int size, float[] floatInput, float[] floatOutput);
}
