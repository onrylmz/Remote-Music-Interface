

import java.rmi.Remote;
import java.rmi.RemoteException;


public interface ISongInfoService extends Remote {
    
    public int addSong (SongInfo arg) throws RemoteException;
    public SongInfo getSong (SongInfo arg) throws RemoteException;
    
}
