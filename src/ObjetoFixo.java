
public class ObjetoFixo extends Objeto {

    public ObjetoFixo() {
    }

    public ObjetoFixo(String id, double x, double y) {
        if (!Utilitarios.formatado(id)) {
            Log.e("id sent to stationary object constructor: " + id);
            return;
        }
        this.id = id;
        this.position = new Posicao(x, y, 1.0, -1);
    }

    public boolean isStationaryObject() {
        return true;
    }
}
