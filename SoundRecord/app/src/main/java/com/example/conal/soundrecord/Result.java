// AUTHORS - This code was written by Artem Usov, 2296905u@student.gla.ac.uk, Olga Jodelka 2266755K@student.gla.ac.uk
// Kara Newlands 2264457n@student.gla.ac.uk, Conall Clements 2197397c@student.gla.ac.uk
// It is licensed under the GNU General Public License version 3, 29 June 2007 (https://www.gnu.org/licenses/gpl-3.0.en.html)

package com.example.conal.soundrecord;

import android.os.Parcel;
import android.os.Parcelable;

public class Result implements Parcelable {
    private final int firstBounce;
    private final int secondBounce;
    private final double timeOfBounce;
    private double bounceHeight;

    public Result(int firstBounce, int secondBounce, double timeOfBounce) {
        this.firstBounce = firstBounce;
        this.secondBounce = secondBounce;
        this.timeOfBounce = timeOfBounce;
    }

    int getFirstBounce() {
        return firstBounce;
    }

    int getSecondBounce() {
        return secondBounce;
    }

    double getTimeOfBounce() {
        return timeOfBounce;
    }

    double getBounceHeight() {
        return bounceHeight;
    }

    void setBounceHeight(double bounceHeight) {
        this.bounceHeight = bounceHeight;
    }

    public String toString() {
        return "" + bounceHeight;
    }

    // PARCELABLE METHODS
    Result(Parcel in) {
        firstBounce = in.readInt();
        secondBounce = in.readInt();
        timeOfBounce = in.readDouble();
        bounceHeight = in.readDouble();
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(firstBounce);
        dest.writeInt(secondBounce);
        dest.writeDouble(timeOfBounce);
        dest.writeDouble(bounceHeight);
    }

    @SuppressWarnings("unused")
    public static final Parcelable.Creator<Result> CREATOR = new Parcelable.Creator<Result>() {
        @Override
        public Result createFromParcel(Parcel in) {
            return new Result(in);
        }

        @Override
        public Result[] newArray(int size) {
            return new Result[size];
        }
    };
}
