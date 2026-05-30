package ru.samsung.gamestudio.Charecers;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.*;

public class Player {
    private static final float PPM = 100f;
    public static final float DRAW_WIDTH = 110f;
    public static final float DRAW_HEIGHT = 110f;
    private static final float HITBOX_WIDTH = 58f;
    private static final float HITBOX_HEIGHT = 92f;
    private static final float JUMP_VELOCITY = 5.4f;
    private static final float WALK_SPEED = 3.5f;

    public enum GameMode { RUNNER, PLATFORMER }
    private GameMode currentMode = GameMode.RUNNER;

    private float x, y;
    private float width = DRAW_WIDTH;
    private float height = DRAW_HEIGHT;

    private Texture idle, jump, fall;
    private Texture[] run;
    private int runFrame;
    private float animationTimer;
    private boolean flipX = false;

    private Body body;
    private World world;
    public boolean isGrounded;
    private final Rectangle bounds = new Rectangle();

    public boolean isGrounded() {
        return isGrounded;
    }

    enum State { IDLE, RUN, JUMP, FALL }
    private State state = State.RUN;

    public Player(World world, float x, float y) {
        if (world == null) {
            throw new IllegalArgumentException("World cannot be null");
        }
        this.world = world;
        this.x = x;
        this.y = y;

        loadTextures();
        createPhysicsBody();
    }

    private void loadTextures() {
        run = new Texture[2];
        run[0] = new Texture("character/run_1.png");
        run[1] = new Texture("character/run_2.png");
        idle = new Texture("character/static_p.png");
        jump = new Texture("character/jump_p.png");
        fall = new Texture("character/jump_p.png");
    }

    private void createPhysicsBody() {
        BodyDef bodyDef = new BodyDef();
        bodyDef.type = BodyDef.BodyType.DynamicBody;
        bodyDef.position.set(x / PPM, y / PPM);
        bodyDef.fixedRotation = true;

        body = world.createBody(bodyDef);
        body.setUserData(this);

        CircleShape circleShape = new CircleShape();
        circleShape.setRadius(width / 2.5f / PPM);
        FixtureDef fixtureDef = new FixtureDef();
        fixtureDef.shape = circleShape;
        fixtureDef.density = 1.0f;
        fixtureDef.friction = 0.0f;
        fixtureDef.restitution = 0.0f;

        body.createFixture(fixtureDef);
        circleShape.dispose();

        createGroundSensor();
    }

    private void createGroundSensor() {
        PolygonShape sensorShape = new PolygonShape();
        sensorShape.setAsBox(
                width / 4f / PPM, 5f / PPM,
                new Vector2(0, -height / 2f / PPM), 0
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

        float horizontalSpeed = 0f;
        if (currentMode == GameMode.PLATFORMER) {
            if (Gdx.input.isKeyPressed(Input.Keys.A) || Gdx.input.isKeyPressed(Input.Keys.LEFT)) {
                horizontalSpeed = -WALK_SPEED;
                flipX = true;
            } else if (Gdx.input.isKeyPressed(Input.Keys.D) || Gdx.input.isKeyPressed(Input.Keys.RIGHT)) {
                horizontalSpeed = WALK_SPEED;
                flipX = false;
            }
            body.setLinearVelocity(horizontalSpeed, body.getLinearVelocity().y);
        } else {
            body.setLinearVelocity(0, body.getLinearVelocity().y);
            flipX = false;
        }

        boolean jumpPressed = Gdx.input.isKeyJustPressed(Input.Keys.SPACE) ||
                Gdx.input.isKeyJustPressed(Input.Keys.W) ||
                Gdx.input.isKeyJustPressed(Input.Keys.UP) ||
                Gdx.input.justTouched();

        if (jumpPressed && isGrounded) {
            body.setLinearVelocity(body.getLinearVelocity().x, JUMP_VELOCITY);
            isGrounded = false;
        }

        updateState();
        updateAnimation(delta);
    }

    private void updateState() {
        float velocityY = body.getLinearVelocity().y;
        float velocityX = body.getLinearVelocity().x;

        if (!isGrounded) {
            if (velocityY > 0.1f) state = State.JUMP;
            else if (velocityY < -0.1f) state = State.FALL;
        } else {
            if (currentMode == GameMode.PLATFORMER && Math.abs(velocityX) < 0.1f) {
                state = State.IDLE;
            } else {
                state = State.RUN;
            }
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
            case JUMP: currentTexture = jump; break;
            case FALL: currentTexture = fall; break;
            case RUN:  currentTexture = run[runFrame]; break;
            default:   currentTexture = idle; break;
        }
        batch.draw(
                currentTexture,
                x - width / 2f,
                y - height / 2f,
                width,
                height,
                0, 0,
                currentTexture.getWidth(), currentTexture.getHeight(),
                flipX, false
        );
    }
    public void setGameMode(GameMode mode) {
        this.currentMode = mode;
        if (mode == GameMode.RUNNER) {
            state = State.RUN;
        }
    }

    public GameMode getCurrentMode() {
        return currentMode;
    }

    public void setGrounded(boolean grounded) {
        this.isGrounded = grounded;
    }

    public Body getBody() {
        return body;
    }

    public float getX() {
        return x;
    }

    public float getY() {
        return y;
    }

    public Rectangle getBounds() {
        return bounds.set(
                x - HITBOX_WIDTH / 2f,
                y - height / 2f + 9f,
                HITBOX_WIDTH,
                HITBOX_HEIGHT
        );
    }

    public void dispose() {
        if (idle != null) idle.dispose();
        if (jump != null) jump.dispose();
        if (fall != null) fall.dispose();
        if (run != null) {
            for (Texture t : run) if (t != null) t.dispose();
        }
        if (body != null && world != null) {
            world.destroyBody(body);
        }
    }
}