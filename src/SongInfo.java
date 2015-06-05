
import java.io.Serializable;


public class SongInfo implements Serializable
{
    protected String name;
    protected String artist;
    protected String genre;
    protected String album;
    protected int year;
    protected int id;
    protected String hash;

    public SongInfo() 
    {
        name = "";
        artist = "";
        genre = "";
        album = "";
    }
    
    public SongInfo(String hash) 
    {
        name = "";
        artist = "";
        genre = "";
        album = "";
        this.hash = hash;
    }

    public SongInfo(String name, String artist, String genre, String album, int year, String hash) 
    {
        this.name = name;
        this.artist = artist;
        this.genre = genre;
        this.album = album;
        this.year = year;
        this.hash = hash;
    }
    
    
}


