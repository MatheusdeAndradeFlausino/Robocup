
public abstract class Objeto {

    public SeeInfo curInfo = new SeeInfo();
    public Direcao direction = new Direcao();
    public Posicao position = new Posicao();
    public VetorVelocidade velocidade = new VetorVelocidade();
    public String id = "UNKNOWN_ID";

    public Objeto() {
    }

    public Objeto(double x, double y) {
        this.position.atualizar(x, y, 1.0, -1);
    }

    public static final Objeto criar(String id) {
        if (id.startsWith("(b")) {
            return new Bola();
        } else if (id.startsWith("(p")) {
            return new Jogador(id);
        }else if (id.startsWith("(f")) {
            return new Flag(id);
        } else if (id.startsWith("(goal")) {
            return new Gol(id);
        } else {
            System.out.println("ID INVÁLIDO");
            return null;
        }
    }
   
    public final double absoluteAngleTo(Objeto object) {
        return this.position.getPosicao().absoluteAngleTo(object.position.getPosicao());
    }

    public final double absoluteAngleTo(Ponto p) {
        return this.position.getPosicao().absoluteAngleTo(p);
    }

    public final double relativeAngleTo(Objeto object) {
        double angle = this.absoluteAngleTo(object) - this.direction.getDirection();
        return Utilitarios.normalizarAngulo(angle);
    }

    public final double relativeAngleTo(Ponto p) {
        double angle = this.absoluteAngleTo(p) - this.direction.getDirection();
        return Utilitarios.normalizarAngulo(angle);
    }

    public double distanceTo(Objeto object) {
        double dx = this.deltaX(object);
        double dy = this.deltaY(object);
        return Math.hypot(dx, dy);
    }

    public double deltaX(Objeto object) {
        double x0 = this.position.getPosicao().getX();
        double x1 = object.position.getPosicao().getX();
        return x1 - x0;
    }

    public double deltaY(Objeto object) {
        double y0 = this.position.getPosicao().getY();
        double y1 = object.position.getPosicao().getY();
        return y1 - y0;
    }

    public boolean hasBrain() {
        return false;
    }

    public boolean inRectangle(Retangulo retangulo) {
        return retangulo.contem(this);
    }

    public boolean isStationaryObject() {
        return false;
    }

    public final double relativeAngleTo(double direction) {
        double angle = direction - this.direction.getDirection();
        return Utilitarios.normalizarAngulo(angle);
    }

    public final void update(Jogador player, String info, int time) {
        boolean inferirVelocidade = false;
        //this.curInfo.copy(oldInfo);
        this.curInfo.reset();
        this.curInfo.time = time;
        String[] args = Utilitarios.extrairArgs(info);
        int offset = 0;  // indicates number of optional parameters read so far
        if (args.length >= 3 && args[args.length - 1].equals("t")) {
            this.curInfo.tackling = true;
            offset++;
        } else if (args.length >= 3 && args[args.length - 1].equals("k")) {
            this.curInfo.kicking = true;
            offset++;
        }
        if (args.length >= 3 && (args.length + offset) % 2 == 1) {
            this.curInfo.pointingDir = Double.valueOf(args[args.length - 1 - offset]);
            offset++;
        }
        switch (args.length - offset) {
            case 6:
                this.curInfo.headFacingDir = Double.valueOf(args[5]);
            case 5:
                this.curInfo.bodyFacingDir = Double.valueOf(args[4]);
            case 4:
                this.curInfo.dirChange = Double.valueOf(args[3]);
                inferirVelocidade = true;
            case 3:
                this.curInfo.distChange = Double.valueOf(args[2]);
            case 2:
                this.curInfo.direction = Double.valueOf(args[1]);
                this.curInfo.distance = Double.valueOf(args[0]);
                // Calculate this object's probable position
                if (!this.isStationaryObject()) {
                    double absDir = Math.toRadians(player.direction.getDirection() + this.curInfo.direction);
                    double dist = this.curInfo.distance;
                    double px = player.position.getX();
                    double py = player.position.getY();
                    double confidence = player.position.getConfianca(time);
                    double x = px + dist * Math.cos(absDir);
                    double y = py + dist * Math.sin(absDir);
                    this.position.atualizar(x, y, confidence, time);
                }
                break;
            case 1:
                this.curInfo.direction = Double.valueOf(args[0]);
                break;
            default:
                Log.e("Field object had " + args.length + " arguments.");
        }

        if (inferirVelocidade && !isStationaryObject()) {
            infereVelocidadedoObjeto(player);
        }
    }

    public void infereVelocidadedoObjeto(Jogador player) {

        double distance = this.curInfo.distance;
        double erx = (this.position.getX() - player.position.getX()) / distance;
        double ery = (this.position.getY() - player.position.getY()) / distance;
        double dChange = this.curInfo.dirChange;
        double distChan = this.curInfo.distChange;
        double vyo = player.velocity().getY();
        double vxo = player.velocity().getX();

        double vely = (((erx * distance * dChange * Math.PI) / 180) + (erx * erx * vyo) + (ery * ery * vyo) + (ery * distChan))
                / ((ery * ery) + (erx * erx));

        double velx = (distChan + (erx * vxo + ery * vyo) - ery * vely) / erx;

        velocidade = new VetorVelocidade(velx, vely);

    }

    public VetorVelocidade velocity() {
        return velocidade;
    }

}
