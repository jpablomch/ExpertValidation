package zika.edu.expertvalidation;

import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;

import com.dropbox.core.DbxException;
import com.dropbox.core.v2.DbxClientV2;
import com.dropbox.core.v2.files.ListFolderResult;
import com.dropbox.core.v2.files.Metadata;

import java.io.File;

public class MoveFilesTask extends AsyncTask<String, Void, Void> {

    Context mContext;
    DbxClientV2 dbClient;
    String fileName;
    TaskListener mTaskListener;

    public interface TaskListener {
        void onMoveComplete();
    }

    public MoveFilesTask(Context ctx, DbxClientV2 client, String fileName, TaskListener listener){
        mContext = ctx;
        dbClient = client;
        this.fileName = fileName;
        mTaskListener = listener;
    }

    @Override
    protected Void doInBackground(String... params) {
        try {
            ListFolderResult dBoxImagesFolder = dbClient.files().listFolder(params[0]);
            ListFolderResult dBoxResultsFolder = dbClient.files().listFolder(params[1]);
            String dBoxImageFilePath = findPath(fileName, dBoxImagesFolder, dBoxResultsFolder, params[0]);
            String dBoxResultFilePath = findPath(fileName, dBoxResultsFolder, dBoxResultsFolder, params[1]);

            if(dBoxImageFilePath != null && dBoxResultFilePath != null) {
                dbClient.files().move(dBoxImageFilePath, params[2] + File.separator + fileName + ".png");
//                dbClient.files().move(dBoxResultFilePath, params[2] + File.separator + fileName + ".csv");
            } else {
                throw new DbxException("Did not find file in folders");
            }
        } catch(DbxException e) {
            Log.d("Msgs", e.toString());
        }
        return null;
    }

    @Override
    protected void onPostExecute(Void result){
        super.onPostExecute(result);
        mTaskListener.onMoveComplete();
    }

    private String findPath(String fileName, ListFolderResult dBoxImageFolder, ListFolderResult dBoxResultsFolder, String folderName) {
        String ext = folderName.equals("/RecognitionResults") ? ".csv" :
                folderName.equals("/RecognitionProcessedThumbnails") ? ".png":null;

        if(ext != null && ext.equals(".png")) {
            String fullName = fileName + ext;
            for(Metadata file : dBoxImageFolder.getEntries()) {
                if(fullName.equals(file.getName())){
                    return file.getPathLower();
                }
            }
        } else if(ext != null && ext.equals(".csv")) {
            String fullName = fileName + ext;
            for(Metadata file : dBoxResultsFolder.getEntries()) {
                if(fullName.equals(file.getName())){
                    return file.getPathLower();
                }
            }
        } else {
            Log.d("Msgs", "ext is null");
        }
        return null;
    }
}
