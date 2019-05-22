package com.example.smarija.mediaplayer.dummy;

import android.util.Log;

import java.io.File;
import java.io.FilenameFilter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


public class DummyContent {
    /**
     *
     */
    public static File Directory;
    private final static String DEFAULT_INITIAL_DIRECTORY = "/mnt/sdcard";
    protected static boolean ShowHiddenFiles = false;
    /**
     * An array of sample (dummy) items.
     */
    public static final List<FileItem> ITEMS = new ArrayList<FileItem>();
    /**
     * A map of sample (dummy) items, by ID.
     */
    public static final Map<String, FileItem> ITEM_MAP = new HashMap<String, FileItem>();


    static {

            addItems();

    }

    private static void addItems() {
        // Set initial directory
        Directory = new File(DEFAULT_INITIAL_DIRECTORY);
        String[] acceptedFileExtensions= new String[] {};

        ExtensionFilenameFilter filter =
                new ExtensionFilenameFilter(acceptedFileExtensions);
        File[] files = Directory.listFiles(filter);
        if(files != null && files.length > 0) {

            for(File f : files) {

                if(f.isHidden() && !ShowHiddenFiles) {

                    continue;
                }

                ITEMS.add(new FileItem(f.getName(), f));
            }

            Collections.sort(ITEMS, new FileComparator());
        }
//        ITEMS.add(item);
//        ITEM_MAP.put(item.id, item);
    }

    /**
     * A dummy item representing a piece of content.
     */
    public static class FileItem {
        public final String id;
        public final File file;

        public FileItem(String id, File file) {
            this.id = id;
            this.file = file;
        }

        @Override
        public String toString() {
            return id;
        }
    }
    private static class ExtensionFilenameFilter implements FilenameFilter {

        private String[] Extensions;

        public ExtensionFilenameFilter(String[] extensions) {

            super();
            Extensions = extensions;
        }

        public boolean accept(File dir, String filename) { //biranje direktorijuma i fajlova

            if(new File(dir, filename).isDirectory()) {

                // Accept all directory names
                return true;
            }

            if(Extensions != null && Extensions.length > 0) {

                for(int i = 0; i < Extensions.length; i++) {

                    if(filename.endsWith(Extensions[i])) {

                        // The filename ends with the extension
                        return true;
                    }
                }
                // The filename did not match any of the extensions
                return false;
            }
            // No extensions has been set. Accept all file extensions.
            return true;
        }
    }
    private static class FileComparator implements Comparator<FileItem> {

        public int compare(FileItem fi1, FileItem fi2) {
            File f1 = fi1.file;
            File f2 = fi2.file;

            if(f1 == f2)
                return 0;

            if(f1.isDirectory() && f2.isFile())
                // Show directories above files
                return -1;

            if(f1.isFile() && f2.isDirectory())
                // Show files below directories
                return 1;

            // Sort the directories alphabetically
            return f1.getName().compareToIgnoreCase(f2.getName());
        }
    }
}
