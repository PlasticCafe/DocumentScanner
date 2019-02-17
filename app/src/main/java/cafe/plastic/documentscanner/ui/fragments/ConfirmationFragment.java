package cafe.plastic.documentscanner.ui.fragments;

import android.graphics.Bitmap;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.github.chrisbanes.photoview.PhotoView;

import androidx.lifecycle.ViewModelProviders;
import androidx.navigation.fragment.NavHostFragment;
import cafe.plastic.documentscanner.R;
import cafe.plastic.documentscanner.util.TempImageManager;
import io.reactivex.Single;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.schedulers.Schedulers;
import timber.log.Timber;

@SuppressWarnings("WeakerAccess")
public class ConfirmationFragment extends Fragment {

    private PhotoView takenPicture;
    private String imageLocation;
    private Single<Bitmap> image;
    CompositeDisposable observers = new CompositeDisposable();

    public ConfirmationFragment() {
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
        image = Single
                .<Bitmap>create(s -> {
                    s.onSuccess(TempImageManager.getInstance(getContext()).loadTempBitmap(imageLocation));
                    Timber.d("Loading image from disk");
                })
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .cache();

    }

    @Override
    public void onResume() {
        super.onResume();
        observers.add(
                image.subscribe(b ->
                                takenPicture.setImageBitmap(b),
                        e -> {
                            Timber.d("Failed to load image, returning");
                            NavHostFragment.findNavController(this).popBackStack();
                        }
                )
        );
    }

    @Override
    public void onPause() {
        super.onPause();
        observers.dispose();
    }
}
