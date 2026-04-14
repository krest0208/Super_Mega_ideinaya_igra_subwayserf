package ru.samsung.gamestudio.Object;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;

public class Ground {
    private Texture texture;
    private float x, y;
    private float width, height;

    public Ground(float x, float y, float width, float height) {
        this.texture = new Texture("Object/Ground1.png");
        this.x = x;
        this.y = y;
        this.width = width;
        this.height = height;
    }

    public void draw(SpriteBatch batch) {
        batch.draw(texture, x, y, width, height);
    }

    public void dispose() {
        texture.dispose();
    }
}