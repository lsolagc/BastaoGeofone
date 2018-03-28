package br.com.gasi.bastogeofone;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;

import java.util.ArrayList;
import java.util.Random;

public class CanvasDrawing extends View {

    private static final String TAG = CanvasDrawing.class.getSimpleName();
    private Canvas mCanvas;
    //private Rect newRect, oldRect;
    private ArrayList<Rect> points = new ArrayList<>();
    private ArrayList<Paint> paints = new ArrayList<>();
    private final int SQUARE_SIZE = 5;
    private final int PADDING = 2;
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
        init();
    }

    public CanvasDrawing(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public CanvasDrawing(Context context, AttributeSet attrs, int defStyleAttr){
        super(context,attrs,defStyleAttr);
        init();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        mCanvas = canvas;
        int width = canvas.getWidth();
        int centerX = width / 2;
        int centerY = canvas.getHeight() / 2;
        if(first){
            int left = centerX - (SQUARE_SIZE / 2);
            int top = centerY - (SQUARE_SIZE / 2);
            int bottom = top + SQUARE_SIZE;
            int right = left + SQUARE_SIZE;
            points.add(new Rect(left, top, right, bottom));
            paints.add(setColor());
            Log.i(TAG, "drawPoint: left, top, right, bottom: "+ left +","+ top +","+ right +","+ bottom);
            first = false;
        }
        for (Rect rect :
                points) {
            canvas.drawRect(rect, paints.get(points.indexOf(rect)));
        }
    }

    private void init(){
        //newRect = new Rect();
    }

    // Dummy method
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
        if (mRect.left >= 0 && mRect.top >= 0 && mRect.bottom <= mCanvas.getHeight() && mRect.right <= mCanvas.getWidth()) {
            points.add(mRect);
            paints.add(setColor());
            postInvalidate();
        }
    }

    public void move(int direction, double receivedIntensity){
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
        if (mRect.left >= 0 && mRect.top >= 0 && mRect.bottom <= mCanvas.getHeight() && mRect.right <= mCanvas.getWidth()) {
            points.add(mRect);
            paints.add(setColor(receivedIntensity));
            postInvalidate();
        }
    }

    // Dummy method
    @Nullable
    private Paint setColor() {
        Paint mmPaint = new Paint();
        dummyReceiveValue();
        if(receivedIntensity<WARNING_THRESHOLD){
            //Toast.makeText(getContext(), "Okay", Toast.LENGTH_SHORT).show();
            mmPaint.setColor(Color.rgb(57,239,57));
            return mmPaint;
        }
        else{
            if(receivedIntensity > WARNING_THRESHOLD && receivedIntensity < DANGER_THRESHOLD){
                //Toast.makeText(getContext(), "Warning", Toast.LENGTH_SHORT).show();
                mmPaint.setColor(Color.rgb(255,255,57));
                return mmPaint;
            }
            else{
                if(receivedIntensity > DANGER_THRESHOLD) {
                   // Toast.makeText(getContext(), "Danger", Toast.LENGTH_SHORT).show();
                    mmPaint.setColor(Color.rgb(255,57,57));
                    return mmPaint;
                }
                else{
                    //Toast.makeText(getContext(), "Invalid Value Received", Toast.LENGTH_SHORT).show();
                    Log.w(TAG, "setColor: invalid value received");
                    return null;
                }
            }
        }
    }

    @Nullable
    private Paint setColor(double receivedIntensity) {
        Paint mmPaint = new Paint();
        if(receivedIntensity<WARNING_THRESHOLD){
           // Toast.makeText(getContext(), "Okay", Toast.LENGTH_SHORT).show();
            mmPaint.setColor(Color.rgb(57,239,57));
            return mmPaint;
        }
        else{
            if(receivedIntensity > WARNING_THRESHOLD && receivedIntensity < DANGER_THRESHOLD){
              //  Toast.makeText(getContext(), "Warning", Toast.LENGTH_SHORT).show();
                mmPaint.setColor(Color.rgb(255,255,57));
                return mmPaint;
            }
            else{
                if(receivedIntensity > DANGER_THRESHOLD) {
                //    Toast.makeText(getContext(), "Danger", Toast.LENGTH_SHORT).show();
                    mmPaint.setColor(Color.rgb(255,57,57));
                    return mmPaint;
                }
                else{
                //    Toast.makeText(getContext(), "Invalid Value Received", Toast.LENGTH_SHORT).show();
                    Log.w(TAG, "setColor: invalid value received");
                    return null;
                }
            }
        }
    }

    // Dummy method
    private void dummyReceiveValue() {
        Random r = new Random();
        receivedIntensity = r.nextInt();
    }

}
