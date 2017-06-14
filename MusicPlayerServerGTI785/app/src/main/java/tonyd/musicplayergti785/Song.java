package tonyd.musicplayergti785;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * Created by tonyd on 5/18/2017.
 */

public class Song implements Parcelable {

    private String filename;
    private String name;
    private Album album;
    private Artist artist;
    private String year;
    private int duration;
    private byte[] cover;

    public Song(String filename, String name, Artist artist, Album album, String year, int duration, byte[] cover) {
        this.filename = filename;
        this.name = name;
        this.artist = artist;
        this.album = album;
        this.year = year;
        this.duration = duration;
        this.cover = cover;
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

    public Artist getArtist() {
        return artist;
    }

    public void setArtist(Artist artist) {
        this.artist = artist;
    }

    public Album getAlbum() {
        return album;
    }

    public void setAlbum(Album album) {
        this.album = album;
    }

    public String getYear() {
        return year;
    }

    public void setYear(String year) {
        this.year = year;
    }

    public byte[] getCover() {
        return cover;
    }

    public void setCover(byte[] cover) {
        this.cover = cover;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(filename);
        dest.writeString(name);
        dest.writeParcelable(artist, flags);
        dest.writeParcelable(album, flags);
        dest.writeInt(duration);

    }

    public static final Parcelable.Creator<Song> CREATOR = new Parcelable.Creator<Song>() {

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
        this.artist = in.readParcelable(Artist.class.getClassLoader());
        this.album = in.readParcelable(Album.class.getClassLoader());
        this.duration = in.readInt();
    }
}
