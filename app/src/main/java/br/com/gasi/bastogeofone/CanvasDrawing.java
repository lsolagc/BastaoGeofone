package br.com.gasi.bastogeofone;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Point;
import android.graphics.Rect;
import android.os.Build;
import android.support.annotation.Nullable;
import android.support.annotation.RequiresApi;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.Random;

/**
 * Created by Resende on 30/01/2018.
 */

public class CanvasDrawing extends View {

    private static final String TAG = CanvasDrawing.class.getSimpleName();
    private Paint mPaint;
    private Canvas mCanvas;
    //private Rect newRect, oldRect;
    private ArrayList<Rect> points = new ArrayList<>();
    private ArrayList<Paint> paints = new ArrayList<>();
    private final int SQUARE_SIZE = 10;
    private final int PADDING = 2;
    private int left, top, right, bottom, width, centerX, centerY;
    private boolean first = true;
    private int receivedIntensity;

    //Class constants
    public static final int LEFT = 801;
    public static final int UPLEFT = 802;
    public static final int UP = 803;
    public static final int UPRIGHT = 804;
    public static final int RIGHT = 805;
    public static final int DOWNRIGHT = 806;
    public static final int DOWN = 807;
    public static final int DOWNLEFT = 808;

    public static final int WARNING_THRESHOLD = 100;
    public static final int DANGER_THRESHOLD = 500;


    public CanvasDrawing(Context context) {
        super(context);
        init(null);
    }

    public CanvasDrawing(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(attrs);
    }

    public CanvasDrawing(Context context, AttributeSet attrs, int defStyleAttr){
        super(context,attrs,defStyleAttr);
        init(attrs);
    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    public CanvasDrawing(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes){
        super(context,attrs,defStyleAttr,defStyleRes);
        init(attrs);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        mCanvas = canvas;
        width = canvas.getWidth();
        centerX = width/2;
        centerY = canvas.getHeight()/2;
        if(first){
            left = centerX-(SQUARE_SIZE/2);
            top = centerY-(SQUARE_SIZE/2);
            bottom = top + SQUARE_SIZE;
            right = left + SQUARE_SIZE;
            points.add(new Rect(left, top, right, bottom));
            paints.add(setColor());
            Log.i(TAG, "drawPoint: left, top, right, bottom: "+left+","+top+","+right+","+bottom);
            first = false;
        }
        for (Rect rect :
                points) {
            canvas.drawRect(rect, paints.get(points.indexOf(rect)));
        }
    }

    private void init(@Nullable AttributeSet attributeSet){
        //newRect = new Rect();
    }

    public void drawCenterPoint(){
        if(first){
            left = centerX-(SQUARE_SIZE/2);
            top = centerY-(SQUARE_SIZE/2);
            bottom = top + SQUARE_SIZE;
            right = left + SQUARE_SIZE;
            points.add(new Rect(left, top, right, bottom));
            Log.i(TAG, "drawPoint: left, top, right, bottom: "+left+","+top+","+right+","+bottom);
            postInvalidate();
        }
        first = false;
    }

    public void move(int direction){
        Rect mRect = new Rect(points.get(points.size()-1));
        int dx, dy;
        switch (direction){
            case LEFT:
                dx = -PADDING-SQUARE_SIZE;
                dy = 0;
                break;
            case UPLEFT:
                dx = -PADDING-SQUARE_SIZE;
                dy = -PADDING-SQUARE_SIZE;
                break;
            case UP:
                dx = 0;
                dy = -PADDING-SQUARE_SIZE;
                break;
            case UPRIGHT:
                dx = PADDING+SQUARE_SIZE;
                dy = -PADDING-SQUARE_SIZE;
                break;
            case RIGHT:
                dx = PADDING+SQUARE_SIZE;
                dy = 0;
                break;
            case DOWNRIGHT:
                dx = PADDING+SQUARE_SIZE;
                dy = PADDING+SQUARE_SIZE;
                break;
            case DOWN:
                dx = 0;
                dy = PADDING+SQUARE_SIZE;
                break;
            case DOWNLEFT:
                dx  = -PADDING-SQUARE_SIZE;
                dy = PADDING+SQUARE_SIZE;
                break;
            default:
                dx = 0;
                dy = 0;
                break;
        }
        mRect.offset(dx,dy);
        if (mRect.left < 0 || mRect.top < 0 || mRect.bottom > mCanvas.getHeight() || mRect.right > mCanvas.getWidth()) {
            return;
        }else{
            points.add(mRect);
            paints.add(setColor());
            postInvalidate();
        }
    }

    @Nullable
    private Paint setColor() {
        Paint mmPaint = new Paint();
        dummyReceiveValue();
        if(receivedIntensity<WARNING_THRESHOLD){
            Toast.makeText(getContext(), "Okay", Toast.LENGTH_SHORT).show();
            mmPaint.setColor(Color.rgb(57,239,57));
            return mmPaint;
        }
        else{
            if(receivedIntensity > WARNING_THRESHOLD && receivedIntensity < DANGER_THRESHOLD){
                Toast.makeText(getContext(), "Warning", Toast.LENGTH_SHORT).show();
                mmPaint.setColor(Color.rgb(255,255,57));
                return mmPaint;
            }
            else{
                if(receivedIntensity > DANGER_THRESHOLD) {
                    Toast.makeText(getContext(), "Danger", Toast.LENGTH_SHORT).show();
                    mmPaint.setColor(Color.rgb(255,57,57));
                    return mmPaint;
                }
                else{
                    Toast.makeText(getContext(), "Invalid Value Received", Toast.LENGTH_SHORT).show();
                    Log.w(TAG, "setColor: invalid value received");
                    return null;
                }
            }
        }
    }

    private void dummyReceiveValue() {
        Random r = new Random();
        receivedIntensity = r.nextInt();
    }

}
