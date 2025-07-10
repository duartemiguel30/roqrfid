import com.sun.jna.Native;
import com.sun.jna.Memory;
import com.sun.jna.ptr.IntByReference;
import com.sun.jna.win32.StdCallLibrary;

public interface RFIDLibrary extends StdCallLibrary {
    RFIDLibrary INSTANCE = Native.load("RFIDAPI", RFIDLibrary.class);

    int SAAT_TCPInit(IntByReference handle, String ip, int port);
    int SAAT_COMInit(IntByReference handle, int reserved, String comPort, int baudRate);
    int SAAT_Open(int handle);
    int SAAT_Close(int handle);
    int SAAT_PowerOff(int handle);
    int SAAT_6CReadEPCCode(int handle, int mem, int wordPtr, int wordCnt);
    int SAAT_6CRevEPCMsg(int handle, IntByReference ant, Memory data, IntByReference dataLen);
}
