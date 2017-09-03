package zika.edu.expertvalidation;

import android.content.DialogInterface;
import android.support.v4.app.Fragment;
import android.content.Context;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.dropbox.core.DbxRequestConfig;
import com.dropbox.core.v2.DbxClientV2;
import com.squareup.picasso.Picasso;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class ListImagesFragment extends Fragment {

    private AppCompatActivity parent;
    private RecyclerView mRecyclerView;
    private ProgressBar mProgressBar;
    private TextView mNoResults;
    private DbxClientV2 dbClient;
    private DbxRequestConfig dbConfig;
    private HashMap<String, String> mDownloadedResults = new LinkedHashMap<>();
    private ImageAdapter mAdapter;
    private DownloadFilesTask.TaskListener mTaskListener;


    public ListImagesFragment() {
        super();
    }

    @Override
    public void onAttach(Context context) {
        parent = (AppCompatActivity)context;
        super.onAttach(context);
    }

    @Override
    public void onResume() {
        new DownloadFilesTask(parent, dbClient, mTaskListener)
                .execute("/RecognitionResults", "/RecognitionProcessedThumbnails", "/ExpertAccepted", "/ExpertRejected");
        super.onResume();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        dbConfig = new DbxRequestConfig("ExpertValidation");
        dbClient = new DbxClientV2(dbConfig, getString(R.string.DropboxKey));

        mTaskListener = new DownloadFilesTask.TaskListener() {
            @Override
            public void onPreDownload() {
                mProgressBar.setVisibility(View.VISIBLE);
                mNoResults.setVisibility(View.GONE);
                mRecyclerView.setVisibility(View.GONE);
            }

            @Override
            public void onDownloadComplete(HashMap<String, String> results) {
                mProgressBar.setVisibility(View.GONE);
                mDownloadedResults = results;
                if(mDownloadedResults.isEmpty()) {
                    mNoResults.setVisibility(View.VISIBLE);
                } else {
                    mRecyclerView.setVisibility(View.VISIBLE);
                    mAdapter = new ImageAdapter(mDownloadedResults);
                    mRecyclerView.setAdapter(mAdapter);
                }

            }
        };
        setHasOptionsMenu(true);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState){
        View view = inflater.inflate(R.layout.fragment_listimages, container, false);
        parent.getSupportActionBar().setTitle("Images");
        parent.getSupportActionBar().setHomeAsUpIndicator(R.drawable.ic_user);
        mRecyclerView = (RecyclerView)view.findViewById(R.id.recycler_view);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(parent));
        mProgressBar = (ProgressBar)view.findViewById(R.id.progress_bar);
        mNoResults = (TextView)view.findViewById(R.id.no_results_text);

        return view;
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.menu_list_images, menu);
        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem menuItem) {
        switch(menuItem.getItemId()) {
            case R.id.reload_results_tab :
                new DownloadFilesTask(parent, dbClient, mTaskListener)
                        .execute("/RecognitionResults", "/RecognitionProcessedThumbnails", "/ExpertAccepted", "/ExpertRejected");
                return true;

            default :
                return super.onOptionsItemSelected(menuItem);
        }
    }

    private class ImageAdapter extends RecyclerView.Adapter<ImageHolder> {

        private HashMap<String, String> mDataSet;
        private List<String> mItemUrls;
        private List<String> mResult;

        public ImageAdapter(HashMap<String, String> dataSet) {
            mDataSet = dataSet;
            mItemUrls = new ArrayList<>(mDataSet.keySet());
            mResult = new ArrayList<>(mDataSet.values());
        }

        @Override
        public ImageHolder onCreateViewHolder(ViewGroup viewGroup, int viewType) {
            LayoutInflater inflater = LayoutInflater.from(parent);
            View view = inflater.inflate(R.layout.image_list_row, viewGroup, false);
            return new ImageHolder(view);
        }

        @Override
        public void onBindViewHolder(ImageHolder holder, int position) {
            holder.onBindZikaImage(mItemUrls.get(position));
            holder.onBindResults(mResult.get(position));
        }

        @Override
        public int getItemCount() {
            return mDataSet.size() > 10 ? 10 : mDataSet.size();
        }

        public void removeItem(String imagePath, int position) {
            mDataSet.remove(imagePath);
            mItemUrls.remove(position);
            mResult.remove(position);
            notifyItemRemoved(position);
        }
    }

    private class ImageHolder extends RecyclerView.ViewHolder implements View.OnClickListener{

        private ImageView mZikaImage;
        private TextView mResults;
        private String mImgPath;
        private String mResString;

        public ImageHolder(View itemView) {
            super(itemView);
            itemView.setOnClickListener(this);
            mZikaImage = (ImageView)itemView.findViewById(R.id.row_image);
            mResults = (TextView)itemView.findViewById(R.id.result_text);
        }

        public void onBindZikaImage(String path){
            Picasso.with(parent).load(new File(path)).fit().into(mZikaImage);
            mImgPath = path;
        }

        public void onBindResults(String results){
            mResults.setText(results);
            mResString = results;
        }

        @Override
        public void onClick(View v) {
            View dialogLayout = LayoutInflater.from(parent).inflate(R.layout.dialog_validate, null);
            TextView res = (TextView)dialogLayout.findViewById(R.id.dialog_results);
            ImageView img = (ImageView)dialogLayout.findViewById(R.id.dialog_image);
            res.setText(mResString);
            Picasso.with(parent).load(new File(mImgPath)).fit().into(img);

            AlertDialog.Builder builder = new AlertDialog.Builder(parent);
            builder.setView(dialogLayout)
                    .setNeutralButton("Accept", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            String fileName = mImgPath.substring(mImgPath.lastIndexOf(File.separator)+1, mImgPath.lastIndexOf("."));
                            new MoveFilesTask(parent, dbClient, fileName, new MoveFilesTask.TaskListener() {
                                @Override
                                public void onMoveComplete() {
                                    deleteFiles();
                                    mAdapter.removeItem(mImgPath, getAdapterPosition());
                                    if(mAdapter.getItemCount() <= 0) {
                                        mRecyclerView.setVisibility(View.GONE);
                                        mNoResults.setVisibility(View.VISIBLE);
                                    }
                                }
                            }).execute("/RecognitionProcessedThumbnails", "/RecognitionResults", "/ExpertAccepted");
                        }
                    })
                    .setPositiveButton("Reject", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            String fileName = mImgPath.substring(mImgPath.lastIndexOf(File.separator)+1, mImgPath.lastIndexOf("."));
                            new MoveFilesTask(parent, dbClient, fileName, new MoveFilesTask.TaskListener() {
                                @Override
                                public void onMoveComplete() {
                                    deleteFiles();
                                    mAdapter.removeItem(mImgPath, getAdapterPosition());
                                    if(mAdapter.getItemCount() <= 0) {
                                        mRecyclerView.setVisibility(View.GONE);
                                        mNoResults.setVisibility(View.VISIBLE);
                                    }
                                }
                            }).execute("/RecognitionProcessedThumbnails", "/RecognitionResults", "/ExpertRejected");
                        }
                    });
            AlertDialog dialog = builder.create();
            dialog.show();
        }

        private void deleteFiles() {
            File imgFile = new File(mImgPath);
            String fileName = mImgPath.substring(mImgPath.lastIndexOf(File.separator)+1, mImgPath.lastIndexOf("."));
            File csvFile = new File(parent.getFilesDir() + File.separator + "image_results" + File.separator + fileName + ".csv");
            imgFile.delete();
            csvFile.delete();
        }
    }
}
