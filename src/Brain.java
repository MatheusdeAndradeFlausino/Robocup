import java.util.ArrayDeque;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Scanner;

public class Brain implements Runnable {
    //Classes foram colocadas no pacote padrão para facilitar compilação no terminal usando javac.
    
    Cliente cliente;
    Jogador jogador;
    public int ciclo;
    public int cicloUltimaVisao;
    public int cicloUltimaSenseInfo;
    public int timeUltimoProcuraBola = 0;

    // Self info & Play mode
    private String playMode;
    private SenseInfo curSenseInfo, lastSenseInfo;
    //public VetorDeAceleracao acceleration;
    //public VetorVelocidade velocity;
    private boolean isPositioned = false;
    HashMap<String, Objeto> fieldObjects = new HashMap<>(100);
    ArrayDeque<String> hearMessages = new ArrayDeque<>();
    LinkedList<Jogador> lastSeenOpponents = new LinkedList<>();
    LinkedList<Jogador> companheirosVisiveis = new LinkedList<>(); // variavel que guarda todos os companheiros visiveis para o jogador no ultimo ciclo
    LinkedList<Configuracoes.RESPONSE> responseHistory = new LinkedList<>();
    private long timeLastSee = 0;
    private long timeLastSenseBody = 0;
    private int lastRan = -1;
    private int noSeeBallCount = 0;
    private final int noSeeBallCountMax = 45;
    private MiniLogger logger;
    private MiniLogger perdaPosicaoAgente;
    private Formacao formacao;
    private double timePasse = 0;
    //private MiniLogger taxaPerdaCiclos;
    //private MiniLogger posEVelObjetos;

    ///////////////////////////////////////////////////////////////////////////
    // CONSTRUCTORS
    ///////////////////////////////////////////////////////////////////////////
    /**
     * This is the primary constructor for the Brain class.
     *
     * @param player a back-reference to the invoking jogador
     * @param client the server cliente by which to send commands, etc.
     */
    public Brain(Jogador player, Cliente client) {
        this.jogador = player;
        this.cliente = client;
        this.curSenseInfo = new SenseInfo();
        this.lastSenseInfo = new SenseInfo();
        // Load the HashMap
        for (int i = 0; i < Configuracoes.FlagsCampo.length; i++) {
            ObjetoFixo object = Configuracoes.FlagsCampo[i];
            //client.log(Log.DEBUG, String.format("Adding %s to my HashMap...", object.id));
            fieldObjects.put(object.id, object);
        }
        // Load the response history
        this.responseHistory.add(Configuracoes.RESPONSE.NONE);
        this.responseHistory.add(Configuracoes.RESPONSE.NONE);
        this.formacao = new Formacao();
    }

    ///////////////////////////////////////////////////////////////////////////
    // GAME LOGIC
    ///////////////////////////////////////////////////////////////////////////        
    /**
     * Returns the direction, in radians, of the jogador at the current ciclo.
     */
    private final double dir() {
        return Math.toRadians(this.jogador.direction.getDirection());
    }

    /**
     * Checks if the play mode allows Move commands.
     *
     * @return true if move commands can be issued
     */
    private final boolean canUseMove() {
        return (playMode.equals("before_kick_off")
                || playMode.startsWith("goal_")
                || playMode.startsWith("free_kick_")
                || playMode.startsWith("corner_kick_"));
    }

    private boolean bolaRolando() {
        return playMode.equals("play_on");
    }

    public Jogador melhorOpcaoDePasse() {
        Jogador melhorOp = null;
        double dist = 1000d;
        double d;
        for (Jogador cpnher : companheirosVisiveis) {
            if (melhorOp == null) {
                melhorOp = cpnher;
            } else {
                d = Utilitarios.distanciaEntre2Objetos(cpnher, jogador);
                if (jogador.time.side == 'l') {
                    if (cpnher.position.getX() >= melhorOp.position.getX() && d < dist) {
                        melhorOp = cpnher;
                        dist = d;
                    } else if (cpnher.position.getX() <= melhorOp.position.getX() && d < dist) {
                        melhorOp = cpnher;
                        dist = d;
                    }
                }
            }
        }
        return melhorOp;
    }

    public boolean acabeiDeEfetuarPasse() {
        if (timePasse == 0) {
            return false;
        } else {
            if (this.ciclo - this.timePasse < 10) {
                return true;
            }
            return false;
        }
    }

    public boolean devoProcurarBola() {
        Objeto bola = this.getOrCreate(Bola.ID);

        double confiancaBola = bola.position.getConfianca(ciclo);
        double confiancaJogador = this.jogador.position.getConfianca(ciclo);

        return ((confiancaJogador - confiancaBola) > Configuracoes.DIF_CONFIANCA_BOLA) || confiancaJogador < Configuracoes.LIMITE_MIN_CONFIANCA;
    }

    /**
     * Returns an estimate of whether the jogador can kick the ball, dependent on
 its distance to the ball and whether it is inside the playing field.
     *
     * @return true if the jogador is on the field and within kicking distance
     */
    public final boolean canKickBall() {
        Objeto ball = this.getOrCreate(Bola.ID);
        return this.jogador.inRectangle(Configuracoes.CAMPO) && ball.curInfo.time >= this.ciclo - 1
                && ball.curInfo.distance < Utilitarios.raioChute();
    }

    /**
     * Returns an indication of whether a given ObjectId was seen in the current
 ciclo step.
     *
     * @return true if the given ObjectId was seen in the current soccer server
 ciclo step
     */
    public final boolean canSee(String id) {
        return this.getOrCreate(id).curInfo.time == this.ciclo;
    }

    /**
     * Accelerates the jogador in the direction of its body.
     *
     * @param power the power of the acceleration (0 to 100)
     */
    private final void dash(double power) {
        // Update this jogador's acceleration           
        escreverNoLog("dash " + power);
        this.cliente.enviaComando(Configuracoes.Comandos.DASH, Double.toString(power));
    }

    private final void changeView(String viewQuality, String viewWidth) {
        // Update this jogador's acceleration           

        this.cliente.enviaComando(Configuracoes.Comandos.CHANGE_VIEW, viewWidth, viewQuality);
    }

    /**
     * Accelerates the jogador in the direction of its body, offset by the given
 angle.
     *
     * @param power the power of the acceleration (0 to 100)
     * @param offset an offset to be applied to the jogador's direction, yielding
 the direction of acceleration
     */
    public final void dash(double power, double offset) {
        cliente.enviaComando(Configuracoes.Comandos.DASH, Double.toString(power), Double.toString(offset));
    }

    /**
     * Returns this jogador's effective dash power. Refer to the soccer server
     * manual for more information.
     *
     * @return this jogador's effective dash power
     */
    private final double edp(double power) {
        System.out.println(this.effort());
        return this.effort() * Configuracoes.DASH_POWER_RATE * power;
    }

    /**
     * Returns an effort value for this jogador. If one wasn't received this ciclo
 step, we guess.
     */
    private final double effort() {
        return this.curSenseInfo.effort;
    }

    private boolean souOJogadorDoTimeMaisProximoDoObjeto(Objeto objeto) {
        double dist = objeto.curInfo.distance;
        for (Jogador companheiro : companheirosVisiveis) {
            if (Utilitarios.distanciaEntre2Objetos(companheiro, objeto) < dist) {
                return false;
            }
        }
        return true;
    }

    private Jogador adversarioMaisProximoDoObjeto(Objeto objeto) {
        Jogador maisProximo = null;
        double dist = 1000d;
        for (Jogador adversario : lastSeenOpponents) {
            if (Utilitarios.distanciaEntre2Objetos(adversario, objeto) < dist) {
                maisProximo = adversario;
            }
        }
        return maisProximo;
    }

    private boolean adversarioMarcando(Jogador player) {
        Ponto posAgente = player.position.getPosicao();
        Retangulo areaMarcacao;
        if (player.time.side == 'l') {
            areaMarcacao = new Retangulo(posAgente.getY() - 7, posAgente.getX() + 10, posAgente.getY() + 7, posAgente.getX());
        } else {
            areaMarcacao = new Retangulo(posAgente.getY() - 7, posAgente.getX(), posAgente.getY() + 7, posAgente.getX() - 10);
        }

        for (Jogador adversario : lastSeenOpponents) {
            if (areaMarcacao.contem(adversario)) {
                return true;
            }
        }
        return false;
    }

