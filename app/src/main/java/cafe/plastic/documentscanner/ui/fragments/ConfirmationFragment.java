package cafe.plastic.documentscanner.ui.fragments;

import android.graphics.Matrix;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.github.chrisbanes.photoview.PhotoView;

import androidx.lifecycle.ViewModelProviders;
import cafe.plastic.documentscanner.R;
import timber.log.Timber;

public class ConfirmationFragment extends Fragment {

    private PhotoView takenPicture;
    private CaptureViewModel mViewModel;

    public ConfirmationFragment() {
        // Required empty public constructor
    }


    public static ConfirmationFragment newInstance(Bundle arguments) {
        ConfirmationFragment fragment = new ConfirmationFragment();
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_confirmation, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        takenPicture = getView().findViewById(R.id.takenPicture);
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        mViewModel = ViewModelProviders.of(getActivity()).get(CaptureViewModel.class);
        mViewModel.currentPhoto.observe(this, photo -> {
            takenPicture.setImageBitmap(photo);
        });
    }
}
