package jtkaiser.imags;

/**
 * Created by jtkai on 1/23/2018.
 */

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;
import android.support.annotation.Nullable;

public class PlayerService extends Service {

    private final IBinder mBinder = new PlayerBinder();
    private SpotifyPlayer mPlayer = new SpotifyPlayer();

    public static Intent getIntent(Context context) {
        return new Intent(context, PlayerService.class);
    }

    public class PlayerBinder extends Binder {
        public Player getService() {
            return mPlayer;
        }
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return mBinder;
    }

    @Override
    public void onDestroy() {
        mPlayer.release();
        super.onDestroy();
    }
}