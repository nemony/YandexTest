package com.gmail.yurapapapa.yandextest;

import android.app.Dialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.yandex.disk.client.Credentials;
import com.yandex.disk.client.ListItem;
import com.yandex.disk.client.ListParsingHandler;
import com.yandex.disk.client.TransportClient;

import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by yurap_000 on 010 10 сен.
 */
public class ListFragment extends android.support.v4.app.ListFragment {

    public static final String CLICKED_ITEM_ID = "clicked_item";
    private static final String TAG = "ListFragment";
    private Credentials credentials;
    private String mCurrentDir;

    private ArrayList<ListItem> mCurrentItemsList;


    @Override
    public void onCreate(Bundle savedInstanceState) {
        Log.d(TAG, "onCreate");
        super.onCreate(savedInstanceState);
        setRetainInstance(true);
        setHasOptionsMenu(true);
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        Log.d(TAG, "onCreateView");
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(getActivity());
        String username = preferences.getString(MyActivity.USERNAME, null);
        String token = preferences.getString(MyActivity.TOKEN, null);
        Log.d(TAG, "Username = " + username);
        Log.d(TAG, "Token = " + token);

        credentials = new Credentials(username, token);


        if (mCurrentDir == null) {
            mCurrentDir = "/";
        }
        new Fetcher().execute();

        return super.onCreateView(inflater, container, savedInstanceState);

    }

    @Override
    public void onListItemClick(ListView l, View v, int position, long id) {
        ListItem item = mCurrentItemsList.get(position);
        if (item.isCollection()) {
            mCurrentDir = item.getFullPath();
            new Fetcher().execute();
            return;
        }
        if (item.getMediaType().equals("image")) {
            Toast.makeText(getActivity(), "is image " + item.getDisplayName(), Toast.LENGTH_LONG).show();
            Intent imageIntent = new Intent(getActivity(), ImageActivity.class);
            ArrayList<ListItem> pics = getPicsList();
            int clickedItemId = 0;
            for (int i = 0; i < pics.size(); i++) {
                if (item.getFullPath().equals(pics.get(i).getFullPath())) {
                    clickedItemId = i;
                }
            }
            imageIntent.putExtra(CLICKED_ITEM_ID, clickedItemId);
            imageIntent.putParcelableArrayListExtra("pics", getPicsList());
            startActivity(imageIntent);
        }
    }

    private ArrayList<ListItem> getPicsList() {
        ArrayList<ListItem> pics = new ArrayList<ListItem>();
        for (ListItem item : mCurrentItemsList) {
            if (!item.isCollection() && item.getMediaType().equals("image")) {
                pics.add(item);
            }
        }
        for (ListItem item : pics)
            Log.d(TAG, item.getDisplayName() + " " + item.getMediaType());
        return pics;
    }

    public void onBackPressed() {
        if (mCurrentDir.equals("/")) {
            getActivity().finish();
        } else {
            int flash = mCurrentDir.lastIndexOf('/');
            mCurrentDir = mCurrentDir.substring(0, flash + 1);
            Log.d(TAG, "mCurrentDir " + mCurrentDir);
            new Fetcher().execute();
        }
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.my, menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.sign_out) {
            SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getActivity());
            prefs.edit().remove(MyActivity.TOKEN).apply();
            getActivity().getSupportFragmentManager().beginTransaction().
                    replace(android.R.id.content, new AuthFragment(), MyActivity.FRAGMENT_AUTH_TAG).commit();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void cancelToken() {
        String token = PreferenceManager.getDefaultSharedPreferences(getActivity()).getString(MyActivity.TOKEN, null);
        Log.d(TAG, "token sdf = " + token);
        HttpPost post = new HttpPost("https://oauth.yandex.ru/api/revoke");
        post.addHeader("Authorization", "Bearer " + token);
        HttpClient client = new DefaultHttpClient();
        try {
            Log.d(TAG, "responseCode " + client.execute(post).getStatusLine().getStatusCode());
        } catch (Exception e) {
            Log.d(TAG, e.toString());
        }
        PreferenceManager.getDefaultSharedPreferences(getActivity()).edit().remove(MyActivity.TOKEN).apply();
        ;
    }

    private class MyAdapter extends ArrayAdapter<ListItem> {
        public MyAdapter(List<ListItem> objects) {
            super(getActivity(), 0, android.R.id.text1, objects);
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            if (convertView == null) {
                convertView = LayoutInflater.from(getActivity()).
                        inflate(android.R.layout.simple_list_item_1, parent, false);
            }
            TextView tv = (TextView) convertView.findViewById(android.R.id.text1);
            ListItem item = getItem(position);

            tv.setText(item.getDisplayName());
            return convertView;
        }

    }

    private class Fetcher extends AsyncTask<Void, Void, List<ListItem>> {
        Dialog dialog;

        Fetcher() {
            super();
            mCurrentItemsList = new ArrayList<ListItem>();
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            if (getListAdapter() == null) {
                return;
            }
            dialog = new Dialog(getActivity());
            dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
            dialog.setContentView(R.layout.dialog);
            dialog.setCancelable(false);
            final Window window = dialog.getWindow();
            window.setLayout(WindowManager.LayoutParams.MATCH_PARENT, WindowManager.LayoutParams.MATCH_PARENT);
            window.clearFlags(WindowManager.LayoutParams.FLAG_DIM_BEHIND);
            window.setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
            dialog.show();
        }

        @Override
        protected List<ListItem> doInBackground(Void... params) {
            TransportClient client = null;
            // https://cloud-api.yandex.net:443/v1/disk/
            // https://cloud-api.yandex.net:443/v1/disk/resources/?path=%2F
            try {
                client = TransportClient.getInstance(getActivity(), credentials);
                client.getList(mCurrentDir, new ListParsingHandler() {
                    @Override
                    public boolean handleItem(ListItem item) {
                        if (!item.getFullPath().equals(mCurrentDir) && !TextUtils.isEmpty(item.getFullPath())) {
                            mCurrentItemsList.add(item);
                        }
                        return true;
                    }
                });
            } catch (Exception e) {
                Log.d(TAG, e.toString());
            } finally {
                TransportClient.shutdown(client);
            }

            return mCurrentItemsList;
        }

        @Override
        protected void onPostExecute(List<ListItem> list) {
            super.onPostExecute(list);
            if (dialog != null && dialog.isShowing())
                dialog.dismiss();
            Log.d(TAG, list.toString());

            setListAdapter(new MyAdapter(list));
        }
    }
}
