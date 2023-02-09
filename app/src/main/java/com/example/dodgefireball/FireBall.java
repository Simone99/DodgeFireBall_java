package com.example.dodgefireball;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.os.Build;

import java.util.LinkedList;
import java.util.List;
import java.util.stream.Collectors;

public class FireBall extends GameObject {

    private enum direction {SO, O, NO, N, NE, E, SE, S}

    private direction rowUsing = direction.SE;
    private final Bitmap[] images;

    // Velocity of game character (pixel/millisecond)
    public static final float VELOCITY = 0.2f;

    private int movingVectorX = 10;
    private int movingVectorY = 5;
    private long lastDrawNanoTime = -1;
    private boolean collisionActive;
    private final GameSurface gameSurface;

    public FireBall(GameSurface gameSurface, Bitmap image, int x, int y) {
        super(image, 8, 1, x, y);
        this.gameSurface = gameSurface;
        this.images = new Bitmap[rowCount];

        for (int row = 0; row < rowCount; row++) {
            images[row] = createSubImageAt(row, 0);
        }

    }

    public void update(List<FireBall> listFireballs) {
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
        } else if (listFireballs.size() >= 2) {
            List<FireBall> tmp = new LinkedList<>();
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                if (listFireballs.stream().noneMatch(this::checkOverlapFireBall)) {
                    this.setCollisionActive(false);
                } else {
                    tmp = listFireballs.stream().filter(f -> checkOverlapFireBall(f) && !f.isCollisionActive()
                    ).collect(Collectors.toList());
                }
            } else {
                boolean all = true;
                for (FireBall f : listFireballs) {
                    if (checkOverlapFireBall(f)) {
                        all = false;
                    }
                }
                if (all) {
                    this.setCollisionActive(false);
                } else {
                    tmp = new LinkedList<>();
                    for (FireBall f : listFireballs) {
                        if (checkOverlapFireBall(f) && !f.isCollisionActive()) {
                            tmp.add(f);
                        }
                    }
                }
            }
            if (tmp.size() != 0 && !this.isCollisionActive()) {
                this.setCollisionActive(true);
                for (FireBall f : tmp) {
                    setNewDirection(f);
                }
            }
        }

        // rowUsing
        if (movingVectorX > 0) {
            if (movingVectorY > 0 && Math.abs(movingVectorX) < 0.75 * Math.abs(movingVectorY)) {
                this.rowUsing = direction.S;
            } else if (movingVectorY > 0 && Math.abs(movingVectorX) >= 0.75 * Math.abs(movingVectorY) && Math.abs(movingVectorX) <= 1.35 * Math.abs(movingVectorY)) {
                this.rowUsing = direction.SE;
            } else if (movingVectorY < 0 && Math.abs(movingVectorX) < 0.75 * Math.abs(movingVectorY)) {
                this.rowUsing = direction.N;
            } else if (movingVectorY < 0 && Math.abs(movingVectorX) >= 0.75 * Math.abs(movingVectorY) && Math.abs(movingVectorX) <= 1.35 * Math.abs(movingVectorY)) {
                this.rowUsing = direction.NE;
            } else {
                this.rowUsing = direction.E;
            }
        } else {
            if (movingVectorY > 0 && Math.abs(movingVectorX) < 0.75 * Math.abs(movingVectorY)) {
                this.rowUsing = direction.S;
            } else if (movingVectorY > 0 && Math.abs(movingVectorX) >= 0.75 * Math.abs(movingVectorY) && Math.abs(movingVectorX) <= 1.35 * Math.abs(movingVectorY)) {
                this.rowUsing = direction.SO;
            } else if (movingVectorY < 0 && Math.abs(movingVectorX) < 0.75 * Math.abs(movingVectorY)) {
                this.rowUsing = direction.N;
            } else if (movingVectorY < 0 && Math.abs(movingVectorX) >= 0.75 * Math.abs(movingVectorY) && Math.abs(movingVectorX) <= 1.35 * Math.abs(movingVectorY)) {
                this.rowUsing = direction.NO;
            } else {
                this.rowUsing = direction.O;
            }
        }
    }

    public void draw(Canvas canvas) {
        canvas.drawBitmap(images[rowUsing.ordinal()], x, y, null);
        // Last draw time.
        this.lastDrawNanoTime = System.nanoTime();
    }

    public void setMovingVector(int movingVectorX, int movingVectorY) {
        this.movingVectorX = movingVectorX;
        this.movingVectorY = movingVectorY;
    }

    public int[] getMovingVector() {
        return new int[]{movingVectorX, movingVectorY};
    }

    public boolean isCollisionActive() {
        return collisionActive;
    }

    public void setCollisionActive(boolean collisionActive) {
        this.collisionActive = collisionActive;
    }

    private boolean checkOverlapFireBall(FireBall f) {
        int xOvest = this.getX();
        int yNord = this.getY();
        int xEst = this.getX() + this.getWidth();
        int ySud = this.getY() + this.getHeight();
        return this.gameSurface.checkOverlapTwoRectangles(f.getX(), xOvest, f.getX() + f.getWidth(), xEst, f.getY(), yNord, f.getY() + f.getHeight(), ySud);
    }

    private void setNewDirection(FireBall f) {
        int xOvest = this.getX();
        int yNord = this.getY();
        int xEst = this.getX() + this.getWidth();
        int ySud = this.getY() + this.getHeight();
        boolean NO = f.getX() < xOvest && xOvest < f.getX() + f.getWidth() && f.getY() < yNord && yNord < f.getY() + f.getHeight();
        boolean NE = f.getX() < xEst && xEst < f.getX() + f.getWidth() && f.getY() < yNord && yNord < f.getY() + f.getHeight();
        boolean SE = f.getX() < xEst && xEst < f.getX() + f.getWidth() && f.getY() < ySud && ySud < f.getY() + f.getHeight();
        boolean SO = f.getX() < xOvest && xOvest < f.getX() + f.getWidth() && f.getY() < ySud && ySud < f.getY() + f.getHeight();
        int[] movingVector = f.getMovingVector();
        f.setCollisionActive(true);
        if (Math.signum(this.movingVectorX) == Math.signum(movingVector[0]) && Math.signum(this.movingVectorY) == Math.signum(movingVector[1])) {
            if (Math.signum(this.movingVectorX) > 0 && Math.signum(this.movingVectorY) > 0 || Math.signum(this.movingVectorX) > 0 && Math.signum(this.movingVectorY) < 0) {
                if ((NE && Math.abs(f.getX() - xEst) < Math.abs(f.getY() + f.getHeight() - yNord)) || (SE && Math.abs(f.getX() - xEst) < Math.abs(f.getY() - ySud))) {
                    this.movingVectorX = -this.movingVectorX;
                } else if ((NE && Math.abs(f.getX() - xEst) == Math.abs(f.getY() + f.getHeight() - yNord)) || (SE && Math.abs(f.getX() - xEst) == Math.abs(f.getY() - ySud))) {
                    this.movingVectorX = -this.movingVectorX;
                    this.movingVectorY = -this.movingVectorY;
                } else if ((NE && Math.abs(f.getX() - xEst) > Math.abs(f.getY() + f.getHeight() - yNord)) || (SE && Math.abs(f.getX() - xEst) > Math.abs(f.getY() - ySud))) {
                    this.movingVectorY = -this.movingVectorY;
                }
            } else if (Math.signum(this.movingVectorX) < 0 && Math.signum(this.movingVectorY) < 0 || Math.signum(this.movingVectorX) < 0 && Math.signum(this.movingVectorY) > 0) {
                if ((NO && Math.abs(f.getX() + f.getWidth() - xOvest) < Math.abs(f.getY() + f.getHeight() - yNord)) || (SO && Math.abs(f.getX() + f.getWidth() - xOvest) < Math.abs(f.getY() - ySud))) {
                    this.movingVectorX = -this.movingVectorX;
                } else if ((NO && Math.abs(f.getX() + f.getWidth() - xOvest) == Math.abs(f.getY() + f.getHeight() - yNord)) || (SO && Math.abs(f.getX() + f.getWidth() - xOvest) == Math.abs(f.getY() - ySud))) {
                    this.movingVectorX = -this.movingVectorX;
                    this.movingVectorY = -this.movingVectorY;
                } else if ((NO && Math.abs(f.getX() + f.getWidth() - xOvest) > Math.abs(f.getY() + f.getHeight() - yNord)) || (SO && Math.abs(f.getX() + f.getWidth() - xOvest) > Math.abs(f.getY() - ySud))) {
                    this.movingVectorY = -this.movingVectorY;
                }
            } else if (Math.signum(this.movingVectorX) == 0) {
                if ((SO || SE) && Math.signum(this.movingVectorY) > 0) {
                    this.movingVectorY = -this.movingVectorY;
                } else if ((NO || NE) && Math.signum(this.movingVectorY) < 0) {
                    this.movingVectorY = -this.movingVectorY;
                }
            } else if (Math.signum(this.movingVectorY) == 0) {
                if ((NO || SO) && Math.signum(this.movingVectorX) < 0) {
                    this.movingVectorX = -this.movingVectorX;
                } else if ((NE || SE) && Math.signum(this.movingVectorX) > 0) {
                    this.movingVectorX = -this.movingVectorX;
                }
            }
        } else if (Math.signum(this.movingVectorX) != Math.signum(movingVector[0]) && Math.signum(this.movingVectorY) == Math.signum(movingVector[1])) {
            this.movingVectorX = -this.movingVectorX;
            f.setMovingVector(-movingVector[0], movingVector[1]);
        } else if (Math.signum(this.movingVectorX) == Math.signum(movingVector[0]) && Math.signum(this.movingVectorY) != Math.signum(movingVector[1])) {
            this.movingVectorY = -this.movingVectorY;
            f.setMovingVector(movingVector[0], -movingVector[1]);
        } else if (Math.signum(this.movingVectorX) != Math.signum(movingVector[0]) && Math.signum(this.movingVectorY) != Math.signum(movingVector[1])) {
            if ((NO && Math.abs(f.getX() + f.getWidth() - xOvest) < Math.abs(f.getY() + f.getHeight() - yNord)) || (NE && Math.abs(f.getX() - xEst) < Math.abs(f.getY() + f.getHeight() - yNord)) || (SE && Math.abs(f.getX() - xEst) < Math.abs(f.getY() - ySud)) || (SO && Math.abs(f.getX() + f.getWidth() - xOvest) < Math.abs(f.getY() - ySud))) {
                this.movingVectorX = -this.movingVectorX;
                f.setMovingVector(-movingVector[0], movingVector[1]);
            } else if ((NO && Math.abs(f.getX() + f.getWidth() - xOvest) == Math.abs(f.getY() + f.getHeight() - yNord)) || (NE && Math.abs(f.getX() - xEst) == Math.abs(f.getY() + f.getHeight() - yNord)) || (SE && Math.abs(f.getX() - xEst) == Math.abs(f.getY() - ySud)) || (SO && Math.abs(f.getX() + f.getWidth() - xOvest) == Math.abs(f.getY() - ySud))) {
                this.movingVectorX = -this.movingVectorX;
                this.movingVectorY = -this.movingVectorY;
                f.setMovingVector(-movingVector[0], movingVector[1]);
                f.setMovingVector(movingVector[0], -movingVector[1]);
            } else if ((NO && Math.abs(f.getX() + f.getWidth() - xOvest) > Math.abs(f.getY() + f.getHeight() - yNord)) || (NE && Math.abs(f.getX() - xEst) > Math.abs(f.getY() + f.getHeight() - yNord)) || (SE && Math.abs(f.getX() - xEst) > Math.abs(f.getY() - ySud)) || (SO && Math.abs(f.getX() + f.getWidth() - xOvest) > Math.abs(f.getY() - ySud))) {
                this.movingVectorY = -this.movingVectorY;
                f.setMovingVector(movingVector[0], -movingVector[1]);
            }
        }
    }
}
