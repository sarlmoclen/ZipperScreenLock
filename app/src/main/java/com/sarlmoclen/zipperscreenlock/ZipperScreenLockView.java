package com.sarlmoclen.zipperscreenlock;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.PorterDuff.Mode;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;
import android.graphics.drawable.BitmapDrawable;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;

/**
 * 拉链解锁动画view
 * from github sarlmoclen
 */
public class ZipperScreenLockView extends View {
	
	/**context*/
	private Context c;
	/**普通画笔*/
	private Paint mPaint;
	/**覆盖图画笔*/
	private Paint mPaintMask;
	/**拉扣图片资源*/
	private Bitmap mBitmapZipper;
	/**左拉链图片资源*/
	private Bitmap mBitmapZipperLeft;
	/**右拉链图片资源*/
	private Bitmap mBitmapZipperRight;
	/**拉链背景图片资源*/
	private Bitmap mBitmapZipperBg;
	/**覆盖图片资源*/
	private Bitmap mBitmapZipperMask;
	/**背景图片资源*/
	private Bitmap mBitmapBg;
	/**view的宽和高*/
	private double wSize,hSize;
	/**手触摸点的x,y坐标*/
	private double xTouch,yTouch;
	/**手是否触摸拉扣*/
	private boolean isTouch = false;
	/**是否是第一次获取屏幕宽和高*/
	private boolean isFirst = true;
	/**拉扣初始触摸点y轴高度*/
	private double moveY;
	/**在当前view下拉链背景图片中单个拉链的宽度(不包含金属拉链部分，只有布的那部分)*/
	private double wZipper;
	/**拉开拉链时是否显示背景图*/
	private boolean isShowBg = true;
	/**拉链背景图片的宽*/
	private final static double ZIPPER_BG_WIDTH = 480.0;
	/**拉链背景图片的高*/
	private final static double ZIPPER_BG_HEIGHT = 845.0;
	/**覆盖图片的宽*/
	private final static double ZIPPER_MASK_WIDTH = 480.0;
	/**覆盖图片的高*/
	private final static double ZIPPER_MASK_HEIGHT = 1100.0;
	/**背景图片的宽*/
	private final static double BG_WIDTH = 640.0;
	/**背景图片的高*/
	private final static double BG_HEIGHT = 1136.0;
	/**拉链背景图片中单个拉链的宽度(不包含金属拉链部分，只有布的那部分)*/
	private final static double ZIPPER_WIDTH = 38.0;
	/**左拉链图片的宽*/
	private final static double ZIPPER_LEFT_WIDTH = 458.0;
	/**左拉链图片的高*/
	private final static double ZIPPER_LEFT_HEIGHT = 1100.0;
	/**左拉链图片中减去所有拉链部分剩下的宽*/
	private final static double ZIPPER_LEFT_WIDTH_NO_ZIPPER = 401.0;
	/**左拉链图片中单个拉链的宽度(不包含金属拉链部分，只有布的那部分)*/
	private final static double LEFT_ZIPPER_WIDTH = 35.0;
	/**覆盖图片,左拉链图片,右拉链图片在Y轴的初始位置(int类型)*/
	private final static int INIT_HEIGHT_INT = 50;
	/**覆盖图片,左拉链图片,右拉链图片在Y轴的初始位置(double类型)*/
	private final static double INIT_HEIGHT_DOUBLE = 50.0;
	/**包含金属拉链部分和不包含金属拉链部分的倍数关系*/
	private final static double ZIPPER_SCALE = 1.32;

	public ZipperScreenLockView(Context context, AttributeSet attrs) {
		super(context, attrs);
		c = context;
		init();
		initView(context);
	}
	
	public ZipperScreenLockView(Context context) {
		super(context);
		c = context;
		init();
		initView(context);
	}
	
	/**
	 * 初始化
	 * from github sarlmoclen
	 */
	private void init(){
		mPaint = new Paint(Paint.ANTI_ALIAS_FLAG);  
		mPaint.setFilterBitmap(true);  
		mPaint.setDither(true);  
		mPaintMask = new Paint(Paint.ANTI_ALIAS_FLAG);
		mPaintMask.setAntiAlias(true);
		mPaintMask.setXfermode(new PorterDuffXfermode(Mode.DST_IN));
	}
	
