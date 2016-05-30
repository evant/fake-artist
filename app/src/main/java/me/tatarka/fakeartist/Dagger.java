package me.tatarka.fakeartist;

import me.tatarka.fakeartist.game.DaggerGameComponent;
import me.tatarka.fakeartist.game.GameComponent;
import me.tatarka.fakeartist.game.GameModule;

public class Dagger {

    private static GameComponent gameComponent;

    public static GameComponent gameComponent() {
        if (gameComponent == null) {
            gameComponent = DaggerGameComponent.builder()
                    .gameModule(new GameModule())
                    .build();
        }
        return gameComponent;
    }
}
