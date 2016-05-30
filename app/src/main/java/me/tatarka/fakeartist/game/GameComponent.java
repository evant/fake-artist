package me.tatarka.fakeartist.game;

import javax.inject.Singleton;

import dagger.Component;
import me.tatarka.fakeartist.game.lobby.GameLobbyActivity;

@Singleton
@Component(modules = GameModule.class)
public interface GameComponent {
    void inject(GameLobbyActivity activity);
}
