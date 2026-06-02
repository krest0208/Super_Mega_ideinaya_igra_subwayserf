package ru.samsung.gamestudio.Object;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.*;

public class Platform {
    private static final float PPM = 100f;

    private Body body;
    private Texture texture;
    private float x, y;
    private float width, height;

    public Platform(World world, float x, float y, float width, float height) {
        this.x = x;
        this.y = y;
        this.width = width;
        this.height = height;

        // Создаем простую текстуру программно, если файла нет
        try {
            texture = new Texture("platform.png");
        } catch (Exception e) {
            // Если текстуры нет, создаем её программно
            com.badlogic.gdx.graphics.Pixmap pixmap = new com.badlogic.gdx.graphics.Pixmap((int)width, (int)height, com.badlogic.gdx.graphics.Pixmap.Format.RGBA8888);
            pixmap.setColor(0.4f, 0.3f, 0.2f, 1); // Коричневый цвет
            pixmap.fill();
            texture = new Texture(pixmap);
            pixmap.dispose();
            System.out.println("Created platform texture programmatically");
        }

        BodyDef bodyDef = new BodyDef();
        bodyDef.type = BodyDef.BodyType.StaticBody;
        bodyDef.position.set(x / PPM, y / PPM);

        body = world.createBody(bodyDef);
        body.setUserData("platform");

        PolygonShape shape = new PolygonShape();
        shape.setAsBox(width / 2f / PPM, height / 2f / PPM);

        FixtureDef fixtureDef = new FixtureDef();
        fixtureDef.shape = shape;
        fixtureDef.friction = 0.5f;

        body.createFixture(fixtureDef);
        shape.dispose();
    }

    public void draw(SpriteBatch batch) {
        if (texture != null) {
            batch.draw(texture, x - width / 2f, y - height / 2f, width, height);
        }
    }

    public void dispose(World world) {
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