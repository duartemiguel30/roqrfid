import com.sun.jna.Memory;
import com.sun.jna.ptr.IntByReference;
import javax.swing.JOptionPane;

public class UserCall {

    public static int handleReader = 0;
    public static final String ipReader = "";
    public static final int portReader = ;
    /*
	public static final String Comcon = "COM1";
	*/
~
    public static boolean TcpInit() {
    IntByReference handleRef = new IntByReference();

    int result = RFIDLibrary.INSTANCE.SAAT_TCPInit(handleRef, ipReader, portReader);
    System.out.println("SAAT_TCPInit result: " + result);

    if (result == 0) {
        handleReader = 0;
        System.out.println("TCP Ligação falhou.");
    } else {
        handleReader = handleRef.getValue();
        System.out.println("TCP Ligação estabelecida. Handle: " + handleReader);
    }

    return result == 1;
}


    public static boolean ComInit() {
        IntByReference handleRef = new IntByReference();

        int result = RFIDLibrary.INSTANCE.SAAT_COMInit(handleRef, 0x00, Comcon, 19200);
        if (result == 0) {
            handleReader = handleRef.getValue();
        }

        return result == 1;
    }

    public static boolean OpenReader() {
        int result = RFIDLibrary.INSTANCE.SAAT_Open(handleReader);
        return result == 1;
    }

    public static boolean PowerOff() {
        int result = RFIDLibrary.INSTANCE.SAAT_PowerOff(handleReader);
        return result != 0;
    }

    public static boolean CloseConnection() {
        int result = RFIDLibrary.INSTANCE.SAAT_Close(handleReader);
        handleReader = 0;
        return result == 1;
    }

    public static boolean ReadEpc() {
        int result = RFIDLibrary.INSTANCE.SAAT_6CReadEPCCode(handleReader, 0, 1, 3);
        return result == 1;
    }

    public static boolean RecEpcMsg() {
        Memory data = new Memory(256);
        IntByReference dLen = new IntByReference(32);
        IntByReference nAnt = new IntByReference(1);

        int result = RFIDLibrary.INSTANCE.SAAT_6CRevEPCMsg(handleReader, nAnt, data, dLen);

        if (result != 0) {
            int len = dLen.getValue();
            byte[] bytes = data.getByteArray(0, len);
            StringBuilder tag = new StringBuilder();
            for (byte b : bytes) {
                tag.append(String.format("%02x", b < 0 ? b + 256 : b));
            }
            JOptionPane.showMessageDialog(null, tag.toString(), "EPC Tag", JOptionPane.INFORMATION_MESSAGE);
        }

        return result == 1;
    }
}
