package com.example.dodgefireball;

import android.graphics.Bitmap;
import android.graphics.Canvas;

public class Counter extends GameObject {

    private Bitmap[] digits;
    private Bitmap[] nFireBall;
    private GameSurface gameSurface;
    private int numberFireBalls;

    public Counter(GameSurface gameSurface, Bitmap image, int x, int y) {
        super(image, 1, 10, x, y);
        digits = new Bitmap[10];
        nFireBall = new Bitmap[3];
        for (int i = 0; i < 10; i++) {
            digits[i] = createSubImageAt(0, i);
        }
        this.gameSurface = gameSurface;
        numberFireBalls = 1;
        nFireBall[0] = digits[0];
        nFireBall[1] = digits[0];
        nFireBall[2] = digits[1];
    }

    public void update() {
        numberFireBalls++;
        int hundreds = numberFireBalls / 100;
        int tens = numberFireBalls % 100 / 10;
        int units = numberFireBalls % 100 % 10;
        nFireBall[0] = digits[hundreds];
        nFireBall[1] = digits[tens];
        nFireBall[2] = digits[units];
    }

    public void draw(Canvas canvas) {
        for (int i = 0; i < 3; i++) {
            canvas.drawBitmap(nFireBall[i], x + i * nFireBall[i].getWidth(), y, null);
        }
    }

    public int getNumberFireBalls() {
        return numberFireBalls;
    }

    public void setNumberFireBalls(int numberFireBalls) {
        this.numberFireBalls = numberFireBalls;
    }
}
