package cafe.plastic.documentscanner;

import android.app.Application;

import timber.log.Timber;

public class DocumentScannerApplication extends Application {

    static {
        System.loadLibrary("page-detect-lib");
    }
    @Override
    public void onCreate() {
        super.onCreate();
        if(BuildConfig.DEBUG) {
            Timber.plant(new Timber.DebugTree());
        }
    }
}
