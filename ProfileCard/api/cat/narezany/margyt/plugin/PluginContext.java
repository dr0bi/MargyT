package cat.narezany.margyt.plugin;

import android.app.Activity;
import android.content.SharedPreferences;

public final class PluginContext {
    public interface Tapped { void tapped(); }
    public interface Downloaded { void finished(java.io.File file, boolean success); }
    public interface Selected { void selected(int index); }

    public SharedPreferences prefs();
    public void log(String line);
    public void addSettingsRow(String title, String detail, Tapped action);
    public Activity activity();
    public void away(String what, Runnable work);
    public byte[] fetch(String url);
    public void onUiThread(Runnable action);
}