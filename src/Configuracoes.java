import java.util.Arrays;
import java.util.HashSet;

/**
 * Classe que armazena todas as informações e parâmetros conhecidos sobre o simulador Robocup 2D.
 */

public class Configuracoes {
   
    public static final String HOSTNAME = "localhost";
    public static final int INIT_PORT = 6000;
    public static final String SOCCER_SERVER_VERSION = "15.0";
    public static final int MSG_SIZE = 4096;
    public static final double MARCACAO = 10;
    public static final String NOME_TIME = "TCC";
    public static final String NOME_OUTRO_TIME = "TCC2";
    public static final String ID_BOLA = "(b)";
    
    public static double ALTURA_GOL                             = 14.02;
    public static final double ALTURA_PEQUENA_AREA              = 16.0;
    public static final double COMPRIMENTO_PEQUENA_AREA         = 6.5;
    public static final double LARGURA_CAMPO                    = 105.0;
    public static final double ALTURA_CAMPO                     = 68.0;
    public static final double BUFFER_CAMPO                     = 5.0; //variavel que indica o tamanho do espaço entre as linhas da lateral do campo e as flags externas
    public static final double LARGURA_AREA_PENALTI             = 16.5; // 97.4% confirmed in robocup; based on size of actual field
    public static final double ALTURA_AREA_PENALTI              = 40.3; // 97.4% confirmed in robocup; based on size of actual field
    public static final double CONFIANCA_BOLA                   = 0.87;
    public static final double PENALTY_X                        = Configuracoes.LARGURA_CAMPO/2 - Configuracoes.LARGURA_AREA_PENALTI;//onde começa a area do penalti
    public static final double MAX_Y_PORCENTAGEM                = 0.8;
    public static final double VARIACAO_DE_INERCIA              = 5.0;
    public static final double ANGULO_PARA_USAR_TURN            = 7.0;
    public static final double VELOCIDADE_FINAL_PASSE           = 1.2;
    public static final double VELOCIDADE_FINAL_PASSE_RAPIDO    = 1.8;
    public static final double ANGULO_PARA_VIRAR_PESCOCO        = 7;
    // Other constants
    public static final char LEFT_SIDE  = 'l';
    public static final char RIGHT_SIDE = 'r';
    
    // Parametros do servidor    
    public static ServerParams_Ball   BOLA_PARAMS           = new ServerParams_Ball();
    public static ServerParams_Player JOGADOR_PARAMS         = new ServerParams_Player();
    public static double              DASH_POWER_RATE       = 0.006;
    public static final double        EFFORT_DEC            = 0.05;
    public static final double        PLAYER_ACCEL_MAX      = 1.0;
    public static final double        PLAYER_SPEED_MAX      = 1.0;
    public static final double        TEAM_FAR_LENGTH       = 40.0;
    public static final double        TEAM_TOO_FAR_LENGTH   = 60.0;
    
    // Inferencia
    public static final double DISTANCE_ESTIMATE = 0.333333 * TEAM_FAR_LENGTH + 0.666666 * TEAM_TOO_FAR_LENGTH;

    // Cordenadas
    public static final Point CENTRO_CAMPO = new Point(0, 0);
    
    /**
     * Constante strings representando os comandos que um cliente pode enviar
     * para o servidor.
     */
    public class Commands {
        public static final String BYE = "bye";
        public static final String DASH = "dash";
        public static final String INIT = "init";
        public static final String KICK = "kick";
        public static final String TURN = "turn";
        public static final String TURN_NECK = "turn_neck";
        public static final String MOVE = "move";
        public static final String CATCH = "catch";
    }
    
    public static enum RESPONSE {
        SEE,
        SENSE_BODY,
        NONE
    }
    
    /**
     * Obtém a taxa de potência do dash.
     * 
     * @return Obtém a taxa de potência do dash.
     */
    public static double getDashPowerRate() {
        return DASH_POWER_RATE;
    }
    
    /**
     * Define a taxa de potência do dash.
     * 
     * @param rate a taxa de potência do dash.
     */
    public static void setDashPowerRate(double rate) {
        DASH_POWER_RATE = rate;
    }
    

	/**
	 * Obtém a altura do gol.
	 * 
	 * @return a altura do gol.
	 */
	public static double getAlturaGol() {
		return ALTURA_GOL;
	}
	
	/**
	 * Define a altura do gol.
	 * 
	 * @param altura altura do gol.
	 */
	public static void setAlturaGol(double altura) {
		ALTURA_GOL = altura;
	}

	/**
	 * 
         * Recria todos os dados do parâmetro do servidor de acordo com as configurações 
         * do Construtor de cada objeto.
	 */
    public static void reconstruirParametros()
    {
    	BOLA_PARAMS = new ServerParams_Ball();
    	JOGADOR_PARAMS = new ServerParams_Player();
    }
	
