package cz.vutbr.fit.tam.meetme.requestcrafter;

import android.content.Context;
import android.content.SharedPreferences;
import android.location.Location;

import cz.vutbr.fit.tam.meetme.exceptions.InternalErrorException;
import cz.vutbr.fit.tam.meetme.schema.DeviceInfo;
import cz.vutbr.fit.tam.meetme.schema.DeviceResponseInfo;
import cz.vutbr.fit.tam.meetme.schema.GroupInfo;
import cz.vutbr.fit.tam.meetme.schema.GroupResponseInfo;
import cz.vutbr.fit.tam.meetme.schema.ResponseInfo;

import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by Lada on 12.10.2015.
 */
public class RequestCrafter {
    private static final String LOG_TAG = "RequestCrafter";
    private static final String PREFS_NAME = "MeetMePreferences";
    private static final String APP_ID = "id";

    public final String REST_HOST_URL = "http://scattergoriesonline.net/mm/rest/api";

    public final String REST_INSTALL = REST_HOST_URL+"/install";

    public final String REST_GROUP_DATA_ID_DEV = "idDevice";
    public final String REST_GROUP_DATA_GROUP_HASH = "groupHash";
    public final String REST_GROUP_DATA = REST_HOST_URL+"/group-data/{"+REST_GROUP_DATA_ID_DEV+"}/{"+REST_GROUP_DATA_GROUP_HASH+"}/";

    public final String REST_GROUP_CREATE = REST_HOST_URL + "/group-create/{"+REST_GROUP_DATA_ID_DEV+"}/";

    public final String REST_GROUP_ATTACH = REST_HOST_URL + "/group-attach/{"+REST_GROUP_DATA_ID_DEV+"}/{"+REST_GROUP_DATA_GROUP_HASH+"}/";

    public final String REST_GROUP_DETACH = REST_HOST_URL +"/group-detach/{"+REST_GROUP_DATA_ID_DEV+"}/{"+REST_GROUP_DATA_GROUP_HASH+"}/";


    private String userAgent;
    private Context context;
    private Integer id;

    public RequestCrafter(String userAgent, Context context) {
        this.userAgent = userAgent;
        this.context = context;
        this.id = null;
    }

    private Integer getID() throws InternalErrorException{

        if(this.id != null){
            return this.id;
        }
        else{
            // Restore preferences
            SharedPreferences preferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
            this.id = preferences.getInt(APP_ID, -1);

            if(this.id == -1) {
                //id not set -> call install and write id to shared prefs
                DeviceInfo di = this.restInstall();
                this.id = di.getId();

                //save to shardprefs
                SharedPreferences.Editor editor = preferences.edit();
                editor.putInt(APP_ID, this.id);
                editor.commit();
            }

            return this.id;
        }

    }

    private DeviceInfo restInstall() throws InternalErrorException{

        MultiValueMap<String, String> body = new LinkedMultiValueMap<>();
        body.add("userAgent", this.userAgent);

        DeviceResponseInfo di = (DeviceResponseInfo) createRestRequestPrivate(DeviceResponseInfo.class, HttpMethod.POST, REST_INSTALL, null, body);

        if(di.getErrorCode() != 0)
            throw new InternalErrorException(di.getErrorMessage());

        return di.getDeviceInfo();
    }

    public GroupInfo restGroupCreate(Location location) throws InternalErrorException{

        Integer id = this.getID();

        HashMap<String,String> urlParams = new HashMap<>();
        urlParams.put(REST_GROUP_DATA_ID_DEV, id + "");

        GroupResponseInfo gri = (GroupResponseInfo) createRestRequestPrivate(GroupResponseInfo.class, HttpMethod.GET, REST_GROUP_CREATE, urlParams, null);

        if(gri.getErrorCode() != 0)
            throw new InternalErrorException(gri.getErrorMessage());

        return restGroupAttach(gri.getGroupInfo().getHash(), location);
    }

