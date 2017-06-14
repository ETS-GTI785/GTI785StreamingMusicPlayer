package tonyd.musicplayergti785;

import android.app.Dialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.util.DisplayMetrics;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.ToggleButton;

import java.util.Timer;
import java.util.TimerTask;

public class MainActivity extends AppCompatActivity {

    private String ipAddress = "10.0.2.2";
    private String port = "5000";

    /* Media Player */
    private MediaPlayer mediaPlayer;
    private int currentPosition; // used to save position when paused

    /* Seekbar components */
    private Handler handler;
    private Runnable runnable;

    /* States */
    private boolean isPlaying;
    private boolean isPaused;
    private boolean remoteMode = false;

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
    private ProgressBar bar;

    /* For sending requests to the server */
    private RequestAsyncTask request;

    /* For receiving broadcast messages from AsynctaskRequest */
    private BroadcastReceiver receiver;

    /* Timer for executing StatusRequest periodically */
    private Timer timer;

    /* Inner class player used for preparing the media player in a different thread */
    private Player player;

    private boolean serverPlaying;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        receiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                String command = intent.getStringExtra(Command.COMMAND);
                switch (command) {
                    case Command.COVER:
                        byte[] bytes = intent.getByteArrayExtra("image");
                        setCover(bytes);
                        break;
                    case Command.STATUS:
                        status(intent);
                        break;
                }
            }
        };

        isPlaying = false;
        isPaused = false;

        Library.getInstance().init();

        sendRequest(Command.PLAYLIST);

        // Trigger the repeated sending of STATUS request
        callServerStatus();

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
        bar = (ProgressBar) findViewById(R.id.progressbar);

        // Listen to the progress of the seek bar for updating timers text fields
        setSeekBarListener();

        initialize();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mediaPlayer.reset();
        mediaPlayer.release();
        timer.cancel();
        timer.purge();
    }

    private void sendRequest(String command) {
        request = new RequestAsyncTask(this, ipAddress, port);
        request.getBroadcaster().registerReceiver(receiver, new IntentFilter(Command.COMMAND));
        request.execute(command);
    }

    private void sendRequest(String command, String param) {
        request = new RequestAsyncTask(this, ipAddress, port);
        request.getBroadcaster().registerReceiver(receiver, new IntentFilter(Command.COMMAND));
        request.execute(command, param);
    }

    private void setCover(byte[] bytes) {
        Bitmap bMap = BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
        DisplayMetrics dm = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(dm);
        cover.setMinimumHeight(dm.heightPixels);
        cover.setMinimumWidth(dm.widthPixels);
        cover.setImageBitmap(bMap);
    }

    private void updateUI() {
        setTextFieldMusicInfo();
    }

    private void prepareSeekBar() {
        handler = new Handler();
        runnable = new Runnable() {
            @Override
            public void run() {
                if (isPlaying) {
                    seekBar.setMax(Library.getInstance().getCurrentSong().getDuration() / 1000);
                    int currentPosition = mediaPlayer.getCurrentPosition();
                    seekBar.setProgress(currentPosition / 1000);
                }
                handler.postDelayed(this, 1000);
            }
        };
    }

    private void setSeekBarListener() {
        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress,
                                          boolean fromUser) {
                if ((isPlaying || isPaused || serverPlaying) && fromUser) {
                    mediaPlayer.seekTo(progress * 1000);
                }
                String timerStartFormatted = formattedTextMinSec(progress);
                String timerEndFormatted = formattedTextMinSec(Library.getInstance().getCurrentSong().getDuration() / 1000 - progress);
                timerStart.setText(timerStartFormatted);
                timerEnd.setText(timerEndFormatted);
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                if (serverPlaying) {
                    if (remoteMode) {
                        request = new RequestAsyncTask(MainActivity.this.getApplicationContext(), ipAddress, port);
                        request.getBroadcaster().registerReceiver(receiver, new IntentFilter(Command.COMMAND));
                        request.execute(Command.SEEK, String.valueOf(seekBar.getProgress() * 1000));
                    }
                }
            }
        });
    }

    private void initialize() {
        mediaPlayer = new MediaPlayer();
        mediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
        mediaPlayer.setOnCompletionListener(mediaPlayer1 -> {
            mediaPlayer1.stop();
            mediaPlayer1.reset();
        });
    }

    private void setTextFieldMusicInfo() {
        title.setText(Library.getInstance().getCurrentSong().getName());
        artist.setText(Library.getInstance().getCurrentSong().getArtist());
        album.setText(Library.getInstance().getCurrentSong().getAlbum());
    }

    /******************** Buttons handler ********************/

    public void playPause(View playPauseView) {
        if (remoteMode) {
            sendRequest(Command.PLAY);
            sendRequest(Command.COVER);
        } else {
            Library library = Library.getInstance();
            if (library.getCurrentSong() != null) {
                if (!isPlaying) {
                    if (!isPaused) {
                        initialize();
                        runOnUiThread(runnable);
                        player = new Player();
                        player.execute(getBaseUrl() + Command.STREAM + "/" + Library.getInstance().getCurrentSong().getFilename());
                        sendRequest(Command.COVER, Library.getInstance().getCurrentSong().getFilename());
                        // Activate progress bar
                        bar.setVisibility(View.VISIBLE);
                    } else {
                        mediaPlayer.seekTo(currentPosition);
                    }
                    playPause.setImageResource(R.drawable.ic_pause_white_48dp);
                } else {
                    isPlaying = false;
                    isPaused = true;
                    mediaPlayer.pause();
                    currentPosition = mediaPlayer.getCurrentPosition();
                    playPause.setImageResource(R.drawable.ic_play_arrow_white_48dp);
                }
            }
        }
    }

    public void stop(View stopView) {
        if (remoteMode) {
            sendRequest(Command.STOP);
            playPause.setImageResource(R.drawable.ic_play_arrow_white_48dp);
        } else {
            mediaPlayer.pause();
            mediaPlayer.reset();

            isPaused = false;
            isPlaying = false;

            timerStart.setText(formattedTextMinSec(0));
            timerEnd.setText(formattedTextMinSec(0));

            currentPosition = 0;
            playPause.setImageResource(R.drawable.ic_play_arrow_white_48dp);
            handler.removeCallbacks(runnable);
            bar.setVisibility(View.GONE);
        }
    }

    public void next(View nextView) {
        if (remoteMode) {
            sendRequest(Command.NEXT);
        } else {
            Library library = Library.getInstance();
            if (library.getNextSong() != null) {
                if (isPlaying) {
                    stop(null);
                    initialize();
                    playPause(null);
                } else {
                    stop(null);
                    initialize();
                }
            }
        }
    }

    public void back(View backView) {
        if (remoteMode) {
            sendRequest(Command.PREVIOUS);
        } else {
            if (Library.getInstance().getPreviousSong() != null) {
                if (isPlaying) {
                    stop(null);
                    playPause(null);
                } else {
                    stop(null);
                    initialize();
                }
            }
        }
    }

    public void shuffle(View backView) {
        if (remoteMode) {
            sendRequest(Command.SHUFFLE);
        } else {
            Library.getInstance().switchShufflePlaylist();
            if (Library.getInstance().isShuffleMode()) {
                shuffle.setImageResource(R.drawable.ic_shuffle_light_blue_800_48dp);
            } else {
                shuffle.setImageResource(R.drawable.ic_shuffle_white_48dp);

            }
        }
    }

    public void repeat(View backView) {
        if (remoteMode) {
            sendRequest(Command.REPEAT);
        } else {
            Library.getInstance().switchRepeatPlaylist();
            if (Library.getInstance().isRepeatMode()) {
                repeat.setImageResource(R.drawable.ic_repeat_one_white_48dp);
            } else {
                repeat.setImageResource(R.drawable.ic_repeat_white_48dp);
            }
        }
    }

    public void status(Intent intent) {
        StatusResponse sr = intent.getParcelableExtra("status");
        if (remoteMode) {
            serverPlaying = sr.isPlaying();
            title.setText(sr.getTitle());
            album.setText(sr.getAlbum());
            artist.setText(sr.getArtist());
            timerStart.setText(formattedTextMinSec(sr.getCurrentPosition()));
            timerEnd.setText(formattedTextMinSec(sr.getDuration() / 1000));
            seekBar.setMax(sr.getDuration() / 1000);
            seekBar.setProgress(sr.getCurrentPosition());
            if (sr.isRepeating()) {
                repeat.setImageResource(R.drawable.ic_repeat_one_white_48dp);
            } else {
                repeat.setImageResource(R.drawable.ic_repeat_white_48dp);
            }
            if (sr.isPlaying()) {
                playPause.setImageResource(R.drawable.ic_pause_white_48dp);
            } else {
                playPause.setImageResource(R.drawable.ic_play_arrow_white_48dp);
            }
        } else {
            if (Library.getInstance().getCurrentSong() != null) {
                updateUI();
            }
        }
    }

    class Player extends AsyncTask<String, Void, Boolean> {

        boolean prepared = false;

        @Override
        protected Boolean doInBackground(String... strings) {
            try {
                mediaPlayer = new MediaPlayer();
                mediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
                mediaPlayer.setDataSource(strings[0]);
                mediaPlayer.prepare();
                prepared = true;
            } catch (Exception e) {
                prepared = false;
            }
            return prepared;
        }

        @Override
        protected void onPostExecute(Boolean aBoolean) {
            super.onPostExecute(aBoolean);
            if (prepared) {
                mediaPlayer.start();
                isPlaying = true;
                isPaused = false;
            }
            bar.setVisibility(View.GONE);
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }
    }

    public void callServerStatus() {
        final Handler handler = new Handler();
        timer = new Timer();
        TimerTask doAsynchronousTask = new TimerTask() {
            @Override
            public void run() {
                handler.post(() -> {
                    try {
                        sendRequest(Command.STATUS);
                    } catch (Exception e) {

                    }
                });
            }
        };
        timer.schedule(doAsynchronousTask, 0, 5000); // Execute every 50000 ms
    }

    public void changeMode(View v) {
        ToggleButton t = (ToggleButton) v;
        boolean on = ((ToggleButton) v).isChecked();
        if (mediaPlayer.isPlaying()) {
            mediaPlayer.stop();
            mediaPlayer.release();
        }
        isPlaying = false;
        isPaused = false;
        Library.getInstance().reset();
        initialize();
        resetUI();
        if (on) {
            remoteMode = true;
        } else {
            remoteMode = false;
        }
    }

    private void resetUI() {
        title.setText("");
        album.setText("");
        artist.setText("");
        timerStart.setText("");
        timerEnd.setText("");
        seekBar.setProgress(0);
        seekBar.setMax(0);
        playPause.setImageResource(R.drawable.ic_play_arrow_white_48dp);
        repeat.setImageResource(R.drawable.ic_repeat_white_48dp);
        shuffle.setImageResource(R.drawable.ic_shuffle_white_48dp);
        cover.setImageResource(0);
        bar.setVisibility(View.GONE);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);//Menu Resource, Menu
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int itemId = item.getItemId();
        switch (itemId) {
            case R.id.preferences_dialog:
                final Dialog dialog = new Dialog(this);
                dialog.setContentView(R.layout.preferences_dialog);
                dialog.setTitle(R.string.pref_port);
                EditText ipAddressEditText = (EditText) dialog.findViewById(R.id.editText_adresseIP);
                EditText portEditText = (EditText) dialog.findViewById(R.id.portEditText);
                ipAddressEditText.setText(ipAddress);
                portEditText.setText(port);
                Button dialogButton = (Button) dialog.findViewById(R.id.dialogButtonOK);
                dialogButton.setOnClickListener(v -> {
                    port = portEditText.getText().toString();
                    ipAddress = ipAddressEditText.getText().toString();
                    dialog.dismiss();
                });
                dialog.show();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    /* Utilities */

    private String formattedTextMinSec(int timeInSeconds) {
        int minutes = timeInSeconds / 60;
        int seconds = timeInSeconds % 60;
        return String.format("%02d:%02d", minutes, seconds);
    }

    public String getBaseUrl() {
        return "http://" + ipAddress + ":" + port + "/";
    }

}