	/**
	 * 初始化图片
	 * from github sarlmoclen
	 */
	private void initView(Context context){
		mBitmapZipper = ((BitmapDrawable) context.getResources().getDrawable(R.mipmap.zipper))
	                .getBitmap();  
		mBitmapZipperLeft = ((BitmapDrawable) context.getResources().getDrawable(R.mipmap.zipper_left))
                .getBitmap();  
		mBitmapZipperRight = ((BitmapDrawable) context.getResources().getDrawable(R.mipmap.zipper_right))
                .getBitmap();
		mBitmapZipperBg = ((BitmapDrawable) context.getResources().getDrawable(R.mipmap.zipper_bg))
				 .getBitmap();
		mBitmapZipperMask = ((BitmapDrawable) context.getResources().getDrawable(R.mipmap.zipper_bg_mask))
				 .getBitmap();
		mBitmapBg = ((BitmapDrawable) context.getResources().getDrawable(R.mipmap.bg))
				 .getBitmap();
	}
	
	/**
	 * 给当前view适配图片
	 * from github sarlmoclen
	 */
	private Bitmap setBitmapSize(Bitmap bitmap,double w,double h){
        int width = bitmap.getWidth();
        int height = bitmap.getHeight();
        // 设置想要的大小
        double newWidth = w;
        double newHeight = h;
        // 计算缩放比例
        float scaleWidth = (float)(newWidth/((double)width));
        float scaleHeight = (float)(newHeight/((double)height));
        // 取得想要缩放的matrix参数
        Matrix matrix = new Matrix();
        matrix.postScale(scaleWidth, scaleHeight);
        // 得到新的图片
        return bitmap = Bitmap.createBitmap(bitmap, 0, 0, width, height, matrix,true);
	}

