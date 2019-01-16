package cafe.plastic.documentscanner.ui.fragments;

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

@SuppressWarnings("WeakerAccess")
public class ConfirmationFragment extends Fragment {

    private PhotoView takenPicture;

    public ConfirmationFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_confirmation, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        takenPicture = view.findViewById(R.id.takenPicture);
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        CaptureViewModel mViewModel = ViewModelProviders.of(requireActivity()).get(CaptureViewModel.class);
        mViewModel.currentPhoto.observe(this, photo -> takenPicture.setImageBitmap(photo));
    }
}
