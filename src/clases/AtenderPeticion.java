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

    public AtenderPeticion(Socket socket) throws IOException {
        FichUsuarios = new File("src\\usuarios\\Usuarios.txt");
        this.socket = socket;
        this.usuarios = this.cargarUsuarios(FichUsuarios);
    }


    public void run() {
        ObjectOutputStream oos = null;
        ObjectInputStream ois = null;
        try (BufferedReader entrada = new BufferedReader(new InputStreamReader(this.socket.getInputStream()));
             BufferedWriter salida = new BufferedWriter(new OutputStreamWriter(this.socket.getOutputStream()));
             DataInputStream dis = new DataInputStream(this.socket.getInputStream());) {
            oos = new ObjectOutputStream((this.socket.getOutputStream()));
            ois = new ObjectInputStream(this.socket.getInputStream());


            int i = Integer.parseInt(entrada.readLine());
            String nom = null;
            String psw;
            String mensaje;
            switch (i) {
                case 1 -> {
                    nom = entrada.readLine();
                    psw = entrada.readLine();
                    mensaje = compruebaDatos(nom, psw);
                    while (!mensaje.equals("Correcto\r\n")) {
                        salida.write(mensaje);
                        salida.flush();
                        nom = entrada.readLine();
                        psw = entrada.readLine();
                        mensaje = compruebaDatos(nom, psw);
                    }
                    salida.write(mensaje);
                    salida.flush();
                }
                case 2 -> {
                    nom = entrada.readLine();
                    mensaje = altaUsuario(nom);
                    while (!mensaje.equals("Correcto\r\n")) {
                        salida.write(mensaje);
                        salida.flush();
                        nom = entrada.readLine();
                        mensaje = altaUsuario(nom);
                    }
                    salida.write(mensaje);
                    salida.flush();
                    psw = entrada.readLine();
                    PrintWriter outUsu = new PrintWriter(new BufferedWriter(new FileWriter(FichUsuarios, true)), true);
                    outUsu.println(nom + "-" + psw);
                    outUsu.close();
                    new File("src/nube/" + nom).mkdirs();
                }
            }
            salida.write("Bienvenido " + nom + "\r\n");
            salida.flush();
            int j = 0;
            while (j != 10) {
                Usuario usuario = null;
                j = Integer.parseInt(entrada.readLine());
                switch (j) {
                    case 1:
                        usuario = (Usuario) ois.readObject();
                        recibirFichero(entrada, usuario, dis);
                        break;
                    case 2:
                        usuario = (Usuario) ois.readObject();
                        recibirCarpeta(usuario, entrada, salida, dis);
                        break;
                    case 3:
                        break;
                    case 4:
                        break;
                    case 5:
                        String dir = entrada.readLine();
                        usuario = (Usuario) ois.readObject();
                        crearDirectorio(dir, usuario, salida);
                        break;
                    case 6:
                        usuario = (Usuario) ois.readObject();
                        mostrarDirec(usuario, salida);
                        break;
                    case 7:
                        String dirC = entrada.readLine();
                        usuario = (Usuario) ois.readObject();
                        cambiarDirec(dirC, usuario, salida, oos);

                        break;
                    case 8:
                        usuario = (Usuario) ois.readObject();
                        usuario.subirDirectorioPadre();
                        oos.writeObject(usuario);
                        oos.flush();
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

    private void recibirFichero(BufferedReader br,Usuario u, DataInputStream dis) {
        try {
            String ruta = br.readLine();
            String nomF = nombreDesdeRuta(ruta);
            File fichNuevo;
            fichNuevo = new File(u.getDirectorioCompleto() + "/" + nomF);
            try (FileOutputStream fos = new FileOutputStream(fichNuevo)) {
                byte[] buf = new byte[1024 * 256];
                int leidos = dis.read(buf);
                while (leidos != -1) {
                    fos.write(buf, 0, leidos);
                    fos.flush();
                    leidos = dis.read(buf);
                }
            } catch (IOException ex) {
                throw new RuntimeException(ex);
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

    }

    private void recibirCarpeta(Usuario u, BufferedReader br, BufferedWriter bw, DataInputStream dis) {
        try {
            String dir = br.readLine();
            while (!dir.equals("")) {
                if (dir.startsWith("<D>")){
                    File dirNube = new File(dir);
                    if (dirNube.mkdir()){
                        bw.write("OK\r\n");
                        bw.flush();
                    } else {
                        bw.write("ERROR\r\n");
                        bw.flush();
                    }
                } else {
                    recibirFichero(br, u, dis);
                }
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }


    }

    private String nombreDesdeRuta(String ruta) {
        String[] trozos = ruta.split(Pattern.quote(System.getProperty("file.separator")));
        return trozos[trozos.length - 1];
    }

    private static void crearDirectorio(String nombre, Usuario u, BufferedWriter bw) {
        File nuevoDir = new File(u.getDirectorioCompleto() + "/" + nombre);
        try {
            if (!nuevoDir.exists()) {
                nuevoDir.mkdirs();
                bw.write("Directorio creado correctamente\r\n");
                bw.flush();
            } else {
                if (nuevoDir.isDirectory()) {
                    bw.write("Ya existe un directorio con ese nombre\r\n");
                    bw.flush();
                } else {
                    bw.write("Directorio creado correctamente\r\n");
                    bw.flush();
                }

            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }


    }

    private static void cambiarDirec(String dirC, Usuario usuario, BufferedWriter bw, ObjectOutputStream oos) {
        try {
            File direc = new File(usuario.getDirectorioCompleto() + "/" + dirC);
            if (direc.exists() && direc.isDirectory()) {
                usuario.setDirectorio(dirC);
                bw.write("Se ha cambiado correctamente de directorio\r\n");
                bw.flush();
            } else {
                bw.write("El directorio introducido no existe\r\n");
                bw.flush();
            }
            oos.writeObject(usuario);
            oos.flush();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

    }

    private static void mostrarDirec(Usuario usuario, BufferedWriter bw) {
        try {
            File directorio = new File(usuario.getDirectorioCompleto());
            File[] listaF = directorio.listFiles();
            long tamkB;
            bw.write("Directorio: " + usuario.getDirectorio() + "\r\n");
            bw.flush();
            bw.write("\t Nombre \t  Tamaño(kB) \t Fecha de última modificación \r\n");
            bw.flush();
            if (listaF == null) {
                bw.write("\tDirectorio vacío \r\n");
                bw.flush();
            } else {
                for (File f : listaF) {
                    if (f.isDirectory()) {
                        bw.write("<DIR> " + f.getName() + "\t" + "-" + "\t" + new Date(f.lastModified()) + "\r\n");
                        bw.flush();
                    } else {
                        tamkB = (long) (f.length() * 0.097656);
                        bw.write("\t" + f.getName() + "\t " + df.format(tamkB) + "\t" + new Date(f.lastModified()) + "\r\n");
                        bw.flush();
                    }

                }
            }
            bw.write("\r\n");
            bw.flush();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

}