	// area campo jogavel
    public static Rectangle CAMPO = new Rectangle(-ALTURA_CAMPO / 2.0, LARGURA_CAMPO / 2.0, ALTURA_CAMPO / 2.0, -LARGURA_CAMPO / 2.0);
    public static Rectangle CAMPO_L = new Rectangle(-ALTURA_CAMPO / 2.0, 0, ALTURA_CAMPO / 2.0, -LARGURA_CAMPO / 2.0);
    public static Rectangle CAMPO_R = new Rectangle(-ALTURA_CAMPO / 2.0, LARGURA_CAMPO / 2.0, ALTURA_CAMPO / 2.0, 0);
    
    
    public static Rectangle PEQUENA_AREA_L = new Rectangle(-ALTURA_PEQUENA_AREA / 2.0, -(LARGURA_CAMPO / 2.0) + COMPRIMENTO_PEQUENA_AREA, ALTURA_PEQUENA_AREA / 2.0 ,-(LARGURA_CAMPO / 2.0) + 1);
    public static Rectangle PEQUENA_AREA_R = new Rectangle(-ALTURA_PEQUENA_AREA / 2.0, (LARGURA_CAMPO / 2.0) - 1 , ALTURA_PEQUENA_AREA / 2.0 ,(LARGURA_CAMPO / 2.0) - COMPRIMENTO_PEQUENA_AREA);
    // Limite físico absoluto do espaço de jogo
    public static Rectangle BORDA_EXTERNA = new Rectangle(CAMPO.getTop() - BUFFER_CAMPO, CAMPO.getRight() + BUFFER_CAMPO, CAMPO.getBottom() + BUFFER_CAMPO, CAMPO.getLeft() - BUFFER_CAMPO);
    
    // area de penalti
    public static Rectangle AREA_PENALTI_ESQUERDA = new Rectangle(-ALTURA_AREA_PENALTI / 2.0, CAMPO.getLeft() + LARGURA_AREA_PENALTI, ALTURA_AREA_PENALTI / 2.0, CAMPO.getLeft());
    public static Rectangle AREA_PENALTI_DIREITA = new Rectangle(-ALTURA_AREA_PENALTI / 2.0, CAMPO.getRight(), ALTURA_AREA_PENALTI / 2.0, CAMPO.getRight() - LARGURA_AREA_PENALTI);
    
 
    
    // Lista de todos os conhecidos play_modes do jogo
    public static final HashSet<String> PLAY_MODES = new HashSet<String>(Arrays.asList(
    		"before_kick_off",
    		"play_on",
    		"time_over",
    		"kick_off_l",
    		"kick_off_r",
    		"kick_in_l",
    		"kick_in_r",
    		"free_kick_l",
    		"free_kick_r",
    		"corner_kick_l",
    		"corner_kick_r",
    		"goal_l_",
    		"goal_r_",
    		"goal_kick_l",
    		"goal_kick_r",
    		"drop_ball",
    		"offside_l",
    		"offside_r"
    ));
    


    
    // Flags das bordas externas
    public static final String[][] GRUPO_FLAGS_EXTERNAS = {
            // top flags externas
            {
                "(f t l 50)",
                "(f t l 40)",
                "(f t l 30)",
                "(f t l 20)",
                "(f t l 10)",
                "(f t 0)",
                "(f t r 10)",
                "(f t r 20)",
                "(f t r 30)",
                "(f t r 40)",
                "(f t r 50)"
            },
            // flags da borda esquerda
            {
                "(f r t 30)",
                "(f r t 20)",
                "(f r t 10)",
                "(f r 0)",
                "(f r b 10)",
                "(f r b 20)",
                "(f r b 30)"
            },
            // flags da borda inferior
            {
                "(f b l 50)",
                "(f b l 40)",
                "(f b l 30)",
                "(f b l 20)",
                "(f b l 10)",
                "(f b 0)",
                "(f b r 10)",
                "(f b r 20)",
                "(f b r 30)",
                "(f b r 40)",
                "(f b r 50)"
            },
            // flags da borda esquerda
            {
                "(f l t 30)",
                "(f l t 20)",
                "(f l t 10)",
                "(f l 0)",
                "(f l b 10)",
                "(f l b 20)",
                "(f l b 30)"
            },
            
    };
    
