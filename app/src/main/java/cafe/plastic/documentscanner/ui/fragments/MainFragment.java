package cafe.plastic.documentscanner.ui.fragments;


import java.io.IOException;

import androidx.fragment.app.Fragment;
import cafe.plastic.documentscanner.util.WorkingImageManager;
import cafe.plastic.pagedetect.PostProcess;
import timber.log.Timber;

public class MainFragment extends Fragment implements BackButtonPressed{
    @Override
    public void onResume() {
        super.onResume();
        WorkingImageManager manager = WorkingImageManager.getInstance(getContext());
        PostProcess.RenderConfiguration config = null;
        try {
            config = manager.loadTempBitmap();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onSupportNavigateUp() {
        Timber.d("onSupportNavigateUp");
    }
}