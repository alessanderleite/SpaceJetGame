package br.com.alessanderleite.spacejetgame;

import android.content.Context;
import android.content.SharedPreferences;
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

    // the score holder
    int score;

    // the high Scores Holder
    int highScore[] = new int[4];

    // Shared Preferences to store the High Scores
    SharedPreferences sharedPreferences;

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

        // setting the score to ) initially
        score = 0;

        // setting the countMisses to 0 initially
        countMisses = 0;

        this.screenX = screenX;

        isGameOver = false;

        sharedPreferences = context.getSharedPreferences("SHAR_PREF_NAME", Context.MODE_PRIVATE);

        // initializing the array high scorres with the previous values
        highScore[0] = sharedPreferences.getInt("score1",0);
        highScore[1] = sharedPreferences.getInt("score2",0);
        highScore[2] = sharedPreferences.getInt("score3",0);
        highScore[3] = sharedPreferences.getInt("score4",0);
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
        // incrementing score as time passes
        score++;

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

            // playing a sound at the collision between player and the enemy

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

                        // Assigning the scores to the highscore integer array
                        for (int i = 0; i < 4; i++) {
                            if (highScore[i] < score) {

                                final int finalI = i;
                                highScore[i] = score;
                                break;
                            }
                        }

                        // storing the scores through shared Preferences
                        SharedPreferences.Editor e = sharedPreferences.edit();
                        for (int i = 0; i < 4; i++) {
                            int j = i + 1;
                            e.putInt("score" + j, highScore[i]);
                        }
                        e.apply();
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

            // setting playing false to stop the game
            playing = false;

            // setting the isGameOver true as the game is over
            isGameOver = true;

            // Assigning the scores to the highscore integer array
            for (int i = 0; i < 4; i++) {
                if (highScore[i] < score) {

                    final int finalI = i;
                    highScore[i] = score;
                    break;
                }
            }

            // Storing the scores through shared Preferences
            SharedPreferences.Editor e = sharedPreferences.edit();
            for (int i = 0; i < 4; i++) {
                int j = i + 1;
                e.putInt("score" + j, highScore[i]);
            }
            e.apply();
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

            // drawing the score on the game screen
            paint.setTextSize(30);
            canvas.drawText("Score: " + score, 100, 50, paint);

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

            // draw game Over when the game is over
            if (isGameOver) {
                paint.setTextSize(150);
                paint.setTextAlign(Paint.Align.CENTER);

                int yPos = (int)((canvas.getHeight() / 2) - ((paint.descent() + paint.ascent()) / 2));
                canvas.drawText("Game Over", canvas.getWidth() / 2, yPos, paint);
            }
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
