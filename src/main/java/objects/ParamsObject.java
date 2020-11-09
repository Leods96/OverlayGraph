package objects;

public class ParamsObject {
    /**
     * Params for the MatrixOverlayGraphManager
     */
    private Boolean angleHint;
    private Boolean overlayOnly;
    private Double neighbourThreshold;
    private Double neighbourDistance;
    /**
     * Params for the MatrixOverlayGraph
     */
    private Boolean splitLatitude;
    private Integer numberNN;

    public ParamsObject() {
        this.angleHint = null;
        this.neighbourDistance = null;
        this.neighbourThreshold = null;
        this.overlayOnly = null;
        this.numberNN = null;
        this.splitLatitude = null;
    }

    public Boolean isAngleHint() {
        return angleHint;
    }

    public Boolean isOverlayOnly() {
        return overlayOnly;
    }

    public Double getNeighbourDistance() {
        return neighbourDistance;
    }

    public Double getNeighbourThreshold() {
        return neighbourThreshold;
    }

    public Boolean isSplitLatitude() {
        return splitLatitude;
    }

    public Integer getNumberNN() {
        return numberNN;
    }

    public void setAngleHint(boolean angleHint) {
        this.angleHint = angleHint;
    }

    public void setNeighbourDistance(double neighbourDistance) {
        this.neighbourDistance = neighbourDistance;
    }

    public void setNeighbourThreshold(double neighbourThreshold) {
        this.neighbourThreshold = neighbourThreshold;
    }

    public void setOverlayOnly(boolean overlayOnly) {
        this.overlayOnly = overlayOnly;
    }

    public void setNumberNN(int numberNN) {
        this.numberNN = numberNN;
    }

    public void setSplitLatitude(boolean splitLatitude) {
        this.splitLatitude = splitLatitude;
    }

    public void update(ParamsObject oldPo) {
        if (oldPo == null)
            return;
        if (this.splitLatitude == null) this.splitLatitude = oldPo.isSplitLatitude();
        if (this.numberNN == null) this.numberNN = oldPo.getNumberNN();
        if (this.overlayOnly == null) this.overlayOnly = oldPo.isOverlayOnly();
        if (this.neighbourThreshold == null) this.neighbourThreshold = oldPo.getNeighbourThreshold();
        if (this.neighbourDistance == null) this.neighbourDistance = oldPo.getNeighbourDistance();
        if (this.angleHint == null) this.angleHint = oldPo.isAngleHint();
    }
}
