import java.io.*;
import java.net.Socket;
import java.util.Scanner;

public class Cliente {


    public static void main(String[] args){
        try (Socket socket = new Socket("localhost", 6666);
            BufferedReader br = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()))){

            Scanner s = new Scanner(System.in);
            int i=0;
            boolean error = true;

            do {
                try {
                    System.out.println("Bienvenido, ¿Que operacion desea realizar? 1:Iniciar sesión, 2:Registrarse, 3:Salir");
                    i = Integer.parseInt(s.nextLine());
                    if (i>0 && i<4){
                        bw.write(i + "\r\n");
                        bw.flush();
                        switch (i){
                            case 1:
                                registro(s, bw, br);
                                error = false;
                                break;
                            case 2:
                                alta(s, bw, br);
                                error = false;
                                break;
                            case 3:
                                break;

                        }

                    }
                } catch (NumberFormatException nfe){
                    System.out.println("Introduce un numero!");
                }
            } while (error && i!=3);


        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private static void registro(Scanner s, BufferedWriter bw, BufferedReader br){
        String respuesta;
        try {
            System.out.println("Introduce el nombre de usuario: ");
            bw.write(s.nextLine() + "\r\n");
            bw.flush();
            System.out.println("Introduce la contraseña: ");
            bw.write(s.nextLine() + "\r\n");
            bw.flush();
            respuesta = br.readLine();
            while (!respuesta.equals("Correcto")){
                System.out.println(respuesta);
                System.out.println("Introduce el nombre de usuario: ");
                bw.write(s.nextLine() + "\r\n");
                bw.flush();
                System.out.println("Introduce la contraseña: ");
                bw.write(s.nextLine() + "\r\n");
                bw.flush();
                respuesta = br.readLine();
            }
            System.out.println(br.readLine());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }


    }

    private static void alta(Scanner s, BufferedWriter bw, BufferedReader br){
        String respuesta;
        try {
            System.out.println("Introduce el nombre del nuevo usuario: ");
            bw.write(s.nextLine() + "\r\n");
            bw.flush();
            respuesta = br.readLine();
            while (!respuesta.equals("Correcto")){
                System.out.println(respuesta);
                System.out.println("Introduce el nombre del nuevo usuario: ");
                bw.write(s.nextLine() + "\r\n");
                bw.flush();
                respuesta = br.readLine();
            }
            System.out.println("Introduce la contraseña: ");
            bw.write(s.nextLine() + "\r\n");
            bw.flush();
            System.out.println(br.readLine());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }







}
