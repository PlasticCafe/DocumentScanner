package cafe.plastic.documentscanner.util;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.UUID;

import cafe.plastic.documentscanner.R;
import cafe.plastic.pagedetect.PostProcess;

public class TempImageManager {
    public static String TEMP_BITMAP_FILE = "temp_bitmap.jpg";
    public static String TEMP_CONFIG_FILE = "temp_config.cfg";
    private static TempImageManager instance;
    private final Context context;
    private final File tempDir;
    private PostProcess.RenderConfiguration cachedRenderConfig;

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

    public synchronized void storeTempImage(PostProcess.RenderConfiguration renderConfig) throws IOException {
        cachedRenderConfig = renderConfig;
        File bitmapFile = getFile(TEMP_BITMAP_FILE);
        File configFile = getFile(TEMP_CONFIG_FILE);
        if(configFile.exists()) configFile.delete();
        FileOutputStream bitmapFOS = new FileOutputStream(bitmapFile);
        cachedRenderConfig.bitmap.compress(Bitmap.CompressFormat.JPEG, 100, bitmapFOS);
        bitmapFOS.close();
        cachedRenderConfig.writeToFile(configFile);
    }

    public synchronized void clearImage() throws FileNotFoundException {
        File bitmapFile = getFile(TEMP_BITMAP_FILE);
        File configFile = getFile(TEMP_CONFIG_FILE);
        if(bitmapFile.exists()) bitmapFile.delete();
        if(configFile.exists()) configFile.delete();
        cachedRenderConfig = null;
    }

    public synchronized PostProcess.RenderConfiguration loadTempBitmap() throws IOException {
        if(cachedRenderConfig == null) {
            File configFile = getFile(TEMP_CONFIG_FILE);
            if(!configFile.exists()) return null;
            Bitmap bitmap = BitmapFactory.decodeFile(getFile(TEMP_BITMAP_FILE).getAbsolutePath());
            cachedRenderConfig = PostProcess.RenderConfiguration.readFromFile(configFile, bitmap);
        }
        return cachedRenderConfig;
    }

    private File getFile(String fileName) {
        File file = new File(tempDir, fileName);
        return file;
    }
}

