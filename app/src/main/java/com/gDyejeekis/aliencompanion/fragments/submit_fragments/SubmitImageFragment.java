package com.gDyejeekis.aliencompanion.fragments.submit_fragments;


import android.os.Bundle;
import android.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.gDyejeekis.aliencompanion.R;

/**
 * A simple {@link Fragment} subclass.
 */
public class SubmitImageFragment extends Fragment {

    @Override
    public void onCreate(Bundle bundle) {
        super.onCreate(bundle);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_submit_image, container, false);
    }


}
