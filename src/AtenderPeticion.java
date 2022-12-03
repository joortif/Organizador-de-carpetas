import java.io.*;
import java.net.Socket;
import java.util.concurrent.ConcurrentHashMap;

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
             BufferedWriter salida = new BufferedWriter(new OutputStreamWriter(this.os))){
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
            salida.write("Bienvenido " + nom);
            salida.flush();
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


    }

