package cat.narezany.htmlwidgets;

import android.app.Activity;
import android.app.AlertDialog;
import android.view.ViewGroup;
import android.webkit.WebView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;

import cat.narezany.margyt.plugin.MargyPlugin;

/**
 * HTML Widgets plugin scaffold for MargyT.
 *
 * This version uses only APIs explicitly described in the supplied MargyT
 * plugin specification. The SAF picker result callback is intentionally left
 * as an integration point because the supplied API does not expose
 * onActivityResult / ActivityResultLauncher callbacks.
 */
public final class HtmlWidgets extends MargyPlugin {
    private static final String PREF_MODE = "display_mode";
    private static final String PREF_WIDTH = "widget_width";
    private static final String PREF_HEIGHT = "widget_height";

    private WebView activeWebView;

    @Override
    public void onStart(android.content.Context context) {
        margyt().log("HTML Widgets loaded");
    }

    @Override
    public boolean onOpenSettings(Activity activity) {
        if (activity == null || activity.isFinishing()) return false;
        showSettings(activity);
        return true;
    }

    private void showSettings(Activity activity) {
        String mode = margyt().prefs().getString(PREF_MODE, "widget");
        int width = margyt().prefs().getInt(PREF_WIDTH, 320);
        int height = margyt().prefs().getInt(PREF_HEIGHT, 220);

        String[] modes = new String[] {
            "Widget (embedded in TikTok screen)",
            "Profile tab (opened by button)"
        };

        int selected = "profile".equals(mode) ? 1 : 0;

        LinearLayout content = new LinearLayout(activity);
        content.setOrientation(LinearLayout.VERTICAL);
        int pad = dp(activity, 16);
        content.setPadding(pad, pad, pad, pad);

        TextView info = new TextView(activity);
        info.setText(
            "Import an HTML file or ZIP website project. " +
            "A ZIP should contain index.html plus its CSS, JS and assets."
        );
        content.addView(info);

        TextView importStatus = new TextView(activity);
        importStatus.setPadding(0, dp(activity, 12), 0, 0);
        importStatus.setText(
            "Import: waiting for a MargyT file-picker result hook."
        );
        content.addView(importStatus);

        AlertDialog dialog = new AlertDialog.Builder(activity)
            .setTitle("HTML Widgets")
            .setSingleChoiceItems(modes, selected, (d, which) -> {
                margyt().prefs().edit()
                    .putString(PREF_MODE, which == 1 ? "profile" : "widget")
                    .apply();
                margyt().log("Display mode changed");
            })
            .setView(content)
            .setPositiveButton("Preview demo", (d, which) -> {
                openDemo(activity, width, height);
            })
            .setNeutralButton("Widget size", (d, which) -> {
                showSizeDialog(activity);
            })
            .setNegativeButton("Close", null)
            .create();

        dialog.show();
    }

    private void showSizeDialog(Activity activity) {
        final String[] sizes = new String[] {
            "Small — 240 × 160 dp",
            "Medium — 320 × 220 dp",
            "Large — 420 × 300 dp"
        };
        dialogSize(activity, sizes);
    }

    private void dialogSize(Activity activity, String[] sizes) {
        new AlertDialog.Builder(activity)
            .setTitle("Widget size")
            .setItems(sizes, (dialog, which) -> {
                int[] widths = {240, 320, 420};
                int[] heights = {160, 220, 300};
                margyt().prefs().edit()
                    .putInt(PREF_WIDTH, widths[which])
                    .putInt(PREF_HEIGHT, heights[which])
                    .apply();
                Toast.makeText(activity, "Size saved", Toast.LENGTH_SHORT).show();
            })
            .setNegativeButton("Cancel", null)
            .show();
    }

    private void openDemo(Activity activity, int widthDp, int heightDp) {
        if (activity == null || activity.isFinishing()) return;

        File demoRoot = new File(activity.getFilesDir(), "htmlwidgets/demo");
        if (!demoRoot.exists() && !demoRoot.mkdirs()) {
            margyt().log("Could not create demo directory");
            return;
        }

        // The demo HTML is embedded in the plugin source and written once.
        File index = new File(demoRoot, "index.html");
        if (!index.exists()) {
            try {
                java.io.FileOutputStream out = new java.io.FileOutputStream(index);
                out.write(DemoHtml.CONTENT.getBytes("UTF-8"));
                out.close();
            } catch (Exception e) {
                margyt().log("Could not write demo HTML: " + e.getClass().getSimpleName());
                return;
            }
        }

        if (activeWebView != null) {
            try {
                activeWebView.destroy();
            } catch (Exception ignored) {}
            activeWebView = null;
        }

        activeWebView = HtmlPage.create(activity, demoRoot);
        int width = dp(activity, widthDp);
        int height = dp(activity, heightDp);
        activeWebView.setLayoutParams(new ViewGroup.LayoutParams(width, height));

        // This preview uses an ordinary Android dialog. Embedding the same
        // WebView into the TikTok profile/feed requires a host-side insertion hook.
        final WebView preview = activeWebView;
        new AlertDialog.Builder(activity)
            .setTitle("HTML Widget preview")
            .setView(preview)
            .setPositiveButton("Close", null)
            .setOnDismissListener(d -> {
                if (activeWebView == preview) {
                    try { preview.stopLoading(); preview.destroy(); } catch (Exception ignored) {}
                    activeWebView = null;
                }
            })
            .show();
        margyt().log("Demo WebView preview opened. TikTok screen insertion hook is still required.");
    }

    @Override
    public void onStop() {
        if (activeWebView != null) {
            try {
                activeWebView.stopLoading();
                activeWebView.destroy();
            } catch (Exception ignored) {}
            activeWebView = null;
        }
    }

    private static int dp(Activity activity, int value) {
        return (int) (value * activity.getResources().getDisplayMetrics().density + 0.5f);
    }
}
