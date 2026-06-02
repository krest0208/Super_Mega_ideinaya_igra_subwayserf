package ru.samsung.gamestudio.Object;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.*;

public class Bullet {
    private static final float PPM = 100f;
    private static final float BULLET_SPEED = 10f;
    private static final float BULLET_SIZE = 15f;

    private Body body;
    private Texture texture;
    private float x, y;
    private boolean toDestroy = false;
    private int direction;
    private float lifeTimer = 0f;
    private static final float MAX_LIFE = 3f;
    private boolean isEnemyBullet;

    public Bullet(World world, float x, float y, int direction, boolean isEnemyBullet) {
        this.direction = direction;
        this.x = x;
        this.y = y;
        this.isEnemyBullet = isEnemyBullet;

        // Создаем заглушку для пули программно
        Pixmap pixmap = new Pixmap((int)BULLET_SIZE, (int)BULLET_SIZE, Pixmap.Format.RGBA8888);
        if (isEnemyBullet) {
            // Красная пуля для врага
            pixmap.setColor(1, 0, 0, 1);  // Красный
        } else {
            // Зеленая/голубая пуля для игрока
            pixmap.setColor(0, 0.8f, 0.2f, 1);  // Зеленый
        }
        pixmap.fill();
        pixmap.fillCircle((int)BULLET_SIZE/2, (int)BULLET_SIZE/2, (int)BULLET_SIZE/2 - 2);
        texture = new Texture(pixmap);
        pixmap.dispose();

        BodyDef bodyDef = new BodyDef();
        bodyDef.type = BodyDef.BodyType.DynamicBody;
        bodyDef.position.set(x / PPM, y / PPM);

        body = world.createBody(bodyDef);
        body.setUserData(this);

        CircleShape shape = new CircleShape();
        shape.setRadius(BULLET_SIZE / 2f / PPM);

        FixtureDef fixtureDef = new FixtureDef();
        fixtureDef.shape = shape;
        fixtureDef.density = 1f;
        fixtureDef.isSensor = true;

        body.createFixture(fixtureDef);
        shape.dispose();

        body.setLinearVelocity(direction * BULLET_SPEED, 0);
    }

    public void update() {
        if (body != null) {
            Vector2 pos = body.getPosition();
            x = pos.x * PPM;
            y = pos.y * PPM;

            lifeTimer += Gdx.graphics.getDeltaTime();

            if (lifeTimer >= MAX_LIFE || x < -100 || x > 1400) {
                toDestroy = true;
            }
        }
    }

    public void draw(SpriteBatch batch) {
        if (!toDestroy && texture != null) {
            batch.draw(texture, x - BULLET_SIZE / 2f, y - BULLET_SIZE / 2f, BULLET_SIZE, BULLET_SIZE);
        }
    }

    public void destroy() {
        toDestroy = true;
    }

    public boolean isToDestroy() {
        return toDestroy;
    }

    public Rectangle getBounds() {
        return new Rectangle(x - BULLET_SIZE / 2f, y - BULLET_SIZE / 2f, BULLET_SIZE, BULLET_SIZE);
    }

    public boolean isEnemyBullet() {
        return isEnemyBullet;
    }

    public void disposeWorldBody(World world) {
        if (body != null) {
            world.destroyBody(body);
            body = null;
        }
        if (texture != null) {
            texture.dispose();
            texture = null;
        }
    }
}