
public class Flag extends ObjetoFixo {

    public Flag(String id) {
        this.id = id;
        this.position = this.definirLocalizacao();
    }

    protected Posicao definirLocalizacao() {

        if ("(f t l 50)".equals(this.id)) {
            return new Posicao(-50, -39, 1.0, -1);
        } else if ("(f t l 40)".equals(this.id)) {
            return new Posicao(-40, -39, 1.0, -1);
        } else if ("(f t l 30)".equals(this.id)) {
            return new Posicao(-30, -39, 1.0, -1);
        } else if ("(f t l 20)".equals(this.id)) {
            return new Posicao(-20, -39, 1.0, -1);
        } else if ("(f t l 10)".equals(this.id)) {
            return new Posicao(-10, -39, 1.0, -1);
        } else if ("(f t 0)".equals(this.id)) {
            return new Posicao(0, -39, 1.0, -1);
        } else if ("(f t r 10)".equals(this.id)) {
            return new Posicao(10, -39, 1.0, -1);
        } else if ("(f t r 20)".equals(this.id)) {
            return new Posicao(20, -39, 1.0, -1);
        } else if ("(f t r 30)".equals(this.id)) {
            return new Posicao(30, -39, 1.0, -1);
        } else if ("(f t r 40)".equals(this.id)) {
            return new Posicao(40, -39, 1.0, -1);
        } else if ("(f t r 50)".equals(this.id)) {
            return new Posicao(50, -39, 1.0, -1);
        } else if ("(f r t 30)".equals(this.id)) {
            return new Posicao(57.5, -30, 1.0, -1);
        } else if ("(f r t 20)".equals(this.id)) {
            return new Posicao(57.5, -20, 1.0, -1);
        } else if ("(f r t 10)".equals(this.id)) {
            return new Posicao(57.5, -10, 1.0, -1);
        } else if ("(f r 0)".equals(this.id)) {
            return new Posicao(57.5, 0, 1.0, -1);
        } else if ("(f r b 10)".equals(this.id)) {
            return new Posicao(57.5, 10, 1.0, -1);
        } else if ("(f r b 20)".equals(this.id)) {
            return new Posicao(57.5, 20, 1.0, -1);
        } else if ("(f r b 30)".equals(this.id)) {
            return new Posicao(57.5, 30, 1.0, -1);
        } else if ("(f b r 50)".equals(this.id)) {
            return new Posicao(50, 39, 1.0, -1);
        } else if ("(f b r 40)".equals(this.id)) {
            return new Posicao(40, 39, 1.0, -1);
        } else if ("(f b r 30)".equals(this.id)) {
            return new Posicao(30, 39, 1.0, -1);
        } else if ("(f b r 20)".equals(this.id)) {
            return new Posicao(20, 39, 1.0, -1);
        } else if ("(f b r 10)".equals(this.id)) {
            return new Posicao(10, 39, 1.0, -1);
        } else if ("(f b 0)".equals(this.id)) {
            return new Posicao(0, 39, 1.0, -1);
        } else if ("(f b l 10)".equals(this.id)) {
            return new Posicao(-10, 39, 1.0, -1);
        } else if ("(f b l 20)".equals(this.id)) {
            return new Posicao(-20, 39, 1.0, -1);
        } else if ("(f b l 30)".equals(this.id)) {
            return new Posicao(-30, 39, 1.0, -1);
        } else if ("(f b l 40)".equals(this.id)) {
            return new Posicao(-40, 39, 1.0, -1);
        } else if ("(f b l 50)".equals(this.id)) {
            return new Posicao(-50, 39, 1.0, -1);
        } else if ("(f l b 30)".equals(this.id)) {
            return new Posicao(-57.5, 30, 1.0, -1);
        } else if ("(f l b 20)".equals(this.id)) {
            return new Posicao(-57.5, 20, 1.0, -1);
        } else if ("(f l b 10)".equals(this.id)) {
            return new Posicao(-57.5, 10, 1.0, -1);
        } else if ("(f l 0)".equals(this.id)) {
            return new Posicao(-57.5, 0, 1.0, -1);
        } else if ("(f l t 10)".equals(this.id)) {
            return new Posicao(-57.5, -10, 1.0, -1);
        } else if ("(f l t 20)".equals(this.id)) {
            return new Posicao(-57.5, -20, 1.0, -1);
        } else if ("(f l t 30)".equals(this.id)) {
            return new Posicao(-57.5, -30, 1.0, -1);
        } else if ("(f l t)".equals(this.id)) {
            return new Posicao(-52.5, -34, 1.0, -1);
        } else if ("(f r t)".equals(this.id)) {
            return new Posicao(52.5, -34, 1.0, -1);
        } else if ("(f r b)".equals(this.id)) {
            return new Posicao(52.5, 34, 1.0, -1);
        } else if ("(f l b)".equals(this.id)) {
            return new Posicao(-52.5, 34, 1.0, -1);
        } else if ("(f c t)".equals(this.id)) {
            return new Posicao(0, -34, 1.0, -1);
        } else if ("(f c)".equals(this.id)) {
            return new Posicao(0, 0, 1.0, -1);
        } else if ("(f c b)".equals(this.id)) {
            return new Posicao(0, 34, 1.0, -1);
        } else if ("(f p l t)".equals(this.id)) {
            return new Posicao(-36, -20.15, 1.0, -1);
        } else if ("(f p l c)".equals(this.id)) {
            return new Posicao(-36, 0, 1.0, -1);
        } else if ("(f p l b)".equals(this.id)) {
            return new Posicao(-36, 20.15, 1.0, -1);
        } else if ("(f p r t)".equals(this.id)) {
            return new Posicao(36, -20.15, 1.0, -1);
        } else if ("(f p r c)".equals(this.id)) {
            return new Posicao(36, 0, 1.0, -1);
        } else if ("(f p r b)".equals(this.id)) {
            return new Posicao(36, 20.15, 1.0, -1);
        } else if ("(f g l t)".equals(this.id)) {
            return new Posicao(-52.5, -7.01, 1.0, -1);
        } else if ("(f g l b)".equals(this.id)) {
            return new Posicao(-52.5, 7.01, 1.0, -1);
        } else if ("(f g r t)".equals(this.id)) {
            return new Posicao(52.5, -7.01, 1.0, -1);
        } else if ("(f g r b)".equals(this.id)) {
            return new Posicao(52.5, 7.01, 1.0, -1);
        }
        return new Posicao(Double.NaN, Double.NaN, 0.0, -1);
    }
}
