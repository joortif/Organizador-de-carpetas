import java.io.*;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.Scanner;

public class Cliente {

    public static void main(String[] args){
        try (Socket socket = new Socket("localhost", 6666);
            BufferedReader br = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()))){
            Scanner s = new Scanner(System.in);

            String linea = br.readLine();
            while (linea != null){
                System.out.println(linea);
                bw.write(s.nextLine() + "\r\n");
                bw.flush();
                linea = br.readLine();
            }



        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }



}
