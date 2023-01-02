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
                            String res;
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
                                    System.out.println("12. Mostrar correo");
                                    System.out.println("13. Vaciar correo");
                                    System.out.println("14. Comprimir y descargar una carpeta");
                                    System.out.println("15. Salir");
                                    j = Integer.parseInt(s.nextLine());

                                    if (j > 0 && j < 16) {
                                        dos.writeBytes(j + "\r\n");
                                        dos.flush();
                                        oos.writeObject(user);
                                        oos.flush();
                                        switch (j) {
                                            case 1:

                                                System.out.println("Introduce la ruta (desde el directorio actual) del fichero a subir ");
                                                input = s.nextLine();
                                                subirFichero(input, dos);
                                                break;
                                            case 2:

                                                System.out.println("Introduce la ruta (desde el directorio actual) de la carpeta a subir");
                                                input = s.nextLine();
                                                File dir = new File(input);
                                                if (dir.exists() && dir.isDirectory()){
                                                    dos.writeBytes(input+"\r\n");
                                                    dos.flush();
                                                    subirCarpeta(user.getDirectorioCompleto(), obtenerNombreDesdeRuta(input), input, dos, dis);
                                                    dos.writeBytes("\r\n");
                                                    dos.flush();
                                                } else {
                                                    System.err.println("La ruta introducida no es válida.");
                                                    dos.writeBytes("error\r\n");
                                                    dos.flush();
                                                }
                                                break;
                                            case 3:
                                                System.out.println("Introduce la ruta en nube (desde el directorio actual) del fichero a descargar");
                                                input = s.nextLine();
                                                dos.writeBytes(input + "\r\n");
                                                dos.flush();
                                                if (dis.readBoolean()){
                                                    System.out.println("Introduce la ruta local en la que se desea guardar el fichero");
                                                    String rutaLocal = s.nextLine();
                                                    descargarFichero(input, new File(rutaLocal), dos, dis);
                                                } else {
                                                    System.err.println("La ruta en la nube no es válida");
                                                }
                                                break;
                                            case 4:
                                                String reempl = "s";
                                                System.out.println("Introduce la ruta (desde el directorio actual) de la carpeta a descargar");
                                                input = s.nextLine();
                                                dos.writeBytes(input + "\r\n");
                                                dos.flush();
                                                if (dis.readBoolean()){
                                                    System.out.println("Introduce la ruta local en la que se desea guardar la carpeta");
                                                    String rutaLocal = s.nextLine();
                                                    File dirDestino = new File(rutaLocal);
                                                    if (dirDestino.exists() && dirDestino.isDirectory()) {
                                                        File nuevoDir = new File(rutaLocal + "\\" + obtenerNombreDesdeRuta(input));
                                                        if (!nuevoDir.exists()){
                                                            nuevoDir.mkdir();
                                                        } else {
                                                            System.out.println("Ya existe un directorio con ese nombre, ¿Desea reemplazarlo? (S/N)");
                                                            reempl = s.nextLine();
                                                            while (!reempl.equalsIgnoreCase("s") && !reempl.equalsIgnoreCase("n")) {
                                                                System.out.println("Ya existe un directorio con ese nombre, ¿Desea reemplazarlo? (S/N)");
                                                                reempl = s.nextLine();
                                                            }
                                                        }
                                                        dos.writeBytes(reempl + "\r\n");
                                                        dos.flush();
                                                        if (reempl.equalsIgnoreCase("s")){
                                                            dos.writeBytes("OK\r\n");
                                                            dos.flush();
                                                            dos.writeBytes(input + "\r\n");
                                                            dos.flush();
                                                            dos.writeBytes(rutaLocal + "\r\n");
                                                            dos.flush();
                                                            descargarCarpeta(nuevoDir.getAbsolutePath(), dis);
                                                            System.out.println("Carpeta descargada correctamente");
                                                        }
                                                    } else {
                                                        System.err.println("La ruta local introducida no es válida");
                                                        dos.writeBytes("ERROR\r\n");
                                                        dos.flush();
                                                    }
                                                } else {
                                                    System.err.println("La ruta de la carpeta en la nube no es válida o se corresponde con un fichero");
                                                }
                                                break;
                                            case 5:
                                                System.out.println("Introduce el nombre del fichero a borrar (desde el directorio actual)");
                                                input = s.nextLine();
                                                borrarFichero(input, dos, dis);
                                                break;
                                            case 6:
                                                System.out.println("Introduce la ruta de la carpeta a eliminar (desde el directorio actual)");
                                                input = s.nextLine();
                                                borrarCarpeta(input, dos);
                                                res = dis.readLine();
                                                while (!res.equals("")){
                                                    if (!res.equals("OK")){
                                                        if (res.equals("ERROR")){
                                                            System.err.println("La carpeta introducida no existe en la nube");
                                                            break;
                                                        } else {
                                                            System.out.println(res);
                                                        }
                                                    }
                                                    res = dis.readLine();
                                                }
                                                break;
                                            case 7:
                                                System.out.println("Introduce el nombre de la carpeta nueva (se crearán las carpetas necesarias en caso de no existir) ");
                                                input = s.nextLine();
                                                dos.writeBytes(input + "\r\n");
                                                dos.flush();
                                                System.out.println(dis.readLine());

                                                break;
                                            case 8:
                                                res = dis.readLine();
                                                while (!res.equals("")) {
                                                    System.out.println(res);
                                                    res = dis.readLine();
                                                }
                                                break;
                                            case 9:
                                                System.out.println("Introduce el nombre del directorio: ");
                                                input = s.nextLine();
                                                dos.writeBytes(input + "\r\n");
                                                dos.flush();
                                                System.out.println(dis.readLine());
                                                break;
                                            case 10:

                                                break;
                                            case 11:
                                                System.out.println("Introduce el nombre del usuario con el que deseas compartir el fichero");
                                                input = s.nextLine();
                                                dos.writeBytes(input + "\r\n");
                                                dos.flush();
                                                if (dis.readBoolean()){ //Servidor comprueba si existe
                                                    System.out.println("Introduce la ruta (desde el directorio actual) del fichero que se desea compartir");
                                                    String nomFich = s.nextLine();
                                                    dos.writeBytes(nomFich + "\r\n");
                                                    dos.flush();
                                                    res = dis.readLine();
                                                    if (res.equals("DIRECTORIO")){
                                                        System.err.println("La ruta introducida se corresponde con un directorio");
                                                    } else if (res.equals("NO EXISTE")){
                                                        System.err.println("La ruta introducida no existe");
                                                    } else {
                                                        System.out.println("Se compartirá el fichero en la carpeta raíz del usuario " + input);
                                                        compartirFichero(input, dis, dos);
                                                        System.out.println("Fichero compartido correctamente");
                                                    }
                                                } else {
                                                    System.err.println("El usuario introducido no está registrado en el sistema");
                                                }
                                                break;
                                            case 12:
                                                user.mostrarCorreo();
                                                break;
                                            case 13:
                                                user.vaciarCorreo();
                                                break;
                                            case 14:
                                                System.out.println("Introduce la ruta (desde el directorio raíz) de la carpeta a descargar");
                                                input = s.nextLine();
                                                dos.writeBytes(input + "\r\n");
                                                dos.flush();
                                                if (dis.readBoolean()){
                                                    System.out.println("Introduce la ruta local en la que se desea guardar la carpeta");
                                                    String rutaLocal = s.nextLine();
                                                    File fLocal = new File(rutaLocal);
                                                    if (fLocal.exists() && fLocal.isDirectory()){
                                                        descargarZip(input, rutaLocal, dis, dos);
                                                    } else {
                                                        System.err.println("La ruta local introducida no es valida");
                                                        dos.writeBytes("ERROR\r\n");
                                                        dos.flush();
                                                    }
                                                } else {
                                                    System.err.println("La ruta introducida no existe en la nube o no es válida.");
                                                }
                                                break;
                                        }
                                        user = (Usuario) ois.readObject();
                                    }
                                } catch (NumberFormatException nfe) {
                                    System.out.println("Introduce un numero!");
                                } catch (ClassNotFoundException e) {
                                    throw new RuntimeException(e);
                                }
                            } while (j != 15);
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
            if (f.exists() && f.isFile()) {
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
                System.err.println("El fichero introducido no existe en el disco duro.");
                dos.writeBytes("Salir\r\n");
                dos.flush();
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

    }

    private static void subirCarpeta(String pathDirNube, String pathDirPadreNube, String pathDirLocal,
                                     DataOutputStream dos, DataInputStream dis) {
        try {
            File dirLocal = new File(pathDirLocal);
            if (dirLocal.exists() && dirLocal.isDirectory()){
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
            } else {
                System.err.println("La ruta introducida no es válida.");
                dos.writeBytes("error\r\n");
                dos.flush();
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
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

    private static void borrarFichero(String nombre, DataOutputStream dos, DataInputStream dis){
        try {
            dos.writeBytes(nombre + "\r\n");
            dos.flush();
            String respuesta = dis.readLine();
            switch (respuesta) {
                case "OK" -> System.out.println("Fichero borrado correctamente.");
                case "ERROR" -> System.err.println("El fichero introducido no existe en la nube");
                case "DIRECTORIO" -> System.err.println("La ruta introducida se corresponde con un directorio.");
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private static void borrarCarpeta(String nombre, DataOutputStream dos){
        try {
            dos.writeBytes(nombre + "\r\n");
            dos.flush();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private static void descargarFichero(String nombre, File dirSalida, DataOutputStream dos, DataInputStream dis) {
        try {
            String res = "s";
            if (dirSalida.exists() && dirSalida.isDirectory()){
                dos.writeBytes("CORRECTO\r\n");
                dos.flush();
                long tam = Long.parseLong(dis.readLine());
                File ficheroEnLocal = new File(dirSalida + "\\" + obtenerNombreDesdeRuta(nombre));
                if (ficheroEnLocal.exists()){
                    System.out.println("Ya existe un fichero en el directorio local con ese nombre, ¿Desea reemplazarlo? (S/N)");
                    Scanner s = new Scanner(System.in);
                    res= s.nextLine();
                    while (!res.equalsIgnoreCase("s") && !res.equalsIgnoreCase("n")){
                        System.out.println("Ya existe un fichero en el directorio local con ese nombre, ¿Desea reemplazarlo? (S/N)");
                        res = s.nextLine();
                    }
                }
                dos.writeBytes(res + "\r\n");
                dos.flush();
                if (res.equalsIgnoreCase("s")){
                    try (FileOutputStream fos = new FileOutputStream(dirSalida + "\\" + obtenerNombreDesdeRuta(nombre))) {
                        byte[] buf = new byte[(int) tam];
                        dis.readFully(buf, 0, buf.length);
                        fos.write(buf, 0, buf.length);
                        fos.flush();
                    }
                    System.out.println("Fichero descargado en " + dirSalida + " correctamente.");
                }
            } else {
                dos.writeBytes("ERROR\r\n");
                dos.flush();
                System.err.println("La ruta local en la que descargar el fichero no es válida");
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        } catch (NumberFormatException nfe) {
            System.out.println("La ruta del fichero no existe en la nube.");
        }
    }

    private static boolean existeEnNube(String nombreD, DataOutputStream dos, DataInputStream dis){
        try {
            dos.writeBytes(nombreD + "\r\n");
            dos.flush();
            return dis.readBoolean();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private static void descargarCarpeta(String dirRaiz, DataInputStream dis){
        try {
            String dir = dirRaiz;
            String nom = dis.readLine();
            while (!nom.equals("")) {
                if (esDirectorio(nom)){
                    dir = nom;
                    File dirNube = new File(nom);
                    dirNube.mkdir();
                } else {
                    descargarFicheroDeCarpeta(nom, dir, dis);
                }
                nom = dis.readLine();
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private static void descargarFicheroDeCarpeta(String nombre, String dirSalida, DataInputStream dis){
        try {
            long tam = Long.parseLong(dis.readLine());
            try (FileOutputStream fos = new FileOutputStream(dirSalida + "\\" + obtenerNombreDesdeRuta(nombre))) {
                byte[] buf = new byte[(int) tam];
                dis.readFully(buf, 0, buf.length);
                fos.write(buf, 0, buf.length);
                fos.flush();
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private static void descargarZip(String entrada, String salida, DataInputStream dis, DataOutputStream dos){
        try {
            dos.writeBytes(entrada + "\r\n");
            dos.flush();
            descargarFichero(entrada + ".zip", new File(salida), dos, dis);

        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private static boolean esDirectorio(String nom) {
        String[] trozos = nom.split(Pattern.quote(System.getProperty("file.separator")));
        String nombre = trozos[trozos.length -1];
        String[] archivoYExt = nombre.split("\\.");
        return archivoYExt.length <= 1;
    }

    private static void compartirFichero(String usuario, DataInputStream dis, DataOutputStream dos){
        try {
            Scanner s = new Scanner(System.in);
            String res = dis.readLine();
            if (res.equals("CORRECTO")){

            } else {
                System.out.println("Ya existe un fichero con ese nombre en el directorio raíz de " + usuario);
                System.out.println("Es necesario cambiarle el nombre. ¿Desea continuar? (S/N)");
                res = s.nextLine();
                while (!res.equalsIgnoreCase("s") && !res.equalsIgnoreCase("n")){
                    System.out.println("Respuesta no válida. ¿Desea continuar? (S/N)");
                    res = s.nextLine();
                }
                if (res.equalsIgnoreCase("s")){
                    System.out.println("Introduce el nuevo nombre del fichero");
                    String nomF = s.nextLine();
                    dos.writeBytes(nomF + "\r\n");
                    dos.flush();
                    compartirFichero(usuario, dis, dos);
                } else {
                    dos.writeBytes("\r\n");
                    dos.flush();
                }
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

    }

}
