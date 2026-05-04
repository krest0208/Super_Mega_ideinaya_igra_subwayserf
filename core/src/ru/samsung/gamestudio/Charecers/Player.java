package ru.samsung.gamestudio.Charecers;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.*;

public class Player {

    private static final float PPM = 100f;

    private float x;
    private float y;

    private float width = 80;
    private float height = 80;

    private Texture idle;
    private Texture jump;
    private Texture fall;
    private Texture[] run;

    private int runFrame;
    private float animationTimer;

    private Body body;
    private World world;

    private boolean isGrounded;

    enum State {
        IDLE, RUN, JUMP, FALL
    }

    private State state = State.RUN;

    public Player(World world, float x, float y) {
        if (world == null) {
            throw new IllegalArgumentException("World не должен быть null. Сначала создай World в ScreenGame, потом передавай его в Player.");
        }

        this.world = world;
        this.x = x;
        this.y = y;

        loadTextures();
        createPhysicsBody();
    }

    private void loadTextures() {
        run = new Texture[10];

        for (int i = 0; i < run.length; i++) {
            run[i] = new Texture("character/run_p.png");
        }

        idle = new Texture("character/static_p.png");
        jump = new Texture("character/jump_p.png");
        fall = new Texture("character/slide_p.png");
    }

    private void createPhysicsBody() {
        BodyDef bodyDef = new BodyDef();
        bodyDef.type = BodyDef.BodyType.DynamicBody;
        bodyDef.position.set(x / PPM, y / PPM);
        bodyDef.fixedRotation = true;

        body = world.createBody(bodyDef);
        body.setUserData(this);

        CircleShape circleShape = new CircleShape();
        circleShape.setRadius(width / 2f / PPM);

        FixtureDef fixtureDef = new FixtureDef();
        fixtureDef.shape = circleShape;
        fixtureDef.density = 1.0f;
        fixtureDef.friction = 0.5f;
        fixtureDef.restitution = 0.1f;

        body.createFixture(fixtureDef);

        circleShape.dispose();

        createGroundSensor();
    }

    private void createGroundSensor() {
        PolygonShape sensorShape = new PolygonShape();

        sensorShape.setAsBox(
                width / 3f / PPM,
                5f / PPM,
                new Vector2(0, -height / 2f / PPM),
                0
        );

        FixtureDef sensorDef = new FixtureDef();
        sensorDef.shape = sensorShape;
        sensorDef.isSensor = true;

        Fixture sensorFixture = body.createFixture(sensorDef);
        sensorFixture.setUserData("player_ground_sensor");

        sensorShape.dispose();
    }

    public void update(float delta) {
        Vector2 pos = body.getPosition();

        x = pos.x * PPM;
        y = pos.y * PPM;

        boolean jumpPressed =
                Gdx.input.isKeyJustPressed(Input.Keys.SPACE)
                        || Gdx.input.isKeyJustPressed(Input.Keys.W)
                        || Gdx.input.justTouched();

        if (jumpPressed && isGrounded) {
            body.applyLinearImpulse(
                    new Vector2(0, 6f),
                    body.getWorldCenter(),
                    true
            );

            isGrounded = false;
        }

        updateState();
        updateAnimation(delta);
    }

    private void updateState() {
        float velocityY = body.getLinearVelocity().y;

        if (!isGrounded) {
            if (velocityY > 0) {
                state = State.JUMP;
            } else {
                state = State.FALL;
            }
        } else {
            state = State.RUN;
        }
    }

    private void updateAnimation(float delta) {
        if (state == State.RUN) {
            animationTimer += delta;

            if (animationTimer >= 0.1f) {
                runFrame = (runFrame + 1) % run.length;
                animationTimer = 0;
            }
        } else {
            animationTimer = 0;
            runFrame = 0;
        }
    }


    public void draw(SpriteBatch batch) {
        Texture currentTexture;

        switch (state) {
            case RUN:
                currentTexture = run[runFrame];
                break;

            case JUMP:
                currentTexture = jump;
                break;

            case FALL:
                currentTexture = fall;
                break;

            case IDLE:
            default:
                currentTexture = idle;
                break;
        }

        batch.draw(
                currentTexture,
                x - width / 2f,
                y - height / 2f,
                width,
                height
        );
    }

    public void setGrounded(boolean grounded) {
        this.isGrounded = grounded;
    }

    public boolean isGrounded() {
        return isGrounded;
    }

    public Body getBody() {
        return body;
    }

    public void dispose() {
        if (idle != null) {
            idle.dispose();
        }

        if (jump != null) {
            jump.dispose();
        }

        if (fall != null) {
            fall.dispose();
        }

        if (run != null) {
            for (Texture texture : run) {
                if (texture != null) {
                    texture.dispose();
                }
            }
        }

        if (body != null && world != null) {
            world.destroyBody(body);
            body = null;
        }
    }
}

