package cz.vutbr.fit.tam.meetme.release.requestcrafter;

import android.location.Location;

import cz.vutbr.fit.tam.meetme.release.exceptions.InternalErrorException;
import cz.vutbr.fit.tam.meetme.release.schema.GroupInfo;

/**
 * Created by Lada on 22.10.2015.
 */
public interface RequestCrafterInterface
{
    /*implemented*/
    public GroupInfo restGroupCreate(Location loc) throws InternalErrorException;
    public GroupInfo restGroupAttach(String groupHash, Location loc) throws InternalErrorException;
    public void restGroupDetach(String groupHash) throws InternalErrorException;
    public GroupInfo restGroupData(String groupHash, Location loc) throws InternalErrorException;


   /*not implemented yet
   public DeviceInfo restInstall(UserInfo userInfo) throws InternalErrorException; // v userInfo muzou byt ruzne polozky, neni pak treba menit hlavicky metod pri pridani dalsi polozky
   public UserInfo restUpdateUserInfo(UserInfo userInfo) throws InternalErrorException;   //v pripade ze by bylo uzivateli poskutnuto nejake nastaveni, bude si updatovat sva data pomoci tohoto
   public UserInfo restLoadUserInfo(int idDevice) throws InternalErrorException; // toto bude ukladano do hashmap, <idDevice, UserInfo> tak jak sme se bavili.
   */
}

