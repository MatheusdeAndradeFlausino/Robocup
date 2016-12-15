
/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

/**
 *
 * @author Valderlei
 */
public class Teste {
    
    public void turnBodyParaPonto( Point ponto, int ciclos ){
        Point posGlobal            = new Point(0,0);
        Vector2D vet               = ponto.asVector();
        vet.menos(posGlobal.asVector());
        double angulo              = Math.toDegrees(vet.direction());
        angulo                    -= 0;
        angulo                     = Futil.simplifyAngle(angulo);
        //angulo                     = Futil.getAnguloParaTurn(angulo, player.velocity().magnitude());
        System.out.println(angulo);
    }
    
    public void procurarBola(){
        int sinal           = 1;
        Point posBola       = new Point(1,-1);
        Point posAgente     = new Point(0,0);
        posBola.menos(posAgente);
        double angulo       = Math.toDegrees(posBola.asVector().direction());
        double anguloAgente = 0;
                
        if(true)
             sinal = ( Futil.isAnguloNoIntervalo(angulo, anguloAgente,Futil.simplifyAngle(anguloAgente+180) ) ) ? 1 : -1;
        
        //timeUltimoProcuraBola   = time;
        Vector2D angTurn        = new Vector2D();
        angTurn.setCoordPolar(Math.toRadians(Futil.simplifyAngle(anguloAgente + 60 * sinal)), 1);
                
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
    
    public Point predictPosDeOutroJogador(Jogador player, int ciclos){
        double dDirection  = 0;
        Point pontoPlayer  = new Point(0,0);
        VetorVelocidade vel = new VetorVelocidade(0,0);

        for( int i = 0; i < ciclos ; i ++ ){
            double dAcc     = 100 * Configuracoes.DASH_POWER_RATE;
            if( dAcc > 0 ){
                Vector2D aux = new Vector2D();
                aux.setCoordPolar(Math.toRadians(dDirection), dAcc);
                vel.mais(aux);
            }else{
                Vector2D aux = new Vector2D();
                aux.setCoordPolar(Math.toRadians(Futil.simplifyAngle(dDirection + 180)), Math.abs(dAcc));
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
    
    public Point getPontoDeIntersecaoBola(){
        FieldObject bola    = new MobileObject();
        bola.position.atualizar(15, 0, 1, 1);
        bola.velocidade     = new VetorVelocidade();
        bola.velocidade.setCoordPolar(Math.toRadians(-135), 1);
        
        Point posAgente     = new Point(0,0);
        VetorVelocidade vel  = new VetorVelocidade(0,0);//player velocity
        double dSpeed, dDistExtra;
        Point posMe,posBall = null;
        double ang, angBody, angNeck;
        SenseInfo sta       = new SenseInfo();
        sta.effort          = 1;
        double dMaxDist;
        
        dMaxDist        = Futil.kickable_radius();
        dSpeed          = 0;
        dDistExtra      = Futil.getSumInfGeomSeries(dSpeed, Configuracoes.JOGADOR_PARAMS.PLAYER_DECAY);
        Vector2D posAux = new Vector2D();
        posAux.setCoordPolar(vel.direction(), dDistExtra);
        posAgente.mais(posAux.asPoint());
        
        for (int i = 0; i < 30; i++) {
            vel         = new VetorVelocidade(0,0);//player velocity
            angBody     = 45;
            angNeck     = 45;
            posBall     = Futil.predictlPosBolaDepoisNCiclos(bola, i+1);
            posMe       = new Point(0,0);
            Point aux   = new Point(posBall);
            aux.menos(posAgente);
            ang         = Math.toDegrees(aux.asVector().direction());
            ang         = Futil.simplifyAngle(ang - angBody );
            int turn    = 0;
            
            while(Math.abs(ang) > 7 && turn < 5){
                turn++;
                double dirBodyENeck[] = Futil.predictEstadoAfterTurn(Futil.getAnguloParaTurn(ang, vel.magnitude()), posMe, vel, angBody, angNeck, sta);
                aux         = new Point(posBall);
                aux.menos(posAgente);
                angBody     = dirBodyENeck[0];
                angNeck     = dirBodyENeck[1];
                ang         = Math.toDegrees(aux.asVector().direction());
                ang         = Futil.simplifyAngle(ang - angBody);
            }
            
            for (; turn < i; turn++) {
                Futil.predictEstadoDepoisDoDash(posMe, vel, Configuracoes.JOGADOR_PARAMS.DASH_POWER_MAX, 1, sta, angBody);
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
        
        angleToTurn         = Futil.validarTurNeckAngle(angleToTurn, relNeck);
        System.out.println(angleToTurn);
    }
    
    public static void main(String[] args) {
        
         Point posCima = new Point(Configuracoes.LARGURA_CAMPO/2 - 0.7 * Configuracoes.LARGURA_AREA_PENALTI, -Configuracoes.ALTURA_CAMPO / 4);
         Point   posBaixo       = new Point(Configuracoes.LARGURA_CAMPO/2 - 0.7 * Configuracoes.LARGURA_AREA_PENALTI, Configuracoes.ALTURA_CAMPO / 4);
    
         System.out.println(posBaixo.render());
         System.out.println(posCima.render());
    }
    
    
}
