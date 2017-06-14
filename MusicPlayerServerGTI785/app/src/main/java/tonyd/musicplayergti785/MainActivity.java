package tonyd.musicplayergti785;

import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.content.res.AssetFileDescriptor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AppCompatActivity;
import android.util.DisplayMetrics;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import java.io.IOException;

public class MainActivity extends AppCompatActivity implements WebServiceCallbacks {

    /* Web Service */
    private WebService webService;
    private boolean bound;
    private ServiceConnection mServiceConnection = new ServiceConnection() {

        @Override
        public void onServiceDisconnected(ComponentName name) {
            bound = false;
        }

        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            WebService.MyBinder myBinder = (WebService.MyBinder) service;
            webService = myBinder.getService();
            bound = true;
            webService.setCallbacks(MainActivity.this); // register
            Toast.makeText(MainActivity.this, "Connected to WebService", Toast.LENGTH_SHORT).show();
        }
    };

    /* Media Player */
    private MediaPlayer mediaPlayer;
    private int currentPosition; // used to save position when paused

    /* States */
    private boolean isPlaying;
    private boolean isPaused;

    /* Seekbar components */
    private Handler handler;
    private Runnable runnable;

    /* UI fields */
    private ImageButton playPause;
    private ImageButton shuffle;
    private ImageButton repeat;
    private TextView title;
    private TextView artist;
    private TextView album;
    private TextView timerStart;
    private TextView timerEnd;
    private ImageView cover;
    private SeekBar seekBar;

    /* For receiving broadcast messages from server */
    private BroadcastReceiver receiver;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        receiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                String command = intent.getStringExtra(Command.COMMAND);
                switch (command) {
                    case Command.PLAY:
                        playPause(null);
                        break;
                    case Command.PAUSE:
                        playPause(null);
                    case Command.STOP:
                        stop(null);
                        break;
                    case Command.NEXT:
                        next(null);
                        break;
                    case Command.PREVIOUS:
                        back(null);
                        break;
                    case Command.SHUFFLE:
                        shuffle(null);
                        break;
                    case Command.REPEAT:
                        repeat(null);
                        break;
                    case Command.SEEK:
                        int seekValue = intent.getExtras().getInt(Command.SEEKVALUE);
                        seek(seekValue);
                        break;
                }
            }
        };

        Library.getInstance().init(this);

        // Start the WebService to handle the server in a different thread
        Intent intent = new Intent(this, WebService.class);
        bindService(intent, mServiceConnection, Context.BIND_AUTO_CREATE);

        isPlaying = false;
        isPaused = false;

        // Set the handler and the runnable for updating the seek bar
        prepareSeekBar();

        playPause = (ImageButton) findViewById(R.id.playPause);
        shuffle = (ImageButton) findViewById(R.id.shuffle);
        repeat = (ImageButton) findViewById(R.id.repeat);
        title = (TextView) findViewById(R.id.title);
        album = (TextView) findViewById(R.id.album);
        artist = (TextView) findViewById(R.id.artist);
        timerStart = (TextView) findViewById(R.id.timerStart);
        timerEnd = (TextView) findViewById(R.id.timerEnd);
        cover = (ImageView) findViewById(R.id.cover);
        seekBar = (SeekBar) findViewById(R.id.seekBar);

        // Listen to the progress of the seek bar for updating timers text fields
        setSeekBarListener();

        initialize();
    }

    @Override
    protected void onStart() {
        super.onStart();

        // Listener registration necessary so that MainActivity is able to receive broadcast
        // messages from the server
        LocalBroadcastManager.getInstance(this).registerReceiver((receiver),
                new IntentFilter(Command.COMMAND)
        );
    }

    @Override
    protected void onResume() {
        super.onResume();
        Intent intent = new Intent(this, WebService.class);
        bindService(intent, mServiceConnection, Context.BIND_AUTO_CREATE);
    }

    @Override
    protected void onStop() {
        LocalBroadcastManager.getInstance(this).unregisterReceiver(receiver);
        super.onStop();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    private void prepareSeekBar() {
        handler = new Handler();
        runnable = new Runnable() {
            @Override
            public void run() {
                if (isPlaying) {
                    int currentPosition = mediaPlayer.getCurrentPosition();
                    seekBar.setProgress(currentPosition / 1000);
                    Library.getInstance().setCurrentPosition(currentPosition / 1000);
                }
                handler.postDelayed(this, 1000);
            }
        };
    }

    private void setSeekBarListener() {
        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if ((isPlaying || isPaused) && fromUser) {
                    // When user is changing the position of the seek bar's cursor
                    mediaPlayer.seekTo(progress * 1000);
                }
                timerStart.setText(formattedTextMinSec(progress));
                timerEnd.setText(formattedTextMinSec(Library.getInstance().
                        getCurrentSong().getDuration() / 1000 - progress));
                Library.getInstance().setCurrentPosition(progress);
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
            }
        });
    }

    private void initialize() {
        initializeMediaPlayer();
        setTextFieldMusicInfo(); // Set title, artist & album text fields
        Library.getInstance().getCurrentSong().setDuration(mediaPlayer.getDuration());
        seekBar.setMax(Library.getInstance().getCurrentSong().getDuration() / 1000);
    }

    private void initializeMediaPlayer() {
        mediaPlayer = new MediaPlayer();
        mediaPlayer.setOnCompletionListener(mediaPlayer -> {
            mediaPlayer.stop();
            mediaPlayer.reset();
            isPlaying = false;
        });
        try {
            AssetFileDescriptor descriptor = getAssets().openFd(Library.getInstance().getCurrentSong().getFilename());
            mediaPlayer.setDataSource(descriptor.getFileDescriptor(), descriptor.getStartOffset(), descriptor.getLength());
            mediaPlayer.prepare();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void setTextFieldMusicInfo() {
        title.setText(Library.getInstance().getCurrentSong().getName());
        artist.setText(Library.getInstance().getCurrentSong().getArtist().getName());
        album.setText(Library.getInstance().getCurrentSong().getAlbum().getName());
    }

    private void setCover(byte[] bytes) {
        Bitmap bMap = BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
        DisplayMetrics dm = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(dm);
        cover.setMinimumHeight(dm.heightPixels);
        cover.setMinimumWidth(dm.widthPixels);
        cover.setImageBitmap(bMap);
    }

    /******************** Buttons handler ********************/

    public void playPause(View v) {
        if (!isPlaying) {
            if (!isPaused) {
                initialize();
                runOnUiThread(runnable);
                setCover(Library.getInstance().getCurrentSong().getCover());
            } else {
                mediaPlayer.seekTo(currentPosition);
            }
            mediaPlayer.start();

            isPaused = false;
            isPlaying = true;
            Library.getInstance().setPlaying(true);
            // Change to 'pause' image while isPlaying
            playPause.setImageResource(R.drawable.ic_pause_white_48dp);
        } else {
            mediaPlayer.pause();

            isPaused = true;
            isPlaying = false;
            Library.getInstance().setPlaying(false);
            currentPosition = mediaPlayer.getCurrentPosition();
            // Change to 'play' image while isPause
            playPause.setImageResource(R.drawable.ic_play_arrow_white_48dp);
        }
    }

    public void stop(View v) {
        if (isPlaying || isPaused) {
            mediaPlayer.pause();
            mediaPlayer.release();

            isPaused = false;
            isPlaying = false;
            currentPosition = 0;
            Library.getInstance().setPlaying(false);

            timerStart.setText(formattedTextMinSec(0));
            timerEnd.setText(formattedTextMinSec(0));

            // Change to 'play' image while !isPlaying
            playPause.setImageResource(R.drawable.ic_play_arrow_white_48dp);
            // Stop seek bar
            handler.removeCallbacks(runnable);
        }
    }

    public void next(View v) {
        if (Library.getInstance().getNextSong() != null) {
            if (isPlaying) {
                stop(null);
                playPause(null);
            } else {
                stop(null);
                initialize();
            }
        }
    }

    public void back(View v) {
        if (Library.getInstance().getPreviousSong() != null) {
            if (isPlaying) {
                stop(null);
                playPause(null);
            } else {
                stop(null);
                initialize();
            }
        } else {
            stop(null);
            initialize();
        }
    }

    public void shuffle(View v) {
        Library.getInstance().switchShufflePlaylist();
        // Switch UI
        if (Library.getInstance().isShuffleMode()) {
            shuffle.setImageResource(R.drawable.ic_shuffle_light_blue_800_48dp);
        } else {
            shuffle.setImageResource(R.drawable.ic_shuffle_white_48dp);
        }
    }

    public void repeat(View v) {
        Library.getInstance().switchRepeatPlaylist();
        // Switch UI
        if (Library.getInstance().isRepeatMode()) {
            repeat.setImageResource(R.drawable.ic_repeat_one_white_48dp);
        } else {
            repeat.setImageResource(R.drawable.ic_repeat_white_48dp);
        }
    }

    public void seek(int seekValue) {
        mediaPlayer.seekTo(seekValue);
    }

    /* Utilities */
    private String formattedTextMinSec(int timeInSeconds) {
        int minutes = timeInSeconds / 60;
        int seconds = timeInSeconds % 60;
        String formattedText = String.format("%02d:%02d", minutes, seconds);
        return formattedText;
    }


}
