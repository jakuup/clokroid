package com.jakuup.clokroid;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;

import androidx.fragment.app.Fragment;

public class ExitClearFragment extends Fragment {

    private final View.OnClickListener onClickListener = view -> {
        CheckBox checkBoxClear = getView().findViewById(R.id.checkBoxClear);

        if (checkBoxClear.isChecked()) {
            ((ScrolledString)getActivity()).returnClear();
        }
        else {
            ((ScrolledString)getActivity()).returnExit();
        }
    };

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_exit_clear, container, false);
        view.findViewById(R.id.buttonExCrFragmentExit).setOnClickListener(onClickListener);
        return view;
    }

}
