package com.example.smarija.mediaplayer;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.io.File;


/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link BlankFragment.OnFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link BlankFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class BlankFragment extends Fragment {

    TextView tv;
    String textView;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View v=inflater.inflate(R.layout.fragment_blank, container, false);
       Button btnBack=v.findViewById(R.id.btn);
       btnBack.setOnClickListener(new View.OnClickListener() {
           @Override
           public void onClick(View v) {
               OnFragmentInteractionListener c= (OnFragmentInteractionListener) getActivity();
               c.goBack();
           }
       });

        tv=v.findViewById(R.id.textViewForVideo);
        tv.setText(textView);
        return v;
    }
    public interface OnFragmentInteractionListener {
        void goBack();

    }
    public void updateTextView(String text) {
         textView=text;
    }
}
