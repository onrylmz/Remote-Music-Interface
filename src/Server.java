
import java.rmi.Remote;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Random;
import java.util.logging.Level;
import java.util.logging.Logger;

public class Server implements ISongInfoService {

    protected Random rng;
    protected static String name;
    protected static String driver;
    protected static String url;
    protected static String database;
    protected static String username;
    protected static String password;
    protected static Connection conn;

    public void setRng(Random rng) {
        this.rng = rng;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setDriver(String driver) {
        this.driver = driver;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public void setDatabase(String database) {
        this.database = database;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public static String getDriver() {
        return driver;
    }

    public static String getUrl() {
        return url;
    }

    public static String getDatabase() {
        return database;
    }

    public static String getUsername() {
        return username;
    }

    public static String getPassword() {
        return password;
    }

    public Random getRng() {
        return rng;
    }

    public static String getName() {
        return name;
    }
    
    public final void initialize() {
        setDriver("com.mysql.jdbc.Driver");
        setUrl("jdbc:mysql://localhost:3306/");
        setDatabase("peertopeer");
        setUsername("root");
        setPassword("");
        setRng(new Random());
        setName("songs");
    }

    public Server() {
        super();
        rng = new Random();
        initialize();
        
        //more initialization stuff
        
        try
        {
            Class.forName(getDriver());
            
            conn = DriverManager.getConnection(getUrl() + getDatabase(), getUsername(), getPassword());
        }
        catch(ClassNotFoundException e)
        {
            System.err.println("ClassNotFoundException");
        } 
        catch (SQLException e) 
        {
            System.err.println("SQLException");
        }
    }

    public static void main(String[] args) throws RemoteException 
    {
        System.setProperty("java.rmi.server.hostname", "localhost");
        System.setProperty("java.rmi.server.codebase", "file:./");
        //create stub and bind it to the name in the registry
        
        Server server = new Server();
        
        ISongInfoService stub = (ISongInfoService) UnicastRemoteObject.exportObject(server, 0);
        
        LocateRegistry.createRegistry(1099);
        LocateRegistry.getRegistry().rebind("RMIServer", stub);

    }

    /**
     * **************************************
     *										*
     * implement the interface methods	* *
     * **************************************
     * @param arg
     * @throws java.rmi.RemoteException
     */
    
   
    
    @Override
    public int addSong (SongInfo arg) throws RemoteException
    {
        int result = 0;
        try 
        {
            /*String query = "update songs set"
                            + " name = " + '"' + arg.name + '"' + ','
                            + " artist = " + '"' + arg.artist + '"' + ','
                            + " album = " + '"' + arg.album + '"' + ','
                            + " genre = " + '"' + arg.genre + '"' + ','
                            + " year = " + arg.year
                            + " where hash = "+ "'" + arg.hash + "'";
            
            Statement st = conn.createStatement();
            
            result = st.executeUpdate(query);*/
            
            
            String query1 = "update songs set name = ?, artist = ?, album = ?, genre = ?, year = ? where hash = ?";
            
            PreparedStatement ps = conn.prepareStatement(query1);
            
            ps.setString(1, arg.name);
            ps.setString(2, arg.artist);
            ps.setString(3, arg.album);
            ps.setString(4, arg.genre);
            ps.setInt(5, arg.year);
            ps.setString(6, arg.hash);
            
            result = ps.executeUpdate();
            
            ps.close();
            
            return result;
        } 
        catch (SQLException ex) {
            Logger.getLogger(Server.class.getName()).log(Level.SEVERE, null, ex);
        }
        return result;
    }
    @Override
    public SongInfo getSong (SongInfo arg) throws RemoteException
    {
        SongInfo songInfo = new SongInfo();
        
        try {
                        
            String query = "select * from songs where hash = "+ "'" + arg.hash +"'";
            
            Statement st = conn.createStatement();
            
            ResultSet rs = st.executeQuery(query);
            
            if(rs.next())
            {
                songInfo.id = rs.getInt("idsongs");
                songInfo.name = rs.getString("name");
                songInfo.artist = rs.getString("artist");
                songInfo.album = rs.getString("album");
                songInfo.genre = rs.getString("genre");
                songInfo.year = rs.getInt("year");
                songInfo.hash = rs.getString("hash");
            }
            else
            {
                insertSong(arg);
                
                String query1 = "select * from songs where hash = " + "'" + arg.hash + "'";
                
                Statement st1 = conn.createStatement();
                
                ResultSet rs1 = st1.executeQuery(query1);
                
                if(rs1.next())
                {
                    songInfo.id = rs1.getInt("idsongs");
                    songInfo.name = rs1.getString("name");
                    songInfo.artist = rs1.getString("artist");
                    songInfo.album = rs1.getString("album");
                    songInfo.genre = rs1.getString("genre");
                    songInfo.year = rs1.getInt("year");
                    songInfo.hash = rs1.getString("hash");
                }
                
            }
            
            
        } catch (SQLException ex) 
        {
            Logger.getLogger(Server.class.getName()).log(Level.SEVERE, null, ex);
        }
        
        return songInfo;
    }

    public void testSongInfo(SongInfo songInfo)
    {
        System.out.println(songInfo.id + " " + songInfo.name + " " +  songInfo.artist + " " + songInfo.album + " " + songInfo.genre + " " + songInfo.year + " " + songInfo.hash);
    }
 
    public void insertSong(SongInfo arg) throws SQLException
    {
        String query = "insert into songs(name, artist, album, genre, year, hash) values(" 
                        + "'" + arg.name + "',"
                        + "'" + arg.artist + "',"
                        + "'" + arg.album + "',"
                        + "'" + arg.genre + "',"
                        +  arg.year + ","
                        + "'" + arg.hash + "')";
        
        Statement st = conn.createStatement();
        
        st.executeUpdate(query);
    }
    
}
