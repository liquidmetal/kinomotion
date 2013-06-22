package in.liquidmetal.kinomotion;

import in.liquidmetal.kinomotion.util.SystemUiHider;

import android.annotation.TargetApi;
import android.app.ActionBar;
import android.app.Activity;
import android.content.pm.ActivityInfo;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.BitmapShader;
import android.graphics.BlurMaskFilter;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.MaskFilter;
import android.graphics.Paint;
import android.graphics.Point;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffColorFilter;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;
import android.hardware.Camera;
import android.opengl.GLSurfaceView;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.GridLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.NumberPicker;
import android.widget.SeekBar;

import java.util.LinkedList;

/**
 * An example full-screen activity that shows and hides the system UI (i.e.
 * status bar and navigation/system bar) with user interaction.
 *
 * @see SystemUiHider
 */
public class EditorActivity extends Activity {
    private EditorView mGLView;
    private ImageView img;
    private SeekBar mSeekBar;
    private Button btnMode;
    private Button btnDraw;
    private NumberPicker npRadius;
    private NumberPicker npSmooth;
    private LinearLayout uberLayout;

    private int frameWidth, frameHeight;


    private Bitmap firstFrame;          // Stores the first frame for quick access
    private Bitmap currentFrame;        // Only for display purposes
    private Bitmap bmMask;              // Used to describe which parts to move
    private Bitmap currentFrameComposite;

    private LinkedList<Point> paintedPoints = new LinkedList<Point>();

