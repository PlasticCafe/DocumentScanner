package cafe.plastic.documentscanner.util;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.util.UUID;

import androidx.annotation.NonNull;
import cafe.plastic.documentscanner.R;
import io.reactivex.Observable;
import io.reactivex.Single;
import io.reactivex.observers.DisposableCompletableObserver;
import io.reactivex.schedulers.Schedulers;

import static io.reactivex.Single.create;

public class TempImageManager {
    private static TempImageManager instance;
    private final Context context;
    private final File tempDir;

    private TempImageManager(Context context) {
        this.context = context.getApplicationContext();
        String baseDirPath = context.getFilesDir().getAbsolutePath();
        String tempDirName = context.getString(R.string.temp_file_path);
        tempDir = new File(baseDirPath + File.separator + tempDirName);
        if (!tempDir.exists())
            tempDir.mkdirs();
    }

    public static synchronized TempImageManager getInstance(Context context) {
        if (instance != null) {
            return instance;
        } else {
            instance = new TempImageManager(context);
            return instance;
        }
    }

    public Single<String> storeTempBitmap(Bitmap bitmap) {
        return Single
                .<String>create(s -> {
                    String fileName = UUID.randomUUID().toString() + ".jpg";
                    File file = new File(tempDir, fileName);
                    try {
                        FileOutputStream fos = new FileOutputStream(file);
                        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, fos);
                        fos.close();
                        s.onSuccess(fileName);
                    } catch (Exception e) {
                        s.onError(e);
                    }
                })
                .subscribeOn(Schedulers.io())
                .cache();
    }

    public Single<Bitmap> loadTempBitmap(String fileName) {
        return Single
                .<Bitmap>create(s -> {
                    File file = new File(tempDir, fileName);
                    try {
                        if(!file.exists()) {
                            throw new FileNotFoundException();
                        }
                        s.onSuccess(BitmapFactory.decodeFile(file.getAbsolutePath()));
                    } catch(Exception e) {
                        s.onError(e);
                    }
                })
                .subscribeOn(Schedulers.io())
                .cache();
    }

}
