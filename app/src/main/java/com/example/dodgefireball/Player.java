package com.example.dodgefireball;

import android.graphics.Bitmap;
import android.graphics.Canvas;

public class Player extends GameObject {

    private enum WalkingDirection {ROW_TOP_TO_BOTTOM, ROW_RIGHT_TO_LEFT, ROW_LEFT_TO_RIGHT, ROW_BOTTOM_TO_TOP}

    // Row index of Image are being used.
    private WalkingDirection rowUsing = WalkingDirection.ROW_LEFT_TO_RIGHT;

    private int colUsing;

    private Bitmap[] leftToRights;
    private Bitmap[] rightToLefts;
    private Bitmap[] topToBottoms;
    private Bitmap[] bottomToTops;

    // Velocity of game character (pixel/millisecond)
    public static final float VELOCITY = 0.15f;

    private int movingVectorX = 10;
    private int movingVectorY = 5;

    private long lastDrawNanoTime = -1;

    private GameSurface gameSurface;

    public Player(GameSurface gameSurface, Bitmap image, int x, int y) {
        super(image, 4, 4, x, y);
        this.gameSurface = gameSurface;

        this.topToBottoms = new Bitmap[colCount]; // 4
        this.rightToLefts = new Bitmap[colCount]; // 4
        this.leftToRights = new Bitmap[colCount]; // 4
        this.bottomToTops = new Bitmap[colCount]; // 4

        for (int col = 0; col < this.colCount; col++) {
            this.topToBottoms[col] = this.createSubImageAt(WalkingDirection.ROW_TOP_TO_BOTTOM.ordinal(), col);
            this.rightToLefts[col] = this.createSubImageAt(WalkingDirection.ROW_RIGHT_TO_LEFT.ordinal(), col);
            this.leftToRights[col] = this.createSubImageAt(WalkingDirection.ROW_LEFT_TO_RIGHT.ordinal(), col);
            this.bottomToTops[col] = this.createSubImageAt(WalkingDirection.ROW_BOTTOM_TO_TOP.ordinal(), col);
        }
    }

    public Bitmap[] getMoveBitmaps() {
        switch (rowUsing) {
            case ROW_BOTTOM_TO_TOP:
                return this.bottomToTops;
            case ROW_LEFT_TO_RIGHT:
                return this.leftToRights;
            case ROW_RIGHT_TO_LEFT:
                return this.rightToLefts;
            case ROW_TOP_TO_BOTTOM:
                return this.topToBottoms;
            default:
                return null;
        }
    }

    public Bitmap getCurrentMoveBitmap() {
        Bitmap[] bitmaps = this.getMoveBitmaps();
        return bitmaps[this.colUsing];
    }

    public void update() {
        this.colUsing++;
        if (colUsing >= this.colCount) {
            this.colUsing = 0;
        }
        // Current time in nanoseconds
        long now = System.nanoTime();

        // Never once did draw.
        if (lastDrawNanoTime == -1) {
            lastDrawNanoTime = now;
        }
        // Change nanoseconds to milliseconds (1 nanosecond = 1000000 milliseconds).
        int deltaTime = (int) ((now - lastDrawNanoTime) / 1000000);

        // Distance moves
        float distance = VELOCITY * deltaTime;

        double movingVectorLength = Math.sqrt(movingVectorX * movingVectorX + movingVectorY * movingVectorY);

        // Calculate the new position of the game character.
        this.x = x + (int) (distance * movingVectorX / movingVectorLength);
        this.y = y + (int) (distance * movingVectorY / movingVectorLength);

        // When the game's character touches the edge of the screen, then change direction

        if (this.x < 0) {
            this.x = 0;
            this.movingVectorX = -this.movingVectorX;
        } else if (this.x > this.gameSurface.getWidth() - width) {
            this.x = this.gameSurface.getWidth() - width;
            this.movingVectorX = -this.movingVectorX;
        }

        if (this.y < 0) {
            this.y = 0;
            this.movingVectorY = -this.movingVectorY;
        } else if (this.y > this.gameSurface.getHeight() - height) {
            this.y = this.gameSurface.getHeight() - height;
            this.movingVectorY = -this.movingVectorY;
        }

        // rowUsing
        if (movingVectorX > 0) {
            if (movingVectorY > 0 && Math.abs(movingVectorX) < Math.abs(movingVectorY)) {
                this.rowUsing = WalkingDirection.ROW_TOP_TO_BOTTOM;
            } else if (movingVectorY < 0 && Math.abs(movingVectorX) < Math.abs(movingVectorY)) {
                this.rowUsing = WalkingDirection.ROW_BOTTOM_TO_TOP;
            } else {
                this.rowUsing = WalkingDirection.ROW_LEFT_TO_RIGHT;
            }
        } else {
            if (movingVectorY > 0 && Math.abs(movingVectorX) < Math.abs(movingVectorY)) {
                this.rowUsing = WalkingDirection.ROW_TOP_TO_BOTTOM;
            } else if (movingVectorY < 0 && Math.abs(movingVectorX) < Math.abs(movingVectorY)) {
                this.rowUsing = WalkingDirection.ROW_BOTTOM_TO_TOP;
            } else {
                this.rowUsing = WalkingDirection.ROW_RIGHT_TO_LEFT;
            }
        }
    }

    public void draw(Canvas canvas) {
        Bitmap bitmap = this.getCurrentMoveBitmap();
        canvas.drawBitmap(bitmap, x, y, null);
        // Last draw time.
        this.lastDrawNanoTime = System.nanoTime();
    }

    public void setMovingVector(int movingVectorX, int movingVectorY) {
        this.movingVectorX = movingVectorX;
        this.movingVectorY = movingVectorY;
    }
}
