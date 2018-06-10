package featurecat.lizzie;

import org.json.JSONException;
import featurecat.lizzie.analysis.Leelaz;
import featurecat.lizzie.plugin.PluginManager;
import featurecat.lizzie.rules.Board;
import featurecat.lizzie.gui.LizzieFrame;
import org.json.JSONObject;

import javax.swing.*;
import java.io.File;
import java.io.IOException;
import java.util.ResourceBundle;

/**
 * Main class.
 */
public class Lizzie {
    public static LizzieFrame frame;
    public static Leelaz leelaz;
    public static Board board;
    public static Config config;
    public static String lizzieVersion = "0.5";

    /**
     * Launches the game window, and runs the game.
     */
    public static void main(String[] args) throws IOException, JSONException, ClassNotFoundException, UnsupportedLookAndFeelException, InstantiationException, IllegalAccessException, InterruptedException {
        UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());

        config = new Config();

        // Check that user has installed leela zero
        JSONObject leelazconfig = Lizzie.config.config.getJSONObject("leelaz");
        ResourceBundle resourceBundle = ResourceBundle.getBundle("l10n.DisplayStrings");
        String startfolder = leelazconfig.optString("engine-start-location", ".");

        // Check if engine is present
        File lef = new File(startfolder + '/' + "leelaz");
        if (!lef.exists()) {
            File leexe = new File(startfolder + '/' + "leelaz.exe");
            if (!leexe.exists()) {
                JOptionPane.showMessageDialog(null, resourceBundle.getString("LizzieFrame.display.leelaz-missing"), "Lizzie - Error!", JOptionPane.ERROR_MESSAGE);
                return;
            }
        }

        PluginManager.loadPlugins();

        board = new Board();

        frame = new LizzieFrame();

        new Thread( () -> {
            try {
                leelaz = new Leelaz();
                if(config.handicapInsteadOfWinrate) {
                	leelaz.estimatePassWinrate();
                }
                leelaz.togglePonder();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }).start();

        
    }

    public static void shutdown() {
        PluginManager.onShutdown();
        if (board != null && config.config.getJSONObject("ui").getBoolean("confirm-exit")) {
            int ret = JOptionPane.showConfirmDialog(null, "Do you want to save this SGF?", "Save SGF?", JOptionPane.OK_CANCEL_OPTION);
            if (ret == JOptionPane.OK_OPTION) {
                LizzieFrame.saveSgf();
            }
        }

        try {
            config.persist();
        } catch (IOException err) {
            // Failed to save config
        }

        if (leelaz != null)
            leelaz.shutdown();
        System.exit(0);
    }

}