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

import cafe.plastic.documentscanner.R;

public class ConfirmationFragment extends Fragment {
    private static final String ARG_PICTURE = "picture_param";
    private static final String ARG_ROTATION = "rotation_param";

    private Bitmap mPicture;
    private Integer mRotation;
    private PhotoView takenPicture;

    public ConfirmationFragment() {
        // Required empty public constructor
    }

    public static Bundle getArguments(Bitmap bitmap, Integer rotation) {
        Bundle args = new Bundle();
        args.putParcelable(ARG_PICTURE, bitmap);
        args.putInt(ARG_ROTATION, rotation);
        return args;
    }

    public static ConfirmationFragment newInstance(Bundle arguments) {
        if(!arguments.containsKey(ARG_PICTURE) || !arguments.containsKey(ARG_ROTATION)) {
            throw new IllegalArgumentException();
        }
        ConfirmationFragment fragment = new ConfirmationFragment();
        fragment.setArguments(arguments);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mPicture = getArguments().getParcelable(ARG_PICTURE);
            mRotation = getArguments().getInt(ARG_ROTATION);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_confirmation, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        takenPicture = getView().findViewById(R.id.takenPicture);
        takenPicture.setImageBitmap(mPicture);
    }
}
