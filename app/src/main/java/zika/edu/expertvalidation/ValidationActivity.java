package zika.edu.expertvalidation;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.view.MenuItem;

/*
 * Activity : Holds the fragments UserFragment, ListImagesFragment
 */
public class ValidationActivity extends AppCompatActivity {

    private ActionBar mActionBar;
    private SharedPreferences prefs;
    private Fragment currFrag;
    private FragmentManager mFragManager;

    @Override
    protected void onCreate(Bundle savedInstanceState){
        setContentView(R.layout.activity_validation);
        super.onCreate(savedInstanceState);
        prefs = getSharedPreferences("ExpertValidation", 0);
        mActionBar = getSupportActionBar();
        mActionBar.setDisplayHomeAsUpEnabled(true);
        mActionBar.setSubtitle(prefs.getString("username", ""));
        mFragManager = getSupportFragmentManager();

        Fragment listFrag = new ListImagesFragment();
        if(!listFrag.isAdded()){
            mFragManager.beginTransaction().add(R.id.frag_container, listFrag, "listFrag").commit();
            mActionBar.setHomeAsUpIndicator(R.drawable.ic_user);
        } else {
            mFragManager.beginTransaction().replace(R.id.frag_container, listFrag, "listFrag").commit();
            mActionBar.setHomeAsUpIndicator(R.drawable.ic_user);
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item){
        Fragment fragment;

        switch(item.getItemId()){
            case android.R.id.home:
                if(currFrag.getTag().equals("listFrag")){
                    fragment = new UserFragment();
                    mFragManager.beginTransaction().replace(R.id.frag_container, fragment, "userFrag").commit();
                } else if(currFrag.getTag().equals("userFrag")){
                    fragment = new ListImagesFragment();
                    mFragManager.beginTransaction().replace(R.id.frag_container, fragment, "listFrag").commit();
                }
                return true;

            default:
                return super.onOptionsItemSelected(item);
        }
    }


    @Override
    public void onAttachFragment(Fragment fragment){
        currFrag = fragment;
    }
}
