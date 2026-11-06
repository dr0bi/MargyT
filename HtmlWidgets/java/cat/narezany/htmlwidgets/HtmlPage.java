package cat.narezany.htmlwidgets;

import android.content.Context;
import android.net.Uri;
import android.webkit.WebResourceRequest;
import android.webkit.WebResourceResponse;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.util.Locale;

/**
 * A small local-file WebView loader.
 *
 * It serves only files below the selected site root and blocks path traversal.
 * JavaScript is enabled for ordinary site functionality. Do not add a
 * JavaScript bridge exposing Android objects to untrusted HTML.
 */
public final class HtmlPage {
    private static final String HOST = "https://margyt.local/";
    private HtmlPage() {}

    public static WebView create(Context context, File siteRoot) {
        WebView web = new WebView(context);
        web.getSettings().setJavaScriptEnabled(true);
        web.getSettings().setDomStorageEnabled(true);
        web.getSettings().setAllowFileAccess(false);
        web.getSettings().setAllowContentAccess(false);
        web.setWebViewClient(new LocalSiteClient(siteRoot));
        web.loadUrl(HOST + "index.html");
        return web;
    }

    private static final class LocalSiteClient extends WebViewClient {
        private final File root;
        private final String rootPath;

        LocalSiteClient(File root) {
            this.root = root.getAbsoluteFile();
            this.rootPath = this.root.getAbsolutePath() + File.separator;
        }

        @Override
        public WebResourceResponse shouldInterceptRequest(
            WebView view, WebResourceRequest request
        ) {
            return serve(request.getUrl());
        }

        @Override
        public WebResourceResponse shouldInterceptRequest(WebView view, String url) {
            return serve(Uri.parse(url));
        }

        private WebResourceResponse serve(Uri uri) {
            if (!"margyt.local".equals(uri.getHost())) return null;
            String relative = uri.getPath();
            if (relative == null || relative.length() == 0 || "/".equals(relative)) {
                relative = "/index.html";
            }

            // Decode URL path, normalize it, then verify canonical location.
            try {
                String decoded = Uri.decode(relative);
                if (decoded.contains("\0")) return notFound();
                File target = new File(root, decoded.startsWith("/")
                    ? decoded.substring(1) : decoded).getCanonicalFile();

                String canonical = target.getCanonicalPath();
                if (!canonical.equals(root.getCanonicalPath())
                    && !canonical.startsWith(rootPath)) {
                    return notFound();
                }
                if (!target.isFile()) return notFound();

                InputStream input = new FileInputStream(target);
                String mime = mimeFor(target.getName());
                String encoding = isText(mime) ? "UTF-8" : null;
                return new WebResourceResponse(mime, encoding, input);
            } catch (Exception e) {
                return notFound();
            }
        }

        private WebResourceResponse notFound() {
            return new WebResourceResponse(
                "text/plain", "UTF-8", 404, "Not Found",
                java.util.Collections.<String, String>emptyMap(),
                new java.io.ByteArrayInputStream(new byte[0])
            );
        }

        private static String mimeFor(String name) {
            String n = name.toLowerCase(Locale.ROOT);
            if (n.endsWith(".html") || n.endsWith(".htm")) return "text/html";
            if (n.endsWith(".css")) return "text/css";
            if (n.endsWith(".js")) return "application/javascript";
            if (n.endsWith(".json")) return "application/json";
            if (n.endsWith(".png")) return "image/png";
            if (n.endsWith(".jpg") || n.endsWith(".jpeg")) return "image/jpeg";
            if (n.endsWith(".gif")) return "image/gif";
            if (n.endsWith(".svg")) return "image/svg+xml";
            if (n.endsWith(".webp")) return "image/webp";
            if (n.endsWith(".woff")) return "font/woff";
            if (n.endsWith(".woff2")) return "font/woff2";
            if (n.endsWith(".ttf")) return "font/ttf";
            if (n.endsWith(".mp4")) return "video/mp4";
            if (n.endsWith(".mp3")) return "audio/mpeg";
            return "application/octet-stream";
        }

        private static boolean isText(String mime) {
            return mime.startsWith("text/")
                || mime.contains("javascript")
                || mime.contains("json")
                || mime.contains("svg");
        }
    }
}
