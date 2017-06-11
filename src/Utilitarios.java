
import java.util.LinkedList;
import java.util.List;

public final class Utilitarios {    

    /**
     * Método para rotacionar um ponto --MELHORAR ESTE MÉTODO
     *
     * @param pontoPrimitivo o ponto a ser rotacionado
     * @param angulo o ângulo (em graus) para realizar a rotação
     * @return um novo ponto rotacionado (angulo)º em relação ao ponto
     * primitivo;
     */
    public static Ponto rotacionarPonto(Ponto pontoPrimitivo, double angulo) {
        return new Ponto(pontoPrimitivo.getX() * Math.cos(Math.toRadians(angulo)) - pontoPrimitivo.getY() * Math.sin(Math.toRadians(angulo)),
                pontoPrimitivo.getX() * Math.sin(Math.toRadians(angulo)) + pontoPrimitivo.getY() * Math.cos(Math.toRadians(angulo)));
    }

    public static double distanciaEntre2Objetos(Objeto objeto1, Objeto objeto2) {
        double anguloRadiano;

        if (objeto2 == null || objeto1 == null) {
            return 1000d;
        }

        anguloRadiano = Math.toRadians(Math.abs(objeto1.curInfo.direction - objeto2.curInfo.direction));

        return Math.sqrt(Math.pow(objeto1.curInfo.distance, 2) + Math.pow(objeto2.curInfo.distance, 2)
                - 2 * objeto1.curInfo.distance * objeto2.curInfo.distance * Math.cos(anguloRadiano));
    }

    public static final int extrairCiclo(String s) {
        if (!formatado(s)) {
            return -1;
        }
        if (!s.startsWith("(see ") && !s.startsWith("(sense_body ")) {
            return -1;
        }
        String cicloArg = s.split("\\s")[1];
        if (cicloArg.endsWith(")")) {
            cicloArg = cicloArg.substring(0, cicloArg.length() - 1);  // remove ')'
        }
        return Integer.parseInt(cicloArg);
    }

    public static final int parentesesFechando(String s, int indexInicio) {
        if (!formatado(s)) {
            return -1;
        }
        if (s.charAt(indexInicio) != '(') {
            return -1;
        }
        int numParentesAberto = 0;
        int numParentesesAbertoComeco = -1;
        for (int i = 0; i < s.length(); i++) {
            if (s.charAt(i) == '(') {
                if (i == indexInicio) {
                    numParentesesAbertoComeco = numParentesAberto;
                }
                numParentesAberto++;
            } else if (s.charAt(i) == ')') {
                numParentesAberto--;
                if (numParentesAberto == numParentesesAbertoComeco) {
                    return i;
                }
            }
        }
       
        return -1;
    }

    public static final boolean objetoUnico(String id) {
        return !(id.startsWith("(F") || id.startsWith("(G") || id.startsWith("(P") || id.startsWith("(l"));
    }

    public static final boolean formatado(String s) {
        int numParantesesAberto = 0;
        if (s.charAt(0) != '(') {
            return false;
        }
        if (s.charAt(s.length() - 1) != ')') {
            return false;
        }
        for (int i = 0; i < s.length(); i++) {
            if (s.charAt(i) == '(') {
                numParantesesAberto++;
            } else if (s.charAt(i) == ')') {
                numParantesesAberto--;
                if (numParantesesAberto < 0) {
                    return false;
                }
            }
        }
        if (numParantesesAberto != 0) {
            Log.e("String has unequal number of parentheses: " + s);
            return false;
        }
        return true;
    }

    public static final String limpar(String s) {
        s = s.trim();
        int indexInicio = 0;
        int indexFim = s.length();
        while (s.charAt(indexFim - 1) != ')') {
            indexFim--;
        }
        while (s.charAt(indexInicio) != '(') {
            indexInicio++;
        }
        String result = s.substring(indexInicio, indexFim);
        if (!Utilitarios.formatado(result)) {
            return "";
        }
        return result;
    }

    /**
     * Método que calcula o angulo entre 2 catetos usando a fórmula tg x =
     * catetoOposto /catetoAdjacente
     *
     * @param catetoAdjacente o comprimento do cateto adjacente
     * @param catetoOposto o comprimento do catetoOposto
     * @return o valor do ângulo em graus
     */
    public static final double anguloEntre2Catetos(double catetoAdjacente, double catetoOposto) {
        return Math.toDegrees(Math.atan((catetoOposto / catetoAdjacente)));
    }