	@Override
	protected void onDraw(Canvas canvas) {
		super.onDraw(canvas);
		if(isFirst){
			wSize = getWidth();
			hSize = getHeight();
			//拉链背景图片适配到这个view的宽,高等比例变化
			mBitmapZipperBg = setBitmapSize(mBitmapZipperBg,wSize,wSize*ZIPPER_BG_HEIGHT/ZIPPER_BG_WIDTH);
			//覆盖图片适配到这个view的宽,高等比例变化
			mBitmapZipperMask = setBitmapSize(mBitmapZipperMask,wSize,wSize*ZIPPER_MASK_HEIGHT/ZIPPER_MASK_WIDTH);
			//背景图片适配到这个view的宽,高等比例变化
			mBitmapBg = setBitmapSize(mBitmapBg,wSize,wSize*BG_HEIGHT/BG_WIDTH);
			//根据拉链背景图片中拉链部分(不包含金属拉链部分，只有布的那部分)的宽度，获取适配后这个view中拉链的宽度
			wZipper = wSize*ZIPPER_WIDTH/ZIPPER_BG_WIDTH;
			//以拉链宽度为基准，算出左右拉链图片的等比例宽度和高度，做适配（这里以左边拉链图为准，右边拉链跟左边拉链保持一致，这样做到左右效果一样）
			mBitmapZipperLeft = setBitmapSize(mBitmapZipperLeft,wZipper*ZIPPER_LEFT_WIDTH/LEFT_ZIPPER_WIDTH,wZipper*ZIPPER_LEFT_HEIGHT/LEFT_ZIPPER_WIDTH);
			mBitmapZipperRight = setBitmapSize(mBitmapZipperRight,wZipper*ZIPPER_LEFT_WIDTH/LEFT_ZIPPER_WIDTH,wZipper*ZIPPER_LEFT_HEIGHT/LEFT_ZIPPER_WIDTH);
			isFirst = false;
		}
		if(isTouch){
			if((yTouch-moveY) > 0){
				//背景图片移动状态的绘制
				if(isShowBg) {
					Rect mSrcRectBg = new Rect(0, 0, mBitmapBg.getWidth(), mBitmapBg.getHeight());
					Rect mDestRectBg = new Rect(0
							, 0
							, (int) wSize
							, (int) hSize);
					canvas.drawBitmap(mBitmapBg, mSrcRectBg, mDestRectBg, mPaint);
				}
				//新建图层
				int  i = 0;
				if(isShowBg){
					i = canvas.saveLayer(0, 0, (float)wSize, (float)hSize, mPaint, Canvas.ALL_SAVE_FLAG);
				}
				//拉链背景图片移动状态的绘制
				Rect mSrcRectZipperBg = new Rect(0, 0, mBitmapZipperBg.getWidth(), mBitmapZipperBg.getHeight());  
				Rect mDestRectZipperBg = new Rect(0
						, 0
						, (int)wSize
						, (int)hSize); 
				canvas.drawBitmap(mBitmapZipperBg, mSrcRectZipperBg, mDestRectZipperBg, mPaint); 
				//覆盖图片移动状态的绘制
				Rect mSrcRectZipperMask = new Rect(0, 0, mBitmapZipperMask.getWidth(), mBitmapZipperMask.getHeight());  
				Rect mDestRectZipperMask = new Rect(0
						, INIT_HEIGHT_INT-mBitmapZipperMask.getHeight()+(int)(yTouch-moveY)
						, mBitmapZipperMask.getWidth()
						, INIT_HEIGHT_INT+(int)(yTouch-moveY));
				canvas.drawBitmap(mBitmapZipperMask, mSrcRectZipperMask, mDestRectZipperMask, mPaintMask);
				//左拉链图片移动状态的绘制
				Rect mSrcRectZipperLeft = new Rect(0, 0, mBitmapZipperLeft.getWidth(),(int)(INIT_HEIGHT_INT+yTouch-moveY));
				Rect mDestRectZipperLeft = new Rect((int)(wSize/2-(INIT_HEIGHT_DOUBLE*mBitmapZipperLeft.getWidth()/mBitmapZipperLeft.getHeight()+ZIPPER_SCALE*wZipper)-(yTouch-moveY)*ZIPPER_LEFT_WIDTH_NO_ZIPPER/ZIPPER_LEFT_HEIGHT)
						, 0
						, (int)(wSize/2-(INIT_HEIGHT_DOUBLE*mBitmapZipperLeft.getWidth()/mBitmapZipperLeft.getHeight()+ZIPPER_SCALE*wZipper)+mBitmapZipperLeft.getWidth()-(yTouch-moveY)*ZIPPER_LEFT_WIDTH_NO_ZIPPER/ZIPPER_LEFT_HEIGHT)
						, (int)(INIT_HEIGHT_INT+yTouch-moveY));
				canvas.drawBitmap(mBitmapZipperLeft, mSrcRectZipperLeft, mDestRectZipperLeft, mPaint); 
				//右拉链图片移动状态的绘制
				Rect mSrcRectZipperRight = new Rect(0, 0, mBitmapZipperRight.getWidth(),(int)(INIT_HEIGHT_INT+yTouch-moveY));
				Rect mDestRectZipperRight = new Rect((int)(wSize/2+(INIT_HEIGHT_DOUBLE*mBitmapZipperRight.getWidth()/mBitmapZipperRight.getHeight()+ZIPPER_SCALE*wZipper)-mBitmapZipperRight.getWidth()+(yTouch-moveY)*ZIPPER_LEFT_WIDTH_NO_ZIPPER/ZIPPER_LEFT_HEIGHT)
						, 0
						, (int)(wSize/2+(INIT_HEIGHT_DOUBLE*mBitmapZipperRight.getWidth()/mBitmapZipperRight.getHeight()+ZIPPER_SCALE*wZipper)+(yTouch-moveY)*ZIPPER_LEFT_WIDTH_NO_ZIPPER/ZIPPER_LEFT_HEIGHT)
						, (int)(INIT_HEIGHT_INT+yTouch-moveY));
				canvas.drawBitmap(mBitmapZipperRight, mSrcRectZipperRight, mDestRectZipperRight, mPaint); 
				//拉扣图片移动状态的绘制
				canvas.drawBitmap(mBitmapZipper, (float)(wSize/2-mBitmapZipper.getWidth()/2), (float)(yTouch-moveY),  mPaint);
				//结束这个图层的绘制
				if(isShowBg){
					canvas.restoreToCount(i);
				}
			}else{
				initState(canvas);
			}
		}else{
			 initState(canvas);
		}
	}
	
