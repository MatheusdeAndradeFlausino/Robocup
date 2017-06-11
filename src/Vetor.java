
public class Vetor {
    private double x;
    private double y;
    private final double EPISLON = 0.0001;
  
    public Vetor() {
        this.reset();
    }
   
    public Vetor(double magnitude) {
        this.x = magnitude;
    }
    
    public Vetor(Vetor vec) {
        this.x = vec.getX();
        this.y = vec.getY();
    }
 
    public Vetor(double x, double y) {
        this.x = x;
        this.y = y;
    }

    public static Vetor ZeroVector() {
        return new Vetor(0.0, 0.0);
    }
  
    public final double getX() {
        return this.x;
    }
   
    protected final void setX(double x) {
        this.x = x;
    }
   
    public final double getY() {
        return this.y;
    }
    
    protected final void setY(double y) {
        this.y = y;
    }
  
    public final double magnitude() {
        return Math.hypot(x, y);
    }
    
    public final void setCoordPolar(double dir, double mag){
        this.x = mag * Math.cos(dir);
        this.y = mag * Math.sin(dir);
    }
    
    public final Vetor rotate(double angulo){
        double mag = this.magnitude();
        double dir = this.direction() + angulo;
        this.setCoordPolar(dir, mag);
        return this;
    }
    
    public final void setMagnitude(double mag){
        if( magnitude() > EPISLON)
            this.vezesEscalar(mag / magnitude());
    }
    
    public final Vetor addPolar(double dir, double mag) {
        double x = mag * Math.cos(dir);
        double y = mag * Math.sin(dir);
        return new Vetor(x, y).add(this);
    }
    
    public final Ponto asPoint(){
        return new Ponto(this.x, this.y);
    }
    
    public final double direction() {
        return Math.atan2(this.y, this.x);
    }
   
    public final Vetor add(Vetor that) {
        return new Vetor(this.x + that.getX(), this.y + that.getY());
    }
    
    public final void mais(Vetor vet){
        this.x += vet.getX();
        this.y += vet.getY();
    }
    
    public final void menos(Vetor vet){
        this.x -= vet.getX();
        this.y -= vet.getY();
    }
    
    public final void vezesEscalar(double escalar){
        this.x *= escalar;
        this.y *= escalar;
    }
    
    public void reset() {
        this.x = Double.NaN;
        this.y = Double.NaN;
    }
}
