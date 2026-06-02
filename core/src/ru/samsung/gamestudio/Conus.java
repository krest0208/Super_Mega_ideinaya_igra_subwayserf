package ru.samsung.gamestudio;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;

public class Conus extends Obstacle {

    float speed;
    Texture[] frames;
    int currentFrame;
    float animationTimer;
    boolean isHit;
    boolean isFinished;

    public Conus(float x, float y) {
        this.x = x;
        this.y = y;
        setSize(70, 70);
        // УМЕНЬШАЕМ ХИТБОКС
        setHitBox(15, 10, 40, 50);  // Было: 9, 7, 52, 58
        setResetX(x + 2600);

        speed = 240;

        frames = new Texture[6];
        frames[0] = new Texture("conus/conus1.png");
        frames[1] = new Texture("conus/conus2.png");
        frames[2] = new Texture("conus/conus3.png");
        frames[3] = new Texture("conus/conus4.png");
        frames[4] = new Texture("conus/conus5.png");
        frames[5] = new Texture("conus/conus6.png");

        currentFrame = 0;
        animationTimer = 0;
        isHit = false;
        isFinished = false;
    }

    public void hit() {
        isHit = true;
    }

    public void update(float delta) {
        moveLeft(speed, delta);

        if (resetIfOffScreen()) {
            currentFrame = 0;
            isHit = false;
            isFinished = false;
        }

        if (!isHit) return;

        animationTimer += delta;
        if (animationTimer > 0.1f) {
            animationTimer = 0;
            currentFrame++;
            if (currentFrame >= frames.length) {
                currentFrame = frames.length - 1;
                isFinished = true;
            }
        }
    }

    public void draw(SpriteBatch batch) {
        batch.draw(frames[currentFrame], x, y, width, height);
    }

    public boolean isFinished() {
        return isFinished;
    }

    public void dispose() {
        for (Texture texture : frames) {
            texture.dispose();
        }
    }
}