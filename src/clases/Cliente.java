package clases;

import java.io.*;
import java.net.Socket;
import java.util.Scanner;
import java.util.regex.Pattern;

public class Cliente {


    public static void main(String[] args) throws IOException {

        ObjectInputStream ois = null;
        ObjectOutputStream oos = null;

        try (Socket socket = new Socket("localhost", 6666);
             DataOutputStream dos = new DataOutputStream(socket.getOutputStream());
             DataInputStream dis = new DataInputStream(socket.getInputStream())) {

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
                        dos.writeBytes(i + "\r\n");
                        dos.flush();
                        switch (i) {
                            case 1:
                                registro(s, dos, dis);
                                error = false;
                                break;
                            case 2:
                                alta(s, dos, dis);
                                error = false;
                                break;
                            case 3:
                                break;

                        }
                        String bienvenida;
                        Usuario user = null;
                        if (i != 3) {
                            bienvenida = dis.readLine();
                            System.out.println(bienvenida);
                            String[] trozos = bienvenida.split(" ");
                            user = new Usuario(trozos[trozos.length - 1]);
                        }
                        if (!error) {
                            int j = 0;
                            String input;
                            do {
                                try {
                                    System.out.println("--------------------------------------------------------------");
                                    System.out.println("Actualmente estas en el directorio " + user.getDirectorio());
                                    System.out.println("--------------------------------------------------------------");
                                    System.out.println("Elija la operación que desea realizar.");
                                    System.out.println("1. Subir un fichero");
                                    System.out.println("2. Subir una carpeta");
                                    System.out.println("3. Descargar un fichero");
                                    System.out.println("4. Descargar una carpeta");
                                    System.out.println("5. Borrar un fichero de la nube");
                                    System.out.println("6. Borrar una carpeta de la nube");
                                    System.out.println("7. Crear una nueva carpeta");
                                    System.out.println("8. Mostrar directorio actual");
                                    System.out.println("9. Cambiar de directorio");
                                    System.out.println("10. Ir al directorio padre");
                                    System.out.println("11. Compartir un fichero con otro usuario");
                                    System.out.println("12. Salir");
                                    j = Integer.parseInt(s.nextLine());

                                    if (j > 0 && j < 11) {
                                        dos.writeBytes(j + "\r\n");
                                        dos.flush();
                                        switch (j) {
                                            case 1:
                                                oos.writeObject(user);
                                                oos.flush();
                                                System.out.println("Introduce la ruta del fichero a subir o 'Salir' para salir.");
                                                String nombreF = s.nextLine();
                                                if (!nombreF.equalsIgnoreCase("salir")) {
                                                    subirFichero(nombreF, dos);
                                                }
                                                break;
                                            case 2:
                                                oos.writeObject(user);
                                                oos.flush();
                                                System.out.println("Introduce la ruta de la carpeta a subir o 'Salir' para salir.");
                                                String nombreD = s.nextLine();
                                                if (!nombreD.equalsIgnoreCase("salir")) {
                                                    crearDirectorioRemoto(user.getDirectorioCompleto() + "\\" + obtenerNombreDesdeRuta(nombreD), dos, dis);
                                                    subirCarpeta(user.getDirectorioCompleto(), obtenerNombreDesdeRuta(nombreD), nombreD, dos, dis);
                                                    dos.writeBytes("\r\n");
                                                    dos.flush();
                                                }
                                                break;
                                            case 3:

                                                break;
                                            case 4:
                                                break;
                                            case 5:
                                                oos.writeObject(user);
                                                oos.flush();
                                                System.out.println("Introduce el nombre del fichero a borrar (existente en el directorio actual) o 'Salir' para salir.");
                                                String nom = s.nextLine();
                                                if (!nom.equalsIgnoreCase("salir")){
                                                    if (borrarFichero(nom, dos, dis)){
                                                        System.out.println("Fichero borrado correctamente.");
                                                    } else {
                                                        System.out.println("Error al eliminar el fichero.");
                                                    }
                                                }
                                                break;
                                            case 6:
                                                oos.writeObject(user);
                                                oos.flush();
                                                System.out.println("Introduce la ruta de la carpeta a eliminar (existente en el directorio actual) o 'Salir' para salir.");
                                                String nomD = s.nextLine();
                                                if (!nomD.equalsIgnoreCase("salir")){
                                                    borrarCarpeta(nomD, dos);
                                                }
                                                break;
                                            case 7:
                                                System.out.println("Introduce el nombre de la carpeta nueva: ");
                                                input = s.nextLine();
                                                dos.writeBytes(input + "\r\n");
                                                dos.flush();
                                                oos.writeObject(user);
                                                oos.flush();
                                                System.out.println(dis.readLine());

                                                break;
                                            case 8:
                                                oos.writeObject(user);
                                                oos.flush();
                                                String linea = dis.readLine();
                                                while (!linea.equals("")) {
                                                    System.out.println(linea);
                                                    linea = dis.readLine();
                                                }


                                                break;
                                            case 9:
                                                System.out.println("Introduce el nombre del directorio: ");
                                                input = s.nextLine();
                                                dos.writeBytes(input + "\r\n");
                                                dos.flush();
                                                oos.writeObject(user);
                                                oos.flush();
                                                System.out.println(dis.readLine());
                                                user = (Usuario) ois.readObject();


                                                break;
                                            case 10:
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
                            } while (j != 12);
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

    private static void registro(Scanner s, DataOutputStream dos, DataInputStream dis) {
        String respuesta;
        try {
            System.out.println("Introduce el nombre de usuario: ");
            dos.writeBytes(s.nextLine() + "\r\n");
            dos.flush();
            System.out.println("Introduce la contraseña: ");
            dos.writeBytes(s.nextLine() + "\r\n");
            dos.flush();
            respuesta = dis.readLine();
            while (!respuesta.equals("Correcto")) {
                System.out.println(respuesta);
                System.out.println("Introduce el nombre de usuario: ");
                dos.writeBytes(s.nextLine() + "\r\n");
                dos.flush();
                System.out.println("Introduce la contraseña: ");
                dos.writeBytes(s.nextLine() + "\r\n");
                dos.flush();
                respuesta = dis.readLine();
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }


    }

    private static void alta(Scanner s, DataOutputStream dos, DataInputStream dis) {
        String respuesta;
        try {
            System.out.println("Introduce el nombre del nuevo usuario: ");
            dos.writeBytes(s.nextLine() + "\r\n");
            dos.flush();
            respuesta = dis.readLine();
            while (!respuesta.equals("Correcto")) {
                System.out.println(respuesta);
                System.out.println("Introduce el nombre del nuevo usuario: ");
                dos.writeBytes(s.nextLine() + "\r\n");
                dos.flush();
                respuesta = dis.readLine();
            }
            System.out.println("Introduce la contraseña: ");
            dos.writeBytes(s.nextLine() + "\r\n");
            dos.flush();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private static void subirFichero(String nombreF, DataOutputStream dos) {
        try {
            File f = new File(nombreF);
            if (f.exists()) {
                dos.writeBytes(nombreF + "\r\n");
                dos.flush();
                dos.writeBytes(f.length() + "\r\n");
                dos.flush();
                try (FileInputStream fs = new FileInputStream(f);
                     DataInputStream fis = new DataInputStream(fs)) {
                    byte[] buf = new byte[(int) (f.length())];
                    fis.readFully(buf,0,buf.length);
                    dos.write(buf, 0, buf.length);
                    dos.flush();
                } catch (IOException ex) {
                    throw new RuntimeException(ex);
                }
            } else {
                System.out.println("El fichero introducido no existe en el disco duro.");
                dos.writeBytes("Salir\r\n");
                dos.flush();
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

    }

    private static void subirCarpeta(String pathDirNube, String pathDirPadreNube, String pathDirLocal,
                                     DataOutputStream dos, DataInputStream dis) {
        File dirLocal = new File(pathDirLocal);
        File[] subFichs = dirLocal.listFiles();
        if (subFichs != null && subFichs.length > 0) {
            for (File item : subFichs) {
                String pathFichNube;
                if (pathDirPadreNube.equals("")) {
                    pathFichNube = pathDirNube + "\\" + item.getName();
                } else {
                    pathFichNube = pathDirNube + "\\" + pathDirPadreNube + "\\" + item.getName();
                }

                if (item.isFile()) {
                    System.out.println("Subiendo fichero: " + item.getAbsolutePath());
                    subirFichero(item.getAbsolutePath(), dos);
                } else {
                    if (crearDirectorioRemoto(pathFichNube, dos, dis)) {
                        String direc = pathFichNube.replace("src\\nube\\", "");
                        System.out.println("Directorio " + direc + " creado correctamente en la nube");
                    }
                    String padre = pathDirPadreNube + "\\" + item.getName();
                    if (pathDirPadreNube.equals("")) {
                        padre = item.getName();
                    }
                    pathDirLocal = item.getAbsolutePath();
                    subirCarpeta(pathDirNube, padre, pathDirLocal, dos, dis);

                }
            }
        }

    }

    private static String obtenerNombreDesdeRuta(String ruta){
        String[] trozos = ruta.split(Pattern.quote(System.getProperty("file.separator")));
        return trozos[trozos.length - 1];
    }

    private static boolean crearDirectorioRemoto(String pathAbsoluto, DataOutputStream dos, DataInputStream dis) {
        try {
            dos.writeBytes( pathAbsoluto + "\r\n");
            dos.flush();
            return dis.readLine().equals("OK");
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static boolean borrarFichero(String nombre, DataOutputStream dos, DataInputStream dis){
        try {
            dos.writeBytes(nombre + "\r\n");
            dos.flush();
            return dis.readLine().equals("OK");

        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static void borrarCarpeta(String nombre, DataOutputStream dos){
        try {
            dos.writeBytes(nombre + "\r\n");
            dos.flush();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }


}
