package com.example.smarija.mediaplayer;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;


public class InfoFragment extends Fragment {

    TextView tv;
    String textView;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        // Inflate the layout for this fragment
        View v=inflater.inflate(R.layout.fragment_blank, container, false);
       Button btnBack=v.findViewById(R.id.btn);
       btnBack.setOnClickListener(new View.OnClickListener() {
           @Override
           public void onClick(View v) {
               OnFragmentInteractionListener c= (OnFragmentInteractionListener) getActivity();
               assert c != null;
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
