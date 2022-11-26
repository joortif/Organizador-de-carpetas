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
             BufferedWriter salida = new BufferedWriter(new OutputStreamWriter(this.os));){
            String usuario = this.menuInicio(salida, entrada);
            salida.write("Bienvenido " + usuario);
            salida.flush();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }


    }

    private ConcurrentHashMap<String, String> cargarUsuarios(File fich){
        ConcurrentHashMap<String, String> listaU = new ConcurrentHashMap<>();
        try (BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(fich)));){
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

    private String registro(BufferedWriter bw, BufferedReader br){
        try {
            bw.write("Introduce el nombre de usuario: \r\n");
            bw.flush();
            String nombre = br.readLine();
            bw.write("Introduce la contraseña: \r\n");
            bw.flush();
            String psw = br.readLine();
            if (this.compruebaDatos(nombre, psw, bw)){
                return nombre;
            } else {
                return null;
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

    }

    private boolean compruebaDatos(String usr, String psw, BufferedWriter bw){
        try {
            if (this.usuarios.containsKey(usr)){
                if (this.usuarios.get(usr).equals(psw)){
                    return true;
                } else {
                    bw.write("Contraseña incorrecta. \r\n");
                    bw.flush();
                }
            } else {
                bw.write("Nombre de usuario incorrecto. \r\n");
                bw.flush();
            }
            return false;
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private String altaUsuario(BufferedWriter bw, BufferedReader br){
        try {
            bw.write("Introduce el nombre del nuevo usuario: \r\n");
            bw.flush();
            String usr = br.readLine();
            while (this.usuarios.containsKey(usr)){
                bw.write("Este usuario ya existe, introduzca un nuevo nombre de usuario: \r\n");
                bw.flush();
                usr = br.readLine();
            }
            bw.write("Introduce la contraseña: \r\n");
            bw.flush();
            String psw = br.readLine();
            this.usuarios.put(usr,psw);
            this.usuarios = cargarUsuarios(FichUsuarios);
            return usr;
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private String menuInicio(BufferedWriter bw, BufferedReader br){
        try {
            bw.write("Bienvenido, ¿Que operacion desea realizar? 1:Iniciar sesión, 2:Registrarse \r\n");
            bw.flush();
            String usr = null;
            int i = Integer.parseInt(br.readLine());
            switch (i) {
                case 1 -> {
                    usr = this.registro(bw, br);
                    if (usr != null) {
                        bw.write("Sesión iniciada correctamente. \r\n");
                    } else {
                        this.menuInicio(bw, br);
                    }
                }
                case 2 -> {
                    usr = this.altaUsuario(bw, br);
                    bw.write("Usuario registrado correctamente. \r\n");
                    bw.flush();
                }
                default -> this.menuInicio(bw, br);
            }
            return usr;
        } catch (IOException e) {
            throw new RuntimeException(e);
        } catch (NumberFormatException e){
            this.menuInicio(bw, br);
        }
        return null;
    }
}
