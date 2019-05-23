package com.example.smarija.mediaplayer;

import android.app.ListFragment;
import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;

import static com.example.smarija.mediaplayer.R.*;


/**
 * A fragment representing a list of Items.
 * <p/>
 * Activities containing this fragment MUST implement the {@link OnListFragmentInteractionListener}
 * interface.
 */
public class FileFragment extends ListFragment {

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


        // Set initial directory
        Directory = new File(DEFAULT_INITIAL_DIRECTORY);


        // Initialize the ArrayList
        Files = new ArrayList<>();

        // Set the ListAdapter
        Adapter = new FileFragment.FileFragmentListAdapter(getActivity(), Files);
        setListAdapter(Adapter);


        // Initialize the extensions array to allow any file extensions
        acceptedFileExtensions = new String[]{};

        // Get intent extras
//        if(getActivity().getIntent().hasExtra(EXTRA_FILE_PATH))
//            Directory = new File(getActivity().getIntent().getStringExtra(EXTRA_FILE_PATH));

        if (getActivity().getIntent().hasExtra(EXTRA_SHOW_HIDDEN_FILES))
            ShowHiddenFiles = getActivity().getIntent().getBooleanExtra(EXTRA_SHOW_HIDDEN_FILES, false);

        if (getActivity().getIntent().hasExtra(EXTRA_ACCEPTED_FILE_EXTENSIONS)) {

            ArrayList<String> collection =
                    getActivity().getIntent().getStringArrayListExtra(EXTRA_ACCEPTED_FILE_EXTENSIONS);

            acceptedFileExtensions = collection.toArray(new String[0]);

        }
        refreshFilesList();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        Log.e("IGOR", "Directory is " + String.valueOf(Directory.listFiles() == null));

        return inflater.inflate(layout.fragment_file_list, container, false);
    }

    @Override
    public void onResume() {
        refreshFilesList();
        super.onResume();
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        getListView().setSelector(R.drawable.level_list_selector);
    }

    protected void refreshFilesList() {

        Files.clear();
        File fileBack = new File(Directory.getParent());
        File[] files = Directory.listFiles();


        if (files != null && files.length > 0) {

            for (File f : files) {

                if (f.isHidden() && !ShowHiddenFiles) {
                    continue;
                }

                Files.add(f);
            }

            Collections.sort(Files, new FileFragment.FileComparator());

        }
        Files.add(0, fileBack);
        Adapter.notifyDataSetChanged();
    }

    @Override
    public void onListItemClick(ListView l, View v, int position, long id) {

        File newFile = (File) l.getItemAtPosition(position);
        if (!newFile.isFile()) {

            Directory = newFile;
            refreshFilesList();
        }
        OnListFragmentInteractionListener c = (OnListFragmentInteractionListener) getActivity();
        c.sendText(newFile.getAbsolutePath());

        super.onListItemClick(l, v, position, id);
    }

    public interface OnListFragmentInteractionListener {
        void sendText(String s);
    }

    private class FileFragmentListAdapter extends ArrayAdapter<File> {

        private List<File> mObjects;

        FileFragmentListAdapter(Context context, List<File> objects) {

            super(context, layout.list_item, android.R.id.text1, objects);
            mObjects = objects;
        }

        @NonNull
        @Override
        public View getView(int position, View convertView, @NonNull ViewGroup parent) {

            View row;

            if (convertView == null) {

                LayoutInflater inflater = (LayoutInflater)
                        getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);

                row = Objects.requireNonNull(inflater).inflate(layout.list_item, parent, false);
            } else
                row = convertView;

            File object = mObjects.get(position);

            ImageView imageView = row.findViewById(id.file_picker_image);
            TextView textView = row.findViewById(id.file_picker_text);
            textView.setSingleLine(true);
            textView.setText(object.getName());
            if (position == 0) {
                imageView.setImageResource(drawable.images_back);
                return row;
            }
            if (object.isFile())
                imageView.setImageResource(drawable.file_icon);

            else
                imageView.setImageResource(drawable.images);

            return row;
        }
    }

    private class FileComparator implements Comparator<File> {

        public int compare(File f1, File f2) {

            if (f1 == f2)
                return 0;

            if (f1.isDirectory() && f2.isFile())
                // Show directories above files
                return -1;

            if (f1.isFile() && f2.isDirectory())
                // Show files below directories
                return 1;

            // Sort the directories alphabetically
            return f1.getName().compareToIgnoreCase(f2.getName());
        }
    }

}
