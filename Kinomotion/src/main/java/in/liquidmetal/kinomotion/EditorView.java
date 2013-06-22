package in.liquidmetal.kinomotion;

import android.content.Context;
import android.opengl.GLSurfaceView;
import android.os.ConditionVariable;

/**
 * Created by utkarsh on 22/6/13.
 */
public class EditorView extends GLSurfaceView {
    private EditorRenderer mRenderer;
    private final ConditionVariable syncObj = new ConditionVariable();
    private String videoPath;

    public EditorView(Context context, String videoPath) {
        super(context);

        setEGLContextClientVersion(2);
        this.videoPath = videoPath;

        mRenderer = new EditorRenderer(videoPath);
        setRenderer(mRenderer);
    }

    @Override
    public void onPause() {
        syncObj.close();
        queueEvent(new Runnable() {
            @Override public void run() {
                mRenderer.onViewPause(syncObj);
            }
        });

        // TODO What's the use of this syncObj?
        syncObj.block();
    }
}
