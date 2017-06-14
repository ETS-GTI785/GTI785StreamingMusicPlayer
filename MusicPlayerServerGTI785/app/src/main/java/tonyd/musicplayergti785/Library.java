package tonyd.musicplayergti785;

import android.content.Context;
import android.content.res.AssetFileDescriptor;
import android.content.res.AssetManager;
import android.media.MediaMetadataRetriever;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * Created by tonyd on 5/18/2017.
 */

public class Library {

    private static Library instance = null;

    protected Library() {

    }

    public static Library getInstance() {
        if (instance == null) {
            instance = new Library();
        }
        return instance;
    }

    private int currentIndex;
    private int currentPosition;
    private boolean isPlaying;

    private List<Song> songs;
    private ArrayList<Album> albums;
    private ArrayList<Artist> artists;

    private ArrayList<Song> originalPlaylist;
    private ArrayList<Song> currentPlaylist;

    /* Modes */
    private boolean shuffleMode;
    private boolean repeatMode;

    void init(Context context) {
        currentIndex = 0;
        currentPosition = 0;
        isPlaying = false;

        songs = new ArrayList<>();
        albums = new ArrayList<>();
        artists = new ArrayList<>();

        originalPlaylist = new ArrayList<>();
        currentPlaylist = new ArrayList<>();

        try {
            MediaMetadataRetriever metaRetriever = new MediaMetadataRetriever();
            AssetManager assetManager = context.getAssets();
            String musicFolder = "music";
            String[] fileList = assetManager.list(musicFolder);

            for (int i = 0; i < fileList.length; i++) {
                String file = musicFolder + "/" + fileList[i];
                AssetFileDescriptor afd = context.getAssets().openFd(file);
                metaRetriever.setDataSource(afd.getFileDescriptor(), afd.getStartOffset(), afd.getLength());

                String albumText = metaRetriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_ALBUM);
                String artistText = metaRetriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_ARTIST);
                String year = metaRetriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_YEAR);
                String durationText = metaRetriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION);
                byte[] cover = metaRetriever.getEmbeddedPicture();

                Artist artist = getOrCreateArtist(artistText);
                Album album = getOrCreateAlbum(albumText);
                String title = metaRetriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_TITLE);
                int duration = 0;
                if (durationText != null) {
                    duration = Integer.parseInt(durationText);
                }

                Song song = new Song(file, title, artist, album, year, duration, cover);

                songs.add(song);
                originalPlaylist.add(song);

                afd.close();
            }
            metaRetriever.release();
            currentPlaylist = (ArrayList) originalPlaylist.clone();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private Artist getOrCreateArtist(String artistText) {
        for (int i = 0; i < artists.size(); i++) {
            if (artistText.equals(artists.get(i).getName())) {
                return artists.get(i);
            }
        }
        return new Artist(artistText);
    }

    private Album getOrCreateAlbum(String albumText) {
        for (int i = 0; i < albums.size(); i++) {
            if (albumText.equals(albums.get(i).getName())) {
                return albums.get(i);
            }
        }
        return new Album(albumText);
    }

    public Song getCurrentSong() {
        return currentPlaylist.get(currentIndex);
    }

    public Song getNextSong() {
        if (repeatMode) {
            return currentPlaylist.get(currentIndex);
        } else {
            currentIndex++;
            if (currentIndex >= currentPlaylist.size()) {
                currentIndex = currentIndex % currentPlaylist.size();
                return currentPlaylist.get(currentIndex);

            } else {
                return currentPlaylist.get(currentIndex);
            }
        }
    }

    public Song getPreviousSong() {
        if (currentIndex > 0) {
            currentIndex--;
            return currentPlaylist.get(currentIndex);
        } else {
            return currentPlaylist.get(currentIndex);
        }
    }

    public void switchShufflePlaylist() {
        currentIndex = 0;
        shuffleMode = !shuffleMode;
        if (shuffleMode) {
            currentPlaylist = shuffledPlaylist();
        } else {
            currentPlaylist = (ArrayList) originalPlaylist.clone();
        }
    }

    private ArrayList<Song> shuffledPlaylist() {
        ArrayList<Song> shuffledPlaylist = new ArrayList<>();
        ArrayList<Song> originalPlaylistCopy = (ArrayList) originalPlaylist.clone();
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

    public boolean isRepeatMode() {
        return repeatMode;
    }

    public ArrayList<Song> getCurrentPlaylist() {
        return currentPlaylist;
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

}
