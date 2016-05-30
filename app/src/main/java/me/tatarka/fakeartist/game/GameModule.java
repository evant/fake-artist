package me.tatarka.fakeartist.game;

import com.shephertz.app42.gaming.multiplayer.client.WarpClient;

import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;

@Module
public class GameModule {

    @Provides
    @Singleton
    public WarpClient provideWarpClient() {
        WarpClient.initialize("f5fecbce98c7f497c1c1bb12a0c4486581d7dc8e6c9eef0723f32488f23c205a", "d358b8713cebbd0414fa122e406cc7c4722e474f21902a6a28879f42c4454686");
        try {
            return WarpClient.getInstance();
        } catch (Exception e) {
            throw new AssertionError(e);
        }
    }
}
