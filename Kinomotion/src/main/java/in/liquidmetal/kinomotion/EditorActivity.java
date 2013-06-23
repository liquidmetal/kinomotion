package in.liquidmetal.kinomotion;

import in.liquidmetal.kinomotion.util.SystemUiHider;
import android.annotation.TargetApi;
import android.app.ActionBar;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
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
import android.os.Environment;
import android.os.Handler;
import android.view.KeyEvent;
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

import java.io.File;
import java.io.FileOutputStream;
import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.DoubleBuffer;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import java.nio.LongBuffer;
import java.nio.ShortBuffer;
import java.util.Date;
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
    private Button btnSave;
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
    private int optToolMode = 0;        // 0 = paint, 1 = erase
    private int currentFrameNumber = 0;
    private boolean isDirty = false;
    private boolean doCancel = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // The editor works only in landscape mode
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);

        Camera.Size sz = VideoCapturer.mParameters.getPreviewSize();
        frameWidth = sz.width;
        frameHeight = sz.height;


        // Uber level horizontal layout
        uberLayout = new LinearLayout(this);
        uberLayout.setOrientation(LinearLayout.HORIZONTAL);
        uberLayout.setLayoutParams(new ActionBar.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));


        LinearLayout buttons = new LinearLayout(this);
        buttons.setLayoutParams(new ActionBar.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.MATCH_PARENT));
        buttons.setOrientation(LinearLayout.VERTICAL);
        btnMode = new Button(this);
        btnMode.setText("Org");
        btnMode.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                changeDisplayMode();
            }
        });
        buttons.addView(btnMode);

        btnDraw = new Button(this);
        btnDraw.setText("Paint");
        btnDraw.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                swapTool();
            }
        });
        buttons.addView(btnDraw);

        npRadius = new NumberPicker(this);
        npRadius.setMinValue(1);
        npRadius.setMaxValue(100);
        npRadius.setOrientation(NumberPicker.VERTICAL);
        npRadius.setValue(15);
        buttons.addView(npRadius);

        npSmooth = new NumberPicker(this);
        npSmooth.setMinValue(0);
        npSmooth.setMaxValue(100);
        npSmooth.setValue(15);
        buttons.addView(npSmooth);


        LinearLayout viewer = new LinearLayout(this);
        viewer.setOrientation(LinearLayout.VERTICAL);
        viewer.setLayoutParams(new ActionBar.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.MATCH_PARENT));

        // The viewer
        //mGLView = new EditorView(this, "/sdcard0/DCIM/kinomotion/testvideo.mp4");
        //mGLView.setLayoutParams(new ActionBar.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, 660));
        //mGLView.setRenderMode(GLSurfaceView.RENDERMODE_WHEN_DIRTY);
        img = new ImageView(this);
        img.setLayoutParams(new ActionBar.LayoutParams(frameWidth, frameHeight));

        /*img.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                return imgTouched(view, motionEvent);
            }
        });*/
        viewer.addView(img);



        // The seekbar at the bottom of the screen
        mSeekBar = new SeekBar(this);
        mSeekBar.setMax(VideoCapturer.frames.length - 1);
        mSeekBar.setLayoutParams(new ActionBar.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
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

        LinearLayout saveBar = new LinearLayout(this);
        saveBar.setOrientation(LinearLayout.HORIZONTAL);
        saveBar.setLayoutParams(new ActionBar.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.MATCH_PARENT));

        btnSave = new Button(this);
        btnSave.setText("Save");
        btnSave.setLayoutParams(new ActionBar.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT));
        btnSave.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                saveComp();
            }
        });
        saveBar.addView(btnSave);



        uberLayout.addView(buttons);
        uberLayout.addView(viewer);
        uberLayout.addView(saveBar);


        bmMask = Bitmap.createBitmap(frameWidth, frameHeight, Bitmap.Config.ARGB_8888);
        bmMask.eraseColor(0xFF000000);

        firstFrame = BitmapFactory.decodeByteArray(VideoCapturer.frames[0], 0, VideoCapturer.frames[0].length);

        setContentView(uberLayout);
    }

    public void swapTool() {
        if(optToolMode==0) {
            optToolMode = 1;
            btnDraw.setText("Erase");
        } else {
            optToolMode = 0;
            btnDraw.setText("Paint");
        }
    }

    public void saveComp() {
        String path = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM).getAbsolutePath() + "/kinomotion/" + (new Date()).getTime();
        File p = new File(path);
        p.mkdirs();
        //agw.start(path + "/kinomotion/temp.gif");
        for(int i = 0;i<VideoCapturer.frames.length;i++) {


            int numValues = VideoCapturer.frames[i].length;
            int[] intArray = new int[numValues/3];
            for(int k=0;k<numValues/3;k++) {
                intArray[k] = (VideoCapturer.frames[i][k*3] << 16) | (VideoCapturer.frames[i][k*3+1] << 8) | (VideoCapturer.frames[i][k*3+2]);
            }

            currentFrame = BitmapFactory.decodeByteArray(VideoCapturer.frames[i], 0, VideoCapturer.frames[i].length);
            updateComp();

            try {
                FileOutputStream fos = new FileOutputStream(path + "/frame" + i + ".png");
                currentFrameComposite.compress(Bitmap.CompressFormat.PNG, 90, fos);
            }
            catch (Exception e) {
                System.exit(1);
            }

            //agw.addFrame(VideoCapturer.frames[i]);
        }
        //agw.finish();
    }

    public boolean onTouchEvent(MotionEvent e) {
        if(e.getAction() == MotionEvent.ACTION_MOVE) {
            int x = (int)e.getX();
            int y = (int)e.getY();

            int[] pos = new int[2];
            img.getLocationOnScreen(pos);

            int left = pos[0];
            int top = pos[1];

            Rect visibleRect = new Rect(left, top, left+img.getMeasuredWidth(), top+img.getMeasuredHeight());
            if(!visibleRect.contains(x, y))
                return true;

            Point pt = new Point(x-left, y-top);
            paintedPoints.add(pt);
            isDirty = true;

            return true;
        } else if(e.getAction() == MotionEvent.ACTION_UP) {
            // Actually apply the drawing
            Canvas c = new Canvas(bmMask);
            Paint p = new Paint();
            p.setColor(Color.WHITE);
            if(optToolMode == 1) {
                p.setColor(0xFF000000);
            }
            p.setStrokeWidth(npRadius.getValue());
            p.setStrokeMiter(1);

            int blurRadius = npSmooth.getValue();
            if(blurRadius>0)
                p.setMaskFilter(new BlurMaskFilter(npSmooth.getValue(), BlurMaskFilter.Blur.NORMAL));

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
            updateViewer();
        }

        return true;
    }

    // Calculates a new version of the comp
    private void updateComp() {
        int[] alphaPix = new int[frameWidth * frameHeight];
        bmMask.getPixels(alphaPix, 0, frameWidth, 0, 0, frameWidth, frameHeight);
        int count = frameWidth * frameHeight;
        for (int i = 0; i < count; ++i)
            alphaPix[i] = (alphaPix[i] << 8) & 0xFF000000;

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

        switch(optDisplayMode) {
            case 0:
                btnMode.setText("Org");
                break;

            case 1:
                btnMode.setText("Msk");
                break;

            case 2:
                btnMode.setText("Fin");
                break;
        }

        updateViewer();
    }

    private void onSeekbarPositionChange(int newFrame) {
        currentFrameNumber = newFrame;
        updateViewer();
    }

    private void updateViewer() {
        byte[] frame = VideoCapturer.frames[currentFrameNumber];
        currentFrame = BitmapFactory.decodeByteArray(frame, 0, frame.length);

        switch(optDisplayMode) {
            case 0:
                img.setImageBitmap(currentFrame);
                break;

            case 1:
                img.setImageBitmap(bmMask);
                break;

            case 2:
                updateComp();
                img.setImageBitmap(currentFrameComposite);
                //img.setImageBitmap(firstFrame);
                break;

        }
    }

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        updateViewer();
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

    public void executeCancel() {
        super.onBackPressed();
    }

    @Override
    public void onBackPressed() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Are you sure?").setMessage("You will lose changes you've made here and will shoot another movie.");
        builder.setCancelable(false);

        builder.setPositiveButton("New movie", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                executeCancel();
            }
        });
        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
            }
        });

        builder.create().show();
        AnimatedGifWriter agw = new AnimatedGifWriter();
    }
}
