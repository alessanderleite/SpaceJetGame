package br.com.alessanderleite.spacejetgame;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

import java.util.ArrayList;

public class GameView extends SurfaceView implements Runnable {

    volatile boolean playing;
    private Thread gameThread = null;
    private Player player;

    // These objects will be used for drawing
    private Paint paint;
    private Canvas canvas;
    private SurfaceHolder surfaceHolder;

    // Adding enemies object array
    private Enemy enemies;

    // created a reference of the class Friend
    private Friend friend;

    // Adding an stars list
    private ArrayList<Star> stars = new ArrayList<Star>();

    // defining a boom object to display blast
    private Boom boom;

    public GameView(Context context, int screenX, int screenY) {
        super(context);

        // initializing player object
        player = new Player(context, screenX, screenY);

        // initializing drawing objects
        surfaceHolder = getHolder();
        paint = new Paint();

        // adding 100 stars you may increase the number
        int starNums = 100;
        for (int i = 0; i < starNums; i++) {
            Star s = new Star(screenX, screenY);
            stars.add(s);
        }

        // single enemy initialization
        enemies = new Enemy(context, screenX, screenY);

        // initializing boom object
        boom = new Boom(context);

        // initializing the Friend class object
        friend = new Friend(context, screenX, screenY);
    }

    @Override
    public void run() {
        while (playing) {
            update();
            draw();
            control();
        }

    }

    private void update() {
        // updating player position
        player.update();

        // setting boom outside the screen
        boom.setX(-250);
        boom.setY(-250);

        // Updating the stars with player speed
        for (Star s : stars) {
            s.update(player.getSpeed());
        }

        enemies.update(player.getSpeed());

        // if collision occurs with player
        if (Rect.intersects(player.getDetectCollision(), enemies.getDetectCollision())) {
            // displaying boom at that location
            boom.setX(enemies.getX());
            boom.setY(enemies.getY());

            // moving enemy outside the left edge
            enemies.setX(-200);
        }

        // updating the friend ships coordinates
        friend.update(player.getSpeed());
    }

    private void draw() {
        // checking if surface is valid
        if (surfaceHolder.getSurface().isValid()) {
            canvas = surfaceHolder.lockCanvas();
            canvas.drawColor(Color.BLACK);// drawing a background color for canvas

            paint.setColor(Color.WHITE);// setting the paint color to white to draw the stars
            paint.setTextSize(20);

            // drawing all stars
            for (Star s : stars) {
                paint.setStrokeWidth(s.getStarWidth());
                canvas.drawPoint(s.getX(), s.getY(), paint);
            }

            // Drawing the player
            canvas.drawBitmap(
                    player.getBitmap(),
                    player.getX(),
                    player.getY(),
                    paint
            );

            // drawing the enemies
            canvas.drawBitmap(
                    enemies.getBitmap(),
                    enemies.getX(),
                    enemies.getY(),
                    paint
            );

            // drawing boom image
            canvas.drawBitmap(
                    boom.getBitmap(),
                    boom.getX(),
                    boom.getY(),
                    paint
            );

            // drawing friends image
            canvas.drawBitmap(
                    friend.getBitmap(),
                    friend.getX(),
                    friend.getY(),
                    paint
            );

            surfaceHolder.unlockCanvasAndPost(canvas);
        }
    }

    private void control() {
        try {
            gameThread.sleep(17);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    public void pause() {
        // when the game is paused
        // setting the variable to false
        playing = false;
        try {
            // stopping the thread
            gameThread.join();
        } catch (InterruptedException e) {

        }
    }

    public void resume() {
        // when the game is resumed
        // starting the thread again
        playing = true;
        gameThread = new Thread(this);
        gameThread.start();
    }

    @Override
    public boolean onTouchEvent(MotionEvent motionEvent) {
        switch (motionEvent.getAction() & MotionEvent.ACTION_MASK) {
            case MotionEvent.ACTION_UP:
                //stopping the boosting when screen is released
                player.stopBoosting();
                break;
            case MotionEvent.ACTION_DOWN:
                //boosting the space jet when screen is pressed
                player.setBoosting();
                break;
        }
        return true;
    }
}
