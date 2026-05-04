package ru.samsung.gamestudio.Object;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.*;

public class Ground {

    private static final float PPM = 100f;

    private Texture texture;

    private float y;
    private float width;
    private float height;
    private float speed;
    private float textureWidth;

    private float x1;
    private float x2;

    private Body body1;
    private Body body2;

    private World world;

    public Ground(World world, float x, float y, float width, float height, float speed) {
        if (world == null) {
            throw new IllegalArgumentException("World не должен быть null. Сначала создай World в ScreenGame.");
        }

        this.world = world;

        this.texture = new Texture("Object/Ground1.png");

        this.x1 = x;
        this.x2 = x + width;

        this.y = y;
        this.width = width;
        this.height = height;
        this.speed = speed;
        this.textureWidth = width;

        body1 = createPhysicsBody(x1, y);
        body2 = createPhysicsBody(x2, y);
    }

    private Body createPhysicsBody(float x, float y) {
        BodyDef bodyDef = new BodyDef();

        bodyDef.type = BodyDef.BodyType.StaticBody;

        bodyDef.position.set(
                (x + width / 2f) / PPM,
                (y + height / 2f) / PPM
        );

        Body body = world.createBody(bodyDef);
        body.setUserData(this);

        PolygonShape shape = new PolygonShape();

        shape.setAsBox(
                width / 2f / PPM,
                height / 2f / PPM
        );

        FixtureDef fixtureDef = new FixtureDef();
        fixtureDef.shape = shape;
        fixtureDef.friction = 0.8f;
        fixtureDef.restitution = 0.1f;

        body.createFixture(fixtureDef);

        shape.dispose();

        return body;
    }

    public void update(float deltaTime) {
        x1 -= speed * deltaTime;
        x2 -= speed * deltaTime;

        if (x1 + textureWidth < 0) {
            x1 = x2 + textureWidth;
        }

        if (x2 + textureWidth < 0) {
            x2 = x1 + textureWidth;
        }

        updateBodyPosition(body1, x1);
        updateBodyPosition(body2, x2);
    }

    private void updateBodyPosition(Body body, float x) {
        if (body != null) {
            body.setTransform(
                    new Vector2(
                            (x + width / 2f) / PPM,
                            (y + height / 2f) / PPM
                    ),
                    0
            );
        }
    }

    public void draw(SpriteBatch batch) {
        batch.draw(texture, x1, y, width, height);
        batch.draw(texture, x2, y, width, height);
    }

    public void dispose() {
        if (texture != null) {
            texture.dispose();
        }

        if (body1 != null && world != null) {
            world.destroyBody(body1);
            body1 = null;
        }

        if (body2 != null && world != null) {
            world.destroyBody(body2);
            body2 = null;
        }
    }
}
