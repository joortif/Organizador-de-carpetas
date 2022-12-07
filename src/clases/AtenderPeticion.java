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
                    new File("src/nube/" + nom).mkdirs();
                }
            }
            ps.print("Bienvenido " + nom + "\r\n");
            int j = 0;
            while (j != 10) {
                Usuario usuario = null;
                j = Integer.parseInt(dis.readLine());
                switch (j) {
                    case 1:
                        usuario = (Usuario) ois.readObject();
                        recibirFichero(usuario);
                        break;
                    case 2:
                        usuario = (Usuario) ois.readObject();
                        recibirCarpeta(usuario);
                        break;
                    case 3:
                        break;
                    case 4:
                        break;
                    case 5:
                        String dir = dis.readLine();
                        usuario = (Usuario) ois.readObject();
                        crearDirectorio(dir, usuario);
                        break;
                    case 6:
                        usuario = (Usuario) ois.readObject();
                        mostrarDirec(usuario);
                        break;
                    case 7:
                        String dirC = dis.readLine();
                        usuario = (Usuario) ois.readObject();
                        cambiarDirec(dirC, usuario, oos);

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

    private void recibirFichero(Usuario u) {
        try {
            String ruta = dis.readLine();
            String nomF = nombreDesdeRuta(ruta);
            File fichNuevo;
            fichNuevo = new File(u.getDirectorioCompleto() + "/" + nomF);
            long tam = Long.parseLong(dis.readLine());
            try (FileOutputStream fos = new FileOutputStream(fichNuevo)) {
                byte[] buf = new byte[(int) (tam + 1)];
                dis.read(buf, 0, buf.length);
                fos.write(buf, 0, buf.length);
                fos.flush();
            } catch (IOException ex) {
                throw new RuntimeException(ex);
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

    }

    private void recibirCarpeta(Usuario u) {
        try {
            String dir = dis.readLine();
            while (!dir.equals("")) {
                if (dir.startsWith("<D>")){
                    File dirNube = new File(dir);
                    if (dirNube.mkdir()){
                        dos.writeBytes("OK\r\n");
                        dos.flush();
                    } else {
                        dos.writeBytes("ERROR\r\n");
                        dos.flush();
                    }
                } else {
                    recibirFichero(u);
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

    private static void crearDirectorio(String nombre, Usuario u) {
        File nuevoDir = new File(u.getDirectorioCompleto() + "/" + nombre);
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
            File direc = new File(usuario.getDirectorioCompleto() + "/" + dirC);
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
            if (listaF == null) {
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

}

