package ru.samsung.gamestudio;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;

public class Barrier extends Obstacle {

    private Texture texture;

    public Barrier(float x, float y) {
        this.x = x;
        this.y = y;

        texture = new Texture("decor/dangerous.png");
    }

    @Override
    public void update(float delta) {
    }

    @Override
    public void draw(SpriteBatch batch) {
    }

    @Override
    public void dispose() {
        texture.dispose();
    }
}