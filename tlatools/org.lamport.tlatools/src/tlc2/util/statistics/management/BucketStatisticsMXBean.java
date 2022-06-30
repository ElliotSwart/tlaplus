/*******************************************************************************
 * Copyright (c) 2015 Microsoft Research. All rights reserved. 
 *
 * The MIT License (MIT)
 * 
 * Permission is hereby granted, free of charge, to any person obtaining a copy 
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies
 * of the Software, and to permit persons to whom the Software is furnished to do
 * so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software. 
 * 
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS
 * FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR
 * COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN
 * AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION
 * WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 *
 * Contributors:
 *   Markus Alexander Kuppe - initial API and implementation
 ******************************************************************************/

package tlc2.util.statistics.management;

import tlc2.util.statistics.BucketStatistics;

public interface BucketStatisticsMXBean {

	String getObjectName();

	/**
	 * @see BucketStatistics#getObservations()
	 */
    long getObservations();

	/**
	 * @see BucketStatistics#getMedian()
	 */
    int getMedian();

	/**
	 * @see BucketStatistics#getMean()
	 */
    double getMean();

	/**
	 * @see BucketStatistics#getMin()
	 */
    int getMin();

	/**
	 * @see BucketStatistics#getMax()
	 */
    int getMax();

	/**
	 * @see BucketStatistics#getStdDev()
	 */
    double getStdDev();

	/**
	 * @see BucketStatistics#getPercentile(double)
	 */
    double get75Percentile();

	/**
	 * @see BucketStatistics#getPercentile(double)
	 */
    double get95Percentile();

	/**
	 * @see BucketStatistics#getPercentile(double)
	 */
    double get98Percentile();

	/**
	 * @see BucketStatistics#getPercentile(double)
	 */
    double get99Percentile();

	/**
	 * @see BucketStatistics#getPercentile(double)
	 */
    double get999Percentile();
}