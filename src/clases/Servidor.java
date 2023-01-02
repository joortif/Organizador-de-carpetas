package clases;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class Servidor {
    public static void main(String[] args) {
        ExecutorService pool = Executors.newCachedThreadPool();

        try (ServerSocket server = new ServerSocket(6666)){
            System.out.println("SERVIDOR INICIADO");
            while (true) {
                Socket cliente = server.accept();
                AtenderPeticion ap = new AtenderPeticion(cliente);
                pool.execute(ap);
            }

        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            pool.shutdown();
        }


    }
}