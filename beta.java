class Distribution {
    // percent down parameters - beta
    double perc_down_https_y_max        = 2.33;
    double perc_down_http_0_75_y_max    = 5.53;
    double perc_down_http_75_100_y_max  = 5.36;

    // b == gamma(beta1 + beta2)/(gamma(beta1)gamma(beta2))
    double perc_down_https_b            = 24.0309;
    double perc_down_http_0_75_b        = 15394.4;
    double perc_down_http_75_100_b      = 2.88159;

    double perc_down_https_beta_1       = 4.5809;
    double perc_down_https_beta_2       = 1.9556;
    double perc_down_http_0_75_beta_1   = 14.015;
    double perc_down_http_0_75_beta_2   = 1.7337;
    double perc_down_http_75_100_beta_1 = 4.3029;
    double perc_down_http_75_100_beta_2 = 1.4296;

    double perc_down_https_x_min        = 0.00001;
    double perc_down_https_x_max        = 0.99999;
    double perc_down_http_0_75_x_min    = 0.00001;
    double perc_down_http_0_75_x_max    = 0.74999;
    double perc_down_http_75_100_x_min  = 0.75000;
    double perc_down_http_75_100_x_max  = 0.99999;

    // total byte parameters - gamma (https)
    double tot_bytes_https_y_max        = 0.00012;
    double tot_bytes_https_shape        = 3.4688;
    double tot_bytes_https_scale        = 2133;
    double tot_bytes_https_gamma_shape  = 3.21143;
    double tot_bytes_https_x_min        = 0;
    double tot_bytes_https_x_max        = 20000;

    // bps down parameters - beta
    double perc_down_https_y_max        = 0.00000518637;
    // b == gamma(beta1 + beta2)/(gamma(beta1)gamma(beta2))
    double bps_down_https_b             = 6.27668;
    double bps_down_https_beta_1        = 1.1806;
    double bps_down_https_beta_2        = 4.3438;
    double bps_down_https_x_min         = 6476.9;
    double bps_down_https_x_max         = 600000;
    // bps down parameters - gamma
    double bps_down_http_y_max          = 0.00000699396;
    double bps_down_http_shape          = 1.6837;
    double bps_down_http_scale          = 61445;
    double bps_down_http_gamma_shape    = 0.905651;
    double bps_down_http_x_min          = 0;
    double bps_down_http_x_max          = 600000;

    double beta_fx(double x, double b, double beta_1, double beta_2,
                   double x_min, double x_max) {
        // b == gamma(beta1 + beta2)/(gamma(beta1)gamma(beta2))
        return b * ((Math.pow(x - x_min,beta_1 - 1)) *
                    (Math.pow(x_max - x,beta_2 - 1)))
                 / Math.pow(x_max - x_min,beta_1 + beta_2 - 1)
    }

    double gamma_fx(double x, double gamma_shape, double shape, double scale) {
        return Math.pow(x,shape - 1) * Math.exp(-x / scale) /
               (gamma_shape * Math.pow(scale, shape))
    }

    double calc_x(x_min, x_max, rnd) {
        return x_min + (x_max - x_min) * rnd;
    }
    double calc_y(y_max, rnd) {
        return y_max * rnd;
    }

    double calc_tot_bytes_https() {
        double x    = 0;
        double f_x  = 0;
        double y    = 0;

        do {
            double rnd1 = generator.nextDouble();
            if(rnd1 == 0 || rnd1 == 1) continue;
            double rnd2 = generator.nextDouble();
            x = calc_x(tot_bytes_https_x_min, tot_bytes_https_x_max, rnd1);
            y = calc_y(tot_bytes_https_y_max, rnd2);
            f_x = gamma_fx(x, tot_bytes_https_gamma_shape,
                          tot_bytes_https_shape, tot_bytes_https_scale);
        }while(y > f_x);

        return x;
    }

    double calc_tot_bytes_http() {
        // inverse transform for Anu's data here
        return 0;
    }

    double calc_perc_down_https() {
        double x    = 0;
        double f_x  = 0;
        double y    = 0;

        do {
            double rnd1 = generator.nextDouble();
            if(rnd1 == 0 || rnd1 == 1) continue;
            double rnd2 = generator.nextDouble();
            x = calc_x(perc_down_https_x_min, perc_down_https_x_max, rnd1);
            y = calc_y(perc_down_https_y_max, rnd2);
            f_x = beta_fx(x, perc_down_https_b,
                          perc_down_https_beta_1, perc_down_https_beta_2,
                          perc_down_https_x_min, perc_down_https_x_max);
        }while(y > f_x);

        return x;
    }

    double calc_perc_down_http() {
        double x    = 0;
        double f_x  = 0;
        double y    = 0;

        do {
            double rnd1 = generator.nextDouble();
            double rnd2 = generator.nextDouble();

            if(rnd1 == 0 || rnd1 == 1) continue;
            if(rnd1 > perc_down_http_0_75_x_max &&
               rnd1 < perc_down_http_75_100_x_min) {
               rnd1 = perc_down_http_75_100_x_min;
            }

            if(rnd1 <= perc_down_http_0_75_x_max) {
                x = calc_x(perc_down_http_0_75_x_min, perc_down_http_0_75_x_max, rnd1);
                y = calc_y(perc_down_http_0_75_y_max, rnd2);
                f_x = beta_fx(x, perc_down_http_0_75_b,
                              perc_down_http_0_75_beta_1, perc_down_http_0_75_beta_2,
                              perc_down_http_0_75_x_min,  perc_down_http_0_75_x_max);
            }
            else {
                x = calc_x(perc_down_http_75_100_x_min, perc_down_http_75_100_x_max, rnd1);
                y = calc_y(perc_down_http_75_100_y_max, rnd2);
                f_x = beta_fx(x, perc_down_http_75_100_b,
                              perc_down_http_75_100_beta_1, perc_down_http_75_100_beta_2,
                              perc_down_http_75_100_x_min,  perc_down_http_75_100_x_max);
            }
        }while(y > f_x);

        return x;
    }

    double calc_bps_down_https() {
        double x    = 0;
        double f_x  = 0;
        double y    = 0;

        do {
            double rnd1 = generator.nextDouble();
            if(rnd1 == 0 || rnd1 == 1) continue;
            double rnd2 = generator.nextDouble();
            x = calc_x(bps_down_https_x_min, bps_down_https_x_max, rnd1);
            y = calc_y(bps_down_https_y_max, rnd2);
            f_x = beta_fx(x, bps_down_https_b,
                          bps_down_https_beta_1, bps_down_https_beta_2,
                          bps_down_https_x_min,  bps_down_https_x_max);
        }while(y > f_x);

        return x;
    }

    double calc_bps_down_http() {
        double x    = 0;
        double f_x  = 0;
        double y    = 0;

        do {
            double rnd1 = generator.nextDouble();
            if(rnd1 == 0 || rnd1 == 1) continue;
            double rnd2 = generator.nextDouble();
            x = calc_x(bps_down_http_x_min, bps_down_http_x_max, rnd1);
            y = calc_y(bps_down_http_y_max, rnd2);
            f_x = gamma_fx(x, bps_down_http_gamma_shape,
                          bps_down_http_shape, bps_down_http_scale);
        }while(y > f_x);

        return x;
    }
}