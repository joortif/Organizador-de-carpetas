package clases;

import java.io.*;
import java.net.Socket;
import java.util.Scanner;
import java.util.concurrent.CyclicBarrier;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class Cliente {


    public static void main(String[] args){
        try (Socket socket = new Socket("localhost", 6666);
            BufferedReader br = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));
            DataOutputStream dos = new DataOutputStream(socket.getOutputStream());){

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
                        if (i!=3){
                            System.out.println(br.readLine());
                        }
                        if (!error){
                            int j = 0;
                            do {
                                try {
                                    System.out.println("Elija la operación que desea realizar.");
                                    System.out.println("1. Subir un fichero");
                                    System.out.println("2. Subir una carpeta");
                                    System.out.println("3. Descargar un fichero");
                                    System.out.println("4. Descargar una carpeta");
                                    System.out.println("5. Crear una nueva carpeta");
                                    System.out.println("6. Mostrar directorio actual");
                                    System.out.println("7. Cambiar de directorio");
                                    System.out.println("8. Ir al directorio padre");
                                    System.out.println("9. Compartir un fichero con otro usuario");
                                    System.out.println("10. Salir");
                                    j = Integer.parseInt(s.nextLine());
                                    if (j>0 && j<11){
                                        bw.write(j + "\r\n");
                                        bw.flush();
                                        switch(j){
                                            case 1:
                                                subirFichero(s, dos, bw);
                                                break;
                                            case 2:
                                                break;
                                            case 3:
                                                break;
                                            case 4:
                                                break;
                                            case 5:
                                                crearDirec(s);
                                                break;
                                            case 6:
                                                break;
                                            case 7:
                                                break;
                                        }
                                    }
                                } catch (NumberFormatException nfe) {
                                    System.out.println("Introduce un numero!");
                                }
                            } while(j != 10);
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
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private static void subirFichero(Scanner s, DataOutputStream dos, BufferedWriter bw){
        try {
            System.out.println("Introduce la ruta del fichero a subir o 'Salir' para salir.");
            String nombreF = s.nextLine();
            if (!nombreF.equalsIgnoreCase("salir")){
                File f = new File(nombreF);
                if (f.exists()){
                    bw.write(nombreF + "\r\n");
                    bw.flush();
                    int leidos;
                    try(FileInputStream fis = new FileInputStream(f)){
                        bw.write(f.length() + "\r\n");
                        bw.flush();
                        if (f.length() > 1000000L){
                            final CyclicBarrier barrera = new CyclicBarrier(4);
                            ExecutorService pool = Executors.newFixedThreadPool(3);
                            long tercio = f.length()/3;

                        } else {
                            byte[] buf = new byte[1024*256];
                            leidos = fis.read(buf);
                            while (leidos != -1){
                                dos.write(buf, 0, leidos);
                                dos.flush();
                                leidos = fis.read(buf);
                            }
                        }
                    }

                } else {
                    System.out.println("El fichero introducido no existe en el disco duro.");
                    bw.write("Salir\r\n");
                    bw.flush();
                }
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

    }

    private static void crearDirec(Scanner s){
        System.out.println("Introduce el nombre del directorio nuevo");
        String nombreD = s.nextLine();
        File f = new File();
        if (){

        }
    }







}