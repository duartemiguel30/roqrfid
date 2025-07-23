import com.sun.jna.Native;
import com.sun.jna.Memory;
import com.sun.jna.Pointer;
import com.sun.jna.ptr.IntByReference;
import com.sun.jna.ptr.PointerByReference;
import com.sun.jna.win32.StdCallLibrary;

public interface RFIDLibrary extends StdCallLibrary {
    RFIDLibrary INSTANCE = Native.load("RFIDAPI", RFIDLibrary.class);

    int SAAT_TCPInit(PointerByReference handle, String ip, int port);
    int SAAT_COMInit(PointerByReference handle, int reserved, String comPort, int baudRate);
    int SAAT_Open(Pointer handle); 
    int SAAT_HeartSend(Pointer handle);
    int SAAT_Close(Pointer handle);
    int SAAT_SetAntennaPortEnable(Pointer handle, byte antNo, byte enable);
    int SAAT_SetAntennaPower(Pointer handle, byte antNo, byte power);
    int SAAT_PowerOff(Pointer handle);
    int SAAT_6CReadEPCCode(Pointer handle, int ant, int type, int tagCount);
    int SAAT_6CRevEPCMsg(Pointer handle, IntByReference ant, Memory data, IntByReference dataLen);
    int SAAT_6BTagSelect(Pointer handle, byte nType, byte nStartAddr, byte nDataBite, byte[] data);
    int SAAT_6CTagSelect(Pointer handle, byte nBank, byte nStartAddr, byte maskBit, byte[] data, byte dataLen, byte sessionZone, byte activeFlag, byte cutFlag);
    int SAAT_6CWriteEPCCode(Pointer handle, byte antenna, byte type, byte[] pwd, byte[] epcData, byte length);
    boolean SAAT_GetErrorMessage(Pointer pHandle, byte[] szMsg, int nLen);
    int SAAT_6CTagLock(Pointer handle, byte memBank, byte lockType, byte[] pwd, byte pwdLen, byte mask, IntByReference lockStatus);
    int SAAT_6CReadUserData(Pointer handle, byte nAntenna, int startAddr, int lenWords, int timeout, Pointer data, byte[] dataLen);
    int SAAT_6CReadTIDCode(Pointer handle, int antenna, int type, int tagCount);
    int SAAT_6CRevTIDMsg(Pointer handle, IntByReference ant, Pointer data, IntByReference dataLen);

}
