package tonyd.musicplayergti785;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * Created by tonyd on 5/18/2017.
 */

public class Song implements Parcelable {

    private String filename;
    private String name;
    private String album;
    private String artist;
    private String year;
    private int duration;

    public Song(String filename, String name, String artist, String album, String year, int duration) {
        this.filename = filename;
        this.name = name;
        this.artist = artist;
        this.album = album;
        this.year = year;
        this.duration = duration;
    }

    public String getFilename() {
        return filename;
    }

    public void setFilename(String filename) {
        this.filename = filename;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getDuration() {
        return duration;
    }

    public void setDuration(int duration) {
        this.duration = duration;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(filename);
        dest.writeString(name);
        dest.writeString(artist);
        dest.writeString(album);
        dest.writeInt(duration);

    }

    public static final Creator<Song> CREATOR = new Creator<Song>() {

        public Song createFromParcel(Parcel in) {
            return new Song(in);
        }

        public Song[] newArray(int size) {
            return new Song[size];
        }
    };

    public Song(Parcel in) {
        this.filename = in.readString();
        this.name = in.readString();
        this.artist = in.readString();
        this.album = in.readString();
        this.duration = in.readInt();
    }

    public String getAlbum() {
        return album;
    }

    public void setAlbum(String album) {
        this.album = album;
    }

    public String getArtist() {
        return artist;
    }

    public void setArtist(String artist) {
        this.artist = artist;
    }

}