    private int optDisplayMode = 0;     // 0 = original frame, 1 = mask, 2 = combined

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // The editor works only in landscape mode
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);


        // Uber level horizontal layout
        uberLayout = new LinearLayout(this);
        uberLayout.setOrientation(LinearLayout.HORIZONTAL);
        uberLayout.setLayoutParams(new ActionBar.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));


        LinearLayout buttons = new LinearLayout(this);
        buttons.setLayoutParams(new ActionBar.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.MATCH_PARENT));
        buttons.setOrientation(LinearLayout.VERTICAL);
        btnMode = new Button(this);
        btnMode.setText("Mode");
        btnMode.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                changeDisplayMode();
            }
        });
        buttons.addView(btnMode);

        btnDraw = new Button(this);
        btnDraw.setText("Draw");
        buttons.addView(btnDraw);

        npRadius = new NumberPicker(this);
        npRadius.setMinValue(1);
        npRadius.setMaxValue(100);
        npRadius.setOrientation(NumberPicker.VERTICAL);
        buttons.addView(npRadius);

        /*btnSmooth = new Button(this);
        btnSmooth.setText("Smooth");
        buttons.addView(btnSmooth);*/




        LinearLayout viewer = new LinearLayout(this);
        viewer.setOrientation(LinearLayout.VERTICAL);
        viewer.setLayoutParams(new ActionBar.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.MATCH_PARENT));

        // The viewer
        //mGLView = new EditorView(this, "/sdcard0/DCIM/kinomotion/testvideo.mp4");
        //mGLView.setLayoutParams(new ActionBar.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, 660));
        //mGLView.setRenderMode(GLSurfaceView.RENDERMODE_WHEN_DIRTY);
        img = new ImageView(this);
        img.setLayoutParams(new ActionBar.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, 660));

        /*img.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                return imgTouched(view, motionEvent);
            }
        });*/
        viewer.addView(img);

        // The seekbar at the bottom of the screen
        mSeekBar = new SeekBar(this);
        mSeekBar.setMinimumHeight(128);
        mSeekBar.setMax(VideoCapturer.frames.length - 1);
        mSeekBar.setLayoutParams(new ActionBar.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, 128));
        mSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int value, boolean fromUser) {
                onSeekbarPositionChange(value);
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });

        viewer.addView(mSeekBar);



        uberLayout.addView(buttons);
        uberLayout.addView(viewer);

        Camera.Size sz = VideoCapturer.mParameters.getPreviewSize();
        frameWidth = sz.width;
        frameHeight = sz.height;


        bmMask = Bitmap.createBitmap(frameWidth, frameHeight, Bitmap.Config.ARGB_8888);
        bmMask.eraseColor(0x000000FF);

        Canvas c = new Canvas(bmMask);
        Paint p = new Paint();
        p.setColor(Color.WHITE);
        p.setStyle(Paint.Style.FILL);
        c.drawCircle(70, 70, 50, p);

        firstFrame = BitmapFactory.decodeByteArray(VideoCapturer.frames[0], 0, VideoCapturer.frames[0].length);

        setContentView(uberLayout);
    }

    public boolean onTouchEvent(MotionEvent e) {
        if(e.getAction() == MotionEvent.ACTION_MOVE) {
            int x = (int)e.getX();
            int y = (int)e.getY();

            int left = img.getLeft();
            int top = img.getTop();
            Rect visibleRect = new Rect(left, top, left+img.getMeasuredWidth(), top+img.getMeasuredHeight());
            if(!visibleRect.contains(x, y))
                return true;

            Point pt = new Point(x - img.getLeft(), y - img.getTop());
            paintedPoints.add(pt);

            return true;
        } else if(e.getAction() == MotionEvent.ACTION_UP) {
            // Actually apply the drawing
            Canvas c = new Canvas(bmMask);
            Paint p = new Paint();
            p.setColor(Color.WHITE);
            p.setStrokeWidth(npRadius.getValue());

            Point previous = null;
            for(Point pt:paintedPoints) {
                if(previous == null) {
                    previous = pt;
                    continue;
                }

                c.drawLine(previous.x, previous.y, pt.x, pt.y, p);
                previous = pt;
            }

            paintedPoints.clear();
        }

        return true;
    }

    // Calculates a new version of the comp
    private void updateComp() {
        int[] alphaPix = new int[frameWidth * frameHeight];
        bmMask.getPixels(alphaPix, 0, frameWidth, 0, 0, frameWidth, frameHeight);
        int count = frameWidth * frameHeight;
        for (int i = 0; i < count; ++i)
            alphaPix[i] = (alphaPix[i] << 2) & 0xFF000000;

        Bitmap tempAlpha = Bitmap.createBitmap(alphaPix, frameWidth, frameHeight, Bitmap.Config.ARGB_8888);

        Paint alphaP = new Paint();
        alphaP.setAntiAlias(true);
        alphaP.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.DST_IN));

        Bitmap temp = Bitmap.createBitmap(frameWidth, frameHeight, Bitmap.Config.ARGB_8888);
        Canvas c = new Canvas(temp);
        c.drawBitmap(currentFrame, 0, 0, new Paint());
        c.drawBitmap(tempAlpha, 0, 0, alphaP);

        currentFrameComposite = firstFrame.copy(Bitmap.Config.ARGB_8888, true);
        c = new Canvas(currentFrameComposite);
        c.drawBitmap(temp, 0, 0, new Paint());

        tempAlpha.recycle();
    }

    private void changeDisplayMode() {
        optDisplayMode += 1;
        if(optDisplayMode>2)
            optDisplayMode = 0;

        updateViewer();
    }

    private void onSeekbarPositionChange(int newFrame) {
        byte[] frame = VideoCapturer.frames[newFrame];
        currentFrame = BitmapFactory.decodeByteArray(frame, 0, frame.length);

        if(optDisplayMode==2)
            updateComp();
        updateViewer();

    }

    private void updateViewer() {
        switch(optDisplayMode) {
            case 0:
                img.setImageBitmap(currentFrame);
                break;

            case 1:
                img.setImageBitmap(bmMask);
                break;

            case 2:
                img.setImageBitmap(currentFrameComposite);
                //img.setImageBitmap(firstFrame);
                break;

        }
    }

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
    }

    @Override
    protected void onStart() {
        super.onStart();
        // The activity is about to become visible.
    }
    @Override
    protected void onResume() {
        super.onResume();
        // The activity has become visible (it is now "resumed").
    }
    @Override
    protected void onPause() {
        super.onPause();
        // Another activity is taking focus (this activity is about to be "paused").
    }
    @Override
    protected void onStop() {
        super.onStop();
        // The activity is no longer visible (it is now "stopped")
    }
    @Override
    protected void onDestroy() {
        super.onDestroy();
        // The activity is about to be destroyed.

        VideoCapturer.frames = null;
    }
}
