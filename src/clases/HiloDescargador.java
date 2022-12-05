package clases;

import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.concurrent.BrokenBarrierException;
import java.util.concurrent.CyclicBarrier;

public class HiloDescargador implements Runnable{

    private BufferedReader br;
    private String ruta;
    private DataInputStream dis;

    public HiloDescargador(BufferedReader br, String ruta, DataInputStream dis) {
        this.br = br;
        this.ruta = ruta;
        this.dis = dis;
    }


    public void run() {
        try (RandomAccessFile raf = new RandomAccessFile(ruta, "rw")){
            long byteInic = Long.parseLong(br.readLine());
            long byteFin = Long.parseLong(br.readLine());
            int tam = (int)(byteFin - byteInic);
            byte[] buf = new byte[tam +1];

            raf.seek(byteInic);
            int leidos = dis.read(buf,0,buf.length);
            System.out.println(leidos);
            raf.write(buf,0,buf.length);

        } catch (IOException | BrokenBarrierException | InterruptedException e) {
            throw new RuntimeException(e);
        }
    }
}
