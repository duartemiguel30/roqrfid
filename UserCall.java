import com.sun.jna.Pointer;
import com.sun.jna.ptr.PointerByReference;
import com.sun.jna.Memory;
import com.sun.jna.ptr.IntByReference;
import javax.swing.JOptionPane;

public class UserCall {

    // atual 
    public static final String ipReader = "10.0.7.10";
    public static final int portReader = 7086;

	public static final String Comcon = "COM1";

    public static Pointer handleReader = null; 

    public static boolean TcpInit() {
    PointerByReference handleRef = new PointerByReference();

    int result = RFIDLibrary.INSTANCE.SAAT_TCPInit(handleRef, ipReader, portReader);
    System.out.println("SAAT_TCPInit result: " + result);

    if (result == 1) {
        handleReader = handleRef.getValue();
        System.out.println("TCP Ligação estabelecida. Handle: " + handleReader);
    } else {
        handleReader = null;
        System.out.println("TCP Ligação falhou.");
    }

    return result == 1;
}

    public static boolean ComInit() {
    PointerByReference handleRef = new PointerByReference();

    int result = RFIDLibrary.INSTANCE.SAAT_COMInit(handleRef, 0x00, Comcon, 19200);
    if (result == 1) {
        handleReader = handleRef.getValue();
    }

    return result == 1;
}

    public static boolean OpenReader() {
    if (handleReader == null) {
        System.err.println("Handle é nulo antes do SAAT_Open.");
        return false;
    }

    int result = RFIDLibrary.INSTANCE.SAAT_Open(handleReader);
    System.out.println("Resultado de SAAT_Open: " + result);

    if (result != 1) {
        handleReader = null;  
        return false;
    }
    return true;
}

    public static boolean PowerOff() {
    if (handleReader == null) {
        return false;
    }

    int result = RFIDLibrary.INSTANCE.SAAT_PowerOff(handleReader);
    return result != 0;
}

    public static boolean CloseConnection() {
    if (handleReader == null) {
        return false;
    }

    int result = RFIDLibrary.INSTANCE.SAAT_Close(handleReader);
    handleReader = null;
    return result == 1;
}


    public static boolean ReadEpc() {
        int result = RFIDLibrary.INSTANCE.SAAT_6CReadEPCCode(handleReader, 0, 1, 3);
        return result == 1;
    }

    public static void SetupAntenna() {
    RFIDLibrary.INSTANCE.SAAT_SetAntennaPortEnable(handleReader, (byte) 1, (byte) 1); // ativa antena 1
    RFIDLibrary.INSTANCE.SAAT_SetAntennaPortEnable(handleReader, (byte) 1, (byte) 2);
    RFIDLibrary.INSTANCE.SAAT_SetAntennaPortEnable(handleReader, (byte) 1, (byte) 3); 
    RFIDLibrary.INSTANCE.SAAT_SetAntennaPortEnable(handleReader, (byte) 1, (byte) 4); 


    RFIDLibrary.INSTANCE.SAAT_SetAntennaPower(handleReader, (byte) 1, (byte) 30);     // potência máxima 
}

public static String RecEpcMsgAsString() {
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
        return tag.toString();
    }

    return null; 
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
            System.out.println("Tag EPC: " + tag.toString());
        }

        return result == 1;
    }
}
