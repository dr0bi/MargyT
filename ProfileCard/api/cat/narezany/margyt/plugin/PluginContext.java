package cat.narezany.margyt.plugin;

import android.app.Activity;
import android.content.SharedPreferences;

public final class PluginContext {
    public interface Tapped { void tapped(); }
    public interface Downloaded { void finished(java.io.File file, boolean success); }
    public interface Selected { void selected(int index); }

    public native SharedPreferences prefs();
    public native void log(String line);
    public native void addSettingsRow(String title, String detail, Tapped action);
    public native Activity activity();
    public native void away(String what, Runnable work);
    public native byte[] fetch(String url);
    public native void onUiThread(Runnable action);
}