	/**
	 * 初始状态显示
	* from github sarlmoclen
	 */
	private void initState(Canvas canvas){
		//拉链背景图片初始状态的绘制
		Rect mSrcRectZipperBg = new Rect(0, 0, mBitmapZipperBg.getWidth(), mBitmapZipperBg.getHeight());  
		Rect mDestRectZipperBg = new Rect(0
				, 0
				, (int)wSize
				, (int)hSize); 
		canvas.drawBitmap(mBitmapZipperBg, mSrcRectZipperBg, mDestRectZipperBg, mPaint);
		//覆盖图片初始状态的绘制
		Rect mSrcRectZipperMask = new Rect(0, 0, mBitmapZipperMask.getWidth(), mBitmapZipperMask.getHeight());  
		Rect mDestRectZipperMask = new Rect(0
				, INIT_HEIGHT_INT-mBitmapZipperMask.getHeight()
				, mBitmapZipperMask.getWidth()
				, INIT_HEIGHT_INT);
		canvas.drawBitmap(mBitmapZipperMask, mSrcRectZipperMask, mDestRectZipperMask, mPaintMask);
		//左拉链图片初始状态的绘制
		Rect mSrcRectZipperLeft = new Rect(0, 0, mBitmapZipperLeft.getWidth(),INIT_HEIGHT_INT);
		Rect mDestRectZipperLeft = new Rect((int)(wSize/2-(INIT_HEIGHT_DOUBLE*mBitmapZipperLeft.getWidth()/mBitmapZipperLeft.getHeight()+ZIPPER_SCALE*wZipper))
				, 0
				, (int)(wSize/2-(INIT_HEIGHT_DOUBLE*mBitmapZipperLeft.getWidth()/mBitmapZipperLeft.getHeight()+ZIPPER_SCALE*wZipper)+mBitmapZipperLeft.getWidth())
				, INIT_HEIGHT_INT);
		canvas.drawBitmap(mBitmapZipperLeft, mSrcRectZipperLeft, mDestRectZipperLeft, mPaint); 
		//右拉链图片初始状态的绘制
		Rect mSrcRectZipperRight = new Rect(0, 0, mBitmapZipperRight.getWidth(),INIT_HEIGHT_INT);
		Rect mDestRectZipperRight = new Rect((int)(wSize/2+(INIT_HEIGHT_DOUBLE*mBitmapZipperRight.getWidth()/mBitmapZipperRight.getHeight()+ZIPPER_SCALE*wZipper)-mBitmapZipperRight.getWidth())
				, 0
				, (int)(wSize/2+(INIT_HEIGHT_DOUBLE*mBitmapZipperRight.getWidth()/mBitmapZipperRight.getHeight()+ZIPPER_SCALE*wZipper))
				, INIT_HEIGHT_INT);
		canvas.drawBitmap(mBitmapZipperRight, mSrcRectZipperRight, mDestRectZipperRight, mPaint); 
		//拉扣图片初始状态的绘制
		canvas.drawBitmap(mBitmapZipper, (float)(wSize/2-mBitmapZipper.getWidth()/2), 0,  mPaint);
	}

	@Override
	public boolean onTouchEvent(MotionEvent event) {
		// TODO Auto-generated method stub
		xTouch = event.getX();   
    	yTouch = event.getY(); 
        switch (event.getAction()) {   
	        case MotionEvent.ACTION_DOWN:
	        	if((xTouch > (wSize/2-mBitmapZipper.getWidth()/2)) 
	        			&& (xTouch < (wSize/2+mBitmapZipper.getWidth()/2)) 
	        				&& (yTouch > 0) 
	        					&& (yTouch < mBitmapZipper.getHeight())){
	        		isTouch = true;
	        		moveY = yTouch;
	        	}
	        	if(isTouch){
	        		invalidate(); 
	        	}
	            break;   
	        case MotionEvent.ACTION_MOVE:  
	        	if(isTouch){
	        		invalidate(); 
	        	}
	            break;   
	        case MotionEvent.ACTION_UP:
	        	isTouch = false;  
	        	invalidate(); 
	            break;   
        }   
		return true;
	}
	
}
