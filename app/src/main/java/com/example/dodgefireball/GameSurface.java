package com.example.dodgefireball;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.media.AudioAttributes;
import android.media.AudioManager;
import android.media.SoundPool;
import android.os.Build;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.LinkedList;
import java.util.List;
import java.util.Random;
import java.util.Scanner;

public class GameSurface extends SurfaceView implements SurfaceHolder.Callback {

    private static final int MAX_STREAMS = 100;
    private GameThread gameThread;
    private Player player;
    private List<FireBall> fireballs;
    private Counter counter;
    private NewRecordText newRecordText;
    private BackgroundImage backgroundImage;
    private RecordText recordText;
    private Explosion finalExplosion;
    private Random rand;
    private File recordFile;
    private int recordBalls;
    private boolean newRecordScored;
    private boolean gameOver;
    private int soundIdExplosion;
    private boolean soundPoolLoaded;
    private SoundPool soundPool;

    public GameSurface(Context context) {
        super(context);
        // Make Game Surface focusable so it can handle events.
        this.setFocusable(true);

        // Sét callback.
        this.getHolder().addCallback(this);
        PrintWriter wr = null;
        recordFile = null;
        Scanner rd;
        try {
            recordFile = new File(context.getFilesDir().getCanonicalPath() + File.separatorChar + "record.txt");
            if (!recordFile.exists()) {
                recordFile.createNewFile();
                wr = new PrintWriter(new FileWriter(recordFile, false), true);
                wr.println(0);
            }
            rd = new Scanner(recordFile);
            recordBalls = rd.nextInt();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (wr != null) {
                wr.close();
            }
        }
        this.initSoundPool();
    }

    private void initSoundPool() {
        // With Android API >= 21.
        if (Build.VERSION.SDK_INT >= 21) {

            AudioAttributes audioAttrib = new AudioAttributes.Builder()
                    .setUsage(AudioAttributes.USAGE_GAME)
                    .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                    .build();

            SoundPool.Builder builder = new SoundPool.Builder();
            builder.setAudioAttributes(audioAttrib).setMaxStreams(MAX_STREAMS);

            this.soundPool = builder.build();
        }
        // With Android API < 21
        else {
            // SoundPool(int maxStreams, int streamType, int srcQuality)
            this.soundPool = new SoundPool(MAX_STREAMS, AudioManager.STREAM_MUSIC, 0);
        }

        // When SoundPool load complete.
        this.soundPool.setOnLoadCompleteListener((soundPool, sampleId, status) -> soundPoolLoaded = true);

        // Load the sound explosion.wav into SoundPool
        this.soundIdExplosion = this.soundPool.load(this.getContext(), R.raw.explosion, 1);
    }

    public void playSoundExplosion() {
        if (this.soundPoolLoaded) {
            float leftVolumn = 0.8f;
            float rightVolumn = 0.8f;
            // Play sound explosion.wav
            this.soundPool.play(this.soundIdExplosion, leftVolumn, rightVolumn, 1, 0, 1f);
        }
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (event.getAction() == MotionEvent.ACTION_DOWN) {
            if (finalExplosion == null) {
                int x = (int) event.getX();
                int y = (int) event.getY();
                int movingVectorX = x - player.getX();
                int movingVectorY = y - player.getY();
                player.setMovingVector(movingVectorX, movingVectorY);
            } else if (finalExplosion.isFinish()) {
                initializeResources(getHolder());
                System.gc();
            }
            return true;
        }
        return false;
    }

