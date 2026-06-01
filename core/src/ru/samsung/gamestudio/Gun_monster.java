package ru.samsung.gamestudio.Charecers;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.World;

public class Gun_monster extends Monsters_all {
    private Texture texture;
    private float fireTimer = 0;

    public Gun_monster(World world, float x, float y) {
        super(world, x, y, 5, 90f, 80f); // 5 единиц HP
        texture = new Texture("monsters/acid_spitter.png");
        hitSound = Gdx.audio.newSound(Gdx.files.internal("sounds/monster_hit.ogg"));
        deathSound = Gdx.audio.newSound(Gdx.files.internal("sounds/monster_death.ogg"));
    }

    @Override
    public void update(float delta, Vector2 playerPosition) {
        if (isDead || body == null) return;

        x = body.getPosition().x * PPM;
        y = body.getPosition().y * PPM;

        float distance = playerPosition.x - x;
        float speed = 1.2f;

        if (Math.abs(distance) > 400f) {
            body.setLinearVelocity(distance > 0 ? speed : -speed, body.getLinearVelocity().y);
        } else {
            body.setLinearVelocity(0, body.getLinearVelocity().y);
        }

        fireTimer += delta;
        if (fireTimer >= 2.5f) {
            fireTimer = 0;
        }
    }

    @Override
    public void draw(SpriteBatch batch) {
        if (isDead) return;
        batch.draw(texture, x - width / 2f, y - height / 2f, width, height);
    }
}