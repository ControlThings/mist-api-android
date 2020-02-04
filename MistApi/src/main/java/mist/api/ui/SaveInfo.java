package mist.api.ui;

import android.content.ContentResolver;
import android.content.Context;
import android.net.Uri;
import android.os.AsyncTask;
import android.util.Log;
import android.widget.Toast;

import org.apache.commons.compress.archivers.tar.TarArchiveEntry;
import org.apache.commons.compress.archivers.tar.TarArchiveInputStream;
import org.apache.commons.compress.compressors.gzip.GzipCompressorInputStream;
import org.apache.commons.compress.utils.IOUtils;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import static utils.Util.byteArrayToHexString;
import static utils.Util.jsonToByteArray;

/**
 * Created by jeppe on 11/18/16.
 */

class SaveInfo extends AsyncTask<Object, Void, SaveInfo.QueryResult> {

    private final String TAG = "SaveInfo";
    private Context _context;
    private Response _listener;
    private CustomUi.AddUiResponse addUiResponse;

    SaveInfo(Context context, Response listener, CustomUi.AddUiResponse addUiResponse) {
        this._context = context;
        this._listener = listener;
        this.addUiResponse = addUiResponse;
    }

    @Override
    protected void onPreExecute() {
        //showDialog("Downloaded " + result + " bytes");
    }

    @Override
    protected QueryResult doInBackground(Object... params) {

        Uri uri = (Uri) params[0];
        String dir = (String) params[1];

        File destinationDirectory = null;

        try {
            ContentResolver cr = _context.getContentResolver();
            InputStream inputStreamMd5 = cr.openInputStream(uri);
            // bufferedInputStream.mark(0);

            String md5 = getMd5(inputStreamMd5);
            inputStreamMd5.close();
            if (md5 == null) {
                return new QueryResult(error, "Md5 parse error");
            }

            destinationDirectory = new File(dir + md5);
            if (destinationDirectory.exists()) {
                return new QueryResult(exists);
            }

            InputStream inputStream = cr.openInputStream(uri);

            TarArchiveInputStream tarInput = new TarArchiveInputStream(new GzipCompressorInputStream(inputStream));
            TarArchiveEntry currentEntry = tarInput.getNextTarEntry();
            while (currentEntry != null) {
                if (currentEntry.getName().equals("package/package.json")) {
                    File curfile = new File(destinationDirectory, currentEntry.getName());
                    File parent = curfile.getParentFile();
                    if (!parent.exists()) {
                        parent.mkdirs();
                    }
                    OutputStream out = new FileOutputStream(curfile);
                    IOUtils.copy(tarInput, out);
                    out.close();
                    try {
                        byte[] bson = getBsonFromJsonFile(curfile);
                        tarInput.close();
                        return new QueryResult(info, uri, bson);
                    } catch (Exception e) {
                        return new QueryResult(error, "bson error: " + e);
                    }
                }
                currentEntry = tarInput.getNextTarEntry();

            }
            tarInput.close();
            return new QueryResult(error, "No package.json");
        } catch (IOException e) {
            Log.d(TAG, "extract error: " + e);
            if (destinationDirectory != null & destinationDirectory.exists()) {
                deleteDirectory(destinationDirectory);
            }
            return new QueryResult(error, "error: " + e.getMessage());
        }
    }

    private byte[] getBsonFromJsonFile(File file) throws Exception {
        InputStream is = new FileInputStream(file);
        int size = is.available();
        byte[] buffer = new byte[size];
        is.read(buffer);
        is.close();
        String json = new String(buffer, "UTF-8");
        return jsonToByteArray(json);
    }

    private static boolean deleteDirectory(File path) {
        if (path.exists()) {
            File[] files = path.listFiles();
            for (int i = 0; i < files.length; i++) {
                if (files[i].isDirectory()) {
                    deleteDirectory(files[i]);
                } else {
                    files[i].delete();
                }
            }
        }
        return (path.delete());
    }

    private String getMd5(InputStream inputStream) {
        try {
            byte[] buffer = new byte[1024];
            MessageDigest md = MessageDigest.getInstance("MD5");

            int numRead;
            while ((numRead = inputStream.read(buffer)) != -1) {
                if (numRead > 0) {
                    md.update(buffer, 0, numRead);
                }
            }
            return byteArrayToHexString(md.digest());

        } catch (NoSuchAlgorithmException | IOException e) {
            Log.d(TAG, "hash error: " + e);
            return null;
        }
    }

    /*
    @Override
    protected void onProgressUpdate(Integer... progress) {
        setProgressPercent(progress[0]);
    }*/

    @Override
    protected void onPostExecute(QueryResult result) {
        if (result.type == error) {
            Toast.makeText(_context, "Couldn't find ui Info", Toast.LENGTH_LONG).show();
            Log.d(TAG, result.msg);
        } else if (result.type == exists) {
            _listener.onInfoExists(addUiResponse);
        } else if (result.type == info) {
            _listener.onInfo(result.uri, result.bson, addUiResponse);
        }
    }

    private final static int error = 1;
    private final static int exists = 2;
    private final static int info = 3;

    class QueryResult {
        int type;
        String msg;
        byte[] bson;
        Uri uri;

        public QueryResult(int type) {
            this.type = type;
        }

        public QueryResult(int type, String msg) {
            this.type = type;
            this.msg = msg;
        }

        public QueryResult(int type, Uri uri, byte[] bson) {
            this.type = type;
            this.bson = bson;
            this.uri = uri;
        }
    }

    interface Response {
        public void onInfoExists(CustomUi.AddUiResponse addUiResponse);

        public void onInfo(Uri uri, byte[] bson, CustomUi.AddUiResponse addUiResponse);
    }
}