package cafe.plastic.documentscanner.ui.fragments;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CompoundButton;
import android.widget.SeekBar;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
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
        if(viewModel.workingImage.getValue() != null) {
            postProcessor = new PostProcess(viewModel.workingImage.getValue());
        } else {
            onSupportNavigateUp();
        }
        viewModel.workingImage.observe(this, this::render);
    }

    @Override
    public void onResume() {
        super.onResume();
        Timber.d("On resume");
    }

    @Override
    public void onPause() {
        super.onPause();
        Timber.d("On pause");
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

    }


    private void render(PostProcess.RenderConfiguration config) {
        if(config != null) {
            postProcessor.updateConfig(config);
            binding.image.setBitmap(postProcessor.render());
            binding.image.setRotation(config.rotation);
            binding.image.invalidate();
            binding.brightnessSlider.setProgress((int) config.brightness);
            binding.contrastSlider.setProgress((int) config.contrast);
        }
    }

    @Override
    public void onSupportNavigateUp() {
        Timber.d("onSupportNavigateUp Confirmation Fragment");
        viewModel.workingImage.deleteConfig();
        Navigation.findNavController(getView()).popBackStack();
    }

    public class Handlers {
        public void onBrightnessChanged(SeekBar seekBar, int progress, boolean fromUser) {
            viewModel.updateBrightness(progress);
        }
        public void onContrastChanged(SeekBar seekbar, int progress, boolean fromUser) {
            viewModel.updateContrast(progress);
        }

        public void toggleFax(CompoundButton view, boolean isChecked) {
            viewModel.updateThreshold(isChecked);
        }

        public void rotate(View view) {
            viewModel.updateRotation();
        }
    }
}
