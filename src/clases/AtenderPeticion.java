package clases;

import java.io.*;
import java.net.Socket;
import java.text.DecimalFormat;
import java.util.Date;
import java.util.concurrent.*;
import java.util.regex.Pattern;

public class AtenderPeticion implements Runnable {

    private Socket socket;
    private ConcurrentHashMap<String, String> usuarios;
    private static File FichUsuarios;
    private static final DecimalFormat df = new DecimalFormat("0.00");
    private static DataInputStream dis;
    private static DataOutputStream dos;

    public AtenderPeticion(Socket socket) throws IOException {
        FichUsuarios = new File("src\\usuarios\\Usuarios.txt");
        this.socket = socket;
        this.usuarios = this.cargarUsuarios(FichUsuarios);
        dis = new DataInputStream(this.socket.getInputStream());
        dos = new DataOutputStream(this.socket.getOutputStream());
    }


    public void run() {
        ObjectOutputStream oos = null;
        ObjectInputStream ois = null;
        try (PrintStream ps = new PrintStream(this.socket.getOutputStream(), true)){
            oos = new ObjectOutputStream((this.socket.getOutputStream()));
            ois = new ObjectInputStream(this.socket.getInputStream());


            int i = Integer.parseInt(dis.readLine());
            String nom = null;
            String psw;
            String mensaje;
            switch (i) {
                case 1 -> {
                    nom = dis.readLine();
                    psw = dis.readLine();
                    mensaje = compruebaDatos(nom, psw);
                    while (!mensaje.equals("Correcto\r\n")) {
                        ps.print(mensaje);
                        nom = dis.readLine();
                        psw = dis.readLine();
                        mensaje = compruebaDatos(nom, psw);
                    }
                    ps.print(mensaje);
                }
                case 2 -> {
                    nom = dis.readLine();
                    mensaje = altaUsuario(nom);
                    while (!mensaje.equals("Correcto\r\n")) {
                        ps.print(mensaje);
                        nom = dis.readLine();
                        mensaje = altaUsuario(nom);
                    }
                    ps.print(mensaje);
                    psw = dis.readLine();
                    PrintWriter outUsu = new PrintWriter(new BufferedWriter(new FileWriter(FichUsuarios, true)), true);
                    outUsu.println(nom + "-" + psw);
                    outUsu.close();
                    new File("src\\nube\\" + nom).mkdirs();
                }
            }
            ps.print("Bienvenido " + nom + "\r\n");
            int j = 0;
            while (j != 12) {
                Usuario usuario;
                String nombre;
                j = Integer.parseInt(dis.readLine());
                switch (j) {
                    case 1:
                        usuario = (Usuario) ois.readObject();
                        nombre = dis.readLine();
                        if (!nombre.equals("Salir")){
                            recibirFichero(nombre, "", usuario);
                        }
                        break;
                    case 2:
                        usuario = (Usuario) ois.readObject();
                        File dirBase = new File(dis.readLine());
                        if (dirBase.mkdir()){
                            dos.writeBytes("OK\r\n");
                        } else {
                            dos.writeBytes("ERROR\r\n");
                        }
                        dos.flush();
                        recibirCarpeta(usuario, dirBase.getName());
                        break;
                    case 3:
                        enviarFichero(dis.readLine());
                        break;
                    case 4:
                        boolean existe = comprobarDirectorio();
                        dos.writeBoolean(existe);
                        if (dis.readLine().equalsIgnoreCase("ok")){
                            String carpetaEnNube = dis.readLine();
                            String carpetaDestinoLocal = dis.readLine();
                            enviarCarpeta(carpetaDestinoLocal, nombreDesdeRuta(carpetaEnNube) , "src\\nube\\" + carpetaEnNube);
                        }
                        dos.writeBytes("\r\n");
                        dos.flush();
                        break;
                    case 5:
                        usuario = (Usuario) ois.readObject();
                        File fich = new File(usuario.getDirectorioCompleto() + "\\" + dis.readLine());
                        if (fich.isDirectory()){
                            dos.writeBytes("DIRECTORIO\r\n");
                            dos.flush();
                        } else {
                            borrarFichero(fich);
                        }
                        break;
                    case 6:
                        usuario = (Usuario) ois.readObject();
                        File dirABorrar = new File(usuario.getDirectorioCompleto() + "\\" + dis.readLine());
                        if (dirABorrar.exists() && dirABorrar.isDirectory()){
                            borrarDirectorioNoVacio(dirABorrar.getAbsolutePath(), "");
                        } else {
                            dos.writeBytes("ERROR\r\n");
                        }
                        dos.flush();
                        dos.writeBytes("\r\n");
                        dos.flush();
                        break;
                    case 7:
                        String dir = dis.readLine();
                        usuario = (Usuario) ois.readObject();
                        crearDirectorio(dir, usuario);
                        break;
                    case 8:
                        usuario = (Usuario) ois.readObject();
                        mostrarDirec(usuario);
                        break;
                    case 9:
                        String dirC = dis.readLine();
                        usuario = (Usuario) ois.readObject();
                        cambiarDirec(dirC, usuario, oos);
                        break;
                    case 10:
                        usuario = (Usuario) ois.readObject();
                        usuario.subirDirectorioPadre();
                        oos.writeObject(usuario);
                        oos.flush();
                        break;
                    case 11:
                        break;
                    case 12:
                        break;
                    default:
                        break;
                }
            }
        } catch (IOException | ClassNotFoundException e) {
            throw new RuntimeException(e);
        } finally {
            try {
                if (oos != null) {
                    oos.close();
                    ois.close();
                }
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }


    }



    private ConcurrentHashMap<String, String> cargarUsuarios(File fich) {
        ConcurrentHashMap<String, String> listaU = new ConcurrentHashMap<>();
        try (BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(fich)))) {
            String linea = br.readLine();
            while (linea != null) {
                String[] usrYpsw = linea.split("-");
                listaU.put(usrYpsw[0], usrYpsw[1]);
                linea = br.readLine();
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return listaU;

    }

    private String compruebaDatos(String usr, String psw) {
        String mensaje;
        if (this.usuarios.containsKey(usr)) {
            if (this.usuarios.get(usr).equals(psw)) {
                mensaje = "Correcto\r\n";
            } else {
                mensaje = "Contraseña incorrecta. \r\n";
            }
        } else {
            mensaje = "Nombre de usuario incorrecto. \r\n";
        }
        return mensaje;
    }

    private String altaUsuario(String usr) {
        String mensaje;
        if (this.usuarios.containsKey(usr)) {
            mensaje = "El usuario ya existe en el sistema. \r\n";
        } else {
            mensaje = "Correcto\r\n";
        }
        return mensaje;
    }

    private void recibirFichero(String ruta, String dir, Usuario u) {
        try {
            String nomF;
            if (!dir.equals("")){
                nomF = convertirARutaNube(ruta, dir);
            } else {
                nomF = nombreDesdeRuta(ruta);
            }
            File fichNuevo = new File(u.getDirectorioCompleto() + "\\" + nomF);
            long tam = Long.parseLong(dis.readLine());
            try (FileOutputStream fos = new FileOutputStream(fichNuevo)) {
                byte[] buf = new byte[(int) (tam)];
                dis.readFully(buf, 0, buf.length);
                fos.write(buf, 0, buf.length);
                fos.flush();
            } catch (IOException ex) {
                throw new RuntimeException(ex);
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

    }

    private void recibirCarpeta(Usuario u, String dirRaiz) {
        try {
            String dir = dirRaiz;
            String nom = dis.readLine();
            while (!nom.equals("")) {
                if (esDirectorio(nom)){
                    dir = dir + "\\" + nombreDesdeRuta(nom);
                    File dirNube = new File(nom);
                    if (dirNube.mkdir()){
                        dos.writeBytes("OK\r\n");
                        dos.flush();
                    } else {
                        dos.writeBytes("ERROR\r\n");
                        dos.flush();
                    }
                } else {
                    recibirFichero(nom, dir, u);
                }
                nom = dis.readLine();
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }


    }

    private static String nombreDesdeRuta(String ruta) {
        String[] trozos = ruta.split(Pattern.quote(System.getProperty("file.separator")));
        return trozos[trozos.length - 1];
    }

    private boolean esDirectorio(String ruta) {
        String[] trozos = ruta.split(Pattern.quote(System.getProperty("file.separator")));
        String nombre = trozos[trozos.length -1];
        String[] archivoYExt = nombre.split("\\.");
        return archivoYExt.length <= 1;
    }

    private static String convertirARutaNube(String ruta, String dir){
        int i = ruta.indexOf(dir);
        return ruta.substring(i);
    }

    private static void crearDirectorio(String nombre, Usuario u) {
        File nuevoDir = new File(u.getDirectorioCompleto() + "\\" + nombre);
        try {
            if (!nuevoDir.exists()) {
                nuevoDir.mkdirs();
                dos.writeBytes("Directorio creado correctamente\r\n");
                dos.flush();
            } else {
                if (nuevoDir.isDirectory()) {
                    dos.writeBytes("Ya existe un directorio con ese nombre\r\n");
                    dos.flush();
                } else {
                    dos.writeBytes("Directorio creado correctamente\r\n");
                    dos.flush();
                }

            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }


    }

    private static void cambiarDirec(String dirC, Usuario usuario, ObjectOutputStream oos) {
        try {
            File direc = new File(usuario.getDirectorioCompleto() + "\\" + dirC);
            if (direc.exists() && direc.isDirectory()) {
                usuario.setDirectorio(dirC);
                dos.writeBytes("Se ha cambiado correctamente de directorio\r\n");
                dos.flush();
            } else {
                dos.writeBytes("El directorio introducido no existe\r\n");
                dos.flush();
            }
            oos.writeObject(usuario);
            oos.flush();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

    }

    private static void mostrarDirec(Usuario usuario) {
        try {
            File directorio = new File(usuario.getDirectorioCompleto());
            File[] listaF = directorio.listFiles();
            long tamkB;
            dos.writeBytes("Directorio: " + usuario.getDirectorio() + "\r\n");
            dos.flush();
            dos.writeBytes("\t Nombre \t  Tamaño(kB) \t Fecha de última modificación \r\n");
            dos.flush();
            dos.writeBytes("********************************************************************\r\n");
            dos.flush();
            if (listaF == null || listaF.length == 0) {
                dos.writeBytes("\tDirectorio vacío \r\n");
                dos.flush();
            } else {
                for (File f : listaF) {
                    if (f.isDirectory()) {
                        dos.writeBytes("<DIR> " + f.getName() + "\t" + "-" + "\t" + new Date(f.lastModified()) + "\r\n");
                        dos.flush();
                    } else {
                        tamkB = (long) (f.length() * 0.097656);
                        dos.writeBytes("\t" + f.getName() + "\t " + df.format(tamkB) + "\t" + new Date(f.lastModified()) + "\r\n");
                        dos.flush();
                    }

                }
            }
            dos.writeBytes("\r\n");
            dos.flush();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private static void borrarFichero(File fich){
        try {
            if (fich.exists()){
                if (fich.delete()){
                    dos.writeBytes("OK\r\n");
                } else {
                    dos.writeBytes("ERROR\r\n");
                }
            } else {
                dos.writeBytes("ERROR\r\n");

            }
            dos.flush();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

    }

    private static void borrarDirectorioNoVacio(String dirPadre, String dirActual){
        try {
            String dir = dirPadre;
            if (!dirActual.equals("")) {
                dir += "\\" + dirActual;
            }
            File fDir = new File(dir);
            File[] subFicheros = fDir.listFiles();
            if (subFicheros != null && subFicheros.length > 0) {
                for (File item : subFicheros) {
                    String nombreFichActual = item.getName();
                    if (item.isDirectory()) {
                        borrarDirectorioNoVacio(dir, nombreFichActual);
                    } else {
                        dos.writeBytes("Borrando fichero " + item.getName().replace("src\\nube\\", "") + "\r\n");
                        dos.flush();
                        borrarFichero(item);
                    }
                }
            }
            if (dirActual.equals("")){
                dos.writeBytes("Borrando directorio raíz " + nombreDesdeRuta(dirPadre) + "\r\n");
            } else {
                dos.writeBytes("Borrando directorio " + dirActual + "\r\n");
            }
            dos.flush();
            borrarFichero(new File(dir));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private static void enviarFichero(String rutaEnNube){
        try {
            File fichEnNube = new File("src\\nube\\" + rutaEnNube);
            if (fichEnNube.exists() && fichEnNube.isFile()){
                String msg = dis.readLine();
                if (msg.equalsIgnoreCase("correcto")){
                    dos.writeBytes(fichEnNube.length() + "\r\n");
                    dos.flush();

                    try (FileInputStream fs = new FileInputStream(fichEnNube);
                         DataInputStream fis = new DataInputStream(fs)){

                        byte[] buf = new byte[(int) fichEnNube.length()];
                        fis.readFully(buf, 0, buf.length);
                        dos.write(buf,0,buf.length);
                        dos.flush();

                    }
                } else {
                    dos.writeBytes("ERROR\r\n");
                    dos.flush();
                }
            } else {
                dos.writeBytes("ERROR\r\n");
                dos.flush();
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private static void enviarFicheroDeCarpeta(String ruta, String dirNube){
        try {
            File fichero = new File(ruta);
            String rutaNube = convertirARutaNube(ruta, dirNube);
            dos.writeBytes(rutaNube + "\r\n");
            dos.flush();
            dos.writeBytes(fichero.length() + "\r\n");
            dos.flush();

            try (FileInputStream fs = new FileInputStream(fichero);
                 DataInputStream fis = new DataInputStream(fs)){

                byte[] buf = new byte[(int) fichero.length()];
                fis.readFully(buf, 0, buf.length);
                dos.write(buf,0,buf.length);
                dos.flush();

            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private static void enviarCarpeta(String dirDestinoLocal, String directorioPadreDescarga , String directorioDescarga) {
        try {
            File dirLocal = new File(directorioDescarga);
            File[] subFichs = dirLocal.listFiles();
            if (subFichs != null && subFichs.length > 0) {
                for (File item : subFichs) {
                    String pathFichNube;
                    if (directorioPadreDescarga.equals("")) {
                        pathFichNube = dirDestinoLocal + "\\" + item.getName();
                    } else {
                        pathFichNube = dirDestinoLocal + "\\" + directorioPadreDescarga + "\\" + item.getName();
                    }

                    if (item.isFile()) {
                        enviarFicheroDeCarpeta(item.getAbsolutePath(), nombreDesdeRuta(directorioDescarga));
                    } else {
                        dos.writeBytes(pathFichNube +"\r\n");
                        dos.flush();
                        String padre = directorioPadreDescarga + "\\" + item.getName();
                        if (directorioPadreDescarga.equals("")) {
                            padre = item.getName();
                        }
                        String directorio = item.getAbsolutePath();
                        enviarCarpeta(dirDestinoLocal, padre, directorio);

                    }
                }
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }


    }

    private static boolean comprobarDirectorio(){
        try {
            File directorio = new File("src\\nube\\" + dis.readLine());
            return (directorio.exists() && directorio.isDirectory());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}






