package mist.api.ui;

import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;
import android.widget.Toast;

import org.apache.commons.compress.archivers.tar.TarArchiveEntry;
import org.apache.commons.compress.archivers.tar.TarArchiveInputStream;
import org.apache.commons.compress.compressors.gzip.GzipCompressorInputStream;
import org.apache.commons.compress.utils.IOUtils;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;


/**
 * Created by jeppe on 11/15/16.
 */

class Install extends AsyncTask<Object, Void, Install.QueryResult> {

    private final String TAG = "Install";
    private Context _context;
    private Response _listener;

    Install(Context context, Response listener) {
        this._context = context;
        this._listener = listener;
    }

    @Override
    protected void onPreExecute() {
        //showDialog("Downloaded " + result + " bytes");
    }

    @Override
    protected QueryResult doInBackground(Object... params) {

        InputStream inputStream = (InputStream) params[0];
        String dir = (String) params[1];
        String md5 = (String) params[2];

        File destinationDirectory = null;

        try {
            destinationDirectory = new File(dir + "/" + md5);
            if (destinationDirectory.exists()) {
                return new QueryResult(exists, md5);
            }

            TarArchiveInputStream tarInput = new TarArchiveInputStream(new GzipCompressorInputStream(inputStream));
            TarArchiveEntry currentEntry = tarInput.getNextTarEntry();
            while (currentEntry != null) {
                if (currentEntry.isDirectory()) {
                    currentEntry = tarInput.getNextTarEntry();
                    continue;
                }

                File curfile = new File(destinationDirectory, currentEntry.getName());
                File parent = curfile.getParentFile();
                if (!parent.exists()) {
                    parent.mkdirs();
                }
                try {
                    OutputStream out = new FileOutputStream(curfile);
                    IOUtils.copy(tarInput, out);
                    out.close();
                } catch (FileNotFoundException e) {
                    System.out.println("err - - skip: "+ curfile);
                }
                currentEntry = tarInput.getNextTarEntry();
            }
            tarInput.close();

        } catch (FileNotFoundException e) {
            e.printStackTrace();
            return new QueryResult(error, "no file");
        } catch (IOException e) {
            if (destinationDirectory != null & destinationDirectory.exists()) {
                deleteDirectory(destinationDirectory);
            }
            return new QueryResult(error, "error: " + e.getMessage());
        }


        return new QueryResult(installed, md5);
    }

    public static boolean deleteDirectory(File path) {
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

    /*
    @Override
    protected void onProgressUpdate(Integer... progress) {
        setProgressPercent(progress[0]);
    }*/

    @Override
    protected void onPostExecute(QueryResult result) {
        Log.d(TAG, "ui response " + result.msg);
        if (result.type == error) {
            Toast.makeText(_context, "Couldn't install Ui", Toast.LENGTH_LONG).show();
            Log.d(TAG, result.msg);
        } else if (result.type == exists) {
            _listener.loadUi(result.msg);
        } else if (result.type == installed) {
            _listener.loadUi(result.msg);
        }
    }

    private final static int error = 1;
    private final static int exists = 2;
    private final static int installed = 3;

    class QueryResult {
        int type;
        String msg;

        public QueryResult(int type, String msg) {
            this.type = type;
            this.msg = msg;
        }
    }

    interface Response {
       public void loadUi(String md5);
    }
}

