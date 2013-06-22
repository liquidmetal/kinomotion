package in.liquidmetal.kinomotion;

import in.liquidmetal.kinomotion.util.SystemUiHider;

import android.annotation.TargetApi;
import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.ImageFormat;
import android.graphics.Rect;
import android.graphics.YuvImage;
import android.hardware.Camera;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.widget.LinearLayout;

import java.io.ByteArrayOutputStream;

/**
 * An example full-screen activity that shows and hides the system UI (i.e.
 * status bar and navigation/system bar) with user interaction.
 *
 * @see SystemUiHider
 */
public class CameraActivity extends Activity {
    private VideoCapturer mVideoCapturer;
    public CameraActivity() {
        super();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);



        mVideoCapturer = new VideoCapturer(this);

        setContentView(mVideoCapturer);
    }

    public void moveToEditor(byte[][] frames, int width, int height) {
        Intent intent = new Intent(this, EditorActivity.class);

        int numFrames = frames.length;
        for(int i=0;i<numFrames;i++) {
            intent.putExtra("in.liquidmetal.CameraActivity.frame" + i, frames[i]);
        }

        intent.putExtra("in.liquidmetal.CameraActivity.width", width);
        intent.putExtra("in.liquidmetal.CameraActivity.height", height);
        startActivity(intent);
    }

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
    }
}
