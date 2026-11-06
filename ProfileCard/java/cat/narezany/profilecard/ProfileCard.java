package cat.narezany.profilecard;

import android.app.Activity;
import android.app.AlertDialog;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import cat.narezany.margyt.plugin.MargyPlugin;

public final class ProfileCard extends MargyPlugin {
    private static final String[] KEYS = {
        "banner_url", "username", "nickname", "avatar_url", "followers", "likes"
    };
    private static final String[] LABELS = {
        "Banner image URL (HTTPS)", "Username (@user)", "Nickname",
        "Avatar image URL (HTTPS)", "Followers", "Likes"
    };
    private static final String[] DEFAULTS = {
        "", "@username", "Your nickname", "", "0", "0"
    };

    @Override
    public void onStart(android.content.Context context) {
        margyt().addSettingsRow("Profile card", "Edit and preview your custom profile card", () -> {
            Activity activity = margyt().activity();
            if (activity != null && !activity.isFinishing()) showEditor(activity);
        });
        margyt().log("Profile Card ready");
    }

    @Override
    public boolean onOpenSettings(Activity activity) {
        if (activity == null || activity.isFinishing()) return false;
        showEditor(activity);
        return true;
    }

    private void showEditor(final Activity activity) {
        final EditText[] fields = new EditText[KEYS.length];
        LinearLayout form = new LinearLayout(activity);
        form.setOrientation(LinearLayout.VERTICAL);
        int pad = dp(activity, 16);
        form.setPadding(pad, dp(activity, 4), pad, dp(activity, 4));

        TextView hint = new TextView(activity);
        hint.setText("Fill in the details. Image links must start with https://");
        hint.setTextSize(13);
        form.addView(hint, new LinearLayout.LayoutParams(-1, -2));

        for (int i = 0; i < KEYS.length; i++) {
            TextView label = new TextView(activity);
            label.setText(LABELS[i]);
            label.setTextSize(13);
            label.setPadding(0, dp(activity, 12), 0, dp(activity, 4));
            form.addView(label);
            EditText input = new EditText(activity);
            input.setSingleLine(true);
            input.setText(margyt().prefs().getString(KEYS[i], DEFAULTS[i]));
            input.setTextSize(14);
            input.setHint(LABELS[i]);
            form.addView(input, new LinearLayout.LayoutParams(-1, dp(activity, 46)));
            fields[i] = input;
        }

        ScrollView scroll = new ScrollView(activity);
        scroll.addView(form);
        new AlertDialog.Builder(activity)
            .setTitle("Profile card editor")
            .setView(scroll)
            .setPositiveButton("Save & preview", (dialog, which) -> {
                android.content.SharedPreferences.Editor edit = margyt().prefs().edit();
                for (int i = 0; i < KEYS.length; i++) {
                    String value = fields[i].getText().toString().trim();
                    if ((i == 0 || i == 3) && !value.isEmpty() && !value.startsWith("https://")) {
                        Toast.makeText(activity, LABELS[i] + " must use HTTPS", Toast.LENGTH_LONG).show();
                        return;
                    }
                    if (value.length() > 500) {
                        Toast.makeText(activity, "Text is too long", Toast.LENGTH_SHORT).show();
                        return;
                    }
                    edit.putString(KEYS[i], value);
                }
                edit.apply();
                showPreview(activity);
            })
            .setNeutralButton("Preview", (dialog, which) -> showPreview(activity))
            .setNegativeButton("Close", null)
            .show();
    }

