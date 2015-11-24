package cz.vutbr.fit.tam.meetme.asynctasks;

import android.content.Context;
import android.os.AsyncTask;

import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.wearable.DataMap;
import com.google.android.gms.wearable.Node;
import com.google.android.gms.wearable.PutDataMapRequest;
import com.google.android.gms.wearable.PutDataRequest;
import com.google.android.gms.wearable.Wearable;

import java.util.ArrayList;

/**
 * Created by Jakub on 21. 11. 2015.
 */
public class WearableSendAsyncTask extends AsyncTask<Node, Void, Void> {

    private Context context;
    private GoogleApiClient googleApiClient;
    private ArrayList<DataMap> dataMap;

    public WearableSendAsyncTask(Context context, GoogleApiClient client, ArrayList<DataMap> dataMap) {
        this.context = context;
        this.googleApiClient = client;
        this.dataMap = dataMap;
    }

    @Override
    protected Void doInBackground(Node... params) {
        PutDataMapRequest mapRequest = PutDataMapRequest.create("/myapp/myevent");
        mapRequest.getDataMap().putDataMapArrayList("data", dataMap);

        PutDataRequest request = mapRequest.asPutDataRequest();
        Wearable.DataApi.putDataItem(googleApiClient, request).await();

        return null;
    }
}
