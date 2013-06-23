package in.liquidmetal.kinomotion;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.ImageFormat;
import android.graphics.Rect;
import android.graphics.YuvImage;
import android.hardware.Camera;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.widget.ImageView;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

/**
 * Created by utkarsh on 22/6/13.
 */
public class VideoCapturer extends SurfaceView implements SurfaceHolder.Callback {
    private Camera mCamera;
    public static Camera.Parameters mParameters;

    private static final int NUM_FRAMES = 30;
    private static final int SKIP_INITIAL = 10;
    private int framesCaptured = 0;
    private int framesSkipped = 0;
    public static byte[][] frames = null;
    private boolean isGrabbing = false;
    private CameraActivity activity;

    public VideoCapturer(Context context) {
        super(context);
        getHolder().addCallback(this);
        this.activity = (CameraActivity)context;
    }

    public void doResume() {
        framesSkipped = 0;
        framesCaptured = 0;
        frames = null;
        frames = new byte[NUM_FRAMES][];
        isGrabbing = false;
    }

    @Override
    public void surfaceCreated(SurfaceHolder surfaceHolder) {

        mCamera = Camera.open();
        mParameters = mCamera.getParameters();
        getHolder().setFixedSize(720, 1280);
        final Camera.Size previewSize = mParameters.getPreviewSize();

        //mParameters.set("orientation", "landscape");
        //mCamera.setParameters(mParameters);
        mCamera.setDisplayOrientation(90);

        setOnTouchListener(new OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                beginGrabbing();
                return true;
            }
        });

        final VideoCapturer self = this;

        // Display the preview image on this surface itself
        try {
            mCamera.setPreviewDisplay(getHolder());
        }
        catch(IOException e) {
            System.exit(1);
        }

        // This hooks into each preview frame - and lets us store raw data for each frame
        mCamera.setPreviewCallback(new Camera.PreviewCallback(){
            public void onPreviewFrame(byte[] data, Camera camera) {
                if(mParameters.getPreviewFormat() == ImageFormat.NV21) {
                    YuvImage img = new YuvImage(data, ImageFormat.NV21, previewSize.width, previewSize.height, null);

                    ByteArrayOutputStream out = new ByteArrayOutputStream();
                    img.compressToJpeg(new Rect(0, 0, previewSize.width, previewSize.height), 50, out);
                    byte[] imageBytes = out.toByteArray();

                    /*if(isGrabbing) {
                        Canvas recCanvas = new Canvas();
                        recCanvas.drawColor(Color.RED);
                        self.draw(recCanvas);
                    }*/

                    if(framesSkipped<SKIP_INITIAL) {
                        framesSkipped++;
                        return;
                    }

                    if(!isGrabbing)
                        return;

                    if(framesCaptured<NUM_FRAMES)
                        frames[framesCaptured++] = imageBytes;
                    else {
                        // Stop grabbing... we can now move onto the editor with all the data we've
                        // collected
                        self.stopGrabbing();
                        self.startEditor();
                    }
                }
            }
        });

        mCamera.startPreview();
    }

    public void beginGrabbing() {
        isGrabbing = true;
    }

    public void stopGrabbing() {
        isGrabbing = false;
        mCamera.stopPreview();
    }

    public void startEditor() {
        activity.moveToEditor();
    }

    @Override
    public void surfaceChanged(SurfaceHolder surfaceHolder, int i, int i2, int i3) {

    }

    @Override
    public void surfaceDestroyed(SurfaceHolder surfaceHolder) {
        mCamera.release();
    }
}