    private void passarABola(Objeto bola, Ponto parca) {
        double angulo = calcularAnguloRelativoAoBody(parca, 1);
        double anguloCorpoBola = bola.curInfo.direction + curSenseInfo.headAngle;

        double forca = (jogador.position.getPosicao().distanceTo(parca) * 7.0 / 2.0) * (1 + 0.5 * Math.abs(anguloCorpoBola / 180) + 0.5 * (bola.curInfo.distance / Utilitarios.raioChute()));
        forca = forca > Configuracoes.JOGADOR_PARAMS.POWER_MAX ? Configuracoes.JOGADOR_PARAMS.POWER_MAX : forca;
        kick(forca, angulo);
        timePasse = this.ciclo;

    }

    private void chutarProGol() {

        double angulo = calcularAnguloRelativoAoBody(jogador.getPontoGolOponente(), 1);
        //int anguloRand      = new Random().nextInt(4);
        //int sinal           = new Random().nextInt(2) == 1 ? 1 : -1;
        //angulo             += sinal * anguloRand;
        //chutarPara(jogador.getPontoGolOponente(), Configuracoes.BOLA_PARAMS.BALL_SPEED_MAX);
        kick(100, angulo);
    }

    public boolean devoPassarABola() {
        Formacao.POSICAO posicao = formacao.getPosicao(jogador.numero);
        double posX = formacao.getXmax(jogador.numero, jogador.time.side);
        boolean foraPosicao = false;

        if (posX < jogador.position.getX() && jogador.time.side == 'l') {
            foraPosicao = true;
        }

        if (posX > jogador.position.getX() && jogador.time.side == 'R') {
            foraPosicao = true;
        }

        Jogador p = melhorOpcaoDePasse();

        switch (posicao) {
            case ATACANTE:
                return adversarioMarcando(jogador) && p != null;
            case LATERAL:
            case MEIO_CAMPISTA:
            case MEIO_CAMPISTA_LATERAL:
            case PONTA_ATACANTE:
            case VOLANTE:
            case ZAGUEIRO:
                return (p != null && foraPosicao) || (adversarioMarcando(jogador) && p != null);
            default:
                return false;
        }
    }

    private void determinarEstrategia() {
        Formacao.POSICAO posicao = formacao.getPosicao(jogador.numero);

        //Adicionar métodos caso deseje implementar estrategias diferentes para cada posição;
        switch (posicao) {
            case GOLEIRO:
            case ATACANTE:
            case LATERAL:
            case MEIO_CAMPISTA:
            case MEIO_CAMPISTA_LATERAL:
            case PONTA_ATACANTE:
            case VOLANTE:
            case ZAGUEIRO:
                estrategiaPadrao();
                break;
        }
    }

    public void estrategiaPadrao() {
        try {

            Objeto bola = this.getOrCreate(Bola.ID);
            Ponto posAgente = jogador.position.getPosicao();
            Ponto posBola = bola.position.getPosicao();
            Ponto posEstrategica;
            perdaPosicaoAgente.log(ciclo + " ( " + posAgente.getX() + " , " + posAgente.getY() + " )");
            if (canUseMove()) {

                if (!isPositioned) {
                    formacao.setFormacao(Formacao.FORMACAO_INICIAL);
                    isPositioned = true;
                    posEstrategica = getPosicaoEstrategica(jogador.numero, formacao.formacaoEmCurso);
                    if (jogador.time.side == 'r') {
                        posEstrategica.setX(-posEstrategica.getX());
                        posEstrategica.setY(-posEstrategica.getY());
                    }
                    move(posEstrategica);
                    System.out.println(jogador.renderizar());
                } else {
                    //turnBodyParaPonto(new Ponto(0,0), 1);
                    alinharPescocoECorpo();
                }

            } else if (bolaRolando()) {

                isPositioned = false;
                //get
                logger.log(ciclo + " ");
                if (devoProcurarBola()) {
                    procurarBola();
                    alinharPescocoECorpo();
                    //logger.log("procurando a bola");                    
                } else if (canKickBall()) {
                    Jogador p = adversarioMaisProximoDoObjeto(jogador);
                    if (bola.velocity().magnitude() > 1) {
                        dominarBola();
                        //logger.log("dominando a bola");
                    } else if (getEnemyPenaltyArea().contem(jogador)) {
                        chutarProGol();
                        //logger.log("chutar para o gol");
                    } else if (Math.abs(jogador.position.getX()) > 40) {
                        chutarPara(getEnemyPenaltyArea().getCentro(), Configuracoes.BOLA_PARAMS.BALL_SPEED_MAX);
                        logger.log("cruzando para a area");
                        logger.log(getEnemyPenaltyArea().getCentro().render());
                    } else if (devoPassarABola()) {
                        Jogador c = melhorOpcaoDePasse();
                        if (c == null) {
                            procurarBola();
                        } else {
                            Ponto pontoParceiro = Utilitarios.preverPosDeOutroJogador(c, 2);
                            passeDireto(pontoParceiro, true);
                            //System.out.println("passando a bola");
                        }
                        //logger.log("passando a bola");
                    } else {
                        //logger.log("avançando");
                        double dire = Utilitarios.normalizarAngulo(direcaoPraFrente(jogador.time.side) - bodyDirection());
                        if (Math.abs(dire) > 90) {
                            if (bola.velocity().magnitude() > 1) {
                                dominarBola();
                            } else {
                                turnBodyParaDirecao(direcaoPraFrente(jogador.time.side));
                            }
                        }
                        chutarBolaProximaAoCorpo(Utilitarios.normalizarAngulo(direcaoPraFrente(jogador.time.side) - bodyDirection()), 0.4);
                    }
                } else if (souOJogadorDoTimeMaisProximoDoObjeto(bola) && !acabeiDeEfetuarPasse()) {
                    if (!colidirComABola()) {
                        //alinharPescocoECorpo();                      
                        Ponto pontoIntersecao = getPontoDeIntersecaoBola();
                        if (pontoIntersecao == null) {
                            correrProPontoVirandoOPescoco(Utilitarios.preverPosBolaDepoisNCiclos(bola, 1), 1);
                        } else {
                            correrProPontoVirandoOPescoco(pontoIntersecao, 1);
                        }
                        //logger.log("corendo atras da bola");     
                    }
                } else {
                    formacao.setFormacao(Formacao.FORMACAO_433_OFENSIVO);
                    posEstrategica = getPosicaoEstrategica(jogador.numero, formacao.formacaoEmCurso);
                    //System.out.println(posEstrategica.renderizar());
                    if (jogador.position.getPosicao().distanceTo(posEstrategica) > 2) {
                        correrProPontoVirandoOPescoco(posEstrategica, 1);
                    } else {
                        alinharPescocoECorpo();
                        virarCorpoParaBola(bola);
                    }

                    //logger.log("indo para formação");
                }
                logger.log("-----------------------------------------------");
            }

        } catch (Exception e) {
            e.printStackTrace();
            Scanner s = new Scanner(System.in);
            s.next();
        }
    }

    public void logAposCiclo() {
        logger.log(ciclo + "");
        logger.log(jogador.renderizar());
        logger.log(jogador.position.renderizar(ciclo));
        logger.log(jogador.direction.renderizar(ciclo));
        logger.log("--------------------------");
    }

    private void ordenarPorDistancia(List<Jogador> listaPlayers, Objeto bola) {
        Jogador aux;
        for (int i = 0; i < listaPlayers.size(); i++) {
            for (int j = i + 1; j < listaPlayers.size(); j++) {
                Jogador iP = listaPlayers.get(i);
                Jogador jP = listaPlayers.get(j);
                if (iP.distanceTo(bola) > jP.distanceTo(bola)) {
                    aux = iP;
                    listaPlayers.set(i, jP);
                    listaPlayers.set(j, aux);
                }
            }
        }
    }

    private void avancarComABola() {
        double targetFacingDir = 0.0;
        Objeto golAdversario = this.getOrCreate(this.jogador.getIdGolOponente());
        if (getEnemyPenaltyArea().contem(jogador)) {
            kick(100, jogador.relativeAngleTo(golAdversario));
        } else {
            if (Math.abs(jogador.position.getX()) > 40) {
                //cruzarParaArea();
            } else {
                if (this.jogador.time.side == 'r') {
                    targetFacingDir = -180.0;
                }
                if (Math.abs(Utilitarios.normalizarAngulo(targetFacingDir - bodyDirection())) > Configuracoes.ANGULO_PARA_USAR_TURN) {
                    this.turnBodyParaDirecao(targetFacingDir);
                } else {
                    kick(15, 0);
                }
            }
        }
    }

