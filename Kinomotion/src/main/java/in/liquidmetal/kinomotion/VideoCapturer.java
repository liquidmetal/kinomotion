package in.liquidmetal.kinomotion;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.ImageFormat;
import android.graphics.Rect;
import android.graphics.YuvImage;
import android.hardware.Camera;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.widget.ImageView;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

/**
 * Created by utkarsh on 22/6/13.
 */
public class VideoCapturer extends SurfaceView implements SurfaceHolder.Callback {
    private Camera mCamera;
    private Camera.Parameters mParameters;

    private final int NUM_FRAMES = 30;
    private int framesCaptured = 0;
    private byte[][] frames = new byte[NUM_FRAMES][];
    private boolean isGrabbing = false;

    public VideoCapturer(Context context) {
        super(context);
        getHolder().addCallback(this);

    }

    @Override
    public void surfaceCreated(SurfaceHolder surfaceHolder) {

        mCamera = Camera.open();
        mParameters = mCamera.getParameters();
        getHolder().setFixedSize(720, 1280);
        final Camera.Size previewSize = mParameters.getPreviewSize();
        final VideoCapturer self = this;

        try {
            mCamera.setPreviewDisplay(getHolder());
        }
        catch(IOException e) {
            System.exit(1);
        }


        mCamera.setPreviewCallback(new Camera.PreviewCallback(){
            public void onPreviewFrame(byte[] data, Camera camera) {
                if(mParameters.getPreviewFormat() == ImageFormat.NV21) {
                    YuvImage img = new YuvImage(data, ImageFormat.NV21, previewSize.width, previewSize.height, null);

                    ByteArrayOutputStream out = new ByteArrayOutputStream();
                    img.compressToJpeg(new Rect(0, 0, previewSize.width, previewSize.height), 50, out);
                    byte[] imageBytes = out.toByteArray();

                    if(isGrabbing) {
                        Canvas recCanvas = new Canvas();
                        recCanvas.drawColor(Color.RED);
                        self.draw(recCanvas);
                    }

                    if(framesCaptured<NUM_FRAMES)
                        frames[framesCaptured++] = imageBytes;
                    else {
                        // Stop grabbing... we can now move onto the editor with all the data we've
                        // collected
                        self.stopGrabbing();

                    }
                }
            }
        });

        mCamera.startPreview();
        beginGrabbing();
    }

    public void beginGrabbing() {
        isGrabbing = true;
    }

    public void stopGrabbing() {
        isGrabbing = false;
        mCamera.stopPreview();
    }

    @Override
    public void surfaceChanged(SurfaceHolder surfaceHolder, int i, int i2, int i3) {

    }

    @Override
    public void surfaceDestroyed(SurfaceHolder surfaceHolder) {

    }
}
