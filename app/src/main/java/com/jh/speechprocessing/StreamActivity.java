package com.jh.speechprocessing;

import android.media.AudioFormat;
import android.media.AudioManager;
import android.media.AudioRecord;
import android.media.AudioTrack;
import android.media.MediaPlayer;
import android.media.MediaRecorder;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Looper;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

/**
 * Email: 1004260403@qq.com
 * Created by jinhui on 2019/1/9.
 */
public class StreamActivity extends AppCompatActivity {

    @BindView(R.id.bt_start)
    Button btStart;
    @BindView(R.id.tv_result)
    TextView tvResult;
    @BindView(R.id.bt_play)
    Button btPlay;

    // 录音状态
    // volatile ，可以保证多线程对它的访问，使看到访问的使一样的数据，避免多线程的问题
    // 保证多线程的内存同步，避免出问题
    private volatile boolean isRecording;

    private ExecutorService executorService;
    private Handler mainThreadHandler;
    private File audioFile;
    private FileOutputStream fileOutputStream;
    private long startRecordTime, stopRecordTime;

    private AudioRecord audioRecord;

    // buffer不能太大， 避免OOM
    private static final int BUFFER_SIZE = 2048;
    private byte[] buffer;

    // 播放逻辑
    // 主线程和后台播放线程数据同步
    private volatile boolean isPlaying;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_stream);
        ButterKnife.bind(this);

        // 录音JNI函数不具备线程安全性，所以要用单线程
        executorService = Executors.newSingleThreadExecutor();
        mainThreadHandler = new Handler(Looper.getMainLooper());

        buffer = new byte[BUFFER_SIZE];

    }

    @OnClick({R.id.bt_start, R.id.bt_play})
    public void onViewClicked(View view) {
        switch (view.getId()) {
            case R.id.bt_start:
                // 根据当前状态， 改变UI， 执行开始/停止录音的逻辑
                if (isRecording) {
                    // 改变UI状态
                    btStart.setText(R.string.start);
                    // 改变录音状态
                    isRecording = false;
//            // 提交后台任务，执行停止录音逻辑
//            executorService.submit(new Runnable() {
//                @Override
//                public void run() {
//                    // 停止录音， 失败提示用户
//                }
//            });
                } else {
                    btStart.setText(R.string.stop);
                    // 改变录音状态
                    isRecording = true;
                    // 提交后台任务，执行录音逻辑
                    executorService.submit(new Runnable() {
                        @Override
                        public void run() {
                            // 开始录音， 失败提示用户
                            if (!startRecord()) {
                                recordFail();
                            }
                        }
                    });
                }
                break;
            case R.id.bt_play:
                // 检查当前状态，防止重复播放
                if (audioFile != null && !isPlaying) {
                    // 设置当前播放状态
                    isPlaying = true;
                    // 提交后台任务，开始播放
                    executorService.submit(new Runnable() {
                        @Override
                        public void run() {
                            doPlay(audioFile);
                        }
                    });
                }
                break;
        }

    }

    /**
     * 录音错误处理
     */
    private void recordFail() {
        mainThreadHandler.post(new Runnable() {
            @Override
            public void run() {
                Toast.makeText(StreamActivity.this, "录音失败", Toast.LENGTH_SHORT).show();

                // 重置录音状态， 以及UI状态
                isRecording = false;
                btStart.setText(R.string.start);
            }
        });
    }

    /**
     * 启动录音逻辑
     */
    private boolean startRecord() {
        try {
            // 创建录音文件
            audioFile = new File(Environment.getExternalStorageDirectory().getAbsolutePath()
                    + "/SpeechProcessDemo/"
                    + System.currentTimeMillis()
                    + ".pcm");
            Log.e("录音文件位置 = ", "doStart: " + audioFile);
            audioFile.getParentFile().mkdirs();  // 创建所有的父目录
            audioFile.createNewFile();

            // 创建文件输出流
            fileOutputStream = new FileOutputStream(audioFile);
            // 配置AudioRecord
            // 从麦克风采集
            int audioSource = MediaRecorder.AudioSource.MIC;
            int sampleRate = 44100; // 所有安卓系统都支持的频率；
            // 单声道输入
            int channelConfig = AudioFormat.CHANNEL_IN_MONO;
            // PCM 16是所有安卓系统都支持
            int audioFormat = AudioFormat.ENCODING_PCM_16BIT;
            // 计算AudioRecord 内部 buffer最小的大小
            int minBufferSize = AudioRecord.getMinBufferSize(sampleRate, channelConfig, audioFormat);

            // buffer 不能小于最低要求，也不能小于我们每次读取的大小
            audioRecord = new AudioRecord(
                    audioSource,
                    sampleRate,
                    channelConfig,
                    audioFormat,
                    Math.max(minBufferSize, BUFFER_SIZE));
            // 开始录音
            audioRecord.startRecording();
            // 记录录音开始时间，用于统计时长
            startRecordTime = System.currentTimeMillis();
            // 循环夺取数据， 写到输出流中
            while (isRecording) {
                // 只要还在录音状态，就一直读取数据
                int read = audioRecord.read(buffer, 0, BUFFER_SIZE);
                if (read > 0) {
                    // 读取成功写道文件中
                    fileOutputStream.write(buffer, 0, read);
                } else {
                    // 读取失败， 返回false， 提示用户
                    return false;
                }
            }
            // 退出循环，停止录音，释放资源
            return stopRecord();
        } catch (IOException | RuntimeException e) {
            e.printStackTrace();
            // 捕获异常，避免闪退返回false,提醒用户失败
            return false;
        } finally {
            // 释放资源
            if (audioRecord != null) {
                audioRecord.release();
            }
        }

    }

    /**
     * 停止录音逻辑
     *
     * @return
     */
    private boolean stopRecord() {
        try {
            // 停止录音， 关闭文件输出流
            audioRecord.stop();
            audioRecord.release();
            audioRecord = null;
            fileOutputStream.close();

            // 记录结束时间，统计时长
            stopRecordTime = System.currentTimeMillis();
            // 大于3秒， 在主线程UI显示
            final int second = (int) ((stopRecordTime - startRecordTime) / 1000);
            if (second > 0) {
                // 在主线程改UI 显示出来
                mainThreadHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        tvResult.setText(tvResult.getText() + "\n录音成功 " + second + "秒");
                    }
                });
            }
        } catch (IOException e) {
            e.printStackTrace();
            // 捕获异常，避免闪退返回false,提醒用户失败
            return false;
        }
        return true;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        // activity 销毁时，停止后台任务， 避免内存泄漏
        executorService.shutdownNow();
    }


    /**
     * 实际播放逻辑
     *
     * @param audioFile
     */
    private void doPlay(File audioFile) {

        // 配置播放器
        // 音乐类型, 扬声器播放
        int streamType = AudioManager.STREAM_MUSIC;
        // 录音时采用的采样频率，所以播放时使用同样的采样频率
        int sampleRate = 44100;
        // MONO 表示单声道，录音输入单声道, 播放用输出单声道
        int channelConfig = AudioFormat.CHANNEL_OUT_MONO;
        // 录音时使用16 bit，所以播放时使用同样的格式
        int audioFormat = AudioFormat.ENCODING_PCM_16BIT;
        // 流模式
        int mode = AudioTrack.MODE_STREAM;
        // 计算最小buffer大小
        int minBufferSize = AudioTrack.getMinBufferSize(sampleRate, channelConfig, audioFormat);
        // 构造AudioTrack
        AudioTrack audioTrack = new AudioTrack(streamType, sampleRate,
                channelConfig, audioFormat,
                // 不能小于AudioTrack的最低要求， 也不能小于我们每次读的大小
                Math.max(minBufferSize, BUFFER_SIZE),
                mode);

        audioTrack.play(); // 播放声音，课程没有这个句，需要加上
        // 从文件流读数据
        FileInputStream inputStream = null;
        try {
            inputStream = new FileInputStream(audioFile);
            // 循环读数据，写到播放器去播放
            int read;
            // 只要没读完，循环写播放
            while ((read = inputStream.read(buffer)) > 0) {
                int ret = audioTrack.write(buffer, 0, read);
                // 检查write返回值， 错误处理
                switch (ret) {
                    case AudioTrack.ERROR_INVALID_OPERATION:
                    case AudioTrack.ERROR_BAD_VALUE:
                    case AudioManager.ERROR_DEAD_OBJECT:
                        playFail();
                        return;
                    default:
                        break;
                }
            }
        } catch (IOException | RuntimeException e) {
            e.printStackTrace();
            // 错误处理， 防止闪退
            playFail();
        } finally {
            isPlaying = false;
            // 关闭文件输入流
            if (inputStream != null) {
                closeQuietly(inputStream);
            }
            // 释放播放器
            resetQuietly(audioTrack);
        }


    }

    private void resetQuietly(AudioTrack audioTrack) {
        try {
            audioTrack.stop();
            audioTrack.release();
        } catch (RuntimeException e) {
            e.printStackTrace();
        }

    }

    /**
     * 播放错误处理
     */
    private void playFail() {
        audioFile = null;

        // 给用户toast提示
        mainThreadHandler.post(new Runnable() {
            @Override
            public void run() {
                Toast.makeText(StreamActivity.this, "播放失败", Toast.LENGTH_SHORT).show();
            }
        });
    }

    /**
     * 静默关闭输入流
     *
     * @param inputStream
     */
    private void closeQuietly(FileInputStream inputStream) {
        try {
            inputStream.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