    public static final boolean isFlag(String id) {
        return id.startsWith("(f");
    }

    public static final LinkedList<String> stringToList(String s) {
        int numOpenParens = 0;
        int indexInicio = -1;
        int indexFim = -1;
        LinkedList<String> objects = new LinkedList<>();
        if (!formatado(s)) {
            return objects;
        }
        for (int i = 0; i < s.length(); i++) {
            if (s.charAt(i) == '(') {
                numOpenParens++;
                if (numOpenParens == 1) {
                    indexInicio = i;
                }
            } else if (s.charAt(i) == ')') {
                numOpenParens--;
                if (numOpenParens == 0) {
                    indexFim = i + 1;
                    objects.add(s.substring(indexInicio, indexFim));
                }
            }
        }
        return objects;
    }

    public static double validarTurNeckAngle(double angulo, double neckAngulo) {
        double head = neckAngulo;
        if (Math.abs(head + angulo) > 90) {
            if (angulo < 0) {
                return -90 - head;
            } else {
                return 90 - head;
            }
        }
        return angulo;
    }

    public static final double normalizarAngulo(double angulo) {
        while (angulo > 180.0) {
            angulo -= 360.0;
        }
        while (angulo < -180.0) {
            angulo += 360.0;
        }
        return angulo;
    }

    public static final double validarAnguloCorpo(double angulo) {
        angulo = normalizarAngulo(angulo);
        if (angulo > Configuracoes.JOGADOR_PARAMS.MOMENT_MAX) {
            angulo = Configuracoes.JOGADOR_PARAMS.MOMENT_MAX;
        } else if (angulo < Configuracoes.JOGADOR_PARAMS.MOMENT_MIN) {
            angulo = Configuracoes.JOGADOR_PARAMS.MOMENT_MIN;
        }
        return angulo;
    }

    public static final double validarAnguloCabeca(double angulo) {
        angulo = normalizarAngulo(angulo);
        if (angulo > Configuracoes.JOGADOR_PARAMS.NECK_ANGLE_MAX) {
            angulo = Configuracoes.JOGADOR_PARAMS.NECK_ANGLE_MAX;
        } else if (angulo < Configuracoes.JOGADOR_PARAMS.NECK_ANGLE_MIN) {
            angulo = Configuracoes.JOGADOR_PARAMS.NECK_ANGLE_MIN;
        }
        return angulo;
    }

    public static double raioChute() {
        return (Configuracoes.JOGADOR_PARAMS.PLAYER_SIZE
                + Configuracoes.JOGADOR_PARAMS.KICKABLE_MARGIN
                + Configuracoes.BOLA_PARAMS.BALL_SIZE);
    }

    public static double getPowerParaDash(Ponto ponto, double angulo, Vetor velocidade, double effort, int ciclos) {
        double dist = ponto.asVector().rotate(-angulo).getX();
        if (ciclos <= 0) {
            ciclos = 1;
        }
        double dAcc = getFirstSpeedFromDist(dist, ciclos, Configuracoes.JOGADOR_PARAMS.PLAYER_DECAY);

        if (dAcc > Configuracoes.PLAYER_SPEED_MAX) {
            dAcc = Configuracoes.PLAYER_SPEED_MAX;
        }

        dAcc -= velocidade.rotate(-angulo).getX();

        double dashPower = dAcc / (Configuracoes.DASH_POWER_RATE * effort);

        if (dashPower > Configuracoes.JOGADOR_PARAMS.DASH_POWER_MAX) {
            dashPower = Configuracoes.JOGADOR_PARAMS.DASH_POWER_MAX;
        }
        if (dashPower < Configuracoes.JOGADOR_PARAMS.DASH_POWER_MIN) {
            dashPower = Configuracoes.JOGADOR_PARAMS.DASH_POWER_MIN;
        }

        return dashPower;
    }

    public static Ponto preverPosBolaDepoisNCiclos(Objeto o, int nCiclos) {
        Ponto result;
        Ponto p = new Ponto(o.position.getPosicao());

        double dDist = getSumGeomSeries(o.velocity().magnitude(), Configuracoes.BOLA_PARAMS.BALL_DECAY, nCiclos);
        Vetor vet = new Vetor();

        vet.setCoordPolar(o.velocity().direction(), dDist);
        vet.mais(p.asVector());
        result = vet.asPoint();

        return result;
    }

