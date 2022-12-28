package clases;

public class Mensaje {

    private String autor;
    private String contenido;

    public Mensaje(String a, String c){
        this.autor = a;
        this.contenido = c;
    }

    public String toString(){
        return "AUTOR: " + this.autor + "\r\n" +
               "MENSAJE: " + this.contenido + "\r\n";

    }


}
