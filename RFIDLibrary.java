import com.sun.jna.Native;
import com.sun.jna.Memory;
import com.sun.jna.ptr.IntByReference;
import com.sun.jna.win32.StdCallLibrary;
import com.sun.jna.Pointer;
import com.sun.jna.ptr.PointerByReference;
import com.sun.jna.ptr.IntByReference;
import com.sun.jna.Memory;
import com.sun.jna.win32.StdCallLibrary;


public interface RFIDLibrary extends StdCallLibrary {
    RFIDLibrary INSTANCE = Native.load("RFIDAPI", RFIDLibrary.class);

    int SAAT_TCPInit(PointerByReference handle, String ip, int port);
    int SAAT_COMInit(PointerByReference handle, int reserved, String comPort, int baudRate);
    int SAAT_Open(Pointer handle); 
    int SAAT_Close(Pointer handle);
    int SAAT_SetAntennaPortEnable(Pointer handle, byte antNo, byte enable);
    int SAAT_SetAntennaPower(Pointer handle, byte antNo, byte power);
    int SAAT_PowerOff(Pointer handle);
    int SAAT_6CReadEPCCode(Pointer handle, int ant, int type, int tagCount);
    int SAAT_6CRevEPCMsg(Pointer handle, IntByReference ant, Memory data, IntByReference dataLen);}
