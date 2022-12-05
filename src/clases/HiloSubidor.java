package clases;

import java.io.*;
import java.util.concurrent.BrokenBarrierException;
import java.util.concurrent.CyclicBarrier;

public class HiloSubidor implements Runnable{

    private BufferedWriter bw;
    private CyclicBarrier barrera;
    private String ruta;
    private DataOutputStream dos;
    private long byteInicial;
    private long byteFinal;

    public HiloSubidor(BufferedWriter bw, CyclicBarrier b, String ruta, DataOutputStream dos, long byteInic, long byteFin){
        this.bw = bw;
        this.barrera = b;
        this.ruta = ruta;
        this.dos = dos;
        this.byteInicial = byteInic;
        this.byteFinal = byteFin;
    }

    public void run() {
        try (RandomAccessFile raf = new RandomAccessFile(ruta, "r")){
            bw.write(byteInicial + "\r\n");
            bw.flush();
            bw.write(byteFinal + "\r\n");
            bw.flush();
            byte[] bytesParcial = new byte[(int)(byteFinal - byteInicial +1)];

            raf.seek(byteInicial);
            raf.readFully(bytesParcial);

            dos.write(bytesParcial,0,bytesParcial.length);
            dos.flush();
            this.barrera.await();
        } catch (IOException | BrokenBarrierException | InterruptedException e) {
            throw new RuntimeException(e);
        }
    }
}
