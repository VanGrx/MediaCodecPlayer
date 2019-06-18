package com.example.smarija.mediaplayer;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;


public class InfoFragment extends Fragment {

    TextView videoTitleView, videoDurationView, videoResolutionView, videoFormatView;
    String textTitle, textFormat;
    int textHeight, textWidth;
    long textDuration;

    @SuppressLint("SetTextI18n")
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
        videoTitleView=v.findViewById(R.id.textViewForVideo);
        videoTitleView.setText(textTitle);

        videoResolutionView =v.findViewById(R.id.textViewForVideoResolution);
        videoResolutionView.setText(textWidth + "x" + textHeight);

        int minutes= (int) (textDuration/60);
        int seconds= (int) (textDuration%60);
        @SuppressLint("DefaultLocale") String min=String.format("%02d", minutes);
        @SuppressLint("DefaultLocale") String sec=String.format("%02d", seconds);
        videoDurationView =v.findViewById(R.id.textViewForVideoDuration);
        videoDurationView.setText(min+":"+sec);

        videoFormatView =v.findViewById(R.id.textViewForVideoFormat);
        videoFormatView.setText(textFormat);
        return v;
    }

    public interface OnFragmentInteractionListener {
        void goBack();

    }
    public void updateTextView(String videoTitle, int videoWidth, int videoHeight, long videoDuration, String videoFormat) {
         textTitle=videoTitle;
         textHeight=videoHeight;
         textWidth =videoWidth;
         textDuration= (long) (videoDuration*(0.000001)); //u sekundama je
         textFormat=videoFormat;

    }
}
