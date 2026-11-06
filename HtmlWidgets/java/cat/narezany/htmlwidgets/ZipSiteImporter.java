package cat.narezany.htmlwidgets;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

/**
 * Safe ZIP extractor for an HTML site.
 *
 * Call importZip(inputStream, destinationDirectory) after the host file-picker
 * returns a ZIP Uri and the plugin opens its content stream.
 */
public final class ZipSiteImporter {
    private static final int MAX_ENTRIES = 500;
    private static final long MAX_TOTAL_BYTES = 32L * 1024L * 1024L;
    private static final long MAX_FILE_BYTES = 8L * 1024L * 1024L;

    private ZipSiteImporter() {}

    public static File importZip(InputStream source, File destination) throws Exception {
        if (!destination.exists() && !destination.mkdirs()) {
            throw new IllegalStateException("Cannot create destination");
        }

        File canonicalRoot = destination.getCanonicalFile();
        String rootPrefix = canonicalRoot.getCanonicalPath() + File.separator;

        int entries = 0;
        long total = 0;
        byte[] buffer = new byte[8192];

        ZipInputStream zip = new ZipInputStream(new BufferedInputStream(source));
        try {
            ZipEntry entry;
            while ((entry = zip.getNextEntry()) != null) {
                entries++;
                if (entries > MAX_ENTRIES) throw new IllegalArgumentException("Too many ZIP entries");

                String name = entry.getName().replace('\\', '/');
                if (name.startsWith("/") || name.contains("../") || name.equals("..")
                    || name.contains("/..") || name.indexOf('\0') >= 0) {
                    throw new IllegalArgumentException("Unsafe ZIP path");
                }

                File out = new File(canonicalRoot, name).getCanonicalFile();
                String outPath = out.getCanonicalPath();
                if (!outPath.equals(canonicalRoot.getCanonicalPath())
                    && !outPath.startsWith(rootPrefix)) {
                    throw new IllegalArgumentException("ZIP path escapes destination");
                }

                if (entry.isDirectory()) {
                    if (!out.exists() && !out.mkdirs()) {
                        throw new IllegalStateException("Cannot create directory");
                    }
                    zip.closeEntry();
                    continue;
                }

                File parent = out.getParentFile();
                if (parent != null && !parent.exists() && !parent.mkdirs()) {
                    throw new IllegalStateException("Cannot create parent directory");
                }

                FileOutputStream output = new FileOutputStream(out);
                long fileBytes = 0;
                try {
                    int read;
                    while ((read = zip.read(buffer)) != -1) {
                        fileBytes += read;
                        total += read;
                        if (fileBytes > MAX_FILE_BYTES || total > MAX_TOTAL_BYTES) {
                            throw new IllegalArgumentException("ZIP expands beyond size limit");
                        }
                        output.write(buffer, 0, read);
                    }
                } finally {
                    output.close();
                }
                zip.closeEntry();
            }
        } finally {
            zip.close();
        }

        File index = findIndex(canonicalRoot);
        if (index == null) throw new IllegalArgumentException("ZIP does not contain index.html");
        return index.getParentFile();
    }

    private static File findIndex(File root) {
        File direct = new File(root, "index.html");
        if (direct.isFile()) return direct;
        return findIndexRecursive(root, 0);
    }

    private static File findIndexRecursive(File dir, int depth) {
        if (depth > 4) return null;
        File[] children = dir.listFiles();
        if (children == null) return null;
        for (File child : children) {
            if (child.isDirectory()) {
                File found = findIndexRecursive(child, depth + 1);
                if (found != null) return found;
            } else if ("index.html".equalsIgnoreCase(child.getName())) {
                return child;
            }
        }
        return null;
    }
}
