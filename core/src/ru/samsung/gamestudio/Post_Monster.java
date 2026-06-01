package ru.samsung.gamestudio.Charecers;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.World;

public class Post_Monster extends BaseMonster {
    private Texture texture;

    public Post_Monster(World world, float x, float y) {
        super(world, x, y, 3, 80f, 60f); // 3 единицы HP
        texture = new Texture("monsters/swarm_crawler.png");

        hitSound = Gdx.audio.newSound(Gdx.files.internal("sounds/monster_hit.ogg"));
        deathSound = Gdx.audio.newSound(Gdx.files.internal("sounds/monster_death.ogg"));
    }

    @Override
    public void update(float delta, Vector2 playerPosition) {
        if (isDead || body == null) return;

        x = body.getPosition().x * PPM;
        y = body.getPosition().y * PPM;

        float speed = 1.8f;
        if (playerPosition.x < x) {
            body.setLinearVelocity(-speed, body.getLinearVelocity().y);
        } else {
            body.setLinearVelocity(speed, body.getLinearVelocity().y);
        }
    }

    @Override
    public void draw(SpriteBatch batch) {
        if (isDead) return;
        batch.draw(texture, x - width / 2f, y - height / 2f, width, height);
    }
}