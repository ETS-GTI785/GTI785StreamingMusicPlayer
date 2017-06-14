package tonyd.musicplayergti785;

/**
 * Created by tonyd on 5/18/2017.
 */

public class PlaylistResponse {

    private String filename;
    private String title;
    private String artist;
    private String album;
    private int duration;
    private String year;

    public PlaylistResponse(String filename, String title, String artist, String album, String year, int duration){
        this.filename = filename;
        this.title = title;
        this.artist = artist;
        this.album = album;
        this.duration = duration;
        this.year = year;
    }

    // getter and setter for each class field

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

    public int getDuration() {
        return duration;
    }

    public void setDuration(int duration) {
        this.duration = duration;
    }

    public String getYear() {
        return year;
    }

    public void setYear(String year) {
        this.year = year;
    }

}
