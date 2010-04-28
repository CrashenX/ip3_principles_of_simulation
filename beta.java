
class Beta {
    void set_down_time() {
        _set_down_time(MIN_DOWN, MAX_DOWN);
    }
    protected void _set_down_time(int min, int max) {
        double a        = 0;
        double b        = 1;
        double c        = 40d/27d;
        double beta_rnd = 0;
        beta_rnd = beta_variate(a, b, c);
        this.down_time = (int)Math.round(60 * (min + (max - min) * beta_rnd));
    }
    protected double beta_variate(double a, double b, double c) {
        double x    = 0;
        double f_x  = 0;
        double y    = 0;
        do {
            double rnd1 = generator.nextDouble();
            double rnd2 = generator.nextDouble();
            x = a + (b - a) * rnd1;
            y = c * rnd2;
            f_x = (10 * Math.pow(x, 2)) - (10 * Math.pow(x, 3));
        }while(y > f_x);
        return x;
    }
}