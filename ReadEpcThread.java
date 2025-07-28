
public class ReadEpcThread extends Thread {

    public ReadEpcThread(String name) {
        super(name);
    }

    @Override
    public synchronized void run() {
        while (true) {
            try {
                UserCall.RecEpcMsg();
                System.out.println(".");
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}
