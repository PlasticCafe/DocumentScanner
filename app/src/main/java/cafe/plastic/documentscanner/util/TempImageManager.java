package cafe.plastic.documentscanner.util;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.LruCache;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.UUID;

import cafe.plastic.documentscanner.R;

public class TempImageManager {
    private static TempImageManager instance;
    private final Context context;
    private final File tempDir;
    private final LruCache<String, Bitmap> cache = new LruCache<>(4000*4000*4*2);

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

    public String storeTempBitmap(Bitmap bitmap) throws IOException {
        String fileName = UUID.randomUUID().toString();
        File file = getFile(fileName);
        FileOutputStream fos = new FileOutputStream(file);
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, fos);
        fos.close();
        cache.put(fileName, bitmap);
        return fileName;
    }

    public void clearBitmap(String fileName) throws FileNotFoundException {
        File file = getFile(fileName);
        file.delete();
        cache.remove(fileName);
    }

    public void clearAllExcept(String fileName) {
        for(File image: tempDir.listFiles()) {
            if(!image.getAbsolutePath().contains(fileName)) {
                image.delete();
                cache.remove(fileName);
            }
        }
    }

    public void clearAllTempBitmaps() {
        for(File image: tempDir.listFiles()) {
            if(image.isFile()) image.delete();
        }
        cache.evictAll();
    }

    public Bitmap loadTempBitmap(String fileName) throws FileNotFoundException {
        Bitmap bitmap = cache.get(fileName);
        if(bitmap == null) {
            File file = getFile(fileName);
            if(!file.exists()) throw new FileNotFoundException();
            bitmap = BitmapFactory.decodeFile(getFile(fileName).getAbsolutePath());
            cache.put(fileName, bitmap);
        }
        return bitmap;
    }

    private File getFile(String fileName) {
        File file = new File(tempDir, fileName + ".jpg");
        return file;
    }

}

