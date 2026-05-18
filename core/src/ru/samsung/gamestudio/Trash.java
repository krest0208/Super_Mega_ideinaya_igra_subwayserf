package ru.samsung.gamestudio;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;

public class Trash extends Obstacle {

    private Texture texture;
    private float speed = 4f;

    public Trash(float x, float y) {
        this.x = x;
        this.y = y;

        texture = new Texture("decor/trash can.png");
    }

    @Override
    public void update(float delta) {
        x -= speed;
    }

    @Override
    public void draw(SpriteBatch batch) {
        batch.draw(texture, x, y, 100, 100);
    }

    @Override
    public void dispose() {
        texture.dispose();
    }
}