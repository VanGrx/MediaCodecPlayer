package com.example.smarija.mediaplayer;

import android.app.ListFragment;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.app.Fragment;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import java.io.File;
import java.io.FilenameFilter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;


/**
 * A fragment representing a list of Items.
 * <p/>
 * Activities containing this fragment MUST implement the {@link OnListFragmentInteractionListener}
 * interface.
 */
public class FileFragment extends ListFragment {

    public final static String EXTRA_FILE_PATH = "file_path";
    public final static String EXTRA_SHOW_HIDDEN_FILES = "show_hidden_files";
    public final static String EXTRA_ACCEPTED_FILE_EXTENSIONS = "*";
    private final static String DEFAULT_INITIAL_DIRECTORY = "/mnt/sdcard";

    protected File Directory;
    protected ArrayList<File> Files;
    protected FileFragment.FileFragmentListAdapter Adapter;
    protected boolean ShowHiddenFiles = false;
    protected String[] acceptedFileExtensions;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

//        LayoutInflater inflator = (LayoutInflater)
//                getSystemService(Context.LAYOUT_INFLATER_SERVICE);


        // Set initial directory
        Directory = new File(DEFAULT_INITIAL_DIRECTORY);


        // Initialize the ArrayList
        Files = new ArrayList<File>();

        // Set the ListAdapter
        Adapter = new FileFragment.FileFragmentListAdapter(getActivity(), Files);
        setListAdapter(Adapter);

        // Initialize the extensions array to allow any file extensions
        acceptedFileExtensions = new String[] {};

        // Get intent extras
//        if(getActivity().getIntent().hasExtra(EXTRA_FILE_PATH))
//            Directory = new File(getActivity().getIntent().getStringExtra(EXTRA_FILE_PATH));

        if(getActivity().getIntent().hasExtra(EXTRA_SHOW_HIDDEN_FILES))
            ShowHiddenFiles = getActivity().getIntent().getBooleanExtra(EXTRA_SHOW_HIDDEN_FILES, false);

        if(getActivity().getIntent().hasExtra(EXTRA_ACCEPTED_FILE_EXTENSIONS)) {

            ArrayList<String> collection =
                    getActivity().getIntent().getStringArrayListExtra(EXTRA_ACCEPTED_FILE_EXTENSIONS);

            acceptedFileExtensions = (String[])
                    collection.toArray(new String[collection.size()]);
            for(int i=0;i<acceptedFileExtensions.length;i++)
            {
            }
        }
        Log.e("IGOR","REFRESHUJ!!!");
        refreshFilesList();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        Log.e("IGOR", "Directory is "+String.valueOf(Directory.listFiles()==null));

        return inflater.inflate(R.layout.fragment_file_list, container, false);
    }

//    @Override
//    protected void onResume() {
//        refreshFilesList();
//        super.onResume();
//    }

    protected void refreshFilesList() {

        Files.clear();
        FileFragment.ExtensionFilenameFilter filter =
                new FileFragment.ExtensionFilenameFilter(acceptedFileExtensions);
        File fileBack= new File(Directory.getParent());
        File[] files = Directory.listFiles();

        Log.e("IGOR", "files is "+String.valueOf(files==null));

        if(files != null && files.length > 0) {

            for(File f : files) {
                Log.e("IGOR","Found file "+f);

                if(f.isHidden() && !ShowHiddenFiles) {

                    continue;
                }

                Files.add(f);
            }

            Collections.sort(Files, new FileFragment.FileComparator());

        }
        Files.add(0, fileBack);
        Adapter.notifyDataSetChanged();
    }

//    @Override
//    public void onBackPressed() {
//
//        if(Directory.getParentFile() != null) {
//
//            Directory = Directory.getParentFile();
//            refreshFilesList();
//            return;
//        }
//
//        super.onBackPressed();
//    }

    @Override
    public void onListItemClick(ListView l, View v, int position, long id) {

        File newFile = (File)l.getItemAtPosition(position);
        if(newFile.isFile()) {

//            Intent extra = new Intent();
//            extra.putExtra(EXTRA_FILE_PATH, newFile.getAbsolutePath());
//            setResult(RESULT_OK, extra);
//            finish();
        }
        else {

            Directory = newFile;
            refreshFilesList();
        }
        Communicate c = (Communicate) getActivity();
        c.sendText(newFile.getAbsolutePath());

        super.onListItemClick(l, v, position, id);
    }

    private class FileFragmentListAdapter extends ArrayAdapter<File> {

        private List<File> mObjects;

        public FileFragmentListAdapter(Context context, List<File> objects) {

            super(context, R.layout.list_item, android.R.id.text1, objects);
            mObjects = objects;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {

            View row = null;

            if(convertView == null) {

                LayoutInflater inflater = (LayoutInflater)
                        getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);

                row = inflater.inflate(R.layout.list_item, parent, false);
            }
            else
                row = convertView;

            File object = mObjects.get(position);

            ImageView imageView = (ImageView)row.findViewById(R.id.file_picker_image);
            TextView textView = (TextView)row.findViewById(R.id.file_picker_text);
            textView.setSingleLine(true);
            textView.setText(object.getName());
            if(position==0){
                imageView.setImageResource(R.drawable.ff);
                return row;
            }
            if(object.isFile())
                imageView.setImageResource(R.drawable.file);

            else
                imageView.setImageResource(R.drawable.pdppr);

            return row;
        }
    }

    private class FileComparator implements Comparator<File> {

        public int compare(File f1, File f2) {

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

    private class ExtensionFilenameFilter implements FilenameFilter {

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
}
