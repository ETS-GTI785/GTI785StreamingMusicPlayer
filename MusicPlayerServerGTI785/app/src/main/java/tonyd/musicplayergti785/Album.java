package tonyd.musicplayergti785;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * Created by tonyd on 5/18/2017.
 */

public class Album  implements Parcelable{

    private String name;

    public Album(String name){
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

    public static final Parcelable.Creator<Album> CREATOR = new Parcelable.Creator<Album>() {

        public Album createFromParcel(Parcel in) {
            return new Album(in);
        }

        public Album[] newArray(int size) {
            return new Album[size];
        }
    };

    public Album(Parcel in) {
        this.name = in.readString();
    }
}
