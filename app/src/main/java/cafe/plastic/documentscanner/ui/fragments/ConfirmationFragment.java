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


import java.io.FileNotFoundException;

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
public class ConfirmationFragment extends Fragment implements BackButtonPressed{

    public static String PREF_WORKING_IMAGE = "pref_working_image";
    public static String PREF_IMAGE_BRIGHTNESS = "pref_image_brightness";
    public static String PREF_IMAGE_CONTRAST = "pref_image_contrast";
    public static String PERF_IMAGE_THRESH = "pref_image_thresh";
    public static String PREF_IMAGE_BOUNDS = "pref_image_bounds";
    public static String PREF_IMAGE_ROTATION = "pref_image_rotation";

    private Single<PostProcess.RenderConfiguration> imageLoader;
    private PostProcess.RenderConfiguration config;
    private boolean imageLoaded = false;
    private boolean faxOn = false;
    private PostProcess postProcessor;
    private ConfirmationFragmentBinding binding;
    private ConfirmationViewModel viewModel;
    private PublishProcessor<Boolean> renderEvents;
    private CompositeDisposable observers = new CompositeDisposable();

    public ConfirmationFragment() {
        imageLoader = Single
                .<PostProcess.RenderConfiguration>create(s -> {
                    try {
                        PostProcess.RenderConfiguration config = TempImageManager.getInstance(getContext()).loadTempBitmap();
                        s.onSuccess(config);
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
        binding.setHandlers(new Handlers());
        binding.setViewmodel(viewModel);
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        viewModel = ViewModelProviders.of(requireActivity()).get(ConfirmationViewModel.class);
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
    }

    private void setConfig(PostProcess.RenderConfiguration config) {
        this.config = config;
        postProcessor = new PostProcess(this.config);
        viewModel.brightness.setValue((int)config.brightness);
        viewModel.contrast.setValue((int)config.contrast);
        imageLoaded = true;
        render();
    }

    private void enableEditingUI() {
        binding.image.setImageBitmap(postProcessor.render());
    }


    private void render() {
        if(imageLoaded) {
            PostProcess.RenderConfiguration config = new PostProcess.RenderConfiguration.Builder(this.config.bitmap)
                    .brightness(viewModel.brightness.getValue())
                    .contrast(viewModel.contrast.getValue())
                    .threshold(faxOn)
                    .region(this.config.region)
                    .rotation(this.config.rotation)
                    .build();
            postProcessor.updateConfig(config);
            binding.image.setImageBitmap(postProcessor.render());
            binding.image.invalidate();
        }
    }

    private void loadImage() {
        observers.add(
                imageLoader.subscribe(
                        c -> {
                            setConfig(c);
                            enableEditingUI();
                        },
                        e -> {
                            NavHostFragment.findNavController(this).popBackStack();
                        }));
    }

    @Override
    public void onSupportNavigateUp() {
        Timber.d("onSupportNavigateUp");
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