    private void goleiroEstrategia() {
        try {
            Ponto posEstrategica;
            int i;
            Ponto posAgente = jogador.position.getPosicao();
            double angBody = bodyDirection();
            Objeto bola = this.getOrCreate(Bola.ID);
            Ponto posCima, posBaixo;
            Ponto fundoCima, fundoBaixo;

            if (jogador.time.side == 'l') {
                posCima = new Ponto(-Configuracoes.LARGURA_CAMPO / 2 + 0.2 * Configuracoes.LARGURA_AREA_PENALTI, -Configuracoes.ALTURA_CAMPO / 5 - 3);
                posBaixo = new Ponto(-Configuracoes.LARGURA_CAMPO / 2 + 0.2 * Configuracoes.LARGURA_AREA_PENALTI, Configuracoes.ALTURA_CAMPO / 5 - 3);
                fundoCima = new Ponto(-Configuracoes.LARGURA_CAMPO / 2 + 2, posCima.getY());
                fundoBaixo = new Ponto(-Configuracoes.LARGURA_CAMPO / 2 + 2, posBaixo.getY());
            } else {
                posCima = new Ponto(Configuracoes.LARGURA_CAMPO / 2 - 0.2 * Configuracoes.LARGURA_AREA_PENALTI, -Configuracoes.ALTURA_CAMPO / 5 - 3);
                posBaixo = new Ponto(Configuracoes.LARGURA_CAMPO / 2 - 0.2 * Configuracoes.LARGURA_AREA_PENALTI, Configuracoes.ALTURA_CAMPO / 5 - 3);
                fundoCima = new Ponto(Configuracoes.LARGURA_CAMPO / 2 - 2, posCima.getY());
                fundoBaixo = new Ponto(Configuracoes.LARGURA_CAMPO / 2 - 2, posBaixo.getY());
            }

            Reta retaFrenteDoGoleiro = Reta.criarRetaEntre2Pontos(posCima, posBaixo);
            Reta retaEsquerdaGoleiro = Reta.criarRetaEntre2Pontos(fundoCima, posCima);
            Reta retaDireitaGoleiro = Reta.criarRetaEntre2Pontos(fundoBaixo, posBaixo);

            if (canUseMove()) {
                if (!isPositioned) {
                    formacao.setFormacao(Formacao.FORMACAO_INICIAL);
                    isPositioned = true;
                    posEstrategica = getPosicaoEstrategica(jogador.numero, formacao.formacaoEmCurso);
                    if (jogador.time.side == 'r') {
                        posEstrategica.setX(-posEstrategica.getX());
                        posEstrategica.setY(-posEstrategica.getY());
                    }
                    move(posEstrategica);
                    System.out.println(jogador.renderizar());
                } else {
                    alinharPescocoECorpo();
                }
            } else if (bolaRolando()) {
                Ponto pontoInterceptacao = getPontoDeIntersecaoBola();
                if (bola.position.getConfianca(ciclo) < Configuracoes.LIMITE_MIN_CONFIANCA) {
                    procurarBola();
                    alinharPescocoECorpo();
                } else if (possoCapturarBola(bola)) {
                    capturarBola();
                } else if (souOJogadorDoTimeMaisProximoDoObjeto(bola) && getMyPenaltyArea().contem(pontoInterceptacao)) {
                    correrProPontoVirandoOPescoco(pontoInterceptacao, 1);
                } else {
                    Reta trajetoriaBola = Reta.criarRetaEntre2Pontos(bola.position.getPosicao(), jogador.getPontoMeuGol());
                    Ponto pontoIntersecao = retaFrenteDoGoleiro.getIntersecao(trajetoriaBola);

                    if (pontoIntersecao.aEsquerda(posCima)) {
                        pontoIntersecao = retaEsquerdaGoleiro.getIntersecao(trajetoriaBola);
                    } else if (pontoIntersecao.aDireita(posBaixo)) {
                        pontoIntersecao = retaDireitaGoleiro.getIntersecao(trajetoriaBola);
                    }

                    if (pontoIntersecao.distanceTo(posAgente) > 1) {
                        correrProPontoVirandoOPescoco(pontoIntersecao, 1);
                    } else {
                        virarCorpoParaBola(bola);
                        alinharPescocoECorpo();
                    }
                }
            } else if (Utilitarios.isFreeKick(playMode, jogador.time.side)) {
                if (canKickBall()) {

                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            Scanner s = new Scanner(System.in);
            s.next();
        }
    }

    private void olharParaFrente() {
        double direcaoPF = direcaoPraFrente(jogador.time.side);
        double anguloTurn = Utilitarios.normalizarAngulo(direcaoPF - jogador.direction.getDirection());
        turn(anguloTurn);
    }

    private final Objeto getOrCreate(String id) {
        if (this.fieldObjects.containsKey(id)) {
            return this.fieldObjects.get(id);
        } else {
            return Objeto.criar(id);
        }
    }

    private int direcaoPraFrente(char time) {
        if (time == Configuracoes.LEFT_SIDE) {
            return 0;
        } else {
            return -180;
        }
    }

    private boolean isValorProximo(double valor1, double valor2, double limite) {
        return Math.abs(valor2 - valor1) <= limite;
    }

    private final void inferPositionAndDirection(Objeto o1, Objeto o2) {

        double x1 = Math.cos(Math.toRadians(o1.curInfo.direction)) * o1.curInfo.distance;
        double y1 = Math.sin(Math.toRadians(o1.curInfo.direction)) * o1.curInfo.distance;
        double x2 = Math.cos(Math.toRadians(o2.curInfo.direction)) * o2.curInfo.distance;
        double y2 = Math.sin(Math.toRadians(o2.curInfo.direction)) * o2.curInfo.distance;
        double direction = -Math.toDegrees(Math.atan((y2 - y1) / (x2 - x1)));

        if (x2 < x1) {
            direction += 180.0;
        } // Need to offset the direction by +/- 90 degrees if using vertical boundary flags
        if (o1.position.getX() == o2.position.getX()) {
            direction += 90.0;
        }

        //double minusX = o1.curInfo.distance * Math.cos(Math.toRadians(direction + o1.curInfo.direction));
        //double minusY = o1.curInfo.distance * Math.sin(Math.toRadians(direction + o1.curInfo.direction));
        this.jogador.direction.update(Utilitarios.normalizarAngulo(direction), 0.95, this.ciclo);
        //double x = o1.position.getX() - minusX;
        //double y = o1.position.getY() - minusY;

        double anguloFlag = this.jogador.direction.getDirection() + o1.curInfo.direction;
        double anguloInverso = anguloFlag + 180;
        double xA = o1.position.getX() + o1.curInfo.distance * Math.cos(Math.toRadians(anguloInverso));
        double yA = o1.position.getY() + o1.curInfo.distance * Math.sin(Math.toRadians(anguloInverso));
        /*if(Math.abs(x) > 57 || Math.abs(y) > 40){            
            return;
        }*/

        this.jogador.position.atualizar(xA, yA, 0.95, this.ciclo);
    }

    public void infereVelocidadeAgente() {
        double magnitude = curSenseInfo.amountOfSpeed;
        double angulo = curSenseInfo.directionOfSpeed;

        jogador.velocidade.setCoordPolar(Math.toRadians(angulo) + this.dir(), magnitude);
    }

    /**
     * Moves the jogador to the specified soccer server coordinates.
     *
     * @param p the Ponto object to pass coordinates with (must be in server
     * coordinates).
     */
    public void move(Ponto p) {
        move(p.getX(), p.getY());
    }

    /**
     * Moves the jogador to the specified soccer server coordinates.
     *
     * @param x x-coordinate
     * @param y y-coordinate
     */
    public void move(double x, double y) {
        cliente.enviaComando(Configuracoes.Comandos.MOVE, Double.toString(x), Double.toString(y));
        escreverNoLog("move (" + x + " , " + y + ")");
        this.jogador.position.atualizar(x, y, 1.0, this.ciclo);
    }

    public void turnNeck(double angulo) {
        escreverNoLog("turn_neck " + angulo);
        cliente.enviaComando(Configuracoes.Comandos.TURN_NECK, angulo);
    }

    /**
     * Kicks the ball in the direction of the jogador.
     *
     * @param power the level of power with which to kick (0 to 100)
     */
    public void kick(double power) {
        escreverNoLog("kick " + power);
        cliente.enviaComando(Configuracoes.Comandos.KICK, Double.toString(power));
    }

    /**
     * Captura a bola no angulo que ela está em relação ao goleiro. (Apenas
     * goleiros podem usar esta habilidade)
     */
    public void capturarBola() {
        Objeto bola = getOrCreate(Bola.ID);
        escreverNoLog("catch " + Double.toString(bola.curInfo.direction));
        cliente.enviaComando(Configuracoes.Comandos.CATCH, Double.toString(bola.curInfo.direction));
    }

    /**
     * Kicks the ball in the jogador's direction, offset by the given angle.
     *
     * @param power the level of power with which to kick (0 to 100)
     * @param offset an angle in degrees to be added to the jogador's direction,
 yielding the direction of the kick
     */
    public void kick(double power, double offset) {
        escreverNoLog("kick " + power + " , " + offset);
        cliente.enviaComando(Configuracoes.Comandos.KICK, Double.toString(power), Double.toString(offset));
    }

    public void escreverNoLog(String mensagem) {
        //logger.log(ciclo + " - ( " +mensagem+ " )");
    }

    public void parseMessage(String message) {
        long timeReceived = System.currentTimeMillis();
        message = Utilitarios.limpar(message);
        // Handle `sense_body` messages
        if (message.startsWith("(sense_body")) {
            //System.out.println(this.ciclo);
            curSenseInfo.copy(lastSenseInfo);
            curSenseInfo.reset();

            this.timeLastSenseBody = timeReceived;
            curSenseInfo.time = Utilitarios.extrairCiclo(message);
            this.cicloUltimaSenseInfo = curSenseInfo.time;
            this.ciclo = curSenseInfo.time;

            String parts[] = message.split("\\(");
            for (String i : parts)
            {
                String nMsg = i.split("\\)")[0].trim();
                if (nMsg.isEmpty()) {
                    continue;
                }
                String nArgs[] = nMsg.split("\\s");

                if (nArgs[0].contains("view_mode")) { 
                    curSenseInfo.viewQuality = nArgs[1];
                    curSenseInfo.viewWidth = nArgs[2];
                } else if (nArgs[0].contains("stamina")) { // Jogador's stamina data
                    curSenseInfo.stamina = Double.parseDouble(nArgs[1]);
                    curSenseInfo.effort = Double.parseDouble(nArgs[2]);
                    curSenseInfo.staminaCapacity = Double.parseDouble(nArgs[3]);
                } else if (nArgs[0].contains("speed")) { // Jogador's speed data
                    curSenseInfo.amountOfSpeed = Double.parseDouble(nArgs[1]);
                    curSenseInfo.directionOfSpeed = Double.parseDouble(nArgs[2]);
                    // Update velocity variable
                    this.infereVelocidadeAgente();
                } else if (nArgs[0].contains("head_angle")) { // Jogador's head angle
                    curSenseInfo.headAngle = Double.parseDouble(nArgs[1]);
                } else if (nArgs[0].contains("ball") || nArgs[0].contains("player")
                        || nArgs[0].contains("post")) { 
                    curSenseInfo.collision = nArgs[0];
                }
            }

           
            if (this.responseHistory.get(0) == Configuracoes.RESPONSE.SEE && this.responseHistory.get(1) == Configuracoes.RESPONSE.SEE && this.ciclo > 0) {
                this.run();
                this.responseHistory.push(Configuracoes.RESPONSE.SENSE_BODY);
                this.responseHistory.removeLast();
            }
        } // Handle `hear` messages
        else if (message.startsWith("(hear")) {
            String parts[] = message.split("\\s");
            this.ciclo = Integer.parseInt(parts[1]);
            if (parts[2].startsWith("s") || parts[2].startsWith("o") || parts[2].startsWith("c")) {
                return;
            } else {
                String nMsg = parts[3].split("\\)")[0];         
                if (nMsg.startsWith("goal_l_")) {
                    nMsg = "goal_l_";
                } else if (nMsg.startsWith("goal_r_")) {
                    nMsg = "goal_r_";
                }
                if (parts[2].startsWith("r")
                        && Configuracoes.PLAY_MODES.contains(nMsg)) 
                {
                    playMode = nMsg;
                    this.isPositioned = false;
                } else {
                    hearMessages.add(nMsg);
                }
            }
        } // Handle `see` messages
        else if (message.startsWith("(see")) {
            long timeSee = System.currentTimeMillis();
            //System.out.println(jogador.renderizar() + " Diferença entre ultimo ciclo e o atual :  "  + (timeSee - timeLastSee)/150 + " ciclos");
            this.timeLastSee = timeSee;
            this.ciclo = Utilitarios.extrairCiclo(message);
            LinkedList<String> infos = Utilitarios.extrairInfos(message);
            lastSeenOpponents.clear();
            companheirosVisiveis.clear();
            for (String info : infos) {
                String id = Utilitarios.extrairId(info);
                if (Utilitarios.objetoUnico(id)) {
                    if (Utilitarios.isFlag(id)) {
                        //System.out.println(id);
                        Objeto obj = this.getOrCreate(id);
                        obj.update(this.jogador, info, this.ciclo);
                        this.fieldObjects.put(id, obj);
                    }
                }
            }
            // Immediately run for the current step. Since our computations takes only a few
            // milliseconds, it's okay to start running over half-way into the 100ms cycle.
            // That means two out of every three ciclo steps will be executed here.
            this.updatePositionAndDirection();

            for (String info : infos) {
                String id = Utilitarios.extrairId(info);
                if (Utilitarios.objetoUnico(id)) {
                    if (!Utilitarios.isFlag(id)) {
                        Objeto obj = this.getOrCreate(id);
                        obj.update(this.jogador, info, this.ciclo);
                        this.fieldObjects.put(id, obj);
                        if (id.startsWith("(p \"") && !(id.startsWith(this.jogador.time.nome, 4))) {
                            lastSeenOpponents.add((Jogador) obj);
                        }
                        if (id.startsWith("(p \"") && (id.startsWith(this.jogador.time.nome, 4))) {
                            companheirosVisiveis.add((Jogador) obj);
                        }
                    }
                }
            }

            this.run();
            if (this.timeLastSee - this.timeLastSenseBody > 100) {
                this.responseHistory.clear();
                this.responseHistory.add(Configuracoes.RESPONSE.SEE);
                this.responseHistory.add(Configuracoes.RESPONSE.SEE);
            } else {
                this.responseHistory.add(Configuracoes.RESPONSE.SEE);
                this.responseHistory.removeLast();
            }
            if (canSee(Bola.ID)) {
                noSeeBallCount = 0;
            } else {
                noSeeBallCount++;
            }

        } 
        else if (message.startsWith("(init")) {
            String[] parts = message.split("\\s");
            char teamSide = message.charAt(6);
            if (teamSide == Configuracoes.LEFT_SIDE) {
                jogador.time.side = Configuracoes.LEFT_SIDE;
                jogador.outroTime.side = Configuracoes.RIGHT_SIDE;
            } else if (teamSide == Configuracoes.RIGHT_SIDE) {
                jogador.time.side = Configuracoes.RIGHT_SIDE;
                jogador.outroTime.side = Configuracoes.LEFT_SIDE;
            } else {
                Log.e("Could not parse teamSide.");
            }
            jogador.numero = Integer.parseInt(parts[2]);
            playMode = parts[3].split("\\)")[0];
            logger = new MiniLogger(jogador.time.nome + "_" + jogador.numero + ".txt");
           perdaPosicaoAgente = new MiniLogger(jogador.time.nome + "_" + jogador.numero + "_taxaPerdaPosAgente.txt");
        } else if (message.startsWith("(server_param")) {
            parseServerParameters(message);
        }
    }

    public void parseServerParameters(String message) {
        String parts[] = message.split("\\(");
        for (String i : parts) // for each structured argument:
        {
            String nMsg = i.split("\\)")[0].trim();
            if (nMsg.isEmpty()) {
                continue;
            }
            String nArgs[] = nMsg.split("\\s");
            if (nArgs[0].startsWith("dash_power_rate")) {
                Configuracoes.setDashPowerRate(Double.parseDouble(nArgs[1]));
            }
            if (nArgs[0].startsWith("goal_width")) {
                Configuracoes.setAlturaGol(Double.parseDouble(nArgs[1]));
            } // Bola arguments:
            else if (nArgs[0].startsWith("ball")) {
                ConfiguracoesBola.Default.dataParser(nArgs);
            } // Jogador arguments:
            else if (nArgs[0].startsWith("player") || nArgs[0].startsWith("min")
                    || nArgs[0].startsWith("max")) {
                ConfiguracoesJogador.Builder.dataParser(nArgs);
            }
        }
    Configuracoes.reconstruirParametros();
    }

    public final Objeto ownGoal() {
        return this.getOrCreate(this.jogador.getIdGol());
    }
    public final Retangulo ownPenaltyArea() {
        if (this.jogador.time.side == 'l') {
            return Configuracoes.AREA_PENALTI_ESQUERDA;
        } else {
            return Configuracoes.AREA_PENALTI_DIREITA;
        }
    }
    public void run() {
        int expectedNextRun = this.lastRan + 1;
        if (this.ciclo > this.lastRan + 1) {
            //Log.e("Brain for jogador " + this.jogador.renderizar() + " did not run during ciclo step " + expectedNextRun + ".");
        }
        this.lastRan = this.ciclo;
        //taxaPerdaCiclos.log(String.valueOf(ciclo));
        if (!jogador.goleiro) {
            this.determinarEstrategia();
        } else {
            this.goleiroEstrategia();
        }
    }
    public final void turn(double offset) {
        double moment = Utilitarios.validarAnguloCorpo(offset);
        escreverNoLog("turn " + offset);
        cliente.enviaComando(Configuracoes.Comandos.TURN, moment);
    }
    public final void turnTo(double direction) {
        this.turn(this.jogador.relativeAngleTo(direction));
    }
    public double bodyDirection() {
        return jogador.direction.getDirection() - curSenseInfo.headAngle;
    }
    private final void dashTo(Ponto point) {
        dashTo(point, 1);
    }
    private final void dashTo(Ponto ponto, int ciclos) {

        Ponto posGlobal = Utilitarios.preverAgentePosDepoisNCiclos(jogador, 0, ciclos, ciclo, curSenseInfo).getPosicao();
        Vetor vet = ponto.asVector();
        vet.menos(posGlobal.asVector());
        double angulo = Math.toDegrees(vet.direction());
        angulo -= bodyDirection();
        angulo = Utilitarios.normalizarAngulo(angulo);

        if (Math.abs(angulo) > Configuracoes.ANGULO_PARA_USAR_TURN) {
            turnBodyParaPonto(ponto, ciclos);
        } else {
            dashParaPonto(ponto, ciclos);
        }
    }

    public void correrProPontoVirandoOPescoco(Ponto ponto, int ciclos) {
        Objeto bola = this.getOrCreate(Bola.ID);

        Ponto posGlobal = Utilitarios.preverAgentePosDepoisNCiclos(jogador, 0, ciclos, ciclo, curSenseInfo).getPosicao();
        Vetor vet = ponto.asVector();
        vet.menos(posGlobal.asVector());
        double angulo = Math.toDegrees(vet.direction());
        angulo -= bodyDirection();
        angulo = Utilitarios.normalizarAngulo(angulo);

        if (Math.abs(angulo) > Configuracoes.ANGULO_PARA_USAR_TURN) {
            turnBodyParaPonto(ponto, ciclos);
            virarPescocoParaPonto("turn", angulo, 0, bola.position.getPosicao());
        } else {
            dashParaPonto(ponto, ciclos);
            virarPescocoParaPonto("dash", 0, 100, bola.position.getPosicao());
        }
    }

    public void virarPescocoParaPonto(String comando, double angulo, double power, Ponto pos) {
        double angulosPosTurn[];
        Ponto posAgente = new Ponto(jogador.position.getPosicao());
        VetorVelocidade vel = new VetorVelocidade(jogador.velocity());
        double anguloBody = bodyDirection();
        double anguloNeck = jogador.direction.getDirection();
        SenseInfo senseAux = new SenseInfo();
        curSenseInfo.copy(senseAux);

        if (comando.equals("turn")) {
            angulosPosTurn = Utilitarios.preverEstadoDepoisTurn(angulo, posAgente, vel, anguloBody, anguloNeck, senseAux);
            anguloBody = angulosPosTurn[0];
            anguloNeck = angulosPosTurn[1];
        } else {
            Utilitarios.preverEstadoDepoisDoDash(posAgente, vel, power, ciclo, senseAux, anguloBody);
        }

        pos.menos(posAgente);
        double angDesejado = Math.toDegrees(pos.asVector().direction());
        double angRelToBody = Utilitarios.normalizarAngulo(angDesejado - anguloBody);
        double angBodToNeck = Utilitarios.normalizarAngulo(anguloBody - anguloNeck);
        double angTurn;

        if (angRelToBody < Configuracoes.JOGADOR_PARAMS.NECK_ANGLE_MIN) {
            angTurn = Configuracoes.JOGADOR_PARAMS.NECK_ANGLE_MIN + angBodToNeck;
        } else if (angRelToBody > Configuracoes.JOGADOR_PARAMS.NECK_ANGLE_MAX) {
            angTurn = Configuracoes.JOGADOR_PARAMS.NECK_ANGLE_MAX + angBodToNeck;
        } else {
            angTurn = angRelToBody + angBodToNeck;
        }

        //logger.log(ciclo + ": " + comando + ": " + angulo + " , " + power + " turnNeck: " + angTurn);
        turnNeck(angTurn);
    }

    private final void updatePositionAndDirection() {
        for (int i = 0; i < 4; i++) {
            LinkedList<Objeto> flagsOnSide = new LinkedList<Objeto>();
            for (String id : Configuracoes.GRUPO_FLAGS_EXTERNAS[i]) {
                Objeto flag = this.fieldObjects.get(id);
                if (flag.curInfo.time == this.ciclo) {
                    flagsOnSide.add(flag);
                } else {
                }
                if (flagsOnSide.size() > 1) {
                    this.inferPositionAndDirection(flagsOnSide.poll(), flagsOnSide.poll());
                    return;
                }
            }
        }
    }

    final public Retangulo getMyPenaltyArea() {
        if (jogador.time == null) {
            throw new NullPointerException("Player team not initialized while getting penelty area.");
        }
        return jogador.time.side == 'l' ? Configuracoes.AREA_PENALTI_ESQUERDA : Configuracoes.AREA_PENALTI_DIREITA;
    }

    final public Retangulo getEnemyPenaltyArea() {
        if (jogador.time == null) {
            throw new NullPointerException("Player team not initialized while getting penelty area.");
        }
        return jogador.time.side == 'l' ? Configuracoes.AREA_PENALTI_DIREITA : Configuracoes.AREA_PENALTI_ESQUERDA;
    }

    final public Retangulo getCampoDeDefesa() {
        if (jogador.time == null) {
            throw new NullPointerException("Player team not initialized while getting penelty area.");
        }
        return jogador.time.side == 'l' ? Configuracoes.CAMPO_L : Configuracoes.CAMPO_R;
    }

    final public Retangulo getCampoDeAtaque() {
        if (jogador.time == null) {
            throw new NullPointerException("Player team not initialized while getting penelty area.");
        }
        return jogador.time.side == 'l' ? Configuracoes.CAMPO_R : Configuracoes.CAMPO_L;
    }

    final public Retangulo getPequenaAreaInimiga() {
        if (jogador.time == null) {
            throw new NullPointerException("Player team not initialized while getting penelty area.");
        }
        return jogador.time.side == 'l' ? Configuracoes.PEQUENA_AREA_R : Configuracoes.PEQUENA_AREA_L;
    }

    final public Retangulo getPequenaArea() {
        if (jogador.time == null) {
            throw new NullPointerException("Player team not initialized while getting penelty area.");
        }
        return jogador.time.side == 'l' ? Configuracoes.PEQUENA_AREA_L : Configuracoes.PEQUENA_AREA_R;
    }

    public void alinharPescocoECorpo() {
        turnNeck(-curSenseInfo.headAngle);
    }

    public void virarCorpoParaBola(Objeto bola) {
        turnBodyParaPonto(Utilitarios.preverPosBolaDepoisNCiclos(bola, 1), 1);
    }

    public void chutarPara(Ponto posAlvo, double velFinal) {
        Objeto bola = getOrCreate(Bola.ID);

        Ponto posBola = new Ponto(bola.position.getPosicao());
        VetorVelocidade velBola = new VetorVelocidade(bola.velocity());
        Ponto posTrajetoria = new Ponto(posAlvo);
        Ponto posAgente = new Ponto(jogador.position.getPosicao());

        posTrajetoria.menos(posBola);
        VetorVelocidade velDes = new VetorVelocidade();
        velDes.setPolar(posTrajetoria.asVector().direction(),
                Utilitarios.getForcaParaAtravessar(posTrajetoria.asVector().magnitude(), velFinal));

        double power;
        double angulo;

        Posicao posEst = Utilitarios.preverAgentePosDepoisNCiclos(jogador, 0, 1, this.ciclo, curSenseInfo);
        Ponto pointEst = posEst.getPosicao();
        Vetor velDesMaisPosBola = posBola.asVector();
        velDesMaisPosBola.mais(velDes);

        if (pointEst.distanceTo(velDesMaisPosBola.asPoint())
                < Configuracoes.BOLA_PARAMS.BALL_SIZE + Configuracoes.JOGADOR_PARAMS.PLAYER_SIZE) {

            Reta reta = Reta.criarRetaEntre2Pontos(posBola, velDesMaisPosBola.asPoint());
            Ponto posAgenteProj = reta.pontoNaRetaMaisProxDoPonto(posAgente);
            double dist = posBola.distanceTo(posAgenteProj);

            if (velDes.magnitude() < dist) {
                dist -= Configuracoes.BOLA_PARAMS.BALL_SIZE + Configuracoes.JOGADOR_PARAMS.PLAYER_SIZE;
            } else {
                dist += Configuracoes.BOLA_PARAMS.BALL_SIZE + Configuracoes.JOGADOR_PARAMS.PLAYER_SIZE;
            }

            velDes.setPolar(velDes.direction(), dist);
        }

        Objeto oponente = Utilitarios.maisProximoDoObjeto(lastSeenOpponents, bola);
        double distOponente = oponente != null
                ? oponente.position.getPosicao().distanceTo(bola.position.getPosicao()) : 100;

        if (velDes.magnitude() > Configuracoes.BOLA_PARAMS.BALL_SPEED_MAX) { // NÃO VAI CHEGAR NO PONTO
            power = Configuracoes.JOGADOR_PARAMS.POWER_MAX;
            double dSpeed = getKickPowerRateAtual(bola, curSenseInfo.headAngle) * power;
            double tmp = velBola.rotate(-velDes.direction()).getY();
            angulo = velDes.direction() - Utilitarios.arcSenGraus(tmp / dSpeed);
            Vetor aux = new Vetor();
            aux.setCoordPolar(angulo, dSpeed);
            aux.mais(bola.velocity());

            double dSpeedPred = aux.magnitude();

            if (dSpeedPred > Configuracoes.JOGADOR_PARAMS.PLAYER_WHEN_TO_KICK * Configuracoes.BOLA_PARAMS.BALL_ACCEL_MAX) {
                acelerarBolaAVelocidade(velDes);    
            } else if (getKickPowerRateAtual(bola, curSenseInfo.headAngle) > Configuracoes.JOGADOR_PARAMS.PLAYER_WHEN_TO_KICK * Configuracoes.JOGADOR_PARAMS.KICK_POWER_RATE) {
                dominarBola();                          
            } else {
                chutarBolaProximaAoCorpo(0, 0.16);            
            }
        } else {
            Vetor velBolaAcele = new Vetor(velDes);
            velBolaAcele.menos(velBola);

            power = velBolaAcele.magnitude() / getKickPowerRateAtual(bola, curSenseInfo.headAngle);
            if (power <= 1.05 * Configuracoes.JOGADOR_PARAMS.POWER_MAX || (distOponente < 2.0 && power <= 1.30 * Configuracoes.JOGADOR_PARAMS.POWER_MAX)) {
                acelerarBolaAVelocidade(velDes); 

            } else {
                chutarBolaProximaAoCorpo(0, 0.16);
            }
        }
    }

    public void acelerarBolaAVelocidade(Vetor velDes) {
        Objeto bola = getOrCreate(Bola.ID);

        double angBody = bodyDirection();
        VetorVelocidade velBall = new VetorVelocidade(bola.velocity());
        Vetor accDes = new Vetor(velDes);
        accDes.menos(velBall);
        double dPower;
        double angActual;

        if (accDes.magnitude() < Configuracoes.BOLA_PARAMS.BALL_ACCEL_MAX) {
            dPower = (accDes.magnitude() / getKickPowerRateAtual(bola, curSenseInfo.headAngle));
            angActual = Utilitarios.normalizarAngulo(Math.toDegrees(accDes.direction()) - angBody);
            if (dPower <= Configuracoes.JOGADOR_PARAMS.POWER_MAX) {
                kick(dPower, angActual);
                return;
            }
        }
        dPower = Configuracoes.JOGADOR_PARAMS.POWER_MAX;
        double dSpeed = getKickPowerRateAtual(bola, curSenseInfo.headAngle) * dPower;
        double tmp = velBall.rotate(-velDes.direction()).getY();
        angActual = Math.toDegrees(velDes.direction() - Utilitarios.arcSenGraus(tmp / dSpeed));
        angActual = Utilitarios.normalizarAngulo(angActual - angBody);
        kick(dPower, angActual);

    }

    /**
     * Habilidade que permite o agente chutar a bola proxima a seu corpo
     *
     * @param angulo relativo angulo em graus
     * @param taxaDeChute padrão 0.16
     */
    public void chutarBolaProximaAoCorpo(double angulo, double taxaDeChute) { // taxa de chute
        Objeto bola = getOrCreate(Bola.ID);
        SenseInfo sense = new SenseInfo();
        curSenseInfo.copy(sense);

        double angAgente = bodyDirection(); // graus
        Posicao p = Utilitarios.preverAgentePosDepoisNCiclos(jogador, 0, 1, ciclo, sense);
        Ponto point = p.getPosicao();
        double dist = Configuracoes.BOLA_PARAMS.BALL_SIZE + Configuracoes.JOGADOR_PARAMS.PLAYER_SIZE
                + Configuracoes.JOGADOR_PARAMS.KICKABLE_MARGIN * taxaDeChute;
        double angGlobal = Utilitarios.normalizarAngulo(angAgente + angulo); // graus
        Vetor posBall = new Vetor();
        posBall.setCoordPolar(Math.toRadians(angGlobal), dist);
        posBall.mais(point.asVector());

        if (Math.abs(posBall.getY()) > Configuracoes.ALTURA_CAMPO / 2 || Math.abs(posBall.getX()) > Configuracoes.LARGURA_CAMPO / 2) {
            Reta lineBody = Reta.criarRetaAPartirDaPosicaoEAngulo(point, Math.toRadians(angGlobal));
            Reta lineSide;
            if (Math.abs(posBall.getY()) > Configuracoes.ALTURA_CAMPO / 2) {
                lineSide = Reta.criarRetaAPartirDaPosicaoEAngulo(new Ponto(0, Math.signum(posBall.getY()) * Configuracoes.ALTURA_CAMPO / 2.0), 0);
            } else {
                lineSide = Reta.criarRetaAPartirDaPosicaoEAngulo(new Ponto(0, Math.signum(posBall.getX()) * Configuracoes.LARGURA_CAMPO / 2.0), Math.toRadians(90));
            }

            Ponto posIntersect = lineSide.getIntersecao(lineBody);
            posBall = point.asVector();
            Vetor n = new Vetor();
            n.setCoordPolar(Math.toRadians(angGlobal), posIntersect.distanceTo(point) - 0.2);
            posBall.mais(n);
        }

        Vetor vecDesired = posBall;
        vecDesired.menos(point.asVector());

        Vetor vecShoot = vecDesired;
        vecShoot.menos(bola.velocity());

        double dPower = vecShoot.magnitude() / getKickPowerRateAtual(bola, curSenseInfo.headAngle);
        double angActual = Math.toDegrees(vecDesired.direction()) - angAgente;
        angActual = Utilitarios.normalizarAngulo(angActual);

        if (dPower > Configuracoes.JOGADOR_PARAMS.POWER_MAX && bola.velocity().magnitude() > 0.1) {
            dominarBola();
            return;
        } else if (dPower > Configuracoes.JOGADOR_PARAMS.POWER_MAX) {
            if (Utilitarios.isBolaParadaParaNos(playMode, jogador.time.side, jogador.outroTime.side)) {
                if (bola.curInfo.direction > 25) {
                    virarCorpoParaBola(bola);
                    return;
                }
            } else {
                dPower = 100;
            }
        }

        kick(dPower, angActual);
    }

    public void procurarBola() {
        Objeto bola = this.getOrCreate(Bola.ID);
        int sinal = 1;
        Ponto posBola = new Ponto(bola.position.getPosicao());
        Ponto posAgente = new Ponto(jogador.position.getPosicao());
        posBola.menos(posAgente);
        double angulo = Math.toDegrees(posBola.asVector().direction());
        double anguloAgente = jogador.direction.getDirection();

        if (ciclo == timeUltimoProcuraBola) {
            return;
        }

        if (ciclo - timeUltimoProcuraBola > 3) {
            sinal = (Utilitarios.isAnguloNoIntervalo(angulo, anguloAgente, Utilitarios.normalizarAngulo(anguloAgente + 180))) ? 1 : -1;
        }

        timeUltimoProcuraBola = ciclo;
        Vetor angTurn = new Vetor();
        angTurn.setCoordPolar(Math.toRadians(Utilitarios.normalizarAngulo(anguloAgente + 60 * sinal)), 1);
        posAgente.mais(angTurn.asPoint());
        turnBodyParaPonto(posAgente, sinal);
    }

    public void dominarBola() {
        Objeto bola = this.getOrCreate(Bola.ID);
        Posicao pe = Utilitarios.preverAgentePosDepoisNCiclos(jogador, 0, 1, ciclo, curSenseInfo);
        double power = bola.velocity().magnitude() / getKickPowerRateAtual(bola, curSenseInfo.headAngle);

        if (power > Configuracoes.JOGADOR_PARAMS.POWER_MAX) {
            power = Configuracoes.JOGADOR_PARAMS.POWER_MAX;
        }

        double angulo = Math.toDegrees(bola.velocity().direction()) + 180 - bodyDirection(); // graus
        angulo = Utilitarios.normalizarAngulo(angulo);

        kick(power, angulo);
    }

    public void intercept(boolean isGoleiro) {

    }

    public Ponto getPosicaoEstrategica(int numero, Formacao.LayoutFormacao[] formacao) {
        Objeto bola = this.getOrCreate(Bola.ID);
        Ponto pos;
        Ponto posBola = bola.position.getPosicao();
        List<Jogador> todosNaVista = new LinkedList<>();
        todosNaVista.addAll(companheirosVisiveis);
        todosNaVista.addAll(lastSeenOpponents);
        boolean nossaBola = Utilitarios.isNossaPosseDeBola(todosNaVista, bola, jogador.time.nome);
        double maxX = Utilitarios.getXImpedimento(lastSeenOpponents, bola);
        maxX = Math.max(-0.5, maxX - 1.5);

        if (Utilitarios.isGoalKick(playMode, jogador.outroTime.side)) {
            maxX = Math.min(Configuracoes.PENALTY_X - 1, maxX);
        } else if (Utilitarios.isBeforeKickOff(playMode)) {
            maxX = Math.min(-2, maxX);
        } else if (Utilitarios.isOffside(playMode, jogador.time.side)) {
            maxX = bola.position.getX() - 0.5;
        }

        if (Utilitarios.isBeforeKickOff(playMode) || bola.position.getConfianca(ciclo) < Configuracoes.LIMITE_MIN_CONFIANCA) {
            posBola = new Ponto(0, 0);
        } else if (Utilitarios.isGoalKick(playMode, jogador.time.side) || (Utilitarios.isFreeKick(playMode, jogador.time.side)
                && posBola.getX() < -Configuracoes.PENALTY_X)) {
            posBola.setX(-Configuracoes.LARGURA_CAMPO / 4 + 5);
        } else if (Utilitarios.isGoalKick(playMode, jogador.outroTime.side) || (Utilitarios.isFreeKick(playMode, jogador.outroTime.side)
                && posBola.getX() > Configuracoes.PENALTY_X)) {
            posBola.setX(Configuracoes.PENALTY_X - 10);
        } else if (Utilitarios.isFreeKick(playMode, jogador.outroTime.side)) {
            posBola.setX(posBola.getX() - 5);
        } else if (nossaBola && !(Utilitarios.isBolaParadaParaEles(playMode, jogador.time.side, jogador.outroTime.side)
                || Utilitarios.isBolaParadaParaNos(playMode, jogador.time.side, jogador.outroTime.side))) {
            posBola.setX(posBola.getX() + 5.0);
        }
        return this.formacao.getPontoEstrategico(numero, posBola, maxX, nossaBola, Configuracoes.MAX_Y_PORCENTAGEM, formacao, jogador.time.side);
    }

    public double turnBodyParaDirecao(double direcao) {
        double angulo = direcao;
        angulo -= bodyDirection();
        angulo = Utilitarios.normalizarAngulo(angulo);
        angulo = Utilitarios.getAnguloParaTurn(angulo, jogador.velocity().magnitude());
        turn(angulo);
        return angulo;
    }

    public double calcularAnguloRelativoAoBody(Ponto ponto, int ciclos) {
        Posicao posGlobal = Utilitarios.preverAgentePosDepoisNCiclos(jogador, 0, ciclos, ciclo, curSenseInfo);
        Vetor vet = ponto.asVector();
        vet.menos(posGlobal.getPosicao().asVector());
        double angulo = Math.toDegrees(vet.direction());
        angulo -= bodyDirection();
        angulo = Utilitarios.normalizarAngulo(angulo);
        return angulo;
    }

    public double calcularAnguloRelativoACabeca(Ponto ponto, int ciclos) {
        Posicao posGlobal = Utilitarios.preverAgentePosDepoisNCiclos(jogador, 0, ciclos, ciclo, curSenseInfo);
        Vetor vet = ponto.asVector();
        vet.menos(posGlobal.getPosicao().asVector());
        double angulo = Math.toDegrees(vet.direction());
        angulo -= jogador.direction.getDirection();
        angulo = Utilitarios.normalizarAngulo(angulo);
        return angulo;
    }

    public double calcularAnguloParaTurn(Ponto ponto, int ciclos) {
        Posicao posGlobal = Utilitarios.preverAgentePosDepoisNCiclos(jogador, 0, ciclos, ciclo, curSenseInfo);
        Vetor vet = ponto.asVector();
        vet.menos(posGlobal.getPosicao().asVector());
        double angulo = Math.toDegrees(vet.direction());
        angulo -= bodyDirection();
        angulo = Utilitarios.normalizarAngulo(angulo);
        angulo = Utilitarios.getAnguloParaTurn(angulo, jogador.velocity().magnitude());
        return angulo;
    }

    public double turnBodyParaPonto(Ponto ponto, int ciclos) {
        double angulo = calcularAnguloParaTurn(ponto, ciclos);
        turn(angulo);
        return angulo;
    }

    public void turnBackBodyParaPonto(Ponto ponto, int ciclos) {
        Posicao posGlobal = Utilitarios.preverAgentePosDepoisNCiclos(jogador, 0, ciclos, ciclo, curSenseInfo);
        Vetor vet = ponto.asVector();
        vet.menos(posGlobal.getPosicao().asVector());
        double angulo = Math.toDegrees(vet.direction());
        angulo -= jogador.direction.getDirection() + 180;
        angulo = Utilitarios.normalizarAngulo(angulo);
        angulo = Utilitarios.getAnguloParaTurn(angulo, jogador.velocity().magnitude());
        turn(angulo);
    }

    public void dashParaPonto(Ponto ponto, int ciclos) {
        double power = 100;//Futil.getPowerParaDash(ponto, jogador.direction.getDirection() , jogador.velocity() , curSenseInfo.effort , ciclos);
        dash(power);
    }

    public void moveToPos(Ponto posTo, double angQuandoVirar, double distTras, boolean moverParaTras, int ciclos) {
        Ponto posAgente = jogador.position.getPosicao();
        Ponto posFinalAgente = Utilitarios.preverPosFinalAgente(posAgente, jogador.velocity());

        double anguloBody = jogador.direction.getDirection(); // graus
        Ponto posAux = posTo;
        posAux.menos(posFinalAgente);
        double anguloPos = Math.toDegrees(posAux.asVector().direction()); // graus
        anguloPos = Utilitarios.normalizarAngulo(anguloPos - anguloBody); // graus

        double anguloAtras = Utilitarios.normalizarAngulo(anguloPos + 180);
        double dist = posAgente.distanceTo(posTo);

        if (moverParaTras) {
            if (Math.abs(anguloAtras) < angQuandoVirar) {
                dashParaPonto(posTo, ciclos);
            } else {
                turnBackBodyParaPonto(posTo, 1);
            }
        } else if (Math.abs(anguloPos) < angQuandoVirar || (Math.abs(anguloAtras) < angQuandoVirar && dist < distTras)) {
            dashParaPonto(posTo, ciclos);
        } else {
            dashTo(posTo, 1);
        }

    }

    public Ponto getPontoDeMarcacao(Ponto pos, double dist) {
        Objeto bola = this.getOrCreate(Bola.ID);
        Ponto posBola = bola.position.getPosicao();
        Ponto posGol = this.jogador.getPontoMeuGol();

        double ang;
        Ponto aux = posGol;
        aux.menos(posBola);

        ang = aux.asVector().direction();

        Vetor vet = new Vetor();
        vet.setCoordPolar(ang, dist);
        pos.mais(vet.asPoint());

        return pos;
    }

    public void directTowards(Ponto pontoTo, double angQuandoVirar) {
        Vetor velAgente = jogador.velocity();
        Ponto posAgente = jogador.position.getPosicao();
        double angAgente = jogador.direction.getDirection();  // graus
        Ponto posAux = pontoTo;
        Ponto posPredAgente = Utilitarios.preverPosFinalAgente(posAgente, jogador.velocity());
        posAux.menos(posPredAgente);

        double anguloFinal = Math.toDegrees(posAux.asVector().direction()); // graus
        double angulo = Utilitarios.normalizarAngulo(anguloFinal - angAgente); // graus
        double angPescoco = 0;

        int turns = 0;
        double[] result;
        while (Math.abs(angulo) > angQuandoVirar && turns < 5) {
            turns++;
            result = Utilitarios.preverEstadoDepoisTurn(Utilitarios.getAnguloParaTurn(angulo, velAgente.magnitude()),
                    posAgente, velAgente, angAgente, angPescoco, curSenseInfo);

            angAgente = result[0];
            angPescoco = result[1];
            angulo = Utilitarios.normalizarAngulo(anguloFinal - angAgente);
        }

        posAgente = jogador.position.getPosicao();

        switch (turns) {
            case 0:
                System.out.println("erro directTowards turns == 0");
                return;
            case 1:
            case 2:
                turnBodyParaPonto(pontoTo, 2);
                break;
            default:
                dashParaPonto(posAgente, 1);  // stop
                break;
        }
    }

    public boolean possoCapturarBola(Objeto bola) {
        return getMyPenaltyArea().contem(bola) && bola.curInfo.distance < 1;
    }

    public void segurarBola() {
        Objeto bola = this.getOrCreate(Bola.ID);

        Ponto posAgente = new Ponto(jogador.position.getPosicao());
        Jogador advProximo = adversarioMaisProximoDoObjeto(jogador);
        Ponto posAdversario;
        double anguloAdversario;
        double angulo = direcaoPraFrente(jogador.time.side) - bodyDirection();
        angulo = Utilitarios.normalizarAngulo(angulo);

        if (advProximo != null) {
            posAdversario = advProximo.position.getPosicao();
            anguloAdversario = advProximo.curInfo.bodyFacingDir;
            if (Utilitarios.distanciaEntre2Objetos(advProximo, bola) < 5) {
                Ponto pAux = new Ponto(posAgente);
                pAux.menos(posAdversario);
                angulo = pAux.asVector().direction();
                double sinal = -Math.signum(anguloAdversario - angulo);
                angulo += sinal * 45 - bodyDirection();
                angulo = Utilitarios.normalizarAngulo(angulo);
            }
        }

        Vetor vetAux = new Vetor();
        vetAux.setCoordPolar(angulo, 0.7);
        posAgente.mais(vetAux.asPoint());

        if (bola.position.getPosicao().distanceTo(posAgente) < 0.3) {
            Ponto bolaPred = Utilitarios.preverPosBolaDepoisNCiclos(bola, 1);
            double angGol = calcularAnguloParaTurn(jogador.getPontoGolOponente(), 1);
            Ponto posAtual = new Ponto(jogador.position.getPosicao());
            Vetor vel = new Vetor(jogador.velocity());
            SenseInfo sense = new SenseInfo();
            curSenseInfo.copy(sense);

            Utilitarios.preverEstadoDepoisTurn(angGol, posAtual, vel, bodyDirection(), jogador.direction.getDirection(), sense);
            if (posAtual.distanceTo(bolaPred) < 0.85 * Utilitarios.raioChute()) {
                turnBodyParaPonto(jogador.getPontoGolOponente(), 1);
            }
        }

        chutarBolaProximaAoCorpo(angulo, 0.1);
    }

    public void passeDireto(Ponto posAlvo, boolean normal) {
        if (normal) {
            chutarPara(posAlvo, Configuracoes.VELOCIDADE_FINAL_PASSE);
        } else {
            chutarPara(posAlvo, Configuracoes.VELOCIDADE_FINAL_PASSE_RAPIDO);
        }
    }

    public boolean colidirComABola() {
        Objeto bola = this.getOrCreate(Bola.ID);

        if (bola.curInfo.distance > bola.velocity().magnitude() + Configuracoes.JOGADOR_PARAMS.PLAYER_SPEED_MAX) {
            return false;
        }

        Ponto posBolaPred = Utilitarios.preverPosBolaDepoisNCiclos(bola, 1);
        Ponto posGlobalAux = new Ponto(jogador.position.getPosicao());
        Vetor vet = new Vetor();       // Usado apenas para acrescentar no ponto global;
        vet.setCoordPolar(0, 1);
        posGlobalAux.mais(vet.asPoint());

        Ponto posAgentePred = new Ponto(jogador.position.getPosicao());
        Vetor vel = new VetorVelocidade(jogador.velocity());
        SenseInfo sense = new SenseInfo();
        curSenseInfo.copy(sense);

        Utilitarios.preverEstadoDepoisTurn(calcularAnguloParaTurn(posGlobalAux, 1), posAgentePred, vel, bodyDirection(), jogador.direction.getDirection(), sense);

        if (posAgentePred.distanceTo(posBolaPred) < Configuracoes.BOLA_PARAMS.BALL_SIZE + Configuracoes.JOGADOR_PARAMS.PLAYER_SIZE) {
            turnBodyParaPonto(posGlobalAux, 1);
            return true;
        }

        posAgentePred = new Ponto(jogador.position.getPosicao());
        vel = new VetorVelocidade(jogador.velocity());
        curSenseInfo.copy(sense);
        Utilitarios.preverEstadoDepoisDoDash(posAgentePred, vel, 100, ciclo, sense, bodyDirection());

        if (posAgentePred.distanceTo(posBolaPred) < Configuracoes.BOLA_PARAMS.BALL_SIZE + Configuracoes.JOGADOR_PARAMS.PLAYER_SIZE) {
            turnBodyParaPonto(posGlobalAux, 1);
            return true;
        }

        return false;
    }

    public void turnComABola() {
        Objeto bola = this.getOrCreate(Bola.ID);
        Ponto posGlobal = new Ponto(jogador.position.getPosicao());
        Ponto posBola = new Ponto(bola.position.getPosicao());
        double direcBody = bodyDirection();
        Jogador adverPerto = adversarioMaisProximoDoObjeto(jogador);
        double distancia = Utilitarios.distanciaEntre2Objetos(adverPerto, jogador);
    }

    public Ponto getPontoDeIntersecaoBola() {
        Objeto bola = this.getOrCreate(Bola.ID);
        Ponto posAgente = new Ponto(jogador.position.getPosicao());
        VetorVelocidade vel = new VetorVelocidade(jogador.velocity());
        double dSpeed, dDistExtra;
        Ponto posMe;
        Ponto posBall = null;
        double ang, angBody, angNeck;
        SenseInfo sta = new SenseInfo();
        double dMaxDist;

        dMaxDist = Utilitarios.raioChute();
        dSpeed = jogador.velocidade.magnitude();
        dDistExtra = Utilitarios.getSumInfGeomSeries(dSpeed, Configuracoes.JOGADOR_PARAMS.PLAYER_DECAY);
        Vetor posAux = new Vetor();
        posAux.setCoordPolar(vel.direction(), dDistExtra);
        posAgente.mais(posAux.asPoint());

        for (int i = 0; i < Configuracoes.JOGADOR_PARAMS.NUMERO_MAX_DE_CICLOS_PARA_INTERECEPTAR_BOLA; i++) {
            vel = new VetorVelocidade(jogador.velocity());
            angBody = bodyDirection();
            angNeck = jogador.direction.getDirection();
            posBall = Utilitarios.preverPosBolaDepoisNCiclos(bola, i + 1);
            posMe = new Ponto(jogador.position.getPosicao());
            Ponto aux = new Ponto(posBall);
            aux.menos(posAgente);
            ang = Math.toDegrees(aux.asVector().direction());
            ang = Utilitarios.normalizarAngulo(ang - angBody);
            curSenseInfo.copy(sta);
            int turn = 0;

            while (Math.abs(ang) > Configuracoes.ANGULO_PARA_USAR_TURN && turn < 5) {
                turn++;
                double dirBodyENeck[] = Utilitarios.preverEstadoDepoisTurn(Utilitarios.getAnguloParaTurn(ang, vel.magnitude()), posMe, vel, angBody, angNeck, sta);
                aux = new Ponto(posBall);
                aux.menos(posAgente);
                angBody = dirBodyENeck[0];
                angNeck = dirBodyENeck[1];
                ang = Math.toDegrees(aux.asVector().direction());
                ang = Utilitarios.normalizarAngulo(ang - angBody);
            }

            for (; turn < i; turn++) {
                Utilitarios.preverEstadoDepoisDoDash(posMe, vel, Configuracoes.JOGADOR_PARAMS.DASH_POWER_MAX, ciclo, sta, angBody);
            }

            if (posMe.distanceTo(posBall) < dMaxDist || (posMe.distanceTo(posAgente) > posBall.distanceTo(posAgente) + dMaxDist)) {
                return posBall;
            }

        }

        return null;
    }

    //calcula a taxa de ruído para dar um chute com precisão
    public static double getKickPowerRateAtual(Objeto bola, double headAngulo) {
        double dir_diff = Math.abs(bola.curInfo.direction + headAngulo);
        double dist = bola.curInfo.distance - Configuracoes.JOGADOR_PARAMS.PLAYER_SIZE - Configuracoes.BOLA_PARAMS.BALL_SIZE;
        return Configuracoes.JOGADOR_PARAMS.KICK_POWER_RATE * (1 - 0.25 * dir_diff / 180.0 - 0.25 * dist / Configuracoes.JOGADOR_PARAMS.KICKABLE_MARGIN);
    }
}