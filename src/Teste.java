public class Teste {
    
    public void turnBodyParaPonto( Ponto ponto, int ciclos ){
        Ponto posGlobal            = new Ponto(0,0);
        Vetor vet               = ponto.asVector();
        vet.menos(posGlobal.asVector());
        double angulo              = Math.toDegrees(vet.direction());
        angulo                    -= 0;
        angulo                     = Utilitarios.normalizarAngulo(angulo);
        //angulo                     = Utilitarios.getAnguloParaTurn(angulo, player.velocity().magnitude());
        System.out.println(angulo);
    }
    
    public void procurarBola(){
        int sinal           = 1;
        Ponto posBola       = new Ponto(1,-1);
        Ponto posAgente     = new Ponto(0,0);
        posBola.menos(posAgente);
        double angulo       = Math.toDegrees(posBola.asVector().direction());
        double anguloAgente = 0;
                
        if(true)
             sinal = ( Utilitarios.isAnguloNoIntervalo(angulo, anguloAgente,Utilitarios.normalizarAngulo(anguloAgente+180) ) ) ? 1 : -1;
        
        //timeUltimoProcuraBola   = time;
        Vetor angTurn        = new Vetor();
        angTurn.setCoordPolar(Math.toRadians(Utilitarios.normalizarAngulo(anguloAgente + 60 * sinal)), 1);
                
        posAgente.mais(angTurn.asPoint());
        System.out.println(sinal);
        System.out.println(angTurn.asPoint().render());
        turnBodyParaPonto(posAgente, sinal);
    }
    
    public void infereVelocidade(){
        double distance = 5;
        double erx      = 4/distance;
        double ery      = 3/distance;
        double dChange  = 8.5;
        double distChan = -2.0;
        double vyo      = 0.3;
        double vxo      = 0.5;
        
        double vely     = (((erx*distance*dChange*Math.PI)/180) + (erx*erx*vyo) + (ery*ery*vyo) + (ery*distChan))/
                ((ery*ery) + (erx*erx));
        
        double velx     = (distChan + (erx*vxo + ery*vyo) - ery*vely)/erx;
        
        double vrx      = velx - vxo;
        double vry      = vely - vyo;
        double aux      = (vrx * erx) + (vry * ery);
        double daux     = ((-(vrx * ery) + (vry * erx))/distance) * (180/Math.PI);
        
        System.out.println("vel ( " + velx + " , " + vely + " )");
        System.out.println("distChange = " + distChan + " distAux = " + aux );
        System.out.println("dirchange = " + dChange + " dirAux = " + daux);
    }
    
    public Ponto predictPosDeOutroJogador(Jogador player, int ciclos){
        double dDirection  = 0;
        Ponto pontoPlayer  = new Ponto(0,0);
        VetorVelocidade vel = new VetorVelocidade(0,0);

        for( int i = 0; i < ciclos ; i ++ ){
            double dAcc     = 100 * Configuracoes.DASH_POWER_RATE;
            if( dAcc > 0 ){
                Vetor aux = new Vetor();
                aux.setCoordPolar(Math.toRadians(dDirection), dAcc);
                vel.mais(aux);
            }else{
                Vetor aux = new Vetor();
                aux.setCoordPolar(Math.toRadians(Utilitarios.normalizarAngulo(dDirection + 180)), Math.abs(dAcc));
                vel.mais(aux);
            }

            if(vel.magnitude() > Configuracoes.JOGADOR_PARAMS.PLAYER_SPEED_MAX)
                vel = new VetorVelocidade(Configuracoes.JOGADOR_PARAMS.PLAYER_SPEED_MAX);

            pontoPlayer.mais(vel.asPoint());
           // p.setX(pos.getX());
           // p.setY(pos.getY());

            vel.vezesEscalar(Configuracoes.JOGADOR_PARAMS.PLAYER_DECAY);

        }
        return pontoPlayer;
    }
    
    public Ponto getPontoDeIntersecaoBola(){
        Objeto bola    = new ObjetoMovel();
        bola.position.atualizar(15, 0, 1, 1);
        bola.velocidade     = new VetorVelocidade();
        bola.velocidade.setCoordPolar(Math.toRadians(-135), 1);
        
        Ponto posAgente     = new Ponto(0,0);
        VetorVelocidade vel  = new VetorVelocidade(0,0);//player velocity
        double dSpeed, dDistExtra;
        Ponto posMe,posBall = null;
        double ang, angBody, angNeck;
        SenseInfo sta       = new SenseInfo();
        sta.effort          = 1;
        double dMaxDist;
        
        dMaxDist        = Utilitarios.raioChute();
        dSpeed          = 0;
        dDistExtra      = Utilitarios.getSumInfGeomSeries(dSpeed, Configuracoes.JOGADOR_PARAMS.PLAYER_DECAY);
        Vetor posAux = new Vetor();
        posAux.setCoordPolar(vel.direction(), dDistExtra);
        posAgente.mais(posAux.asPoint());
        
        for (int i = 0; i < 30; i++) {
            vel         = new VetorVelocidade(0,0);//player velocity
            angBody     = 45;
            angNeck     = 45;
            posBall     = Utilitarios.preverPosBolaDepoisNCiclos(bola, i+1);
            posMe       = new Ponto(0,0);
            Ponto aux   = new Ponto(posBall);
            aux.menos(posAgente);
            ang         = Math.toDegrees(aux.asVector().direction());
            ang         = Utilitarios.normalizarAngulo(ang - angBody );
            int turn    = 0;
            
            while(Math.abs(ang) > 7 && turn < 5){
                turn++;
                double dirBodyENeck[] = Utilitarios.preverEstadoDepoisTurn(Utilitarios.getAnguloParaTurn(ang, vel.magnitude()), posMe, vel, angBody, angNeck, sta);
                aux         = new Ponto(posBall);
                aux.menos(posAgente);
                angBody     = dirBodyENeck[0];
                angNeck     = dirBodyENeck[1];
                ang         = Math.toDegrees(aux.asVector().direction());
                ang         = Utilitarios.normalizarAngulo(ang - angBody);
            }
            
            for (; turn < i; turn++) {
                Utilitarios.preverEstadoDepoisDoDash(posMe, vel, Configuracoes.JOGADOR_PARAMS.DASH_POWER_MAX, 1, sta, angBody);
            }
            
            if (posMe.distanceTo( posBall ) < dMaxDist || (posMe.distanceTo( posAgente) > posBall.distanceTo( posAgente ) + dMaxDist) ){                
                return posBall;
            }
            
        }       
        return posBall;
    }
    
    public void turnNeckToDirection(double direction){
        double headDir      = 0;
        double angleToTurn  = direction - headDir;
        double relNeck      = 20;
        
        angleToTurn         = Utilitarios.validarTurNeckAngle(angleToTurn, relNeck);
        System.out.println(angleToTurn);
    }
    
    public Ponto getPontoDeMarcacao(Ponto pos, double dist){
        
        Ponto posBola       = new Ponto(-25,-10);
        Ponto posGol        = new Ponto(-52.5,0);
        Ponto posAgente     = new Ponto(-20,10);
        
        double angBola, angGol, ang;
        Ponto posMarcacao;
        
        Ponto aux           = posGol;
        aux.menos(posBola);
        
        ang = aux.asVector().direction();
        
        Vetor vet = new Vetor();
        vet.setCoordPolar(ang, dist);
        pos.mais(vet.asPoint());
                
        return pos;
        
       /* Ponto aux           = posBola;
        aux.menos(pos);
        angBola             = aux.asVector().direction();
        
        aux                 = posGol;
        aux.menos(pos);
        angGol              = aux.asVector().direction();
        
        ang                 = Utilitarios.getBisectorDe2Angulos(angBola, angGol);
        System.out.println(ang);
        Vetor vet        = new Vetor();
        vet.setCoordPolar(ang, dist);
        posMarcacao         = pos;
        posMarcacao.mais(vet.asPoint());*/
        
        //return posMarcacao;
    }
    
    public static void main(String[] args) {    
        Teste t = new Teste();
        Ponto res = t.getPontoDeMarcacao(new Ponto(-25,-10), 3);
        System.out.println(res.render());
                         
    }
    
    
}
