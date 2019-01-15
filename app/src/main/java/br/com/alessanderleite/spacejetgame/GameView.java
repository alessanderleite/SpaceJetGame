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

    // a screenX holder
    int screenX;

    // To count the number of Misses
    int countMisses;

    // indicator that the enemy has just entered the game screen
    boolean flag;

    // an indicator if the game is Over
    private boolean isGameOver;

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

        // setting the countMisses to 0 initially
        countMisses = 0;

        this.screenX = screenX;

        isGameOver = false;
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

        // setting the flag true when the enemy just enters the screen
        if (enemies.getX() == screenX) {
            flag = true;
        }

        enemies.update(player.getSpeed());

        // if collision occurs with player
        if (Rect.intersects(player.getDetectCollision(), enemies.getDetectCollision())) {
            // displaying boom at that location
            boom.setX(enemies.getX());
            boom.setY(enemies.getY());
            //will play a sound at the collision between player and the enemy

            // moving enemy outside the left edge
            enemies.setX(-200);
        }
        else {
            // if the enemy has just entered
            if (flag) {
                // if player's x coordinate is more than the enemies's x coordinate.i.e enemy has just pass across the player
                if (player.getDetectCollision().exactCenterX() >= enemies.getDetectCollision().exactCenterX()) {
                    // increment countMisses
                    countMisses++;

                    // setting the flag false so that the else part is executed only when new enemy enters the screen
                    flag = false;
                    // if no of Misses is equal to 3, then game is over.
                    if (countMisses == 3) {
                        // setting playing false to stop the game.
                        playing = false;
                        isGameOver = true;
                    }
                }
            }
        }

        // updating the friend ships coordinates
        friend.update(player.getSpeed());

        // checking for a collision between player and a friend
        if (Rect.intersects(player.getDetectCollision(), friend.getDetectCollision())) {

            // displaying the boom at the collision
            boom.setX(friend.getX());
            boom.setY(friend.getY());

            // setting the isGameOver true as the game is over
            isGameOver = true;
        }
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
