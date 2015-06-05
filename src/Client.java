
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.math.BigInteger;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Random;
import java.util.Scanner;

/**
 *
 * @author Yamac
 */
public class Client {

    protected Random rng;
    protected static MessageDigest md;
    protected static ISongInfoService stub;

    public static void main(String[] args) throws  RemoteException, IOException, ClassNotFoundException 
    {

        
        Client client = new Client();

        //Code logic goes here
        //get input
        Scanner scanner = new Scanner(System.in);
        
        while(true)
        {
            try
            {
                System.out.println("Please Enter a Process such as 'get <filename>' OR 'update<filename>' OR 'read <filename> | *' OR 'close connection'");

                String command = scanner.nextLine();
                String[] splittedCommand = command.split(" ");
                String commandType = splittedCommand[0];
                String commandFile = splittedCommand[1];

                //process command
                //************************************************
                //get input.audio: 
                //read the input file. 
                if(commandType.equalsIgnoreCase("get"))
                {
                    File file = new File(commandFile+ ".audio");

                    InputStream in = new FileInputStream(file);

                    byte[] inputContent = new byte[(int) file.length()];

                    in.read(inputContent);

                    //calculate hash
                    byte[] digest = md.digest(inputContent);
                    BigInteger bi = new BigInteger(1, digest);
                    String hash = bi.toString(16);

                    //get the song info using RMI
                    SongInfo inf = stub.getSong(new SongInfo(hash));


                    //create new song
                    Song s = new Song(inf, inputContent);

                    //ask user for serialized file name
                    System.out.println("Please Enter a Filename to be Serialized (without .ser)!!!");
                    String desiredFileName = scanner.nextLine();

                    //serialize s to inputted file name
                    client.serializeObject(desiredFileName, s);
                }
                //**************************************************
                //read input.ser | *:
                else if(commandType.equalsIgnoreCase("read"))
                {
                    //if input.ser is supplied
                    if(!commandFile.equalsIgnoreCase("*"))
                    {
                        Song printableSong = client.deserializeObject(commandFile);

                        //print the content of input.ser 's SongInfo
                        printSongInfo(printableSong.info);
                    }


                    //if * is supplied
                    else
                    {
                        //print all the .ser files
                        File dir = new File(System.getProperty("user.dir"));
                        File[] list = dir.listFiles(
                        new FilenameFilter()
                        {

                            @Override
                            public boolean accept(File dir, String name) 
                            {
                                // get last index for '.' char
                                int lastIndex = name.lastIndexOf('.');
                                // get extension
                                if (lastIndex > 0) {
                                    String str = name.substring(lastIndex);
                                    // match extension
                                    if (str.equals(".ser")) {
                                        return true;
                                    }
                                }
                                return false;
                            }

                        });

                        for(File file: list)
                        {
                            String fileName = file.getName().split("\\.")[0];
                            Song pSong = client.deserializeObject(fileName);
                            printSongInfo(pSong.info);
                        }
                    }
                }
                //****************************************************
                else if(commandType.equalsIgnoreCase("update"))
                {
                    //update input.ser
                    //deserialize the input.ser to an object
                    Song printableSong = client.deserializeObject(commandFile);
                    //change related fields in the SongInfo using interface or command arguments
                    System.out.println("Please Provide Updated Name OR Enter for No Change");
                    String name = scanner.nextLine();
                    if(name.equalsIgnoreCase(""))
                        name = printableSong.info.name;

                    System.out.println("Please Provide Updated Artist OR Enter for No Change");
                    String artist = scanner.nextLine();
                    if(artist.equalsIgnoreCase(""))
                        artist = printableSong.info.artist;


                    System.out.println("Please Provide Updated Album OR Enter for No Change");
                    String album = scanner.nextLine();
                    if(album.equalsIgnoreCase(""))
                        album = printableSong.info.album;

                    System.out.println("Please Provide Updated Genre OR Enter for No Change");
                    String genre = scanner.nextLine();
                    if(genre.equalsIgnoreCase(""))
                        genre = printableSong.info.genre;

                    System.out.println("Please Provide Updated Year OR Enter for No Change");
                    String year = scanner.nextLine();
                    if(year.equalsIgnoreCase(""))
                        year = Integer.toString(printableSong.info.year);

                    String hash = printableSong.info.hash;

                    //update the song in the database using RMI
                    SongInfo songInfo = new SongInfo(name, artist, genre, album, Integer.parseInt(year), hash);

                    int result = stub.addSong(songInfo);
                    
                    if(result != 0)
                        //overwrite the serialized file with the new info
                        client.serializeObject(commandFile, new Song(songInfo));
                }
                //****************************************************
                else if(commandType.equalsIgnoreCase("close"))
                {
                    break;
                }
            }
            catch(FileNotFoundException e)
            {
                System.out.println("There is no such a file.. Please a Provide a Valid Name");
            }
        }
    }

    public Client() {
        try 
        {
            rng = new Random();        
            md = MessageDigest.getInstance("MD5");

            Registry registry = LocateRegistry.getRegistry("localhost");
            
            stub = (ISongInfoService) registry.lookup("RMIServer");
            
        } 
        catch (NoSuchAlgorithmException ex) 
        {
            System.err.println("NoSuchAlgorithmException");
        } 
        catch (RemoteException ex) 
        {
            System.err.println("RemoteException");
        } 
        catch (NotBoundException ex) {
            System.err.println("NotBoundException");
        }
        //connect to RMI repository. Pay attention to the bound service name in the server.
        //(do not forget to start the repository service)

    }

    
    public static void printSongInfo(SongInfo songInfo)
    {
        System.out.println(songInfo.name + " " +  songInfo.artist + " " + songInfo.album + " " + songInfo.genre + " " + songInfo.year + " " + songInfo.hash);
    }
    
    public Song deserializeObject(String inputName) throws FileNotFoundException, IOException, ClassNotFoundException
    {
        //read the input.ser to an object
        FileInputStream fis = new FileInputStream( inputName + ".ser");
        ObjectInputStream ois = new ObjectInputStream(fis);

        Song printableSong = (Song) ois.readObject();
        
        ois.close();
        
        return printableSong;
        
    }
    
    public void serializeObject(String desiredFileName, Song s) throws FileNotFoundException, IOException
    {
        FileOutputStream fos = new FileOutputStream(desiredFileName + ".ser");
        ObjectOutputStream oos = new ObjectOutputStream(fos);

        oos.writeObject(s);

        oos.close();
    }
}
