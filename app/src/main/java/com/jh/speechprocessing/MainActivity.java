package com.jh.speechprocessing;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.easypermission.EasyPermission;
import com.easypermission.GrantResult;
import com.easypermission.NextAction;
import com.easypermission.Permission;
import com.easypermission.PermissionRequestListener;
import com.easypermission.RequestPermissionRationalListener;

import java.util.Map;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

/**
 * 语音处理
 *
 *  targetSdkVersion 28
 *  23之后录音引入了动态权限，所以需要配置下
 *
 *  一个非常轻便而且可用的Android动态权限申请库
 *  https://github.com/panyiho/EasyPermission
 *
 *
 *  出现异常: Error: Program type already present: org.hamcrest.BaseDescription
 *  发现存在一个1.1与1.3版本的重复库删除一个即可，不起作用，注释ExecutorServiceTest类与引入的库即可
 */
public class MainActivity extends AppCompatActivity {

    @BindView(R.id.bt_file)
    Button btFile;
    @BindView(R.id.bt_stream)
    Button btStream;
    @BindView(R.id.bt_visualizer)
    Button btVisualizer;
    @BindView(R.id.bt_effect)
    Button btEffect;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);

        // 引入对应权限
        EasyPermission.with(this)
//                .addPermissions(Permission.Group.LOCATION)      //申请定位权限组
//                .addPermissions(Permission.CALL_PHONE)          //申请打电话权限
                .addPermission(Permission.RECORD_AUDIO)      // 盛情录音权限
//                .addRequestPermissionRationaleHandler(Permission.ACCESS_FINE_LOCATION, new RequestPermissionRationalListener() {
//                    @Override
//                    public void onRequestPermissionRational(String permission, boolean requestPermissionRationaleResult, final NextAction nextAction) {
//                        //这里处理具体逻辑，如弹窗提示用户等,但是在处理完自定义逻辑后必须调用nextAction的next方法
//                    }
//                })
//                .addRequestPermissionRationaleHandler(Permission.CALL_PHONE, new RequestPermissionRationalListener() {
//                    @Override
//                    public void onRequestPermissionRational(String permission, boolean requestPermissionRationaleResult, final NextAction nextAction) {
//                        //这里处理具体逻辑，如弹窗提示用户等,但是在处理完自定义逻辑后必须调用nextAction的next方法
//                    }
//                })
                .addRequestPermissionRationaleHandler(Permission.RECORD_AUDIO, new RequestPermissionRationalListener() {
                    @Override
                    public void onRequestPermissionRational(String permission, boolean requestPermissionRationaleResult, NextAction nextAction) {
                        //这里处理具体逻辑，如弹窗提示用户等,但是在处理完自定义逻辑后必须调用nextAction的next方法
//                        Toast.makeText(MainActivity.this, "1", Toast.LENGTH_SHORT).show();
                    }
                })
                .request(new PermissionRequestListener() {
                    @Override
                    public void onGrant(Map<String, GrantResult> result) {
                        //权限申请返回
//                        Toast.makeText(MainActivity.this, "2", Toast.LENGTH_SHORT).show();
                    }

                    @Override
                    public void onCancel(String stopPermission) {
                        //在addRequestPermissionRationaleHandler的处理函数里面调用了NextAction.next(NextActionType.STOP,就会中断申请过程，直接回调到这里来
//                        Toast.makeText(MainActivity.this, "3", Toast.LENGTH_SHORT).show();
                    }
                });

    }

    @OnClick({R.id.bt_file, R.id.bt_stream, R.id.bt_visualizer, R.id.bt_effect})
    public void onViewClicked(View view) {
        switch (view.getId()) {
            case R.id.bt_file:
                startActivity(new Intent(this, FileActivity.class));
                break;
            case R.id.bt_stream:
                startActivity(new Intent(this, StreamActivity.class));
                break;
            case R.id.bt_visualizer:
                startActivity(new Intent(this, VisualizerActivity.class));
                break;
            case R.id.bt_effect:
                startActivity(new Intent(this, EffectActivity.class));
                break;
        }
    }
}
