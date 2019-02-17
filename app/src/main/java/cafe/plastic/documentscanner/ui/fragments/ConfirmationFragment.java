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
import cafe.plastic.documentscanner.util.TempImageManager;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;

@SuppressWarnings("WeakerAccess")
public class ConfirmationFragment extends Fragment {

    private PhotoView takenPicture;
    private TempImageManager imageManager;
    private String imageLocation;
    CompositeDisposable observers = new CompositeDisposable();

    public ConfirmationFragment() {
        // Required empty public constructor
        imageManager = TempImageManager.getInstance(getContext());
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
        imageLocation = ConfirmationFragmentArgs.fromBundle(getArguments()).getImageFileName();
        takenPicture = view.findViewById(R.id.takenPicture);

    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
    }

    @Override
    public void onResume() {
        super.onResume();
        observers.add(imageManager.loadTempBitmap(imageLocation)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(b -> {
                    takenPicture.setImageBitmap(b);
                }));
    }

    @Override
    public void onPause() {
        super.onPause();
        observers.dispose();
    }
}
