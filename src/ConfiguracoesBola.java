
public class ConfiguracoesBola {
    //Informacoes obtidas no manual Robocup A respeito da Bola.
    public final double BALL_SIZE;
    public final double BALL_DECAY;
    public final double BALL_RAND;
    public final double BALL_WEIGHT;
    public final double BALL_SPEED_MAX;
    public final double BALL_ACCEL_MAX;
    public final double BALL_STUCK_AREA;

    public ConfiguracoesBola() {
        BALL_SIZE = Default.BALL_SIZE;
        BALL_DECAY = Default.BALL_DECAY;
        BALL_RAND = Default.BALL_RAND;
        BALL_WEIGHT = Default.BALL_WEIGHT;
        BALL_SPEED_MAX = Default.BALL_SPEED_MAX;
        BALL_ACCEL_MAX = Default.BALL_ACCEL_MAX;
        BALL_STUCK_AREA = Default.BALL_STUCK_AREA;
    }

    public static class Default {

        private static double BALL_SIZE = 0.085;
        private static double BALL_DECAY = 0.94;
        private static double BALL_RAND = 0.05;
        private static double BALL_WEIGHT = 0.2;
        private static double BALL_SPEED_MAX = 3.0;
        private static double BALL_ACCEL_MAX = 2.7;
        private static double BALL_STUCK_AREA = 3.0;

        public static void dataParser(String[] args) {
            try {
                Double val = Double.parseDouble(args[1]);
                if (args[0].contains("ball_size")) {
                    set_size(val);
                } else if (args[0].contains("ball_decay")) {
                    set_decay(val);
                } else if (args[0].contains("ball_rand")) {
                    set_rand(val);
                } else if (args[0].contains("ball_weight")) {
                    set_weight(val);
                } else if (args[0].contains("ball_accel_max")) {
                    set_accel_max(val);
                } else if (args[0].contains("ball_speed_max")) {
                    set_speed_max(val);
                } else if (args[0].contains("ball_stuck_area")) {
                    set_stuck_area(val);
                }
            } catch (Exception ne) {
                Log.e("ERRO");
            }
        }

        public static void set_size(double ball_size) {
            BALL_SIZE = ball_size;
        }
      
        public static void set_decay(double ball_decay) {
            BALL_DECAY = ball_decay;
        }

        public static void set_rand(double ball_rand) {
            BALL_RAND = ball_rand;
        }

        public static void set_weight(double ball_weight) {
            BALL_WEIGHT = ball_weight;
        }

        public static void set_speed_max(double ball_speed_max) {
            BALL_SPEED_MAX = ball_speed_max;
        }

        public static void set_accel_max(double ball_accel_max) {
            BALL_ACCEL_MAX = ball_accel_max;
        }

        public static void set_stuck_area(double ball_stuck_area) {
            BALL_STUCK_AREA = ball_stuck_area;
        }
    }
}
