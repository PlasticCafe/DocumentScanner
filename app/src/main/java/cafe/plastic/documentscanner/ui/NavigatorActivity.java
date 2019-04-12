package cafe.plastic.documentscanner.ui;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;

import androidx.navigation.Navigation;
import cafe.plastic.documentscanner.R;
import cafe.plastic.documentscanner.ui.fragments.BackButtonPressed;

public class NavigatorActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_navigator);
        if(getSupportActionBar() != null) {
            getSupportActionBar().hide();
        }
    }

    @Override
    public boolean onSupportNavigateUp() {
        BackButtonPressed fragment = (BackButtonPressed) getSupportFragmentManager()
                .findFragmentById(R.id.nav_host_fragment)
                .getChildFragmentManager().getFragments()
                .get(0);
        fragment.onSupportNavigateUp();
        return Navigation.findNavController(this, R.id.nav_host_fragment).navigateUp();
    }
}
