package clases;

import java.io.DataOutputStream;
import java.io.FileInputStream;
import java.util.concurrent.CyclicBarrier;

public class HiloSubidor implements Runnable{

    private CyclicBarrier barrera;
    private FileInputStream fis;
    private DataOutputStream dos;
    private long byteInicial;
    private long byteFinal;

    public HiloSubidor(CyclicBarrier b, FileInputStream fis, DataOutputStream dos, long byteInic, long byteFin){
        this.barrera = b;
        this.fis = fis;
        this.dos = dos;
        this.byteInicial = byteInic;
        this.byteFinal = byteFin;
    }

    public void run() {

    }
}