    /**
     * List of known stationary objects.
     * Although they could theoretically be parsed on the fly, we think it's
     * probably more efficient to parse and store them in advance. They are
     * stationary, after all.
     */
    public static final StationaryObject[] OBJETOS_FIXOS = {
        // Physical boundary flags
        new StationaryObject("(f t l 50)", -50.0, BORDA_EXTERNA.getTop()),
        new StationaryObject("(f t l 40)", -40.0, BORDA_EXTERNA.getTop()),
        new StationaryObject("(f t l 30)", -30.0, BORDA_EXTERNA.getTop()),
        new StationaryObject("(f t l 20)", -20.0, BORDA_EXTERNA.getTop()),
        new StationaryObject("(f t l 10)", -10.0, BORDA_EXTERNA.getTop()),
        new StationaryObject("(f t 0)", 0.0, BORDA_EXTERNA.getTop()),
        new StationaryObject("(f t r 10)", 10.0, BORDA_EXTERNA.getTop()),
        new StationaryObject("(f t r 20)", 20.0, BORDA_EXTERNA.getTop()),
        new StationaryObject("(f t r 30)", 30.0, BORDA_EXTERNA.getTop()),
        new StationaryObject("(f t r 40)", 40.0, BORDA_EXTERNA.getTop()),
        new StationaryObject("(f t r 50)", 50.0, BORDA_EXTERNA.getTop()),
        new StationaryObject("(f r t 30)", BORDA_EXTERNA.getRight(), -30.0),
        new StationaryObject("(f r t 20)", BORDA_EXTERNA.getRight(), -20.0),
        new StationaryObject("(f r t 10)", BORDA_EXTERNA.getRight(), -10.0),
        new StationaryObject("(f r 0)", BORDA_EXTERNA.getRight(), 0.0),
        new StationaryObject("(f r b 10)", BORDA_EXTERNA.getRight(), 10.0),
        new StationaryObject("(f r b 20)", BORDA_EXTERNA.getRight(), 20.0),
        new StationaryObject("(f r b 30)", BORDA_EXTERNA.getRight(), 30.0),
        new StationaryObject("(f b r 50)", 50.0, BORDA_EXTERNA.getBottom()),
        new StationaryObject("(f b r 40)", 40.0, BORDA_EXTERNA.getBottom()),
        new StationaryObject("(f b r 30)", 30.0, BORDA_EXTERNA.getBottom()),
        new StationaryObject("(f b r 20)", 20.0, BORDA_EXTERNA.getBottom()),
        new StationaryObject("(f b r 10)", 10.0, BORDA_EXTERNA.getBottom()),
        new StationaryObject("(f b 0)", 0.0, BORDA_EXTERNA.getBottom()),
        new StationaryObject("(f b l 10)", -10.0, BORDA_EXTERNA.getBottom()),
        new StationaryObject("(f b l 20)", -20.0, BORDA_EXTERNA.getBottom()),
        new StationaryObject("(f b l 30)", -30.0, BORDA_EXTERNA.getBottom()),
        new StationaryObject("(f b l 40)", -40.0, BORDA_EXTERNA.getBottom()),
        new StationaryObject("(f b l 50)", -50.0, BORDA_EXTERNA.getBottom()),
        new StationaryObject("(f l b 30)", BORDA_EXTERNA.getLeft(), 30.0),
        new StationaryObject("(f l b 20)", BORDA_EXTERNA.getLeft(), 20.0),
        new StationaryObject("(f l b 10)", BORDA_EXTERNA.getLeft(), 10.0),
        new StationaryObject("(f l 0)", BORDA_EXTERNA.getLeft(), 0.0),
        new StationaryObject("(f l t 10)", BORDA_EXTERNA.getLeft(), 10.0),
        new StationaryObject("(f l t 20)", BORDA_EXTERNA.getLeft(), 20.0),
        new StationaryObject("(f l t 30)", BORDA_EXTERNA.getLeft(), 30.0),
        
        // Field corner flags
        new StationaryObject("(f l t)", CAMPO.getLeft(), CAMPO.getTop()),
        new StationaryObject("(f r t)", CAMPO.getRight(), CAMPO.getTop()),
        new StationaryObject("(f r b)", CAMPO.getRight(), CAMPO.getBottom()),
        new StationaryObject("(f l b)", CAMPO.getLeft(), CAMPO.getBottom()),
        
        // Field center flags
        new StationaryObject("(f c t)", 0.0, CAMPO.getTop()),
        new StationaryObject("(f c)", 0.0, 0.0),
        new StationaryObject("(f c b)", 0.0, CAMPO.getBottom()),
        
        // Penalty area flags
        new StationaryObject("(f p l t)", AREA_PENALTI_ESQUERDA.getRight(), AREA_PENALTI_ESQUERDA.getTop()),
        new StationaryObject("(f p l c)", AREA_PENALTI_ESQUERDA.getRight(), 0.0),
        new StationaryObject("(f p l b)", AREA_PENALTI_ESQUERDA.getRight(), AREA_PENALTI_ESQUERDA.getBottom()),
        new StationaryObject("(f p r t)", AREA_PENALTI_DIREITA.getLeft(), AREA_PENALTI_DIREITA.getTop()),
        new StationaryObject("(f p r c)", AREA_PENALTI_DIREITA.getLeft(), 0.0),
        new StationaryObject("(f p r b)", AREA_PENALTI_DIREITA.getLeft(), AREA_PENALTI_DIREITA.getBottom()),
        
        // Goalpost flags
        new StationaryObject("(f g l t)", CAMPO.getLeft(), ALTURA_GOL / 2),
        new StationaryObject("(f g l b)", CAMPO.getLeft(), -ALTURA_GOL / 2),
        new StationaryObject("(f g r t)", CAMPO.getRight(), ALTURA_GOL / 2),
        new StationaryObject("(f g r b)", CAMPO.getRight(), -ALTURA_GOL / 2),
        
        // Goals
        new StationaryObject("(g l)", CAMPO.getLeft(), 0.0),
        new StationaryObject("(g r)", CAMPO.getRight(), 0.0)
    };
}
