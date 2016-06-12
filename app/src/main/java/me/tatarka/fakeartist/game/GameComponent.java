package me.tatarka.fakeartist.game;

import javax.inject.Singleton;

import dagger.Component;
import me.tatarka.fakeartist.game.lobby.GameLobbyActivity;
import me.tatarka.fakeartist.game.main.GameActivity;

@Singleton
@Component(modules = GameModule.class)
public interface GameComponent {
    void inject(GameLobbyActivity activity);
   
    void inject(GameActivity activity);
}
