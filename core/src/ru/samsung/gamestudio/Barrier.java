package ru.samsung.gamestudio;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;

public class Barrier extends Obstacle {

    private Texture texture;
    private float speed = 240f;

    public Barrier(float x, float y) {
        this.x = x;
        this.y = y;
        setSize(70, 50);
        // УМЕНЬШАЕМ ХИТБОКС
        setHitBox(12, 12, 46, 26);  // Было: 7, 9, 56, 36
        setResetX(x + 2600);

        texture = new Texture("decor/dangerous.png");
    }

    @Override
    public void update(float delta) {
        moveLeft(speed, delta);
        resetIfOffScreen();
    }

    @Override
    public void draw(SpriteBatch batch) {
        batch.draw(texture, x, y, width, height);
    }

    @Override
    public void dispose() {
        texture.dispose();
    }
}