package cafe.plastic.documentscanner.ui;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;

import androidx.navigation.Navigation;
import cafe.plastic.documentscanner.R;

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
        Navigation.findNavController(this, R.id.nav_host_fragment).
        return Navigation.findNavController(this, R.id.nav_host_fragment).navigateUp();
    }
}
