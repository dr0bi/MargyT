package cat.narezany.htmlwidgets;

final class DemoHtml {
    private DemoHtml() {}
    static final String CONTENT =
        "<!doctype html><html><head><meta name='viewport' content='width=device-width,initial-scale=1'>" +
        "<style>body{font-family:sans-serif;padding:18px;background:#171923;color:#fff}" +
        ".card{padding:16px;border-radius:14px;background:#292d3e}button{padding:10px 14px;border:0;border-radius:9px}</style>" +
        "</head><body><div class='card'><h2>HTML Widget</h2>" +
        "<p>Your local HTML project will appear here.</p>" +
        "<button onclick='document.getElementById(\"status\").textContent=\"JavaScript works!\"'>Test JS</button>" +
        "<p id='status'>Waiting...</p></div></body></html>";
}