    public static Posicao preverAgentePosDepoisNCiclos(Objeto o, double dashPower, int nCiclos, int time, SenseInfo info) {
        Posicao resultPos = new Posicao();
        SenseInfo stamina = info;
        double dir = o.direction.getDirection();
        Ponto p = new Ponto(o.position.getPosicao());
        VetorVelocidade vel = new VetorVelocidade(o.velocity());

        for (int i = 0; i < nCiclos; i++) {
            preverEstadoDepoisDoDash(p, vel, dashPower, time, stamina, dir);
        }

        resultPos.atualizar(p, Math.pow(0.95, nCiclos), time + nCiclos);

        return resultPos;
    }

    public static double getAnguloParaTurn(double anguloDesejado, double velocidade) {
        double angulo = anguloDesejado * (1.0 + Configuracoes.VARIACAO_DE_INERCIA * velocidade);

        if (angulo > Configuracoes.JOGADOR_PARAMS.TURN_MAX_MOMENT) {
            angulo = Configuracoes.JOGADOR_PARAMS.TURN_MAX_MOMENT;
        } else if (angulo < Configuracoes.JOGADOR_PARAMS.TURN_MIN_MOMENT) {
            angulo = Configuracoes.JOGADOR_PARAMS.TURN_MIN_MOMENT;
        }

        return angulo;
    }

    public static double getAtualTurnAngulo(double angTurn, double dSpeed) {
        return angTurn / (1.0 + Configuracoes.VARIACAO_DE_INERCIA * dSpeed);
    }

    public static boolean isValorConfiancaAceitavel(double confianca) {
        return confianca > 0.85;
    }

    public static Objeto maisProximoDoObjeto(List<Jogador> objetos, Objeto objeto) {
        Jogador maisProximo = null;
        double menorDistancia = 1000d;
        Ponto p;

        for (Jogador player : objetos) {
            if (!player.equals(objeto)) {
                p = player.position.getPosicao();
                p.menos(objeto.position.getPosicao());
                if (p.asVector().magnitude() < menorDistancia) {
                    maisProximo = player;
                    menorDistancia = p.asVector().magnitude();
                }
            }
        }

        return maisProximo;
    }

    public static boolean souOMaisProximoDoObjeto(List<Jogador> objetos, Objeto objeto, double distancia) {
        double menorDistancia = distancia;
        Ponto p;

        for (Jogador player : objetos) {
            if (!player.equals(objeto)) {
                p = new Ponto(player.position.getPosicao());
                p.menos(objeto.position.getPosicao());
                if (p.asVector().magnitude() < menorDistancia) {
                    menorDistancia = p.asVector().magnitude();
                }
            }
        }

        return menorDistancia == distancia;
    }

    public static Objeto maisRapidoAteObjeto(List<Jogador> todosPlayerNaVista, Objeto bola) {
        return null;
    }

    public static boolean isNossaPosseDeBola(List<Jogador> todosPlayerNaVista, Objeto bola, String time) {
        Objeto maisRapido = maisProximoDoObjeto(todosPlayerNaVista, bola);

        return (maisRapido != null)
                ? maisRapido.id.startsWith("(p \"") && (maisRapido.id.startsWith(time, 4))
                : false;
    }

    //Os metodos de previsão podem ser melhorados
    public static Ponto preverPosDeOutroJogador(Jogador player, int ciclos) {
        double dDirection = player.velocity().direction();
        Ponto pontoPlayer = new Ponto(player.position.getPosicao());
        VetorVelocidade vel = new VetorVelocidade(player.velocity());

        for (int i = 0; i < ciclos; i++) {
            double dAcc = vel.magnitude();
            if (dAcc > 0) {
                Vetor aux = new Vetor();
                aux.setCoordPolar(Math.toRadians(dDirection), dAcc);
                vel.mais(aux);
            } else {
                Vetor aux = new Vetor();
                aux.setCoordPolar(Math.toRadians(normalizarAngulo(dDirection + 180)), Math.abs(dAcc));
                vel.mais(aux);
            }

            if (vel.magnitude() > Configuracoes.JOGADOR_PARAMS.PLAYER_SPEED_MAX) {
                vel = new VetorVelocidade(Configuracoes.JOGADOR_PARAMS.PLAYER_SPEED_MAX);
            }

            pontoPlayer.mais(vel.asPoint());
            // p.setX(pos.getX());
            // p.setY(pos.getY());

            vel.vezesEscalar(Configuracoes.JOGADOR_PARAMS.PLAYER_DECAY);

        }
        return pontoPlayer;
    }

