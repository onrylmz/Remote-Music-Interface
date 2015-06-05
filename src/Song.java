
import java.io.Serializable;


public class Song implements Serializable
{
    protected SongInfo info;
    protected byte[] data = null;
    Song(SongInfo arg, byte[] data) 
    {
         info = arg;
         this.data = data;
    }

    public Song(SongInfo info) {
        this.info = info;
    }
	
    
    
}
