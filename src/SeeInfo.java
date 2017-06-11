public class SeeInfo extends Info{
    
    double distance;
    double direction;
    double distChange;
    double dirChange;
    double bodyFacingDir;
    double headFacingDir;
    double pointingDir;
    boolean tackling;
    boolean kicking;

    public SeeInfo() {
    	super();
    }

    @Override
    public void reset() {
    	super.reset();
        this.distance = -1.0;
        this.direction = -181.0;
        this.distChange = -1.0;
        this.dirChange = -1.0;
        this.bodyFacingDir = -1.0;
        this.headFacingDir = -1.0;
        this.pointingDir = -1.0;
        this.tackling = false;
        this.kicking = false;
    }
    
    public void copy(SeeInfo info){
    	info.time = time;
    	info.distance = distance;
    	info.direction = direction;
    	info.distChange = distChange;
    	info.dirChange = dirChange;
    	info.bodyFacingDir = bodyFacingDir;
    	info.headFacingDir = headFacingDir;
    	info.pointingDir = pointingDir;
    	info.tackling = tackling;
    	info.kicking = kicking;
    }
}
