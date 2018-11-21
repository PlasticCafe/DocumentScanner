package cafe.plastic.documentscanner;

import android.app.Application;

import timber.log.Timber;

public class DocumentScannerApplication extends Application {

    @Override
    public void onCreate() {
        super.onCreate();
        if(BuildConfig.DEBUG) {
            Timber.plant(new Timber.DebugTree());
        }
    }
}
