package ru.samsung.gamestudio;

import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Rectangle;

public abstract class Obstacle {

    protected float x;
    protected float y;
    protected float width;
    protected float height;

    private final Rectangle bounds = new Rectangle();
    private float boundsOffsetX;
    private float boundsOffsetY;
    private float boundsWidth;
    private float boundsHeight;
    private float resetX;

    public Rectangle getBounds() {
        return bounds.set(
                x + boundsOffsetX,
                y + boundsOffsetY,
                boundsWidth,
                boundsHeight
        );
    }

    protected void setSize(float width, float height) {
        this.width = width;
        this.height = height;
        setHitBox(0, 0, width, height);
    }

    protected void setHitBox(
            float offsetX,
            float offsetY,
            float boundsWidth,
            float boundsHeight
    ) {
        this.boundsOffsetX = offsetX;
        this.boundsOffsetY = offsetY;
        this.boundsWidth = boundsWidth;
        this.boundsHeight = boundsHeight;
    }

    protected void moveLeft(float speed, float delta) {
        x -= speed * delta;
    }

    protected void setResetX(float resetX) {
        this.resetX = resetX;
    }

    protected boolean resetIfOffScreen() {
        if (x + width >= 0) {
            return false;
        }

        x = resetX;
        return true;
    }

    public abstract void update(float delta);

    public abstract void draw(SpriteBatch batch);

    public abstract void dispose();
}
