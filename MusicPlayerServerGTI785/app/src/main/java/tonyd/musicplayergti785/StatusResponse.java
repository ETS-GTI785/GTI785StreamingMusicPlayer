package tonyd.musicplayergti785;

/**
 * Created by tonyd on 5/18/2017.
 */


public class StatusResponse{
    private String filename;
    private String title;
    private String artist;
    private String album;
    private boolean playing;
    private boolean repeating;
    private int currentPosition;
    private int duration;
    private String year;

    public StatusResponse(String filename, String title, String artist, String album, String year, boolean playing, boolean repeating, int currentPosition, int duration){
        this.filename = filename;
        this.title = title;
        this.artist = artist;
        this.album = album;
        this.year = year;
        this.playing = playing;
        this.repeating = repeating;
        this.currentPosition = currentPosition;
        this.duration = duration;
        this.year = year;
    }

    //Getters and Setters

    public String getFilename() {
        return filename;
    }

    public void setFilename(String filename) {
        this.filename = filename;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getArtist() {
        return artist;
    }

    public void setArtist(String artist) {
        this.artist = artist;
    }

    public String getAlbum() {
        return album;
    }

    public void setAlbum(String album) {
        this.album = album;
    }

    public boolean isPlaying() {
        return playing;
    }

    public void setPlaying(boolean playing) {
        this.playing = playing;
    }

    public boolean isRepeating() {
        return repeating;
    }

    public void setRepeating(boolean repeating) {
        this.repeating = repeating;
    }

    public int getCurrentPosition() {
        return currentPosition;
    }

    public void setCurrentPosition(int currentPosition) { this.currentPosition = currentPosition; }

    public int getDuration() {
        return duration;
    }

    public void setDuration(int duration) {
        this.duration = duration;
    }

    public String getYear() { return year; }

    public void setYear(String year) {
        this.year = year;
    }
}

