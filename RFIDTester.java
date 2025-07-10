import java.io.File;
import java.io.IOException;

import javax.swing.JFileChooser;
import javax.swing.JOptionPane;

public class RFIDTester {

    public static void main(String[] args) throws InterruptedException, IOException {

        loadLib();

        UserCall.TcpInit();

		if (!UserCall.TcpInit()) {
				System.out.println("Falha ao estabelecer ligação TCP.");
				return;
			} else {
				System.out.println("Ligação TCP estabelecida com sucesso.");
			}

			if (!UserCall.OpenReader()) {
            JOptionPane.showMessageDialog(null, "Open RFID reader failed", "Failed", JOptionPane.ERROR_MESSAGE);
            return;
        }

        try {
            if (UserCall.ReadEpc()) {
                Thread.sleep(100);
                UserCall.RecEpcMsg();
            }

            UserCall.PowerOff();
            UserCall.CloseConnection();
        } catch (Exception e) {
            e.printStackTrace();
            UserCall.PowerOff();
            UserCall.CloseConnection();
        }
    }

    public static void loadLib() throws IOException {
        File f = new File("JNativeCpp.dll");
        if (f.exists()) {
            System.load(f.getCanonicalPath());
        } else {
            JFileChooser jfc = new JFileChooser(new File("."));
            jfc.addChoosableFileFilter(new javax.swing.filechooser.FileFilter() {
                @Override
                public boolean accept(File f) {
                    return f.isDirectory() || f.getName().toLowerCase().endsWith(".dll");
                }

                @Override
                public String getDescription() {
                    return "DLL : Dynamic Link Library";
                }
            });

            if (JFileChooser.APPROVE_OPTION == jfc.showOpenDialog(null)) {
                System.load(jfc.getSelectedFile().getCanonicalPath());
            } else {
                JOptionPane.showMessageDialog(null, "Nenhuma DLL carregada", "Erro", JOptionPane.ERROR_MESSAGE);
            }
        }
    }
}
