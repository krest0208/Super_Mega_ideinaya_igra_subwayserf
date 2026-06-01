package ru.samsung.gamestudio.Charecers;

import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.*;

public abstract class Monsters_all {
    protected static final float PPM = 100f;
    protected Body body;
    protected int hp;
    protected float x, y;
    protected float width, height;
    protected boolean isDead = false;

    protected Sound hitSound;
    protected Sound deathSound;

    public Monsters_all(World world, float x, float y, int hp, float width, float height) {
        this.x = x;
        this.y = y;
        this.hp = hp;
        this.width = width;
        this.height = height;

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
        fixtureDef.friction = 0.2f;

        body.createFixture(fixtureDef);
        shape.dispose();
    }

    public void takeDamage(int amount) {
        if (isDead) return;
        hp -= amount;
        if (hitSound != null) hitSound.play(0.5f);

        if (hp <= 0) {
            isDead = true;
            if (deathSound != null) deathSound.play(0.8f);
        }
    }

    public abstract void update(float delta, Vector2 playerPosition);
    public abstract void draw(SpriteBatch batch);

    public boolean isDead() { return isDead; }

    public void dispose(World world) {
        if (body != null) {
            world.destroyBody(body);
            body = null;
        }
    }
}