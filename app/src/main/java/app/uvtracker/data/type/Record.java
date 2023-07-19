package app.uvtracker.data.type;

import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import org.json.JSONException;
import org.json.JSONObject;

public class Record implements IFlattenable {

    private static final String TAG = Record.class.getSimpleName();

    private static float round2(float input) {
        if(Float.isNaN(input)) return 0.0f;
        return Math.round(input * 100.0f) / 100.0f;
    }

    @Nullable
    public static Record unflatten(@NonNull String input) {
        try {
            JSONObject obj = new JSONObject(input);
            return new Record(
                    (float)obj.getDouble("uv"),
                    (float)obj.getDouble("vis")
            );
        } catch (JSONException e) {
            Log.w(TAG, "Could not parse record " + input);
            e.printStackTrace();
            return null;
        }
    }


    private final float uvIndex;
    private final float illuminance;

    @Nullable
    private String flattenedString;

    public Record(float uvIndex, float illuminance) {
        this.uvIndex = round2(uvIndex);
        this.illuminance = round2(illuminance);
    }

    @NonNull
    @Override
    public String flatten() {
        if(this.flattenedString == null)
            this.flattenedString = this.flattenCore();
        return this.flattenedString;
    }

    @NonNull
    public String flattenCore() {
        try {
            JSONObject obj = new JSONObject();
            obj.put("uv", this.uvIndex);
            obj.put("vis", this.illuminance);
            return obj.toString();
        } catch (JSONException e) {
            // As per documented suggestion
            throw new RuntimeException(e);
        }
    }

    @NonNull
    @Override
    public String toString() {
        return this.flatten();
    }

}
