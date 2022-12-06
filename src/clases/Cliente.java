package clases;

import java.io.*;
import java.net.Socket;
import java.util.Scanner;
import java.util.concurrent.BrokenBarrierException;
import java.util.concurrent.CyclicBarrier;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.regex.Pattern;

public class Cliente {


    public static void main(String[] args) throws IOException {

        ObjectInputStream ois = null;
        ObjectOutputStream oos = null;

        try (Socket socket = new Socket("localhost", 6666);
             BufferedReader br = new BufferedReader(new InputStreamReader(socket.getInputStream()));
             BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));
             DataOutputStream dos = new DataOutputStream(socket.getOutputStream())) {

            oos = new ObjectOutputStream(socket.getOutputStream());
            ois = new ObjectInputStream(socket.getInputStream());

            Scanner s = new Scanner(System.in);
            int i = 0;
            boolean error = true;

            do {
                try {
                    System.out.println("Bienvenido, ¿Que operacion desea realizar? 1:Iniciar sesión, 2:Registrarse, 3:Salir");
                    i = Integer.parseInt(s.nextLine());
                    if (i > 0 && i < 4) {
                        bw.write(i + "\r\n");
                        bw.flush();
                        switch (i) {
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
                        String bienvenida;
                        Usuario user = null;
                        if (i != 3) {
                            bienvenida = br.readLine();
                            System.out.println(bienvenida);
                            String[] trozos = bienvenida.split(" ");
                            user = new Usuario(trozos[trozos.length - 1]);
                        }
                        if (!error) {
                            int j = 0;
                            String input;
                            do {
                                try {
                                    System.out.println("Actualmente estas en el directorio " + user.getDirectorio());
                                    System.out.println("--------------------------------------------------------------");
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

                                    if (j > 0 && j < 11) {
                                        bw.write(j + "\r\n");
                                        bw.flush();
                                        switch (j) {
                                            case 1:
                                                oos.writeObject(user);
                                                oos.flush();
                                                System.out.println("Introduce la ruta del fichero a subir o 'Salir' para salir.");
                                                String nombreF = s.nextLine();
                                                if (!nombreF.equalsIgnoreCase("salir")) {
                                                    subirFichero(nombreF, dos, bw);
                                                }
                                                break;
                                            case 2:
                                                oos.writeObject(user);
                                                oos.flush();
                                                System.out.println("Introduce la ruta de la carpeta a subir o 'Salir' para salir.");
                                                String nombreD = s.nextLine();
                                                if (!nombreD.equalsIgnoreCase("salir")) {
                                                    subirCarpeta(user.getDirectorioCompleto(), obtenerNombreDesdeRuta(nombreD), nombreD, dos, bw, br);
                                                    bw.write("");
                                                    bw.flush();
                                                }
                                                break;
                                            case 3:
                                                break;
                                            case 4:
                                                break;
                                            case 5:
                                                System.out.println("Introduce el nombre de la carpeta nueva: ");
                                                input = s.nextLine();
                                                bw.write(input);
                                                bw.flush();
                                                oos.writeObject(user);
                                                oos.flush();
                                                System.out.println(br.readLine());
                                                break;
                                            case 6:
                                                oos.writeObject(user);
                                                oos.flush();
                                                String linea = br.readLine();
                                                while (!linea.equals("")) {
                                                    System.out.println(linea);
                                                    linea = br.readLine();
                                                }

                                                break;
                                            case 7:
                                                System.out.println("Introduce el nombre del directorio: ");
                                                input = s.nextLine();
                                                bw.write(input + "\r\n");
                                                bw.flush();
                                                oos.writeObject(user);
                                                oos.flush();
                                                System.out.println(br.readLine());
                                                user = (Usuario) ois.readObject();
                                                break;
                                            case 8:
                                                oos.writeObject(user);
                                                oos.flush();
                                                user = (Usuario) ois.readObject();

                                                break;
                                        }

                                    }
                                } catch (NumberFormatException nfe) {
                                    System.out.println("Introduce un numero!");
                                } catch (ClassNotFoundException e) {
                                    throw new RuntimeException(e);
                                }
                            } while (j != 10);
                        }

                    }
                } catch (NumberFormatException nfe) {
                    System.out.println("Introduce un numero!");
                }
            } while (error && i != 3);


        } catch (IOException e) {
            throw new RuntimeException(e);
        } finally {
            ois.close();
            oos.close();
        }
    }

    private static void registro(Scanner s, BufferedWriter bw, BufferedReader br) {
        String respuesta;
        try {
            System.out.println("Introduce el nombre de usuario: ");
            bw.write(s.nextLine() + "\r\n");
            bw.flush();
            System.out.println("Introduce la contraseña: ");
            bw.write(s.nextLine() + "\r\n");
            bw.flush();
            respuesta = br.readLine();
            while (!respuesta.equals("Correcto")) {
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

    private static void alta(Scanner s, BufferedWriter bw, BufferedReader br) {
        String respuesta;
        try {
            System.out.println("Introduce el nombre del nuevo usuario: ");
            bw.write(s.nextLine() + "\r\n");
            bw.flush();
            respuesta = br.readLine();
            while (!respuesta.equals("Correcto")) {
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

    private static void subirFichero(String nombreF, DataOutputStream dos, BufferedWriter bw) {
        try {
            File f = new File(nombreF);
            if (f.exists()) {
                bw.write(nombreF + "\r\n");
                bw.flush();
                int leidos;
                try (FileInputStream fis = new FileInputStream(f)) {
                    byte[] buf = new byte[1024 * 256];
                    leidos = fis.read(buf);
                    while (leidos != -1) {
                        dos.write(buf, 0, leidos);
                        dos.flush();
                        leidos = fis.read(buf);
                    }
                } catch (IOException ex) {
                    throw new RuntimeException(ex);
                }
            } else {
                System.out.println("El fichero introducido no existe en el disco duro.");
                bw.write("Salir\r\n");
                bw.flush();
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

    }

    private static void subirCarpeta(String pathDirNube, String pathDirPadreNube, String pathDirLocal,
                                     DataOutputStream dos, BufferedWriter bw, BufferedReader br) {
        File dirLocal = new File(pathDirLocal);
        File[] subFichs = dirLocal.listFiles();
        if (subFichs != null && subFichs.length > 0) {
            for (File item : subFichs) {
                String pathFichNube;
                if (pathDirPadreNube.equals("")) {
                    pathFichNube = pathDirNube + "/" + item.getName();
                } else {
                    pathFichNube = pathDirNube + "/" + pathDirPadreNube + "/" + item.getName();
                }

                if (item.isFile()) {
                    System.out.println("Subiendo fichero: " + item.getAbsolutePath());
                    subirFichero(item.getAbsolutePath(), dos, bw);
                } else {
                    if (crearDirectorioRemoto(pathFichNube, bw, br)) {
                        System.out.println("Directorio creado correctamente en la nube");
                    }
                    String padre = pathDirPadreNube + "/" + item.getName();
                    if (pathDirPadreNube.equals("")) {
                        padre = item.getName();
                    }
                    pathDirLocal = item.getAbsolutePath();
                    subirCarpeta(pathDirNube, padre, pathDirLocal, dos, bw, br);

                }
            }
        }


    }

    private static String obtenerNombreDesdeRuta(String ruta){
        String[] trozos = ruta.split(Pattern.quote(System.getProperty("file.separator")));
        return trozos[trozos.length - 1];
    }

    private static boolean crearDirectorioRemoto(String pathAbsoluto, BufferedWriter bw, BufferedReader br) {
        try {
            bw.write("<D> " + pathAbsoluto + "\r\n");
            bw.flush();
            if (br.readLine().equals("OK")) {
                return true;
            } else {
                return false;
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }


}
