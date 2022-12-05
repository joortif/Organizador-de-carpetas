package clases;

import java.io.*;
import java.util.concurrent.BrokenBarrierException;
import java.util.concurrent.CyclicBarrier;

public class HiloDescargador implements Runnable{

    private BufferedReader br;
    private File fich;
    private DataInputStream dis;

    public HiloDescargador(BufferedReader br, File fich, DataInputStream dis) {
        this.br = br;
        this.fich = fich;
        this.dis = dis;
    }


    public void run() {
        try (RandomAccessFile raf = new RandomAccessFile(this.fich, "rw")){
            long byteInic = Long.parseLong(br.readLine());
            long byteFin = Long.parseLong(br.readLine());
            int tam = (int)(byteFin - byteInic);
            byte[] buf = new byte[tam +1];

            raf.seek(byteInic);
            dis.read(buf,0,buf.length);
            raf.write(buf,0,buf.length);

        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