    public static void preverEstadoDepoisDoDash(Ponto p, Vetor vel, double dashPower, int time, SenseInfo info, double dir) {
        double effort = info.effort;
        double dAcc = dashPower * Configuracoes.DASH_POWER_RATE * effort;
        Vetor pos = p.asVector();

        if (dAcc > 0) {
            Vetor aux = new Vetor();
            aux.setCoordPolar(Math.toRadians(dir), dAcc);
            vel.mais(aux);
        } else {
            Vetor aux = new Vetor();
            aux.setCoordPolar(Math.toRadians(normalizarAngulo(dir + 180)), Math.abs(dAcc));
            vel.mais(aux);
        }

        if (vel.magnitude() > Configuracoes.JOGADOR_PARAMS.PLAYER_SPEED_MAX) {
            vel = new VetorVelocidade(Configuracoes.JOGADOR_PARAMS.PLAYER_SPEED_MAX);
        }

        preverStaminaDepoisDoDash(dashPower, info);

        pos.mais(vel);
        p.setX(pos.getX());
        p.setY(pos.getY());

        vel.vezesEscalar(Configuracoes.JOGADOR_PARAMS.PLAYER_DECAY);
    }

    public static double getXImpedimento(List<Jogador> jogadoresInimigos, Objeto bola) {
        double maiorX = 0;

        for (Jogador player : jogadoresInimigos) {
            if (player.position.getPosicao().getX() > maiorX && !player.id.contains("goalie")) {
                maiorX = player.position.getPosicao().getX();
            }
        }

        double x = bola.position.getX();
        x = Math.max(x, maiorX);

        return x;
    }

    public static boolean isGoalKick(String playMode, char lado) {
        return playMode.equals("goal_kick_" + lado);
    }

    public static boolean isBeforeKickOff(String playMode) {
        return playMode.equals("before_kick_off");
    }

    public static boolean isOffside(String playMode, char lado) {
        return playMode.equals("offside_" + lado);
    }

    public static boolean isFreeKick(String playMode, char lado) {
        return playMode.equals("free_kick_" + lado);
    }

    public static boolean isCornerKick(String playMode, char lado) {
        return playMode.equals("corner_kick_" + lado);
    }

    public static boolean isKickIn(String playMode, char lado) {
        return playMode.equals("kick_in_" + lado);
    }

    public static boolean isKickOff(String playMode, char lado) {
        return playMode.equals("kick_off_" + lado);
    }

    public static void preverStaminaDepoisDoDash(double dashPower, SenseInfo info) {
        double stamina = info.stamina;
        double effort = info.effort;

        stamina -= (dashPower > 0.0) ? dashPower : -2 * dashPower;

        if (stamina < 0) {
            stamina = 0;
        }

        if (stamina <= Configuracoes.JOGADOR_PARAMS.D_EFFORT_DEC_THR * Configuracoes.JOGADOR_PARAMS.STAMINA_MAX
                && effort > Configuracoes.JOGADOR_PARAMS.D_EFFORT_MIN) {
            effort -= Configuracoes.JOGADOR_PARAMS.D_EFFORT_DEC;
        }

        if (stamina >= Configuracoes.JOGADOR_PARAMS.D_EFFORT_INC_THR * Configuracoes.JOGADOR_PARAMS.STAMINA_MAX && effort < 1.0) {
            effort += Configuracoes.JOGADOR_PARAMS.D_EFFORT_INC;
            if (effort > 1.0) {
                effort = 1.0;
            }
        }

        stamina += 0.6 * Configuracoes.JOGADOR_PARAMS.STAMINA_INC_MAX;
        if (stamina > Configuracoes.JOGADOR_PARAMS.STAMINA_MAX) {
            stamina = Configuracoes.JOGADOR_PARAMS.STAMINA_MAX;
        }

        info.effort = effort;
        info.stamina = stamina;
    }

