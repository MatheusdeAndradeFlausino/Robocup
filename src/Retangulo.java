
import java.util.Random;

public class Retangulo {

    private double aresta_baixo = -1.0;
    private double aresta_esquerda = -1.0;
    private double aresta_direita = -1.0;
    private double aresta_cima = -1.0;

    public Retangulo(){
    }
    
    public Retangulo(double cima, double direita, double baixo, double esquerda) {
        this.aresta_cima = cima;
        this.aresta_direita = direita;
        this.aresta_baixo = baixo;
        this.aresta_esquerda = esquerda;
    }

    public boolean contem(Objeto objeto) {
        double x = objeto.position.getPosicao().getX();
        double y = objeto.position.getPosicao().getY();
        return x >= aresta_esquerda && x <= aresta_direita && y <= aresta_baixo && y >= aresta_cima;
    }
    
    public boolean contem(Ponto ponto) {
        if(ponto == null)
            return false;
        
        double x = ponto.getX();
        double y = ponto.getY();
        return x >= aresta_esquerda && x <= aresta_direita && y <= aresta_baixo && y >= aresta_cima;
    }

    public Ponto getCentro() {
        double centerX = (aresta_esquerda + aresta_direita) / 2;
        double centerY = (aresta_baixo + aresta_cima) / 2;
        return new Ponto(centerX, centerY);
    }

    
    public Ponto gerarPontoDentroDoRetangulo() {
        double x, y, margem;
        Random r = new Random();
        Double margemY = Math.abs(this.getAresta_baixo()- this.getAresta_cima());
        Double margemX = Math.abs(this.getAresta_esquerda()- this.getAresta_direita());

        y = getAresta_baixo()- r.nextInt(margemY.intValue());
        x = getAresta_esquerda()+ r.nextInt(margemX.intValue());
        
        return new Ponto(x,y);
    }

    public double getAresta_baixo() {
        return aresta_baixo;
    }

    public void setAresta_baixo(double aresta_baixo) {
        this.aresta_baixo = aresta_baixo;
    }

    public double getAresta_esquerda() {
        return aresta_esquerda;
    }

    public void setAresta_esquerda(double aresta_esquerda) {
        this.aresta_esquerda = aresta_esquerda;
    }

    public double getAresta_direita() {
        return aresta_direita;
    }

    public void setAresta_direita(double aresta_direita) {
        this.aresta_direita = aresta_direita;
    }

    public double getAresta_cima() {
        return aresta_cima;
    }

    public void setAresta_cima(double aresta_cima) {
        this.aresta_cima = aresta_cima;
    }   
    
}
