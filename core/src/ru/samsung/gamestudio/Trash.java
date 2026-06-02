package ru.samsung.gamestudio;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;

public class Trash extends Obstacle {

    private Texture texture;
    private float speed = 240f;

    public Trash(float x, float y) {
        this.x = x;
        this.y = y;
        setSize(100, 100);
        // УМЕНЬШАЕМ ХИТБОКС для упрощения
        setHitBox(25, 15, 50, 65);  // Было: 17, 11, 66, 82
        setResetX(x + 2600);

        texture = new Texture("decor/trash can.png");
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