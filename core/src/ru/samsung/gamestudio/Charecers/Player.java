package ru.samsung.gamestudio;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;

public class Player {

    float x, y;
    float velocityY;
    float gravity = -800;

    Texture idle;
    Texture jump;
    Texture fall;
    Texture[] run;

    int runFrame;
    float animationTimer;

    enum State {
        IDLE,
        RUN,
        JUMP,
        FALL
    }

    State state = State.RUN;

    public Player() {
        run = new Texture[10];

        for (int i = 0; i < 10; i++) {
            run[i] = new Texture("run_p.png" + (i + 1) + ".png");
        }

        idle = new Texture('static_p.png');
        jump = new Texture('jump_p.png');
        fall = new Texture("slide2_p.png');

        x = 100;
        y = 200;
    }
    public void update(float delta) {
        velocityY += gravity * delta;
        y += velocityY * delta;

        if (velocityY > 0) {
            state = State.JUMP;
        } else {
            state = State.FALL;
        }

        if (y <= 100) {
            y = 100;
            velocityY = 0;
            state = State.RUN;
        }

        animationTimer += delta;

        if (animationTimer > 0.1f) {
            runFrame++;
            if (runFrame >= 10) runFrame = 0;
            animationTimer = 0;
        }
    }
    public void jump() {
        velocityY = 400;
    }
    public void draw(SpriteBatch batch) {
        Texture current;

        switch (state) {
            case RUN:
                current = run[runFrame];
                break;
            case JUMP:
                current = jump;
                break;
            case FALL:
                current = fall;
                break;
            default:
                current = idle;
        }

        batch.draw(current, x, y);
    }