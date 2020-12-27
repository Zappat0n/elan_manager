package ui;

import main.ApplicationLoader;
import utils.MyLogger;
import utils.SettingsManager;
import utils.Updater;

import javax.swing.*;
import java.awt.*;
import java.io.*;
import java.net.URL;
import java.net.URLConnection;

/**
 * Created by angel on 4/05/17.
 */
public class UpdateInfoForm  extends JDialog {
    private static final String TAG = UpdateInfoForm.class.getSimpleName();
    private final SettingsManager settingsManager;
    private final String root;
    private final JFrame frame;
    private JEditorPane infoPane;
    private JButton ok;
    private JProgressBar pb;
    private String filename;

    public UpdateInfoForm(JFrame owner, SettingsManager settingsManager, String info, Boolean isAdmin) {
        super(owner, "Actualización", true);
        frame = owner;
        this.settingsManager = settingsManager;
        root = settingsManager.getDir() + "update" + settingsManager.getSeparator();
        initComponents();
        infoPane.setText(info);
        int x = owner.getX() + owner.getWidth()/2 - getWidth()/2;
        int y = owner.getY() + owner.getHeight()/2 - getHeight()/2;
        setLocation(x, y);
        setVisible(true);
    }

    private void initComponents() {
        setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);
        setTitle("Actualización encontrada");
        JPanel pan1 = new JPanel();
        pan1.setLayout(new BorderLayout());

        JPanel pan2 = new JPanel();
        pan2.setLayout(new FlowLayout());

        infoPane = new JEditorPane();
        infoPane.setContentType("text/html");

        JScrollPane scp = new JScrollPane();
        scp.setViewportView(infoPane);
        pb = new JProgressBar();
        JPanel panMain = new JPanel();
        panMain.setLayout(new BorderLayout());
        panMain.add(pb, BorderLayout.SOUTH);
        panMain.add(scp, BorderLayout.CENTER);

        ok = new JButton("Actualizar");
        ok.addActionListener(e -> update());

        JButton cancel = new JButton("Cancelar");
        cancel.addActionListener(e -> UpdateInfoForm.this.dispose()
        );
        pan2.add(cancel);
        pan2.add(ok);
        pan1.add(pan2, BorderLayout.SOUTH);
        pan1.add(panMain, BorderLayout.CENTER);
        this.add(pan1);
        setSize(300, 200);
        //pack();
    }

    private void update() {
        try {
            infoPane.setText("");
            download();
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(frame, "Error. No se ha podido actualizar la aplicación",
                    "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void download() {
        try {
            File dir = new File(root);
            if (!dir.exists()) dir.mkdir();
        } catch (Exception e) {
            MyLogger.e(TAG, e);
            JOptionPane.showMessageDialog(frame, "Error. No he podido crear el directorio 'update'.",
                    "Error", JOptionPane.ERROR_MESSAGE);
        }
        Thread worker = new Thread(() -> {
            try {
                filename = ApplicationLoader.settingsManager.getValue(SettingsManager.FILENAME);
                downloadFile(filename, ApplicationLoader.settingsManager.getValue(SettingsManager.DOWNLOADURL));
                copyFiles(new File(root),new File(settingsManager.getDir()).getAbsolutePath());
                cleanup();
                infoPane.setText("<html>Actualización terminada!<br>Por favor, vuelva a ejecutar la aplicación.");
                settingsManager.addValue(SettingsManager.VERSION, String.valueOf(Updater.version));
                launchRestartDialog();
            } catch (Exception ex) {
                ex.printStackTrace();
                JOptionPane.showMessageDialog(null, "Ha ocurrido un error mientras realizaba la actualización!");
            }
        });
        worker.start();
    }

    private void launchRestartDialog() {
        ok.addActionListener(e -> System.exit(0));
        ok.setText("Cerrar");
    }

    private void launch() {
        String[] run = {"java",  "-jar", filename};
        try {
            Runtime.getRuntime().exec(run);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        System.exit(0);
    }

    private void cleanup() {
        File f = new File(root+filename);
        f.delete();
        remove(new File(root));
        new File(root).delete();
    }

    private void remove(File f) {
        File[]files = f.listFiles();
        assert files != null;
        for(File ff:files) {
            if(ff.isDirectory()) {
                remove(ff);
                ff.delete();
            } else {
                ff.delete();
            }
        }
    }

    private void copyFiles(File f,String dir) throws IOException {
        File[]files = f.listFiles();
        assert files != null;
        for(File ff:files) {
            if(ff.isDirectory()){
                new File(dir+"/"+ff.getName()).mkdir();
                copyFiles(ff,dir+"/"+ff.getName());
            } else {
                copy(ff.getAbsolutePath(),dir+"/"+ff.getName());
            }
        }
    }

    private void copy(String srFile, String dtFile) throws IOException{
        File f1 = new File(srFile);
        File f2 = new File(dtFile);
        InputStream in = new FileInputStream(f1);

        OutputStream out = new FileOutputStream(f2);

        byte[] buf = new byte[1024];
        int len;
        while ((len = in.read(buf)) > 0){
            out.write(buf, 0, len);
        }
        in.close();
        out.close();
    }

    private void downloadFile(String fileName, String link) throws IOException {
        URL url = new URL(link);
        URLConnection conn = url.openConnection();
        InputStream is = conn.getInputStream();
        int max = conn.getContentLength();
        infoPane.setText("<html>Descargando fichero...<br>Tamaño: "+max+" Bytes");
        BufferedOutputStream fOut = new BufferedOutputStream(new FileOutputStream(new File(root+fileName)));
        byte[] buffer = new byte[32 * 1024];
        int bytesRead;
        int in = 0;
        pb.setMinimum(1);
        pb.setMaximum(max);
        new Thread(() -> pb.setVisible(true)).start();

        try {
            while ((bytesRead = is.read(buffer)) != -1) {
                in += bytesRead;
                fOut.write(buffer, 0, bytesRead);
                pb.setValue(in);
            }
        } finally {
            fOut.flush();
            fOut.close();
            is.close();
        }
        infoPane.setText(infoPane.getText()+"\nDescarga Completa!");
    }
}
