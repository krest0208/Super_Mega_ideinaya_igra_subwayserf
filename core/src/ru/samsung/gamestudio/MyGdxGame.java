package ru.samsung.gamestudio;

import com.badlogic.gdx.Game;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;

public class MyGdxGame extends Game {

    public SpriteBatch batch;
    public OrthographicCamera camera;

    public ScreenGame ScreenGame;

    @Override
    public void create() {
        batch = new SpriteBatch();
        camera = new OrthographicCamera();
        camera.setToOrtho(false, GameSettings.SCREEN_WIDTH, GameSettings.SCREEN_HEIGHT);

        ScreenGame = new ScreenGame(this);

        setScreen(ScreenGame);
    }

    @Override
    public void dispose() {
        batch.dispose();
    }
}
