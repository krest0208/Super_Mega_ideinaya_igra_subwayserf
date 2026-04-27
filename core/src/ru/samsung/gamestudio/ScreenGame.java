package ru.samsung.gamestudio;

import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.utils.ScreenUtils;
import ru.samsung.gamestudio.Object.Ground;

public class ScreenGame implements Screen {

    private OrthographicCamera camera;
    private SpriteBatch batch;
    private Texture playerTexture;
    private Texture backgroundTexture;  // текстура фона
    private Ground ground;

    private static final int SCREEN_WIDTH = 1280;
    private static final int SCREEN_HEIGHT = 720;

    // Игрок
    private float playerX = 20;
    private float playerY = 40;
    private float playerWidth = 256;
    private float playerHeight = 256;

    public ScreenGame(MyGdxGame myGdxGame) {}

    @Override
    public void show() {
        camera = new OrthographicCamera();
        camera.setToOrtho(false, SCREEN_WIDTH, SCREEN_HEIGHT);

        batch = new SpriteBatch();

        // Загружаем текстуры
        playerTexture = new Texture("charecter/static_p.png");
        backgroundTexture = new Texture("background/background1.png");  // загружаем фон
        ground = new Ground(-60, -420,1400,1012 );
    }

    @Override
    public void render(float delta) {
        // Очищаем экран (на всякий случай, но фон его перекроет)
        ScreenUtils.clear(0.1f, 0.1f, 0.2f, 1);

        camera.update();

        batch.setProjectionMatrix(camera.combined);
        batch.begin();

        // Рисуем фон (первым, чтобы был позади всего)
        batch.draw(backgroundTexture, 0, 0, SCREEN_WIDTH, SCREEN_HEIGHT);

        // Рисуем пол
        ground.draw(batch);

        // Рисуем игрока
        batch.draw(playerTexture, playerX, playerY, playerWidth, playerHeight);

        batch.end();
    }

    @Override
    public void resize(int width, int height) {
        camera.setToOrtho(false, SCREEN_WIDTH, SCREEN_HEIGHT);
    }

    @Override
    public void pause() {

    }

    @Override
    public void resume() {

    }
    @Override
    public void hide() {

    }

    @Override
    public void dispose() {
        batch.dispose();
        playerTexture.dispose();
        backgroundTexture.dispose();
        ground.dispose();
    }
}