    private void showPreview(final Activity activity) {
        final String bannerUrl = value("banner_url");
        final String avatarUrl = value("avatar_url");
        final LinearLayout card = new LinearLayout(activity);
        card.setOrientation(LinearLayout.VERTICAL);
        card.setPadding(dp(activity, 14), 0, dp(activity, 14), dp(activity, 18));
        card.setBackground(round(Color.rgb(30, 30, 34), dp(activity, 18)));

        final ImageView banner = new ImageView(activity);
        banner.setScaleType(ImageView.ScaleType.CENTER_CROP);
        banner.setBackground(round(Color.rgb(68, 73, 90), dp(activity, 12)));
        card.addView(banner, new LinearLayout.LayoutParams(-1, dp(activity, 116)));

        final ImageView avatar = new ImageView(activity);
        avatar.setScaleType(ImageView.ScaleType.CENTER_CROP);
        avatar.setBackground(round(Color.rgb(90, 95, 110), dp(activity, 100)));
        LinearLayout.LayoutParams avatarParams = new LinearLayout.LayoutParams(dp(activity, 76), dp(activity, 76));
        avatarParams.topMargin = dp(activity, -34);
        avatarParams.leftMargin = dp(activity, 12);
        card.addView(avatar, avatarParams);

        TextView nickname = new TextView(activity);
        nickname.setText(value("nickname"));
        nickname.setTextColor(Color.WHITE);
        nickname.setTextSize(21);
        nickname.setTypeface(Typeface.DEFAULT, Typeface.BOLD);
        nickname.setPadding(dp(activity, 4), dp(activity, 8), 0, 0);
        card.addView(nickname);

        TextView username = new TextView(activity);
        username.setText(value("username"));
        username.setTextColor(Color.LTGRAY);
        username.setTextSize(14);
        username.setPadding(dp(activity, 4), dp(activity, 2), 0, dp(activity, 14));
        card.addView(username);

        LinearLayout stats = new LinearLayout(activity);
        stats.setOrientation(LinearLayout.HORIZONTAL);
        stats.addView(stat(activity, "Followers", value("followers")), new LinearLayout.LayoutParams(0, -2, 1));
        stats.addView(stat(activity, "Likes", value("likes")), new LinearLayout.LayoutParams(0, -2, 1));
        card.addView(stats);

        ScrollView wrapper = new ScrollView(activity);
        wrapper.setFillViewport(true);
        wrapper.addView(card);
        AlertDialog preview = new AlertDialog.Builder(activity)
            .setTitle("Profile card preview")
            .setView(wrapper)
            .setPositiveButton("Edit", (d, w) -> showEditor(activity))
            .setNegativeButton("Close", null)
            .create();
        preview.show();

        loadImage(bannerUrl, banner, activity);
        loadImage(avatarUrl, avatar, activity);
    }

    private void loadImage(final String url, final ImageView target, final Activity activity) {
        if (url == null || url.trim().isEmpty()) return;
        margyt().away("load profile image", () -> {
            try {
                byte[] bytes = margyt().fetch(url);
                if (bytes == null || bytes.length == 0 || bytes.length > 8 * 1024 * 1024) return;
                final Bitmap bitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
                if (bitmap != null && !activity.isFinishing()) {
                    margyt().onUiThread(() -> {
                        if (!activity.isFinishing()) target.setImageBitmap(bitmap);
                    });
                }
            } catch (Throwable error) {
                margyt().log("Image load failed: " + error.getClass().getSimpleName());
            }
        });
    }

    private LinearLayout stat(Activity activity, String label, String number) {
        LinearLayout box = new LinearLayout(activity);
        box.setOrientation(LinearLayout.VERTICAL);
        TextView value = new TextView(activity);
        value.setText(number);
        value.setTextColor(Color.WHITE);
        value.setTextSize(19);
        value.setTypeface(Typeface.DEFAULT, Typeface.BOLD);
        TextView caption = new TextView(activity);
        caption.setText(label);
        caption.setTextColor(Color.LTGRAY);
        caption.setTextSize(12);
        box.addView(value);
        box.addView(caption);
        return box;
    }

    private String value(String key) {
        return margyt().prefs().getString(key, "");
    }

    private static GradientDrawable round(int color, int radius) {
        GradientDrawable shape = new GradientDrawable();
        shape.setColor(color);
        shape.setCornerRadius(radius);
        return shape;
    }

    private static int dp(Activity activity, int value) {
        return (int) (value * activity.getResources().getDisplayMetrics().density + 0.5f);
    }
}