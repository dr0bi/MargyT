package cat.narezany.margyt.plugin;

import android.app.Activity;
import android.content.Context;

public abstract class MargyPlugin {
    public static final int API = 5;
    public final void attach(PluginContext context);
    public final PluginContext margyt();
    public void onStart(Context context) {}
    public boolean onOpenSettings(Activity activity) { return false; }
    public void onActivityResumed(Activity activity) {}
    public void onStop() {}
}