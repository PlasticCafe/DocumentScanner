package cafe.plastic.documentscanner.ui.fragments;

import android.content.Context;
import android.graphics.Bitmap;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.SeekBar;

import com.github.chrisbanes.photoview.PhotoView;

import java.util.concurrent.TimeUnit;

import androidx.lifecycle.ViewModelProviders;
import androidx.navigation.fragment.NavHostFragment;
import cafe.plastic.documentscanner.R;
import cafe.plastic.documentscanner.databinding.ConfirmationFragmentBinding;
import cafe.plastic.documentscanner.util.TempImageManager;
import cafe.plastic.pagedetect.PostProcess;
import io.reactivex.Single;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.processors.PublishProcessor;
import io.reactivex.schedulers.Schedulers;
import timber.log.Timber;

@SuppressWarnings("WeakerAccess")
public class ConfirmationFragment extends Fragment {

    private String imageLocation;
    private Single<Bitmap> imageLoader;
    private Bitmap originalImage;
    private Bitmap scaledImage;
    private Bitmap workingImage;
    private boolean imageLoaded = false;
    private boolean faxOn = false;
    private ConfirmationFragmentBinding binding;
    private ConfirmationViewModel viewModel;
    private PublishProcessor<Boolean> renderEvents;
    CompositeDisposable observers = new CompositeDisposable();

    public ConfirmationFragment() {
        imageLoader = Single
                .<Bitmap>create(s -> {
                    try {
                        s.onSuccess(TempImageManager.getInstance(getContext()).loadTempBitmap(imageLocation));
                    } catch (Exception e) {
                        s.onError(e);
                    }
                })
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .cache();
        renderEvents = PublishProcessor.create();
    }


    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        binding = ConfirmationFragmentBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        imageLocation = ConfirmationFragmentArgs.fromBundle(getArguments()).getImageFileName();
        binding.setHandlers(new Handlers());
        binding.setViewmodel(viewModel);
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        viewModel = ViewModelProviders.of(requireActivity()).get(ConfirmationViewModel.class);
        viewModel.brightness.setValue(50);
        viewModel.contrast.setValue(0);
        viewModel.brightness.observe(this, i -> render());
        viewModel.contrast.observe(this, i -> render());
    }

    @Override
    public void onResume() {
        super.onResume();
        if (!imageLoaded) {
            loadImage();
        } else {
            enableEditingUI();
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        observers.dispose();
    }

    private void initialize() {


    }

    private void setBitmaps(Bitmap sourceImage) {
        originalImage = sourceImage;
        scaledImage = getScaledBitmap(originalImage);
        workingImage = scaledImage.copy(scaledImage.getConfig(), true);
        imageLoaded = true;
    }

    private Bitmap getScaledBitmap(Bitmap sourceImage) {
        int width = sourceImage.getWidth();
        int height = sourceImage.getHeight();
        float factor = Math.min(1000.0f / width, 1000.0f / height);
        if (factor > 1.0f) factor = 1.0f;
        return Bitmap.createScaledBitmap(sourceImage, (int) (width * factor), (int) (height * factor), true);
    }

    private void enableEditingUI() {
        binding.image.setImageBitmap(workingImage);
    }


    private void render() {
        int brightness = viewModel.brightness.getValue();
        int contrast = viewModel.contrast.getValue();
        if (imageLoaded) {
            PostProcess.brightness(scaledImage, workingImage, (float) (brightness - 50) / 50.0f, (contrast) / 40.0f, false);
        }

        if (faxOn) {

            PostProcess.threshold(workingImage, workingImage);
        }
        binding.image.invalidate();
    }

    private void loadImage() {
        observers.add(
                imageLoader.subscribe(
                        b -> {
                            setBitmaps(b);
                            enableEditingUI();
                        },
                        e -> {
                            NavHostFragment.findNavController(this).popBackStack();
                        }));
    }

    public class Handlers {
        public void onBrightnessChanged(SeekBar seekBar, int progress, boolean fromUser) {
            viewModel.brightness.setValue(progress);
        }

        public void onContrastChanged(SeekBar seekbar, int progress, boolean fromUser) {
            viewModel.contrast.setValue(progress);
        }

        public void toggleFax(View view) {
            faxOn = !faxOn;
            render();
        }
    }
}
