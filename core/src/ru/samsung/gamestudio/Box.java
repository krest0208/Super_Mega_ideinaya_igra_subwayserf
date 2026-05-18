package ru.samsung.gamestudio;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;

public class Box extends Obstacle {

    private Texture texture;
    private float speed = 4f;

    public Box(float x, float y) {
        this.x = x;
        this.y = y;

        texture = new Texture("decor/box.png");
    }

    @Override
    public void update(float delta) {
        x -= speed;
    }

    @Override
    public void draw(SpriteBatch batch) {
        batch.draw(texture, x, y, 50, 50);
    }

    @Override
    public void dispose() {
        texture.dispose();
    }
}
