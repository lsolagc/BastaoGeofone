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
import android.view.View;

/**
 * Created by Resende on 30/01/2018.
 */

public class CanvasDrawing extends View {

    private Paint mPaint;
    private Canvas mCanvas;
    private Rect mRect;

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
        mRect = new Rect();
        mRect.bottom = 20;
        mRect.right = 20;
        mRect.left = 10;
        mRect.top = 10;
        canvas.drawRect(mRect, mPaint);
        for(int i=0;i<5;i++){
            Rect mmRect = mRect;
            mmRect.left = mRect.right+2;
            canvas.drawRect(mmRect, mPaint);
            postInvalidate();
        }
    }

    private void init(@Nullable AttributeSet attributeSet){
        mPaint = new Paint();
        mPaint.setColor(Color.GREEN);
    }

    public void draw(String figure, float x, float y){
        figure = figure.toLowerCase();
        switch (figure){
            case "rect":
                break;
            default:
                break;
        }
    }
}
