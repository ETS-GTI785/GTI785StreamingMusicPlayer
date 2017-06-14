package tonyd.musicplayergti785;

import java.util.ArrayList;
import java.util.Random;

/**
 * Created by tonyd on 5/18/2017.
 */

public class Library {

    private static Library instance = null;

    public Library() {

    }

    public static Library getInstance() {
        if (instance == null) {
            instance = new Library();
        }
        return instance;
    }

    private int currentIndex;
    private int currentPosition = 0;
    private boolean isPlaying = false;

    private ArrayList<Song> songs;
    private ArrayList<Song> originalSongs;

    /* Modes */
    private boolean shuffleMode = false;
    private boolean repeatMode = false;

    void init() {
        currentIndex = 0;
        songs = new ArrayList<>();
        originalSongs = new ArrayList<>();
    }

    public Song getCurrentSong() {
        if (songs.size() > 0) {
            return songs.get(currentIndex);
        }
        return null;
    }

    public Song getNextSong() {
        if (repeatMode) {
            return songs.get(currentIndex);
        } else {
            currentIndex++;
            if (currentIndex >= songs.size()) {
                currentIndex = currentIndex % songs.size();
                return songs.get(currentIndex);

            } else {
                return songs.get(currentIndex);
            }
        }
    }

    public Song getPreviousSong() {
        if (currentIndex > 0) {
            currentIndex--;
            return songs.get(currentIndex);
        } else {
            return songs.get(currentIndex);
        }
    }

    public void switchShufflePlaylist() {
        currentIndex = 0;
        shuffleMode = !shuffleMode;
        if (shuffleMode) {
            songs = shuffledPlaylist();
        } else {
            songs = (ArrayList) originalSongs.clone();
        }
    }

    private ArrayList<Song> shuffledPlaylist() {
        ArrayList<Song> shuffledPlaylist = new ArrayList<>();
        ArrayList<Song> originalPlaylistCopy = (ArrayList) originalSongs.clone();
        Random random = new Random();
        for (int i = originalPlaylistCopy.size() - 1; i >= 0; i--) {
            int index = random.nextInt(originalPlaylistCopy.size());
            shuffledPlaylist.add(originalPlaylistCopy.get(index));
            originalPlaylistCopy.remove(index);
        }
        return shuffledPlaylist;
    }

    public void switchRepeatPlaylist() {
        repeatMode = !repeatMode;
    }

    public boolean isShuffleMode() {
        return shuffleMode;
    }

    public void setShuffleMode(boolean shuffleMode) {
        this.shuffleMode = shuffleMode;
    }

    public boolean isRepeatMode() {
        return repeatMode;
    }

    public void setRepeatMode(boolean repeatMode) {
        this.repeatMode = repeatMode;
    }

    public boolean isPlaying() {
        return isPlaying;
    }

    public void setPlaying(boolean playing) {
        isPlaying = playing;
    }

    public int getCurrentPosition() {
        return currentPosition;
    }

    public void setCurrentPosition(int currentPosition) {
        this.currentPosition = currentPosition;
    }

    public void addSong(Song song) {
        songs.add(song);
    }

    public void addOriginalSong(Song song) {
        originalSongs.add(song);
    }

    public void reset() {
        isPlaying = false;
        repeatMode = false;
        shuffleMode = false;
        currentPosition = 0;
        currentIndex = 0;
    }
}