    public GroupInfo restGroupData(String groupHash, Location location) throws InternalErrorException{

        Integer id = this.getID();

        MultiValueMap<String, String> body = new LinkedMultiValueMap<>();
        body.add("latitude", location.getLatitude() + "");
        body.add("longitude", location.getLongitude() + "");


        HashMap<String,String> urlParams = new HashMap<>();
        urlParams.put(REST_GROUP_DATA_ID_DEV, id+"");
        urlParams.put(REST_GROUP_DATA_GROUP_HASH, groupHash);


        GroupResponseInfo gri = (GroupResponseInfo) createRestRequestPrivate(GroupResponseInfo.class, HttpMethod.POST, REST_GROUP_DATA, urlParams, body);

        if(gri.getErrorCode() != 0)
            throw new InternalErrorException(gri.getErrorMessage());

        return gri.getGroupInfo();
    }

    public GroupInfo restGroupAttach(String groupHash, Location location) throws InternalErrorException{

        Integer id = this.getID();

        MultiValueMap<String, String> body = new LinkedMultiValueMap<>();
        body.add("latitude", location.getLatitude() + "");
        body.add("longitude", location.getLongitude() + "");


        HashMap<String,String> urlParams = new HashMap<>();
        urlParams.put(REST_GROUP_DATA_ID_DEV, id+"");
        urlParams.put(REST_GROUP_DATA_GROUP_HASH, groupHash);

        GroupResponseInfo gri = (GroupResponseInfo) createRestRequestPrivate(GroupResponseInfo.class, HttpMethod.POST, REST_GROUP_ATTACH, urlParams, body);

        if(gri.getErrorCode() != 0)
            throw new InternalErrorException(gri.getErrorMessage());

        return gri.getGroupInfo();
    }

    public void restGroupDetach(String groupHash) throws InternalErrorException{

        Integer id = this.getID();

        HashMap<String,String> urlParams = new HashMap<>();
        urlParams.put(REST_GROUP_DATA_ID_DEV, id+"");
        urlParams.put(REST_GROUP_DATA_GROUP_HASH, groupHash);

        GroupResponseInfo gri = (GroupResponseInfo) createRestRequestPrivate(GroupResponseInfo.class, HttpMethod.GET, REST_GROUP_DETACH, urlParams, null);

        if(gri.getErrorCode() != 0)
            throw new InternalErrorException(gri.getErrorMessage());
    }

    private ResponseInfo createRestRequestPrivate(final Class paramClass,
                                                  final HttpMethod method,
                                                  final String url,
                                                  final Map<String, String> urlParams,
                                                  final MultiValueMap<String, String> body) {


        // Create a new RestTemplate instance
        RestTemplate restTemplate = new RestTemplate();
        restTemplate.getMessageConverters().add(new MappingJackson2HttpMessageConverter());

        HttpHeaders headers = new HttpHeaders();
        headers.setAccept(Arrays.asList(MediaType.ALL));
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

        HttpEntity<String> entity;

        if (method == HttpMethod.POST) {
            entity = new HttpEntity(body, headers);
        } else {
            entity = new HttpEntity(headers);
        }

        //result = restTemplate.exchange(url, method, entity, DeviceResponseInfo.class);

        ResponseInfo ri = forObject(paramClass, method, url, entity, restTemplate, urlParams);

        return ri;
    }

    private ResponseInfo forObject(Class paramClass,
                                   HttpMethod method,
                                   String url,
                                   HttpEntity<String> entity,
                                   RestTemplate restTemplate,
                                   Map<String, String> urlParams) {

        // Make the HTTP POST request, marshaling the response to a String
        if (urlParams != null) {
            if(method == HttpMethod.POST)
                if(paramClass == DeviceResponseInfo.class)
                    return restTemplate.postForObject(url, entity, DeviceResponseInfo.class, urlParams);
                else
                    return restTemplate.postForObject(url, entity, GroupResponseInfo.class, urlParams);
            else
                if(paramClass == DeviceResponseInfo.class)
                    return restTemplate.getForObject(url, DeviceResponseInfo.class, urlParams);
                else
                    return restTemplate.getForObject(url, GroupResponseInfo.class, urlParams);
        } else {
            if(method == HttpMethod.POST)
                if(paramClass == DeviceResponseInfo.class)
                    return restTemplate.postForObject(url, entity, DeviceResponseInfo.class);
                else
                    return restTemplate.postForObject(url, entity, GroupResponseInfo.class);
            else
                if(paramClass == DeviceResponseInfo.class)
                    return restTemplate.postForObject(url, entity, DeviceResponseInfo.class);
                else
                    return restTemplate.postForObject(url, entity, DeviceResponseInfo.class);
        }
    }

}