    public static Ponto preverPosFinalAgente(Ponto pos, Vetor vel) {
        Ponto posAgente = pos;
        Vetor velAgn = vel;

        double dist = getSumInfGeomSeries(velAgn.magnitude(), Configuracoes.JOGADOR_PARAMS.PLAYER_DECAY);
        Vetor vetAux = new Vetor();
        vetAux.setCoordPolar(vel.direction(), dist);
        posAgente.mais(vetAux.asPoint());

        return posAgente;
    }

    public static double[] preverEstadoDepoisTurn(double dSendAngle, Ponto pos, Vetor vel, double angBody, double angNeck, SenseInfo sta) {
        double dEffectiveAngle;
        dEffectiveAngle = getAtualTurnAngulo(dSendAngle, vel.magnitude());

        angBody = Utilitarios.normalizarAngulo(angBody + dEffectiveAngle);
        angNeck = Utilitarios.normalizarAngulo(angNeck + dEffectiveAngle);

        preverEstadoDepoisDoDash(pos, vel, 0, sta.time, sta, angBody);

        double[] result = new double[2];
        result[0] = angBody;
        result[1] = angNeck;

        return result;
    }

    public static double getBisectorDe2Angulos(double ang1, double ang2) {
        double senos = (Math.toDegrees(Math.sin(ang1)) + Math.toDegrees(Math.sin(ang2))) / 2;
        double cossenos = (Math.toDegrees(Math.cos(ang1)) + Math.toDegrees(Math.cos(ang2))) / 2;
        double result = Math.toDegrees(Math.atan2(senos, cossenos));
        return normalizarAngulo(result);
    }

    public static double getForcaParaAtravessar(double distancia, double velFinal) {

        if (velFinal < 0.0001) {
            return getFirstInfGeomSeries(distancia, Configuracoes.BOLA_PARAMS.BALL_DECAY);
        }

        double ciclos = getLengthGeomSeries(velFinal, 1.0 / Configuracoes.BOLA_PARAMS.BALL_DECAY, distancia);
        return getForcaInicialParaVeloFinal(velFinal, (int) Math.rint(ciclos), -1);
    }

    public static double getFirstSpeedFromDist(double dist, int ciclos, double decay) {
        if (decay < 0) {
            decay = Configuracoes.BOLA_PARAMS.BALL_DECAY;
        }

        return getFirstGeomSeries(dist, decay, ciclos);
    }

    //Calcula a força inicial necesária para que a bola chegue no determinado ponto com vel final desejada.
    public static double getForcaInicialParaVeloFinal(double velFinal, double ciclos, double decay) {
        if (decay < 0) {
            decay = Configuracoes.BOLA_PARAMS.BALL_DECAY;
        }

        // geometric serie: s = a + a*r^1 + .. + a*r^n, now given endspeed = a*r^n ->
        // endspeed = firstspeed * ratio ^ length ->
        // firstpeed = endspeed * ( 1 / ratio ) ^ length
        return velFinal * Math.pow(1 / decay, ciclos);
    }

    public static double getFirstGeomSeries(double dSum, double dRatio, double dLength) {
        return dSum * (1 - dRatio) / (1 - Math.pow(dRatio, dLength));
    }

    public static double getSumGeomSeries(double dFirst, double dRatio, double dLength) {
        return dFirst * (1 - Math.pow(dRatio, dLength)) / (1 - dRatio);
    }

    public static double getFirstInfGeomSeries(double dSum, double dRatio) {
        if (dRatio > 1) {
            System.out.println("SÉRIE NÃO CONVERGE");
        }

        // s = a(1-r^n)/(1-r) with r->inf and 0<r<1 => r^n = 0 => a = s ( 1 - r)
        return dSum * (1 - dRatio);
    }

    public static double getLengthGeomSeries(double dFirst, double dRatio, double dSum) {
        if (dRatio < 0) {
            System.out.println("ratio negativo");
        }

        // s = a + ar + ar^2 + .. + ar^n-1 and thus sr = ar + ar^2 + .. + ar^n
        // subtract: sr - s = - a + ar^n) =>  s(1-r)/a + 1 = r^n = temp
        // log r^n / n = n log r / log r = n = length
        double temp = (dSum * (dRatio - 1) / dFirst) + 1;
        if (temp <= 0) {
            return -1.0;
        }
        return Math.log(temp) / Math.log(dRatio);
    }

