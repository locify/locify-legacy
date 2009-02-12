package com.locify.client.utils.math;

/**
 * <p>Collection of math fuctions specially made for Locify ;)</p>
 * @author Menion
 */
public class LMath {

    /** Square root from 3 */
    final static public double SQRT3 = 1.732050807568877294;
    /** Log10 constant */
    final static public double LOG10 = 2.302585092994045684;
    /** ln(0.5) constant */
    final static public double LOGdiv2 = -0.6931471805599453094;
    //


//    public static double asin(double x) {
//        if (x == Double.NaN) {
//            return Double.NaN;
//        }
//        HFloat hf = new HFloat(String.valueOf(x));
//        HFloat outhf = hf.asin();
//        return Double.valueOf(outhf.toString()).doubleValue();
//    }
//
//    public static double acos(double x) {
//        if (x == Double.NaN) {
//            return Double.NaN;
//        }
//        HFloat hf = new HFloat(String.valueOf(x));
//        HFloat outhf = hf.acos();
//        return Double.valueOf(outhf.toString()).doubleValue();
//    }
//
//    public static double atan(double x) {
//        if (x == Double.NaN) {
//            return Double.NaN;
//        }
//        HFloat hf = new HFloat(String.valueOf(x));
//        HFloat outhf = hf.atan();
//        return Double.valueOf(outhf.toString()).doubleValue();
//    }
//
//    static public double exp(double x) {
//        if (x == 0.) {
//            return 1.;
//        //
//        }
//        double f = 1;
//        long d = 1;
//        double k;
//        boolean isless = (x < 0.);
//        if (isless) {
//            x = -x;
//        }
//        k = x / d;
//        //
//        for (long i = 2; i < 50; i++) {
//            f = f + k;
//            k = k * x / i;
//        }
//        //
//        if (isless) {
//            return 1 / f;
//        } else {
//            return f;
//        }
//    }

    static public double acos(double x) {
        double f = asin(x);
        if (f == Double.NaN) {
            return f;
        }
        return Math.PI / 2 - f;
    }

    static public double asin(double x) {
        if (x < -1. || x > 1.) {
            return Double.NaN;
        }
        if (x == -1.) {
            return -Math.PI / 2;
        }
        if (x == 1) {
            return Math.PI / 2;
        }
        return atan(x / Math.sqrt(1 - x * x));
    }

    static public double atan(double x) {
        boolean signChange = false;
        boolean invert = false;
        int sp = 0;
        double x2, a;
        // check up the sign change
        if (x < 0.) {
            x = -x;
            signChange = true;
        }
        // check up the invertation
        if (x > 1.) {
            x = 1 / x;
            invert = true;
        }
        // process shrinking the domain until x<PI/12
        while (x > Math.PI / 12) {
            sp++;
            a = x + SQRT3;
            a = 1 / a;
            x = x * SQRT3;
            x = x - 1;
            x = x * a;
        }
        // calculation core
        x2 = x * x;
        a = x2 + 1.4087812;
        a = 0.55913709 / a;
        a = a + 0.60310579;
        a = a - (x2 * 0.05160454);
        a = a * x;
        // process until sp=0
        while (sp > 0) {
            a = a + Math.PI / 6;
            sp--;
        }
        // invertation took place
        if (invert) {
            a = Math.PI / 2 - a;
        }
        // sign change took place
        if (signChange) {
            a = -a;
        }
        //
        return a;
    }

//    /**
//     * compute atan with right quadrant
//     * http://www.dspguru.com/comp.dsp/tricks/alg/fxdatan2.htm
//     * @param y delta y
//     * @param x delta x
//     * @return value atan2(dx, dy)
//     */
//    public static double atan2(double y, double x) {
//        double coeff_1 = Math.PI / 4d;
//        double coeff_2 = 3d * coeff_1;
//        double abs_y = Math.abs(y) + (1e-10);
//        double angle;
//        if (x >= 0d) {
//            double r = (x - abs_y) / (x + abs_y);
//            angle = coeff_1 - coeff_1 * r;
//        } else {
//            double r = (x + abs_y) / (abs_y - x);
//            angle = coeff_2 - coeff_1 * r;
//        }
//        return y < 0d ? -angle : angle;
//    }
//
    static public double atan2(double y, double x) {
        // if x=y=0
        if (y == 0. && x == 0.) {
            return 0.;
        }
        // if x>0 atan(y/x)
        if (x > 0.) {
            return atan(y / x);
        }
        // if x<0 sign(y)*(pi - atan(|y/x|))
        if (x < 0.) {
            if (y < 0.) {
                return -(Math.PI - atan(y / x));
            } else {
                return Math.PI - atan(-y / x);
            }
        }
        // if x=0 y!=0 sign(y)*pi/2
        if (y < 0.) {
            return -Math.PI / 2.;
        } else {
            return Math.PI / 2.;
        }
    }

    static public double exp(double x) {
        if (x == 0.) {
            return 1.;
        }
        //
        double f = 1;
        long d = 1;
        double k;
        boolean isless = (x < 0.);
        if (isless) {
            x = -x;
        }
        k = x / d;
        //
        for (long i = 2; i < 50; i++) {
            f = f + k;
            k = k * x / i;
        }
        //
        if (isless) {
            return 1 / f;
        } else {
            return f;
        }
    }

