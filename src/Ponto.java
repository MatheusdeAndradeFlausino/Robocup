public class Ponto {
    private double x;
    private double y;
    
    public Ponto() {
        x = Double.NaN;
        y = Double.NaN;
    }
    
    public Ponto(Ponto point) {
        this.x = point.getX();
        this.y = point.getY();
        
        
        
    }
    public Ponto(double x, double y) {
        this.x = x;
        this.y = y;
    }
    public final static Ponto Unknown() {
        return new Ponto(Double.NaN, Double.NaN);
    }

    public final double absoluteAngleTo(Ponto otherPoint) {
        double dx = this.deltaX(otherPoint);
        double dy = this.deltaY(otherPoint);
        if (dx == 0.0) {
            if (dy >= 0.0) {
                return 90.0;
            }
            else {
                return -90.0;
            }
        }
        double angle = Math.toDegrees(Math.atan(dy/dx));
        if (dx > 0) {
            return angle;
        }
        else {
            return 180.0 + angle;
        }
    }

    public final Vetor asVector() {
        return new Vetor(this.x, this.y);
    }

    public final boolean isUnknown() {
        return Double.isNaN(this.x) || Double.isNaN(this.y);
    }

    public void atualizar(double x, double y) {
        this.x = x;
        this.y = y;
    }
    
    public void update(Ponto point) {
        this.x = point.getX();
        this.y = point.getY();
    }
    
    public double getX() {
        return this.x;
    }

    public double getY() {
        return this.y;
    }
    
    public void setX(double x){
        this.x = x;
    }
    
    public void setY(double y){
        this.y = y;
    }
    
    public boolean isEqual(Ponto otherPoint) {
        return this.getX() == otherPoint.getX() && this.getY() == otherPoint.getY();
    }

    public double deltaX(Ponto otherPoint) {
        return otherPoint.getX() - this.getX();
    }
    
    public double deltaY(Ponto otherPoint) {
        return otherPoint.getY() - this.getY();
    }

    public double distanceTo(Ponto otherPoint) {
        return Math.hypot(this.deltaX(otherPoint), this.deltaY(otherPoint));
    }
    
    public final Ponto midpointTo(Ponto p) {
        return new Ponto( (this.x + p.getX()) / 2.0, (this.y + p.getY()) / 2.0);
    }
    

    public String render() {
        return String.format("(%f, %f)", this.x, this.y);
    }
    
    public final void mais(Ponto vet){
        this.x += vet.getX();
        this.y += vet.getY();
    }
    
    public final void menos(Ponto vet){
        this.x -= vet.getX();
        this.y -= vet.getY();
    }
    
    public final void vezesEscalar(double escalar){
        this.x *= escalar;
        this.y *= escalar;
    }
    
    public boolean aEsquerda( Ponto p ){
      return this.getY() < p.getY();
    }
    
    public boolean aDireita( Ponto p ){
      return this.getY() > p.getY();
    }
    
  
}
