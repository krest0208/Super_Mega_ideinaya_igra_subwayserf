package ru.samsung.gamestudio;

import java.util.Random;

public class ObstacleSpawner {
    private static final float MIN_SPAWN_DISTANCE = 700f;   // Минимальное расстояние
    private static final float MAX_SPAWN_DISTANCE = 950f;   // Максимальное расстояние
    private static final float START_X = 1400f;             // Начальная позиция (дальше за экраном)

    private Random random;
    private float nextSpawnX;
    private float lastSpawnX;

    public ObstacleSpawner() {
        random = new Random();
        nextSpawnX = START_X;
        lastSpawnX = 0;
    }

    public Obstacle spawnObstacle(float groundY) {
        int type = random.nextInt(4);
        float x = nextSpawnX;

        Obstacle obstacle;
        switch (type) {
            case 0:
                obstacle = new Box(x, groundY);
                break;
            case 1:
                obstacle = new Barrier(x, groundY);
                break;
            case 2:
                obstacle = new Trash(x, groundY);
                break;
            case 3:
            default:
                obstacle = new Conus(x, groundY);
                break;
        }

        // Следующая позиция с увеличенным расстоянием
        float distance = MIN_SPAWN_DISTANCE + random.nextFloat() * (MAX_SPAWN_DISTANCE - MIN_SPAWN_DISTANCE);
        nextSpawnX += distance;
        lastSpawnX = x;

        System.out.println("Spawned obstacle at: " + x + ", next at: " + nextSpawnX + ", distance: " + distance);

        return obstacle;
    }

    public void reset() {
        nextSpawnX = START_X;
        lastSpawnX = 0;
    }

    public float getNextSpawnX() {
        return nextSpawnX;
    }
}