    static private double _log(double x) {
        if (!(x > 0.)) {
            return Double.NaN;
        }
        //
        double f = 0.0;
        //
        int appendix = 0;
        while (x > 0.0 && x <= 1.0) {
            x *= 2.0;
            appendix++;
        }
        //
        x /= 2.0;
        appendix--;
        //
        double y1 = x - 1.;
        double y2 = x + 1.;
        double y = y1 / y2;
        //
        double k = y;
        y2 = k * y;
        //
        for (long i = 1; i < 50; i += 2) {
            f += k / i;
            k *= y2;
        }
        //
        f *= 2.0;
        for (int i = 0; i < appendix; i++) {
            f += LOGdiv2;
        }
        //
        return f;
    }

    static public double log(double x) {
        if (!(x > 0.)) {
            return Double.NaN;
        }
        //
        if (x == 1.0) {
            return 0.0;
        }
        // Argument of _log must be (0; 1]
        if (x > 1.) {
            x = 1 / x;
            return -_log(x);
        }
        return _log(x);
    }

//    public static double log(double x) {
//        if (x == Double.NaN) {
//            return Double.NaN;
//        }
//
//        HFloat hf = new HFloat(String.valueOf(x));
//        HFloat outhf = hf.ln();
//        double r = Double.valueOf(outhf.toString()).doubleValue();
//        return r;

//        double val;
//        double res = 0;
//        for (int i = 1; i < 8; i = i + 2) {
//            val = (x - 1) / (x + 1);
//            res += (1 / i) * pow(val, i);
//        }
//        res *= 2;
////System.out.println("\n" + r + "  " + res + "  " + (r - res));
//        return res;
//}

    static public double log10(double x) {
        return log(x) / LOG10;
    }

//    static public double log10(double x) {
//        if (!(x > 0.)) {
//            return Double.NaN;
//        }
//        //
//        boolean neg = false;
//        if (x < 0) {
//            neg = true;
//            x = -x;
//        }
//        //
//        int index = 0;
//        if (x > 1.) {
//            // Great 1
//            while (x > 1.) {
//                x = x / 10;
//                index++;
//            }
//        } else {
//            // Less 1
//            while (x < 1.) {
//                x = x * 10.;
//                index--;
//            }
//        }
//        //
//        double res = index;
//        if (x != 1.) {
//            res = res + (log(x) / LOG10);
//        }
//        //
//        if (neg) {
//            return 1. / res;
//        } else {
//            return res;
//        }
//    }

    static public double pow(double x, double y) {
        if (y == 0.) {
            return 1.;
        }
        if (y == 1.) {
            return x;
        }
        if (x == 0.) {
            return 0.;
        }
        if (x == 1.) {
            return 1.;
        }
        //
        long l = (long) Math.floor(y);
        boolean integerValue = (y == (double) l);
        //
        if (integerValue) {
            boolean neg = false;
            if (y < 0.) {
                neg = true;
            }
            //
            double result = x;
            for (long i = 1; i < (neg ? -l : l); i++) {
                result = result * x;
            }
            //
            if (neg) {
                return 1. / result;
            } else {
                return result;
            }
        } else {
            if (x > 0.) {
                return exp(y * log(x));
            } else {
                return Double.NaN;
            }
        }
    }

//    public static double pow(double a, int b) {
////long tim = System.currentTimeMillis();
////System.out.println(a + " " + b);
//        if (a == Double.NaN) {
//            return Double.NaN;
//        }
//        if (b == 0)
//            return 1;
//        if (b == 1)
//            return a;
//
//        // menions easy method
//        double p = a;
//        for (int i = 1; i < Math.floor(b); i++) {
//            p *= a;
//        }
//
//        if (b > 0) {
//            return p;
//        } else {
//            return 1 / p;
//        }
//
//        // default georges method thankx to polish
////        HFloat hf = new HFloat(String.valueOf(a));
////        HFloat outhf = hf.pow(b);
////        double r = Double.valueOf(outhf.toString()).doubleValue();
////        return r;
//
//        // Taylor method for pow (int b can be also double b)
////        boolean gt1 = (Math.sqrt((a - 1) * (a - 1)) <= 1) ? false : true;
////        int oc = -1, iter = 30;
////        double p = a, x, x2, sumX, sumY;
////
////        if ((b - Math.floor(b)) == 0) {
////            for (int i = 1; i < b; i++) {
////                p *= a;
////            }
//////System.out.println(r + "  " + p + "  " + (r - p));
////            return p;
////        }
////
////        x = (gt1) ? (a / (a - 1)) : (a - 1);
////        sumX = (gt1) ? (1 / x) : x;
////
////        for (int i = 2; i < iter; i++) {
////            p = x;
////            for (int j = 1; j < i; j++) {
////                p *= x;
////            }
////            double xTemp = (gt1) ? (1 / (i * p)) : (p / i);
////
////            sumX = (gt1) ? (sumX + xTemp) : (sumX + (xTemp * oc));
////
////            oc *= -1;
////        }
////
////        x2 = b * sumX;
////        sumY = 1 + x2;
////
////        for (int i = 2; i <= iter; i++) {
////            p = x2;
////            for (int j = 1; j < i; j++) {
////                p *= x2;
////            }
////            int yTemp = 2;
////            for (int j = i; j > 2; j--) {
////                yTemp *= j;
////            }
////            sumY += p / yTemp;
////        }
////        return sumY;
//
////System.out.println("pow: " + (System.currentTimeMillis() - tim));
//    }
}
