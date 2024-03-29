package clases;

import java.io.Serializable;
import java.util.regex.Pattern;

public class Usuario implements Serializable {

    private static final long serialVersionUID = 111L;
    private String nombre;
    private String directorio;

    public Usuario(String n){
        this.nombre = n;
        this.directorio = "src\\nube\\" + n;
    }

    public String getNombre(){
        return this.nombre;
    }

    public String getDirectorioCompleto(){
        return this.directorio;
    }

    public String getDirectorio(){
        String dir = this.directorio;
        return dir.replace("src\\nube\\", "");
    }

    public void setDirectorio(String d){
        this.directorio = this.directorio + "\\" + d;
    }

    public void subirDirectorioPadre(){
        String[] directorios = this.directorio.split(Pattern.quote(System.getProperty("file.separator")));
        StringBuilder dir = new StringBuilder(directorios[0] + "\\" + directorios[1]);
        for (int i=2; i< directorios.length-1; i++){
            dir.append("\\").append(directorios[i]);
        }
        if (!dir.toString().equals("src\\nube")){
            this.directorio = dir.toString();
        }
    }

    public boolean equals(Object usu) {
        if (usu != null){
            if (usu instanceof Usuario){
                return ((Usuario) usu).nombre.equals(this.nombre);
            }
        }
        return false;


    }
}
