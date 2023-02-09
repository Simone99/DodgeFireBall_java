package com.example.dodgefireball;

import android.graphics.Bitmap;
import android.graphics.Canvas;

public class RecordText extends GameObject {

    private int nFireballs;
    private Counter counter;

    public RecordText(GameSurface gameSurface, Bitmap image, int x, int y, int nFireballs, Counter counter) {
        super(image, 1, 1, x, y);
        this.nFireballs = nFireballs;
        this.counter = counter;
        this.counter.setNumberFireBalls(nFireballs);
        this.counter.update();
    }

    public void draw(Canvas canvas) {
        canvas.drawBitmap(image, x, y, null);
        counter.draw(canvas);
    }
}
