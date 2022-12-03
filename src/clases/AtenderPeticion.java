package clases;

import javax.xml.crypto.Data;
import java.io.*;
import java.net.Socket;
import java.util.concurrent.ConcurrentHashMap;
import java.util.regex.Pattern;

public class AtenderPeticion implements Runnable{

    private Socket socket;
    private ConcurrentHashMap<String, String> usuarios;
    private InputStream is;
    private OutputStream os;
    private static File FichUsuarios;

    public AtenderPeticion(Socket socket) throws IOException {
        FichUsuarios = new File("src\\usuarios\\Usuarios.txt");
        this.socket = socket;
        this.usuarios = this.cargarUsuarios(FichUsuarios);
        this.is = socket.getInputStream();
        this.os = socket.getOutputStream();
    }


    public void run() {
        try (BufferedReader entrada = new BufferedReader(new InputStreamReader(this.is));
             BufferedWriter salida = new BufferedWriter(new OutputStreamWriter(this.os));
             DataInputStream dis = new DataInputStream(this.is);){
            int j;
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
            j= Integer.parseInt(entrada.readLine());
            while (j != 10){
                switch(j){
                    case 1:
                        subirFichero(entrada, nom, null, dis);
                        break;
                    case 2:
                        break;

                }
                j = Integer.parseInt(entrada.readLine());
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }


    }

    private ConcurrentHashMap<String, String> cargarUsuarios(File fich){
        ConcurrentHashMap<String, String> listaU = new ConcurrentHashMap<>();
        try (BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(fich)))){
            String linea = br.readLine();
            while (linea != null){
                String[] usrYpsw = linea.split("-");
                listaU.put(usrYpsw[0], usrYpsw[1]);
                linea = br.readLine();
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return listaU;

    }

    private String compruebaDatos(String usr, String psw){
        String mensaje;
        if (this.usuarios.containsKey(usr)){
            if (this.usuarios.get(usr).equals(psw)){
                mensaje = "Correcto\r\n";
            } else {
                mensaje = "Contraseña incorrecta. \r\n";
            }
        } else {
            mensaje ="Nombre de usuario incorrecto. \r\n";
        }
        return mensaje;
    }

    private String altaUsuario(String usr){
        String mensaje;
        if (this.usuarios.containsKey(usr)){
            mensaje = "El usuario ya existe en el sistema. \r\n";
        } else {
            mensaje = "Correcto\r\n";
        }
        return mensaje;
    }

    private void subirFichero(BufferedReader entrada, String nom, String direc, DataInputStream dis){
        try {
            String ruta = entrada.readLine();
            if (!ruta.equals("Salir")){
                String nomF = nombreDesdeRuta(ruta);
                long tam = Long.parseLong(entrada.readLine());
                File fichNuevo;
                if (direc == null){
                    fichNuevo = new File("src/nube/"+ nom + "/" + nomF);
                } else {
                    fichNuevo = new File("src/nube/" + nom + "/" + direc + "/" + nomF);
                }
                if (tam > 1000000L){

                } else {
                    try (FileOutputStream fos = new FileOutputStream(fichNuevo)){
                        byte[] buf = new byte[1024*256];
                        int leidos = dis.read(buf);
                        while (leidos != -1){
                            fos.write(buf, 0, leidos);
                            fos.flush();
                            leidos = dis.read(buf);
                        }
                    }
                }
        }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private String nombreDesdeRuta(String ruta){
        String[] trozos = ruta.split(Pattern.quote(System.getProperty("file.separator")));
        return trozos[trozos.length-1];
    }

}

