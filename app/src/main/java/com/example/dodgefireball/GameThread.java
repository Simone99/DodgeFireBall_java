package com.example.dodgefireball;

import android.graphics.Canvas;
import android.view.SurfaceHolder;

public class GameThread extends Thread {
    private boolean running;
    private long lastTimeSpawned;
    private final GameSurface gameSurface;
    private final SurfaceHolder surfaceHolder;

    public GameThread(GameSurface gameSurface, SurfaceHolder surfaceHolder) {
        this.gameSurface = gameSurface;
        this.surfaceHolder = surfaceHolder;
        lastTimeSpawned = System.nanoTime();

    }

    @Override
    public void run() {
        long startTime = System.nanoTime();

        while (running) {
            Canvas canvas = null;
            try {
                // Get Canvas from Holder and lock it.
                canvas = this.surfaceHolder.lockCanvas();

                // Synchronized
                synchronized (canvas) {
                    if ((System.nanoTime() - lastTimeSpawned) / 1000000 >= 10000) {
                        gameSurface.spawnNewFireBall();
                        lastTimeSpawned = System.nanoTime();
                    }
                    this.gameSurface.update();
                    this.gameSurface.draw(canvas);
                }
            } catch (Exception e) {
                // Do nothing.
            } finally {
                if (canvas != null) {
                    // Unlock Canvas.
                    this.surfaceHolder.unlockCanvasAndPost(canvas);
                }
            }
            long now = System.nanoTime();
            // Interval to redraw game
            // (Change nanoseconds to milliseconds)
            long waitTime = (now - startTime) / 1000000;
            if (waitTime < 10) {
                waitTime = 10; // Millisecond.
            }
            System.out.print(" Wait Time=" + waitTime);

            try {
                // Sleep.
                sleep(waitTime);
            } catch (InterruptedException ignored) {

            }
            startTime = System.nanoTime();
            System.out.print(".");
        }
    }

    public void setRunning(boolean running) {
        this.running = running;
    }

    public void setRunning(boolean running, long lastTimeSpawned) {
        this.running = running;
        this.lastTimeSpawned = lastTimeSpawned;
    }
}
