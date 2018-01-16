package jtkaiser.imags;

import android.content.Context;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.spotify.sdk.android.authentication.AuthenticationClient;
import com.spotify.sdk.android.authentication.AuthenticationRequest;
import com.spotify.sdk.android.authentication.AuthenticationResponse;
import com.spotify.sdk.android.player.Config;
import com.spotify.sdk.android.player.ConnectionStateCallback;
import com.spotify.sdk.android.player.Error;
import com.spotify.sdk.android.player.Player;
import com.spotify.sdk.android.player.PlayerEvent;
import com.spotify.sdk.android.player.Spotify;
import com.spotify.sdk.android.player.SpotifyPlayer;

public class SessionActivity extends AppCompatActivity implements SpotifyPlayer.NotificationCallback, ConnectionStateCallback {

    private Button mPlayToggle;
    private Button mContinueButton;
    private Button mHelpButton;
    private Player mPlayer;
    private String mTrackUri;
    private TextView mTrackTitle;

    private static final int REQUEST_CODE = 1337;
    private static final String CLIENT_ID = "0e496f3bf31344c0aaf87a89ea883e0d";
    private static final String REDIRECT_URI = "unique://callback";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_session);

        mTrackUri = getIntent().getStringExtra("trackUri");

        mPlayToggle = (Button) findViewById(R.id.play_toggle);
        mPlayToggle.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(mPlayer.getPlaybackState().isPlaying){
                    pausePlayer();
                }
                else if (mPlayer.getPlaybackState().positionMs != 0){
                    resumePlayer();
                }
                else{
                    startPlayer();
                }
            }
        });

        mContinueButton = (Button) findViewById(R.id.session_continue);
        mContinueButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                pausePlayer();
                Spotify.destroyPlayer(this);

                Intent i = new Intent(SessionActivity.this, MedicationActivity.class);
                startActivity(i);
            }
        });

        mTrackTitle = (TextView) findViewById(R.id.track_title);
        mTrackTitle.setText(getTrackTitle());

        AuthenticationRequest.Builder builder = new AuthenticationRequest.Builder(CLIENT_ID,
                AuthenticationResponse.Type.TOKEN,
                REDIRECT_URI);
        builder.setScopes(new String[]{"user-read-private", "streaming"});
        AuthenticationRequest request = builder.build();

        AuthenticationClient.openLoginActivity(this, REQUEST_CODE, request);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent intent) {
        super.onActivityResult(requestCode, resultCode, intent);

        // Check if result comes from the correct activity
        if (requestCode == REQUEST_CODE) {
            AuthenticationResponse response = AuthenticationClient.getResponse(resultCode, intent);
            if (response.getType() == AuthenticationResponse.Type.TOKEN) {
                Config playerConfig = new Config(this, response.getAccessToken(), CLIENT_ID);
                Spotify.getPlayer(playerConfig, this, new SpotifyPlayer.InitializationObserver() {
                    @Override
                    public void onInitialized(SpotifyPlayer spotifyPlayer) {
                        mPlayer = spotifyPlayer;
                        mPlayer.addConnectionStateCallback(SessionActivity.this);
                        mPlayer.addNotificationCallback(SessionActivity.this);
                    }

                    @Override
                    public void onError(Throwable throwable) {
                        Log.e("LoginActivity", "Could not initialize player: " + throwable.getMessage());
                    }
                });
            }
        }
    }

    @Override
    protected void onDestroy() {
        Spotify.destroyPlayer(this);
        super.onDestroy();
    }

    @Override
    public void onPlaybackEvent(PlayerEvent playerEvent) {
        Log.d("LoginActivity", "Playback event received: " + playerEvent.name());
        switch (playerEvent) {
            // Handle event type as necessary
            default:
                break;
        }
    }

    @Override
    public void onPlaybackError(Error error) {
        Log.d("LoginActivity", "Playback error received: " + error.name());
        switch (error) {
            // Handle error type as necessary
            default:
                break;
        }
    }

    @Override
    public void onLoggedIn() {
        Log.d("LoginActivity", "User logged in");
    }

    @Override
    public void onLoggedOut() {
        Log.d("LoginActivity", "User logged out");
    }

    @Override
    public void onLoginFailed(Error error) {
        Log.d("LoginActivity", "LoginActivity failed");
    }

    @Override
    public void onTemporaryError() {
        Log.d("LoginActivity", "Temporary error occurred");
    }

    @Override
    public void onConnectionMessage(String message) {
        Log.d("LoginActivity", "Received connection message: " + message);
    }

    private void pausePlayer(){
        mPlayToggle.setText(R.string.play_button);
        mPlayer.pause(null);
    }

    private void resumePlayer(){
        mPlayToggle.setText(R.string.pause_button);
        mPlayer.resume(null);
    }

    private void startPlayer(){
        mPlayToggle.setText(R.string.pause_button);
        mPlayer.playUri(null, mTrackUri, 0, 0);
    }

    public static Intent newIntent(Context packageContext, String selectedUri) {
        Intent i = new Intent(packageContext, SessionActivity.class);
        i.putExtra("trackUri", selectedUri);
        return i;
    }

    private String getTrackTitle(){
        return mTrackUri;
    }
}