/**
 * Anielly.
 * Uma estimativa de uma direção, com confiança.
 */
public class Direcao extends Estimativa {
    private double direcao;

    public Direcao() {
        direcao = 0;
        confiancaInicial = 0;
    }
    
    /**
     * Construtor para copiar outra estimativa direcao.
     * 
     * @param direcao Construtor para copiar outra estimativa direcao.
     */
    public Direcao(Direcao direcao) {
        this.direcao = direcao.getDirection();
        this.confiancaInicial = direcao.getConfiancaInicial();
        this.cicloDaEstimativa = direcao.getCicloDaEstimativa();
    }
    
    /**
     * Construtor tomando um direcao e se deve ou não manter a confiança
     * para sempre (útil para objetos estacionários.)
     * 
     * @param direcao a direcao da estimativa
     * @param paraSempre manter a confiança para sempre
     */
    public Direcao(double direcao, boolean paraSempre) {
        if (paraSempre) {
            this.setForever(direcao);
        }
        else {
            this.direcao = direcao;
        }
    }
    
    /**
     * Calcula o valor de confiança em um determinado ciclo.
     * 
     * @param ciclo o intervalo de tempo para estimar a confiança.
     * @return Uma medida da confiança no valor direção.
     */
    public double getConfianca(int ciclo) {
        if( cicloDaEstimativa == -1 )       
            return 0.0;
        double dConf = confiancaInicial;
        int dif      = (ciclo - cicloDaEstimativa);
        for (int i = 0; i < dif ; i++) {
            dConf *= 0.99;
        }
        if( dConf > 1.0 )
            return 0.0;
        return dConf;
    }
    
    public double getDirection() {
        return this.direcao;
    }
    
    /** 
     * Define uma direcao permanentemente com total certeza.
     * 
     * Isso pode ser usado para definir o direcao de coisas que só têm
     *  direcao por convenção, como objetos estacionários que dizemos
     *  sempre face leste.
     * 
     * @param direcao
     */
    public void setForever(double direcao) {
        this.direcao = direcao;
        this.confiancaInicial = 1.0;
        this.cicloDaEstimativa = -1;
    }
    
    /**
     * Atualiza direção com novo valor de confiança
     * 
     * @param direcao valor da nova direcao
     * @param confianca um valor da confiança
     * @param ciclo
     */
    public void update(double direcao, double confianca, int ciclo) {
        this.direcao = normalizarDirecao(direcao);
        this.confiancaInicial = confianca;
        this.cicloDaEstimativa = ciclo;
    }
    
    /**
     * Converte uma direcao equivalente entre -180 e 180 graus.
     * 
     * @param direcao uma direcao original
     * @return direcao em um direcao equivalente entre -180 e 180 graus.
     */
    public double normalizarDirecao(double direcao) {
        while (direcao < -180) {
            direcao += 360;
        }
        while (direcao > 180) {
            direcao -= 360;
        }
        return direcao;
    }
    
    /**
     * Renderiza uma descrição da direção como uma string.
     * 
     * @param ciclo o ciclo atual do jogo.
     * @return uma string representando a estimativa 
     */
    public String renderizar(int ciclo) {
        return Double.toString(this.direcao) + " graus com " + this.getConfianca(ciclo) + " confiança.";
    }
}
