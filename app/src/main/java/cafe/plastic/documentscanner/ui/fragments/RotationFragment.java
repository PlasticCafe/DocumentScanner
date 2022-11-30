package cafe.plastic.documentscanner.ui.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;
import cafe.plastic.documentscanner.databinding.RotationFragmentBinding;

public class RotationFragment extends Fragment implements BackButtonPressed {
    RotationFragmentBinding binding;
    Integer rotation;
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = RotationFragmentBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onSupportNavigateUp() {
        Navigation.findNavController(getView()).popBackStack();
    }

    public class Handlers {
        public void onRotation() {

        }
    }
}
