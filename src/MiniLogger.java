
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.logging.Level;

public class MiniLogger {

    private BufferedWriter bw;
    private final String LOG_FOLDER = "C:\\Users\\Anielly\\Desktop\\log\\";

    public MiniLogger(String fileName) {
        try {
            
            bw = new BufferedWriter(new FileWriter(new File(LOG_FOLDER + fileName), false));
                                
        } catch (IOException ex) {
            java.util.logging.Logger.getLogger(MiniLogger.class.getName()).log(Level.SEVERE, null, ex);
        }

    }
    
    public void log(String msg){
        try {
            bw.append(msg + "\n");
            bw.flush();
        } catch (IOException ex) {
            java.util.logging.Logger.getLogger(MiniLogger.class.getName()).log(Level.SEVERE, null, ex);
        }        
    }

}
