package ru.samsung.gamestudio.Charecers;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.*;

public class Boss {
    protected static final float PPM = 100f;

    protected Body body;
    protected int hp;
    protected int maxHp;
    protected float x, y;
    protected float width, height;
    protected boolean isDead = false;
    protected boolean isFacingRight = true;

    protected Texture[] idleFrames;
    protected int currentFrame = 0;
    protected float animationTimer = 0;
    protected float animationSpeed = 0.1f;

    protected float shootTimer = 0;
    protected float shootDelay = 1.2f;  // Стреляет каждые 1.2 секунды
    protected float speed = 0.3f;

    public Boss(World world, float x, float y) {
        this.width = 140f;
        this.height = 130f;
        this.hp = 30;
        this.maxHp = 30;

        BodyDef bodyDef = new BodyDef();
        bodyDef.type = BodyDef.BodyType.DynamicBody;
        bodyDef.position.set(x / PPM, y / PPM);
        bodyDef.fixedRotation = true;

        body = world.createBody(bodyDef);
        body.setUserData(this);

        PolygonShape shape = new PolygonShape();
        shape.setAsBox(width / 2f / PPM, height / 2f / PPM);

        FixtureDef fixtureDef = new FixtureDef();
        fixtureDef.shape = shape;
        fixtureDef.density = 1.0f;
        fixtureDef.friction = 0.5f;

        body.createFixture(fixtureDef);
        shape.dispose();

        idleFrames = new Texture[7];
        for (int i = 0; i < 7; i++) {
            idleFrames[i] = new Texture("monsetrs/boss" + (i + 1) + ".png");
        }
    }

    public void update(float delta, Vector2 playerPosition) {
        if (isDead) {
            updateAnimation(delta);
            updatePosition();
            return;
        }

        updatePosition();

        float distance = playerPosition.x - x;
        float absDistance = Math.abs(distance);

        if (absDistance > 300f) {
            if (distance > 0) {
                body.setLinearVelocity(speed, body.getLinearVelocity().y);
                isFacingRight = true;
            } else {
                body.setLinearVelocity(-speed, body.getLinearVelocity().y);
                isFacingRight = false;
            }
        } else {
            body.setLinearVelocity(0, body.getLinearVelocity().y);
        }

        shootTimer += delta;
        updateAnimation(delta);
    }

    protected void updateAnimation(float delta) {
        if (idleFrames == null) return;

        animationTimer += delta;
        if (animationTimer >= animationSpeed) {
            animationTimer = 0;
            currentFrame++;
            if (currentFrame >= idleFrames.length) {
                currentFrame = 0;
            }
        }
    }

    protected void updatePosition() {
        if (body != null) {
            Vector2 pos = body.getPosition();
            x = pos.x * PPM;
            y = pos.y * PPM;
        }
    }

    public void takeDamage(int amount) {
        if (isDead) return;
        hp -= amount;
        System.out.println("Boss hit! HP: " + hp + "/" + maxHp);
        if (hp <= 0) {
            isDead = true;
            System.out.println("Boss defeated!");
        }
    }

    public boolean canShoot() {
        return shootTimer >= shootDelay;
    }

    public void resetShootTimer() {
        shootTimer = 0;
    }

    public Rectangle getBounds() {
        return new Rectangle(x - width / 2f, y - height / 2f, width, height);
    }

    public void draw(SpriteBatch batch) {
        if (isDead || idleFrames == null) return;

        Texture currentTexture = idleFrames[currentFrame];
        if (!isFacingRight) {
            batch.draw(currentTexture, x + width / 2f, y - height / 2f, -width, height);
        } else {
            batch.draw(currentTexture, x - width / 2f, y - height / 2f, width, height);
        }
    }

    public boolean isDead() { return isDead; }
    public float getX() { return x; }
    public float getY() { return y; }
    public int getHp() { return hp; }
    public int getMaxHp() { return maxHp; }
    public boolean isFacingRight() { return isFacingRight; }

    public float getShootX() {
        return isFacingRight ? x + width / 2f : x - width / 2f;
    }
    public float getShootY() {
        return y;
    }
    public int getShootDirection() {
        return isFacingRight ? 1 : -1;
    }

    public void dispose(World world) {
        if (body != null) {
            world.destroyBody(body);
            body = null;
        }
        if (idleFrames != null) {
            for (Texture t : idleFrames) {
                if (t != null) t.dispose();
            }
        }
    }
}