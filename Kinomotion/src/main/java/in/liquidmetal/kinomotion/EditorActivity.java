package in.liquidmetal.kinomotion;

import in.liquidmetal.kinomotion.util.SystemUiHider;

import android.annotation.TargetApi;
import android.app.ActionBar;
import android.app.Activity;
import android.content.pm.ActivityInfo;
import android.graphics.Color;
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
import android.widget.LinearLayout;
import android.widget.SeekBar;

/**
 * An example full-screen activity that shows and hides the system UI (i.e.
 * status bar and navigation/system bar) with user interaction.
 *
 * @see SystemUiHider
 */
public class EditorActivity extends Activity {
    private EditorView mGLView;
    private SeekBar mSeekBar;
    private Button btnMode;
    private Button btnDraw;
    private Button btnRadius;
    private Button btnSmooth;
    private LinearLayout uberLayout;


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
        buttons.addView(btnMode);

        btnDraw = new Button(this);
        btnDraw.setText("Draw");
        buttons.addView(btnDraw);

        btnRadius = new Button(this);
        btnRadius.setText("Radius");
        buttons.addView(btnRadius);

        btnSmooth = new Button(this);
        btnSmooth.setText("Smooth");
        buttons.addView(btnSmooth);




        LinearLayout viewer = new LinearLayout(this);
        viewer.setOrientation(LinearLayout.VERTICAL);
        viewer.setLayoutParams(new ActionBar.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.MATCH_PARENT));

        // The viewer
        mGLView = new EditorView(this, "/sdcard0/DCIM/kinomotion/testvideo.mp4");
        //mGLView.setMinimumWidth(950);
        //mGLView.setMinimumHeight(550);
        mGLView.setLayoutParams(new ActionBar.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
        mGLView.setRenderMode(GLSurfaceView.RENDERMODE_WHEN_DIRTY);
        viewer.addView(mGLView);

        // The seekbar at the bottom of the screen
        mSeekBar = new SeekBar(this);
        mSeekBar.setMinimumHeight(128);
        mSeekBar.setMax(VideoCapturer.frames.length - 1);
        mSeekBar.setLayoutParams(new ActionBar.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
        viewer.addView(mSeekBar);






        uberLayout.addView(buttons);
        uberLayout.addView(viewer);
        setContentView(uberLayout);

        int width = mGLView.getWidth();
        int height = mGLView.getHeight();

        int l = VideoCapturer.frames.length;
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
