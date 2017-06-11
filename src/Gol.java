
public class Gol extends ObjetoFixo {

    public Gol(String id) {
        this.id = id;
        this.position = this.definirLocalizacao();
    }

    protected Posicao definirLocalizacao() {
        if ("(goal l)".equals(this.id)) {
            return new Posicao(-52.5, 0, 1.0, -1);
        } else if ("(goal r)".equals(this.id)) {
            return new Posicao(52.5, 0, 1.0, -1);
        }      
        return new Posicao(-1.0, -1.0, 0.0, -1);
    }
}
