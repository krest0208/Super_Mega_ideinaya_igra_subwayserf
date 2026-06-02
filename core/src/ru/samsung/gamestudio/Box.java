package ru.samsung.gamestudio;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;

public class Box extends Obstacle {

    private Texture texture;
    private float speed = 240f;

    public Box(float x, float y) {
        this.x = x;
        this.y = y;
        setSize(50, 50);
        // НЕМНОГО УМЕНЬШАЕМ ХИТБОКС
        setHitBox(8, 8, 34, 34);  // Было: 4, 5, 42, 41
        setResetX(x + 2600);

        texture = new Texture("decor/box.png");
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