    public void update() {
        if (finalExplosion == null && checkOverlapPlayer()) {
            gameOver = true;
            Bitmap tmp = BitmapFactory.decodeResource(this.getResources(), R.drawable.explosion_sequece);
            finalExplosion = new Explosion(this, Bitmap.createScaledBitmap(tmp, tmp.getWidth() / 3, tmp.getHeight() / 3, true), player.getX() - 50, player.getY() - 50);
            int nFB = counter.getNumberFireBalls();
            if (nFB > recordBalls) {
                newRecordScored = true;
                recordBalls = nFB;
                try (PrintWriter wr = new PrintWriter(new FileWriter(recordFile, false), true)) {
                    wr.println(recordBalls);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            return;
        }
        if (!gameOver) {
            player.update();
            for (FireBall tmp : fireballs) {
                tmp.update(fireballs);
            }
        } else {
            if (!finalExplosion.isFinish()) {
                finalExplosion.update();
            } else {
                gameThread.setRunning(false);
            }
        }
    }

    @Override
    public void surfaceCreated(SurfaceHolder holder) {
        initializeResources(holder);
    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {

    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {
        boolean retry = true;
        while (retry) {
            try {
                this.gameThread.setRunning(false);

                // Parent thread must wait until the end of GameThread.
                this.gameThread.join();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            retry = true;
        }
    }

    @Override
    public void draw(Canvas canvas) {
        super.draw(canvas);
        backgroundImage.draw(canvas);
        counter.draw(canvas);
        recordText.draw(canvas);
        if (finalExplosion == null || !finalExplosion.isGreaterHalf()) {
            player.draw(canvas);
        }
        for (FireBall tmp : fireballs) {
            tmp.draw(canvas);
        }
        if (gameOver) {
            finalExplosion.draw(canvas);
            if (newRecordScored && finalExplosion.isFinish())
                newRecordText.draw(canvas);
        }
    }

    public int randInt(int min, int max) {
        return rand.nextInt((max - min) + 1) + min;
    }

    public boolean checkOverlapPlayer() {
        int deltaX = (int) Math.floor(player.getWidth() * 0.21875);
        int deltaY = (int) Math.floor(player.getHeight() * 0.09375);
        int xOvest = player.getX() + deltaX;
        int yNord = player.getY() + deltaY;
        int xEst = player.getX() + player.getWidth() - deltaX;
        int ySud = player.getY() + player.getHeight() - deltaY;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            return fireballs.stream().anyMatch(
                    f -> checkOverlapTwoRectangles(f.getX(), xOvest, f.getX() + f.getWidth(), xEst, f.getY(), yNord, f.getY() + f.getHeight(), ySud)
            );
        } else {
            for (FireBall f : fireballs) {
                if (checkOverlapTwoRectangles(f.getX(), xOvest, f.getX() + f.getWidth(), xEst, f.getY(), yNord, f.getY() + f.getHeight(), ySud)) {
                    return true;
                }
            }
            return false;
        }
    }

    public boolean checkOverlapPlayer(FireBall f) {
        int deltaX = (int) Math.floor(player.getWidth() * 0.21875);
        int deltaY = (int) Math.floor(player.getHeight() * 0.09375);
        int xOvest = player.getX() + deltaX;
        int yNord = player.getY() + deltaY;
        int xEst = player.getX() + player.getWidth() - deltaX;
        int ySud = player.getY() + player.getHeight() - deltaY;
        return checkOverlapTwoRectangles(f.getX(), xOvest, f.getX() + f.getWidth(), xEst, f.getY(), yNord, f.getY() + f.getHeight(), ySud);
    }

    public boolean checkOverlapFireBall(FireBall f) {
        int xOvest = f.getX();
        int yNord = f.getY();
        int xEst = f.getX() + f.getWidth();
        int ySud = f.getY() + f.getHeight();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            return fireballs.stream().anyMatch(
                    f1 -> checkOverlapTwoRectangles(f1.getX(), xOvest, f1.getX() + f1.getWidth(), xEst, f1.getY(), yNord, f1.getY() + f1.getHeight(), ySud)
            );
        } else {
            for (FireBall f1 : fireballs) {
                if (checkOverlapTwoRectangles(f1.getX(), xOvest, f1.getX() + f1.getWidth(), xEst, f1.getY(), yNord, f1.getY() + f1.getHeight(), ySud)) {
                    return true;
                }
            }
            return false;
        }

    }

    public void spawnNewFireBall() {
        Bitmap image = BitmapFactory.decodeResource(this.getResources(), R.drawable.fireball);
        int x = randInt(0, this.getWidth());
        int y = randInt(0, this.getHeight());
        FireBall tmp = new FireBall(this, Bitmap.createScaledBitmap(image, image.getWidth() * 21 / 100, image.getHeight() * 21 / 100, true), x, y);
        while (checkOverlapPlayer(tmp) || checkOverlapFireBall(tmp)) {
            x = randInt(0, this.getWidth());
            y = randInt(0, this.getHeight());
            tmp = new FireBall(this, Bitmap.createScaledBitmap(image, image.getWidth() * 21 / 100, image.getHeight() * 21 / 100, true), x, y);
        }
        tmp.setMovingVector(randInt(0, 20), randInt(0, 20));
        fireballs.add(tmp);
        counter.update();
    }

    public void initializeResources(SurfaceHolder holder) {
        fireballs = new LinkedList<>();
        Bitmap image = BitmapFactory.decodeResource(this.getResources(), R.drawable.player);
        player = new Player(this, Bitmap.createScaledBitmap(image, image.getWidth() * 3 / 4, image.getHeight() * 3 / 4, true), 100, 50);
        image = BitmapFactory.decodeResource(this.getResources(), R.drawable.fireball);
        FireBall tmp = new FireBall(this, Bitmap.createScaledBitmap(image, image.getWidth() * 21 / 100, image.getHeight() * 21 / 100, true), 300, 150);
        fireballs.add(tmp);
        image = BitmapFactory.decodeResource(this.getResources(), R.drawable.numbers);
        counter = new Counter(this, Bitmap.createScaledBitmap(image, image.getWidth() / 5, image.getHeight() / 5, true), 0, 0);
        image = BitmapFactory.decodeResource(this.getResources(), R.drawable.new_record_text);
        image = Bitmap.createScaledBitmap(image, image.getWidth() / 2, image.getHeight() / 2, true);
        newRecordText = new NewRecordText(this, image, this.getWidth() / 2 - image.getWidth() / 2, this.getHeight() / 2 - image.getHeight() / 2);
        image = BitmapFactory.decodeResource(this.getResources(), R.drawable.background);
        backgroundImage = new BackgroundImage(this, Bitmap.createScaledBitmap(image, this.getWidth(), this.getHeight(), true), 0, 0);
        image = BitmapFactory.decodeResource(this.getResources(), R.drawable.numbers);
        image = Bitmap.createScaledBitmap(image, image.getWidth() / 20, image.getHeight() / 20, true);
        Bitmap tmp1 = BitmapFactory.decodeResource(this.getResources(), R.drawable.record_text);
        tmp1 = Bitmap.createScaledBitmap(tmp1, tmp1.getWidth() / 20, tmp1.getHeight() / 20, true);
        Counter counter_tmp = new Counter(this, image, tmp1.getWidth(), counter.getHeight() + 4);
        recordText = new RecordText(this, tmp1, 0, counter.getHeight(), recordBalls, counter_tmp);
        rand = new Random();
        newRecordScored = false;
        gameOver = false;
        finalExplosion = null;
        this.gameThread = new GameThread(this, holder);
        this.gameThread.setRunning(true);
        this.gameThread.start();
    }

    public boolean checkOverlapTwoRectangles(int xOvest1, int xOvest, int xEst1, int xEst, int yNord1, int yNord, int ySud1, int ySud) {
        return (xOvest1 < xOvest && xOvest < xEst1 && yNord1 < yNord && yNord < ySud1) ||
                (xOvest1 < xEst && xEst < xEst1 && yNord1 < yNord && yNord < ySud1) ||
                (xOvest1 < xEst && xEst < xEst1 && yNord1 < ySud && ySud < ySud1) ||
                (xOvest1 < xOvest && xOvest < xEst1 && yNord1 < ySud && ySud < ySud1);
    }

}
