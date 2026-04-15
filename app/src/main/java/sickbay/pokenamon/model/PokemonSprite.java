package sickbay.pokenamon.model;

import android.os.Parcel;
import android.os.Parcelable;

import com.google.firebase.database.Exclude;

public class PokemonSprite implements Parcelable {
    private String front;
    private String back;
    private String frontFallback;
    private String backFallback;

    @Exclude
    public int getStability(){return 0;}

    public PokemonSprite() {}

    public PokemonSprite(Parcel in) {
        front = in.readString();
        back = in.readString();
        frontFallback = in.readString();
        backFallback = in.readString();
    }

    public PokemonSprite(String front, String back, String frontFallback, String backFallback) {
        this.front = front;
        this.back = back;
        this.frontFallback = frontFallback;
        this.backFallback = backFallback;
    }

    public PokemonSprite(String frontFallback, String backFallback) {
        this.frontFallback = frontFallback;
        this.backFallback = backFallback;
    }

    public String getFront() {
        return front;
    }

    public void setFront(String front) {
        this.front = front;
    }

    public String getBack() {
        return back;
    }

    public void setBack(String back) {
        this.back = back;
    }

    public String getFrontFallback() {
        return frontFallback;
    }

    public void setFrontFallback(String frontFallback) {
        this.frontFallback = frontFallback;
    }

    public String getBackFallback() {
        return backFallback;
    }

    public void setBackFallback(String backFallback) {
        this.backFallback = backFallback;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(front);
        dest.writeString(back);
        dest.writeString(frontFallback);
        dest.writeString(backFallback);
    }

    @Override
    public int describeContents() { return 0; }

    public static final Creator<PokemonSprite> CREATOR = new Creator<>() {
        @Override
        public PokemonSprite createFromParcel(Parcel in) {
            return new PokemonSprite(in);
        }

        @Override
        public PokemonSprite[] newArray(int size) {
            return new PokemonSprite[size];
        }
    };
}
