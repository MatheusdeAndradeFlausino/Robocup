
public class VetorVelocidade extends Vetor {

   
    public VetorVelocidade() {
        super();
    }
   
    public VetorVelocidade(double magnitude) {
        super(magnitude);
    }
    
    public VetorVelocidade(Vetor vec) {
        super(vec);
    }
    
    public VetorVelocidade(double x, double y) {
        super(x, y);
    }
    
    public final void setPolar(double dir, double mag) {
        this.setX(mag * Math.cos(dir));
        this.setY(mag * Math.sin(dir));
    }
}
