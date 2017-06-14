package tonyd.musicplayergti785;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * Created by tonyd on 5/18/2017.
 */

public class Artist implements Parcelable{

    private String name;

    public Artist(String name){
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }


    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(name);
    }

    public static final Parcelable.Creator<Artist> CREATOR = new Parcelable.Creator<Artist>() {

        public Artist createFromParcel(Parcel in) {
            return new Artist(in);
        }

        public Artist[] newArray(int size) {
            return new Artist[size];
        }
    };

    public Artist(Parcel in) {
        this.name = in.readString();
    }
}
