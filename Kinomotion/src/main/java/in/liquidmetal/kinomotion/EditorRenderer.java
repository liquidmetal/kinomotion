package in.liquidmetal.kinomotion;

import android.opengl.GLES20;
import android.opengl.GLSurfaceView;
import android.opengl.Matrix;
import android.os.ConditionVariable;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

/**
 * Created by utkarsh on 22/6/13.
 */
public class EditorRenderer implements GLSurfaceView.Renderer {
    private int mViewportHeight,mViewportWidth, mViewportXoff, mViewportYoff;
    static float mProjectionMatrix[] = new float[16];
    private String videoPath;

    public EditorRenderer(String videoPath) {
        this.videoPath = videoPath;
    }

    private void initialize() {
        GLES20.glClearColor(1f, 0f, 0f, 0f);
    }

    @Override
    public void onSurfaceCreated(GL10 gl10, EGLConfig eglConfig) {
        initialize();
    }

    @Override
    public void onSurfaceChanged(GL10 gl10, int width, int height) {
        float arenaRatio = 480/ 640;
        int x, y, viewWidth, viewHeight;

        // Limited by width?
        if(height > (int)(width*arenaRatio)) {
            viewWidth = width;
            viewHeight = (int)(width*arenaRatio);
            x = 0;
            y = (height - viewHeight) / 2;
        } else {
            viewHeight = height;
            viewWidth = (int)(height / arenaRatio);
            x = (width - viewWidth) / 2;
            y = 0;
        }

        // Setup the OpenGL viewport based on these calculations
        GLES20.glViewport(x, y, viewWidth, viewHeight);

        mViewportHeight = viewHeight;
        mViewportWidth = viewWidth;
        mViewportXoff = x;
        mViewportYoff = y;

        // Now, setup an orthographic projection
        Matrix.orthoM(mProjectionMatrix, 0, 0, width, 0, height, -1.0f, 1.0f);
    }

    @Override
    public void onDrawFrame(GL10 gl10) {
        GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT);
    }

    public void onViewPause(ConditionVariable syncObj) {
        // Save game state

        syncObj.open();
    }
}
