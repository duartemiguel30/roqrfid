import java.util.HashSet;
import java.util.Set;
import java.io.File;
import java.io.IOException;
import javax.swing.JFileChooser;
import javax.swing.JOptionPane;

public class RFIDTester {

    public static void main(String[] args) throws InterruptedException, IOException {
        
        loadLib();

        if (!UserCall.TcpInit()) {
            JOptionPane.showMessageDialog(null, "Falha ao estabelecer ligação TCP.", "Erro", JOptionPane.ERROR_MESSAGE);
            return;
        }

        if (!UserCall.OpenReader()) {
            JOptionPane.showMessageDialog(null, "Falha ao abrir leitor RFID.", "Erro", JOptionPane.ERROR_MESSAGE);
            return;
        }

        try {
        UserCall.SetupAntenna();

        System.out.println("Leitura periódica iniciada (a cada 60 segundos). Prima CTRL+C para sair.");

        while (true) {
    System.out.println("➤ Nova leitura EPC...");

    if (UserCall.ReadEpc()) {
        Thread.sleep(200); // aguarda pela leitura física

        Set<String> epcsLidas = new HashSet<>();
        boolean encontrou = false;

        // tentar ler até não haver mais tags disponíveis
        while (true) {
            String epc = UserCall.RecEpcMsgAsString();
            if (epc == null) break;

            if (epcsLidas.add(epc)) {
                System.out.println("📡 Tag nova: " + epc);
                encontrou = true;
            }
        }

        if (!encontrou) {
            System.out.println("ℹ️ Nenhuma tag detetada.");
        }

    } else {
        System.err.println("❌ Falha ao iniciar leitura EPC.");
    }

    System.out.println("⏳ A aguardar 10 segundos para próxima leitura...\n");
    Thread.sleep(10000); // espera 10 segundos
}


    } catch (Exception e) {
        System.err.println("⚠️ Erro inesperado: " + e.getMessage());
        e.printStackTrace();
    }
        finally {
        System.out.println("Encerrando ligação com leitor...");

        if (UserCall.handleReader != null) {
                UserCall.PowerOff();
                UserCall.CloseConnection();

            System.out.println("Ligação encerrada.");
        } else {
            System.out.println("Ligação não foi estabelecida corretamente.");
        }
    }}

    public static void loadLib() throws IOException {
        File f = new File("RFIDAPI.dll");
        if (f.exists()) {
            System.load(f.getCanonicalPath());
        } else {
            JFileChooser jfc = new JFileChooser(new File("."));
            jfc.setDialogTitle("Seleciona a DLL RFIDAPI.dll");
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
                System.out.println("[INFO] DLL carregada via seleção manual: " + jfc.getSelectedFile().getName());
            } else {
                JOptionPane.showMessageDialog(null, "Nenhuma DLL carregada", "Erro", JOptionPane.ERROR_MESSAGE);
                System.err.println("[ERRO] DLL não carregada.");
            }
        }
    }
}
