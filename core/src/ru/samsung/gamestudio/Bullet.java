package ru.samsung.gamestudio.Object;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.*;

public class Bullet {
    private static final float PPM = 100f;
    private Body body;
    private Texture texture;
    private boolean toDestroy = false;
    private boolean isDestroyed = false;

    public Bullet(World world, float startX, float startY, boolean directionLeft) {
        texture = new Texture("weapons/bullet.png");

        BodyDef bodyDef = new BodyDef();
        bodyDef.type = BodyDef.BodyType.DynamicBody;
        bodyDef.position.set(startX / PPM, startY / PPM);
        bodyDef.gravityScale = 0;

        body = world.createBody(bodyDef);
        body.setUserData(this);

        CircleShape shape = new CircleShape();
        shape.setRadius(8f / PPM);

        FixtureDef fixtureDef = new FixtureDef();
        fixtureDef.shape = shape;
        fixtureDef.isSensor = true;

        body.createFixture(fixtureDef);
        shape.dispose();

        float speed = directionLeft ? -8.0f : 8.0f;
        body.setLinearVelocity(new Vector2(speed, 0));
    }

    public void update() {
        if (isDestroyed) return;
        float screenX = body.getPosition().x * PPM;
        if (screenX < 0 || screenX > 1280) {
            destroy();
        }
    }

    public void draw(SpriteBatch batch) {
        if (isDestroyed) return;
        batch.draw(texture,
                (body.getPosition().x * PPM) - 8,
                (body.getPosition().y * PPM) - 8,
                16, 16
        );
    }

    public void destroy() {
        toDestroy = true;
    }

    public boolean isToDestroy() { return toDestroy; }
    public boolean isDestroyed() { return isDestroyed; }

    public void disposeWorldBody(World world) {
        if (!isDestroyed && body != null) {
            world.destroyBody(body);
            body = null;
            if (texture != null) texture.dispose();
            isDestroyed = true;
        }
    }
}
