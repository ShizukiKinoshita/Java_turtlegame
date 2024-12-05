public class TurtleInfo{
    private String id;
    private String name;
    private double x;
    private double y;
    private double a;
    private double e;
    public TurtleInfo(String id,String name,double x, double y, double a,double e) {
        this.id = id;
        this.name = name;
        this.x = x;
        this.y = y;
        this.a = a;
        this.e = e;
    }

    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public double getX() {
        return x;
    }

    public double getY() {
        return y;
    }

    public double getAng() {
        return a;
    }

    public double getE() {
        return e;
    }
    
    public void setAng(double ang) {
        this.a = ang;
    }

    public void setE(double eng) {
        this.e = eng;
    }
    public void setX(double rex) {
        this.x = rex;
    }
    public void setY(double rey) {
        this.y = rey;
    }
}



