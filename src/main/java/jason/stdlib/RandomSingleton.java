package jason.stdlib;

import java.util.Random;

/**
 Manages the random number generator (and its seed) for:
 @see jason.functions.Random
 @see jason.stdlib.random

 @author Timotheus Kampik
*/

public class RandomSingleton {
    private static Random random = new Random();

    public static void setSeed(long seed) {
        random.setSeed(seed);
    }

    public static double nextDouble() {
        return random.nextDouble();
    }

    public static int nextInt(int bound) {
        return random.nextInt(bound);
    }
}
