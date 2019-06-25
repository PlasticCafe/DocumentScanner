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


import androidx.lifecycle.ViewModelProviders;
import androidx.navigation.Navigation;
import cafe.plastic.documentscanner.databinding.ConfirmationFragmentBinding;
import cafe.plastic.pagedetect.PostProcess;
import timber.log.Timber;

@SuppressWarnings("WeakerAccess")
public class ConfirmationFragment extends Fragment implements BackButtonPressed {

    private PostProcess postProcessor;
    private ConfirmationFragmentBinding binding;
    private ConfirmationViewModel viewModel;

    public ConfirmationFragment() {
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
        if(viewModel.config != null) {
            postProcessor = new PostProcess(viewModel.config);
        } else {
            onSupportNavigateUp();
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        Timber.d("On resume");
        enableEditingUI();
    }

    @Override
    public void onPause() {
        super.onPause();
        Timber.d("On pause");
    }

    private void enableEditingUI() {
        binding.image.setBitmap(postProcessor.render());
        binding.brightnessSlider.setProgress((int)viewModel.config.brightness);
        binding.contrastSlider.setProgress((int)viewModel.config.contrast);

    }


    private void render() {
        PostProcess.RenderConfiguration config = new PostProcess.RenderConfiguration.Builder(viewModel.config)
                .build();
        postProcessor.updateConfig(config);
        binding.image.setBitmap(postProcessor.render());
        binding.image.invalidate();
    }

    @Override
    public void onSupportNavigateUp() {
        Timber.d("onSupportNavigateUp Confirmation Fragment");
        Navigation.findNavController(getView()).popBackStack();
    }

    public class Handlers {
        public void onBrightnessChanged(SeekBar seekBar, int progress, boolean fromUser) {
            viewModel.config.brightness = progress;
            render();
        }
        public void onContrastChanged(SeekBar seekbar, int progress, boolean fromUser) {
            viewModel.config.contrast = progress;
            render();
        }

        public void toggleFax(View view) {
            viewModel.config.threshold = !viewModel.config.threshold;
            render();
        }

        public void rotate(View view) {
            viewModel.config.rotation = (viewModel.config.rotation + 90) % 360;
            render();
        }
    }
}
