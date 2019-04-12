package cafe.plastic.documentscanner.ui.fragments;

import android.os.Bundle;
import android.view.View;

import java.io.IOException;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.fragment.NavHostFragment;
import cafe.plastic.documentscanner.util.TempImageManager;
import cafe.plastic.pagedetect.PostProcess;
import timber.log.Timber;

public class MainFragment extends Fragment implements BackButtonPressed{
    @Override
    public void onResume() {
        super.onResume();
        TempImageManager manager = TempImageManager.getInstance(getContext());
        PostProcess.RenderConfiguration config = null;
        try {
            config = manager.loadTempBitmap();
        } catch (IOException e) {
            e.printStackTrace();
        }
        if (config == null) {
//            NavHostFragment.findNavController(this).navigate(MainFragmentDirections.actionMainFragmentToConfirmFragment());

        } else {
            NavHostFragment.findNavController(this).navigate(MainFragmentDirections.actionMainFragmentToCaptureFragment());
        }
    }

    @Override
    public void onSupportNavigateUp() {
        Timber.d("onSupportNavigateUp");
    }
}
