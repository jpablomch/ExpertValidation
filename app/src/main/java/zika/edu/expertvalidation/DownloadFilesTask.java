package zika.edu.expertvalidation;

import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;

import com.dropbox.core.DbxException;
import com.dropbox.core.v2.DbxClientV2;
import com.dropbox.core.v2.files.ListFolderResult;
import com.dropbox.core.v2.files.Metadata;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.TreeMap;

public class DownloadFilesTask extends AsyncTask<String, Void, HashMap<String, String>> {

    DbxClientV2 dbClient;
    TaskListener mTaskListener;
    Context mContext;

    public interface TaskListener {
        void onPreDownload();
        void onDownloadComplete(HashMap<String, String> results);
    }

    public DownloadFilesTask(Context ctx, DbxClientV2 dbClient, TaskListener listener){
        this.dbClient = dbClient;
        this.mTaskListener = listener;
        this.mContext = ctx;
    }

    @Override
    protected void onPreExecute(){
        mTaskListener.onPreDownload();
        super.onPreExecute();
    }

    @Override
    protected HashMap<String, String> doInBackground(String... params) {

        try {
            ListFolderResult dBoxResultsFolder = dbClient.files().listFolder(params[0]);
            ListFolderResult dBoxImagesFolder = dbClient.files().listFolder(params[1]);
            ListFolderResult dBoxAcceptedFolder = dbClient.files().listFolder(params[2]);
            ListFolderResult dBoxRejectedFolder = dbClient.files().listFolder(params[3]);
            TreeMap<String, String> sortedResults = new TreeMap<>(Collections.<String>reverseOrder());
            HashMap<String, String> downloadedResults = new LinkedHashMap<>();

            for(Metadata imgFile : dBoxImagesFolder.getEntries()) {
                if(!inFolder(imgFile.getName(), dBoxAcceptedFolder) && !(inFolder(imgFile.getName(), dBoxRejectedFolder))){
                    String results = downloadResult(imgFile.getName(), dBoxResultsFolder);
                    String thumbnailURI = downloadImage(imgFile);
                    sortedResults.put(thumbnailURI, results);
                }
            }

            downloadedResults.putAll(sortedResults);
            return downloadedResults;

        } catch(DbxException e) {
            Log.d("Msgs", e.toString());
        }
        return new LinkedHashMap<>();
    }

    @Override
    protected void onPostExecute(HashMap<String, String> results){
        mTaskListener.onDownloadComplete(results);
        super.onPostExecute(results);
    }

    private boolean inFolder(String imgFile, ListFolderResult dBoxFolder) {
        for(Metadata file : dBoxFolder.getEntries()) {
            if(file.getName().equals(imgFile)) {
                return true;
            }
        }
        return false;
    }

    private String downloadResult(String imgFile, ListFolderResult dBoxResultsFolder) {
        String imgTimeStamp = getTimeStamp(imgFile);

        for(Metadata file : dBoxResultsFolder.getEntries()) {
            if(imgTimeStamp.equals(getTimeStamp(file.getName()))){
                return getResultString(file);
            }
        }

        return null;
    }

    private String downloadImage(Metadata imgFile) {
        File img = new File(mContext.getFilesDir() + File.separator + "image_thumbnails" + File.separator + imgFile.getName());
        File localFiles = img.getParentFile();

        if(!localFiles.exists()) {
            localFiles.mkdir();
        }

        try {
            for(File f : localFiles.listFiles()) {
                if(f.getName().equals(imgFile.getName())){
                    return f.getPath();
                }
            }

            OutputStream stream = new FileOutputStream(img);
            dbClient.files().download(imgFile.getPathLower()).download(stream);
            stream.close();
            return img.getPath();
        } catch (DbxException | IOException e) {
            Log.d("Msgs", e.toString());
        }

        return null;
    }

    private String getResultString(Metadata file) {
        File csvFile = new File(mContext.getFilesDir() + File.separator + "image_results" + File.separator + file.getName());
        File localFiles = csvFile.getParentFile();

        if(!localFiles.exists()) {
            localFiles.mkdir();
        }

        try {
            for(File f : localFiles.listFiles()) {
                if(f.getName().equals(file.getName())){
                    return formatResult(f);
                }
            }

            OutputStream stream = new FileOutputStream(csvFile);
            dbClient.files().download(file.getPathLower()).download(stream);
            stream.close();
            return formatResult(csvFile);
        } catch(DbxException | IOException e){
            Log.d("Msgs", e.toString());
        }

        return null;
    }

    private String formatResult(File file) {
        try {
            BufferedReader br = new BufferedReader(new FileReader(file));
            String line;
            StringBuilder result = new StringBuilder();
            while ((line = br.readLine()) != null) {
                String[] parts = line.split(",");
                double percent = Double.parseDouble(parts[1]);
                String larvae = parts[0].equals("Larvae") ? "Larvae":"Not Larvae";
                result.append(larvae + " : " + Math.round(percent * 10000.0) / 100.0 + "%\n");
            }
            result.replace(result.lastIndexOf("\n"), result.length(), "");

            return result.toString();
        } catch(IOException e){
          Log.d("Msgs", e.toString());
        }
        return "";
    }

    private String getTimeStamp(String fileName) {
        String[] parts = fileName.split("_|\\.");
        return parts[1] + "_" + parts[2];
    }
}