    public static double getSumInfGeomSeries(double dFirst, double dRatio) {
        if (dRatio > 1) {
            System.out.println("(Geometry:CalcLengthGeomSeries): series does not converge");
        }

        // s = a(1-r^n)/(1-r) with n->inf and 0<r<1 => r^n = 0
        return dFirst / (1 - dRatio);
    }

    public static double arcSenGraus(double x) {
        if (x >= 1) {
            return (90.0);
        } else if (x <= -1) {
            return (-90.0);
        }

        return (Math.toDegrees(Math.asin(x)));
    }

    public static Vetor somaVetores(Vetor vet1, Vetor vet2) {
        Vetor result = new Vetor();
        result.setX(vet1.getX() + vet2.getX());
        result.setY(vet1.getY() + vet2.getY());
        return result;
    }

    public static boolean isBolaParadaParaNos(String playMode, char lado, char ladoInimigo) {
        return (playMode.equals("kick_in_" + lado)
                || playMode.equals("kick_off_" + lado)
                || playMode.equals("offside_" + ladoInimigo)
                || playMode.equals("free_kick_" + lado)
                || playMode.equals("corner_kick_" + lado)
                || playMode.equals("goal_kick_" + lado)
                || playMode.equals("free_kick_fault_" + ladoInimigo)
                || playMode.equals("back_pass_" + ladoInimigo));
    }

    public static boolean isBolaParadaParaEles(String playMode, char lado, char ladoInimigo) {
        return (playMode.equals("kick_in_" + ladoInimigo)
                || playMode.equals("kick_off_" + ladoInimigo)
                || playMode.equals("offside_" + lado)
                || playMode.equals("free_kick_" + ladoInimigo)
                || playMode.equals("corner_kick_" + ladoInimigo)
                || playMode.equals("goal_kick_" + ladoInimigo)
                || playMode.equals("back_pass_" + lado)
                || playMode.equals("free_kick_fault_" + lado));
    }

    public static void preverInfoBolaDepoisComando(String comando, Vetor pos, VetorVelocidade vel, double power, double angulo, double anguloGlobal, double kickRate) {
        Vetor posBola = pos;
        VetorVelocidade velBola = vel;

        if (comando.equals("kick")) {
            double ang = Utilitarios.normalizarAngulo(angulo + anguloGlobal); // graus
            Vetor nAux = new Vetor();
            nAux.setCoordPolar(Math.toRadians(ang), kickRate * power);
            velBola.mais(nAux);

            if (velBola.magnitude() > Configuracoes.BOLA_PARAMS.BALL_SPEED_MAX) {
                velBola.setMagnitude(Configuracoes.BOLA_PARAMS.BALL_SPEED_MAX);
            }

        }

        posBola.mais(velBola);
        velBola.vezesEscalar(Configuracoes.BOLA_PARAMS.BALL_DECAY);

        pos.setX(posBola.getX());
        pos.setY(posBola.getY());

        vel.setX(velBola.getX());
        vel.setY(velBola.getY());
    }

    public static boolean isAnguloNoIntervalo(double ang, double angMin, double angMax) {
        if ((ang + 360) < 360) {
            ang += 360;
        }
        if ((angMin + 360) < 360) {
            angMin += 360;
        }
        if ((angMax + 360) < 360) {
            angMax += 360;
        }

        if (angMin < angMax) {
            return angMin < ang && ang < angMax;
        } else {
            return !(angMax < ang && ang < angMin);
        }
    }
    
    public static final String[] extrairArgs(String info) {
        if (!formatado(info)) {
            return new String[]{};
        }
        int indexInicio = 2 + extrairId(info).length();
        int indexFim = info.length() - 1;
        return info.substring(indexInicio, indexFim).split("\\s");
    }

    public static final LinkedList<String> extrairInfos(String see) {
        int indexInicio = see.indexOf("((");
        int indexFim = see.length() - 1;
        if (indexInicio == -1) {
            return new LinkedList<>();
        }
        return stringToList(see.substring(indexInicio, indexFim));
    }

    public static final String extrairId(String info) {
        if (!formatado(info)) {
            return "UNKNOWN";
        }
        int indexInicio = 1;
        int indexFim = parentesesFechando(info, 1);
        String result = info.substring(indexInicio, indexFim + 1);

        if (result.equals("(B)")) {
            result = "(b)";
        }
        return result;
    }

}
