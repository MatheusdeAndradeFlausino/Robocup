public final class Log {

    public static int DEBUG = 2;
    public static int INFO = 1;
    public static int ERROR = 0;
    public static int NONE = -1;
    
    private static void log(String message){
    	 System.out.println(message);
    }

    public static void log(int verbosity, String message) {
       
    }

    public static void d(String message){
    	log(DEBUG, message);
    }

    public static void i(String message){
    	log(INFO, message);
    }

    public static void e(String message){
    	log(ERROR, message);
    }
}
