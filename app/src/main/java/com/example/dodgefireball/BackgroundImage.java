package com.example.dodgefireball;

import android.graphics.Bitmap;
import android.graphics.Canvas;

public class BackgroundImage extends GameObject {

    private Bitmap image;

    public BackgroundImage(GameSurface gameSurface, Bitmap image, int x, int y) {
        super(image, 1, 1, x, y);
        this.image = image;
    }

    public void draw(Canvas canvas) {
        canvas.drawBitmap(image, x, y, null);
    }
}
