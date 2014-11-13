package View;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;

import java.util.ArrayList;

import blokusgame.mi.android.hazi.blokus.GameLogic.Block;
import blokusgame.mi.android.hazi.blokus.GameLogic.Map;
import blokusgame.mi.android.hazi.blokus.GameLogic.PlayerConstants;
import blokusgame.mi.android.hazi.blokus.GameLogic.Point;

/**
 * Created by elekt on 2014.10.04..
 */
public class BoardView extends View {
    ArrayList<BoardTouchListener> listeners = new ArrayList<BoardTouchListener>();

    private Paint paintBg;
    private Paint paintLine;
    private Paint paintRect;
    private Paint paintOverlay;

    private ArrayList<Point> corners;

    private Block overlayBlock = null;
    private Point overlayPos = null;

    private GestureDetector gestureDetector;

    public BoardView(Context context, AttributeSet attrs){
        super(context, attrs);

        paintBg = new Paint();
        paintBg.setColor(Color.BLACK);
        paintBg.setStyle(Paint.Style.FILL);

        paintLine = new Paint();
        paintLine.setColor(Color.WHITE);
        paintLine.setStyle(Paint.Style.STROKE);
        paintLine.setStrokeWidth(1);

        paintRect = new Paint();
        paintRect.setColor(Color.CYAN);
        paintRect.setStyle(Paint.Style.FILL);

        paintOverlay = new Paint();
        paintOverlay.setColor(Color.GREEN);
        paintOverlay.setStyle(Paint.Style.FILL);
        paintOverlay.setAlpha(10);

        gestureDetector = new GestureDetector(context, new BoardGestureListener(this, listeners));

    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        canvas.drawRect(0, 0, getWidth(), getHeight(), paintBg);

        drawGameArea(canvas);

        drawPlayers(canvas);

        drawCorners(canvas);

        if (overlayBlock != null) {
            drawOverlayBlock(canvas);
        }
    }

    private void drawGameArea(Canvas canvas) {
        Map map = Map.getInstance();
        // border
        canvas.drawRect(0, 0, getWidth(), getHeight(), paintLine);

        for(int i=0; i<map.getLineSize(); ++i) {        // height-1 horizontal lines
            canvas.drawLine(0, i* ((float)getHeight() / map.getLineSize()), getWidth(), i* ((float)getHeight() / map.getLineSize()),
                    paintLine);

        }

        for(int i=0; i<map.getLineSize(); ++i) {
            canvas.drawLine(i*((float)getWidth() / map.getLineSize()), 0, i*((float)getWidth() / map.getLineSize()), getHeight(),
                    paintLine);
        }
    }

    private void drawPlayers(Canvas canvas) {
        Map map = Map.getInstance();

        for(int i=0; i<map.getLineSize(); ++i){
            for(int j=0; j<map.getLineSize(); ++j){
                int cell = map.getCell(i,j);
                if(cell>0) {
                    paintRect.setColor(getColor(cell));
                    int x = i * (getWidth() / map.getLineSize());
                    int y = j * (getHeight() / map.getLineSize());
                    Rect rect = new Rect(x, y, x + (int)((float)getWidth() / map.getLineSize()), y + (int)((float)getHeight() / map.getLineSize()));
                    canvas.drawRect(rect, paintRect);
                }
            }
        }
    }

    private void drawCorners(Canvas canvas){
        paintRect.setColor(Color.GRAY);
        paintRect.setAlpha(160);
        Map map = Map.getInstance();
        for (Point corner : corners) {
            float x = corner.x * ((float)getWidth() / map.getLineSize());
            float y = corner.y * ((float)getHeight() / map.getLineSize());
            Rect rect = new Rect((int)x, (int)y, (int)(x + ((float)getWidth() / map.getLineSize())), (int)(y + ((float)getHeight() / map.getLineSize())));
            canvas.drawRect(rect, paintRect);
        }
    }


    private void drawOverlayBlock(Canvas canvas) {
        Map map = Map.getInstance();

        int drawColor = (map.isPlaceable(overlayBlock, corners, overlayPos))? Color.GREEN:Color.RED;
        paintOverlay.setColor(drawColor);

        for(int i = 0; i<overlayBlock.getSize(); ++i){
            Point temp = new Point(overlayPos.x +  overlayBlock.getPoint(i).x, overlayPos.y + overlayBlock.getPoint(i).y);
            float x = temp.x * ((float)getWidth() / map.getLineSize());
            float y = temp.y * ((float)getHeight() / map.getLineSize());
            Rect rect = new Rect((int)x, (int)y, (int)(x + ((float)getWidth() / map.getLineSize())), (int)(y + ((float)getHeight() / map.getLineSize())));
            canvas.drawRect(rect, paintOverlay);
        }

    }

    private int getColor(int cell) {
        switch (cell){
            case 1:
                return PlayerConstants.PLAYER_ONE;
            case 2:
                return PlayerConstants.PLAYER_TWO;
            case 3:
                return PlayerConstants.PLAYER_THREE;
            case 4:
                return PlayerConstants.PLAYER_FOUR;
            default:
                return Color.CYAN;
        }
    }


    public void setCorners(ArrayList<Point> _corners){
        corners = _corners;
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        return gestureDetector.onTouchEvent(event);
    }

    public void setWasTouchedListener(BoardTouchListener listener){
        listeners.add(listener);
    }

    @Override
    public void onMeasure(int widthMeasureSpec, int heightMeasureSpec)
    {
        int halfWidth = (MeasureSpec.getSize(widthMeasureSpec)) - 5;

        this.setMeasuredDimension(halfWidth, halfWidth);
    }

    // when the player have choosen a block, but havent put down yes
    public void setOverlayBlock(Block block, Point pt) {
        overlayBlock = block;
        overlayPos = pt;
    }
}

