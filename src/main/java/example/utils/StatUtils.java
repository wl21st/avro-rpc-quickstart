/**
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package example.utils;

/**
 * Statistics utils.
 */
public class StatUtils {

    private StatUtils() {
    }

    public static double stddev(long[] samples) {
        double avg = average(samples);

        double result = 0;
        for (long sample : samples) {
            result += (sample - avg) * (sample - avg);
        }

        result /= samples.length;

        return Math.sqrt(result);
    }

    public static double average(long[] samples) {
        return sum(samples) / (samples.length * 1d);
    }

    public static double sum(long[] samples) {
        double result = 0;

        for (long sample : samples) {
            result += sample;
        }

        return result;
    }

    public static double min(long[] samples) {
        long result = Long.MAX_VALUE;

        for(long sample : samples) {
            if (sample < result) {
                result = sample;
            }
        }

        return result;
    }

    public static double max(long[] samples) {
        long result = Long.MIN_VALUE;

        for(long sample : samples) {
            if (sample > result) {
                result = sample;
            }
        }

        return result;
    }


    /**
     */
    public static double[] percentile(long[] samples, int n) {
        double stddev = stddev(samples);
        double avg = average(samples);

        double[] result = new double[n];

        double[] lowValues = new double[n];
        double[] highValues = new double[n];

        for(int i = 0; i < n ; i++) {
            lowValues[i] = avg - (i+1) * stddev;
            highValues[i] = avg + (i+1) * stddev;
        }

        for(long sample : samples) {
            for(int i = 0; i < n; i++) {
                if (sample >= lowValues[i] && sample <= highValues[i]) {
                    result[i]++;
                    break;
                }
            }
        }

        for(int i = 0; i < n; i++){
            result[i] = result[i] / samples.length * 100d;
            if (i != 0) {
                result[i] += result[i-1];
            }
        }

        return result;
    }

}