/**
 * This file is part of JEMMA - http://jemma.energy-home.org
 * (C) Copyright 2013 Telecom Italia (http://www.telecomitalia.it)
 *
 * JEMMA is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License (LGPL) version 3
 * or later as published by the Free Software Foundation, which accompanies
 * this distribution and is available at http://www.gnu.org/licenses/lgpl.html
 *
 * JEMMA is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License (LGPL) for more details.
 *
 */
package org.energy_home.jemma.ah.ebrain.algo;



/**
 * Given a time series, say a complete monthly data for 12 months, the
 * Holt-Winters smoothing and forecasting technique is built on the following
 * formulae (multiplicative version):
 * 
 * St[i] = alpha * y[i] / It[i - period] + (1.0 - alpha) * (St[i - 1] + Bt[i -
 * 1]) Bt[i] = gamma * (St[i] - St[i - 1]) + (1 - gamma) * Bt[i - 1] It[i] =
 * beta * y[i] / St[i] + (1.0 - beta) * It[i - period] Ft[i + m] = (St[i] + (m *
 * Bt[i])) * It[i - period + m]
 * 
 * Note: Many authors suggest calculating initial values of St, Bt and It in a
 * variety of ways, but some of them are incorrect e.g. determination of It
 * parameter using regression. This implementation uses the NIST recommended
 * methods.
 * 
 * For more details, see: http://adorio-research.org/wordpress/?p=1230
 * http://www.itl.nist.gov/div898/handbook/pmc/section4/pmc435.htm
 * 
 */

public class TripleExponentialSmoothing {
	private int period, seasons;
	private double alpha, beta, gamma;
	private double mse = Double.NaN, mape = Double.NaN;
	private double[] lastForecast, lastSeries;

	TripleExponentialSmoothing() {}
	
	/*
	 * @param alpha
	 *            - overall smoothing coefficient.
	 * @param beta
	 *            - seasonal smoothing coefficient.
	 * @param gamma
	 *            - trend smoothing coefficient.
	 */
	public TripleExponentialSmoothing(double alpha, double beta, double gamma) {
		if ((alpha < 0.0) || (alpha > 1.0)) {
			throw new IllegalArgumentException("Value of Alpha should satisfy 0.0 <= alpha <= 1.0");
		}
		if ((beta < 0.0) || (beta > 1.0)) {
			throw new IllegalArgumentException("Value of Beta should satisfy 0.0 <= beta <= 1.0");
		}
		if ((gamma < 0.0) || (gamma > 1.0)) {
			throw new IllegalArgumentException("Value of Gamma should satisfy 0.0 <= gamma <= 1.0");
		}
		
		this.alpha = alpha;
		this.beta = beta;
		this.gamma = gamma;
	}
	
	public double getMSE() {return mse;}
	public double getMAPE() {return mape;}
	public int getPeriod() {return period;}
	public int getSeasons() {return seasons;}
	public double[] getLastForecast() {return lastForecast;}
	public double[] getObservedData() {return lastSeries;}
	
	/**
	 * This method is the entry point. It calculates the initial values and
	 * returns the forecast for the future k periods.
	 * 
	 * @param timeSeries
	 *            - Time series data.
	 * @param alpha
	 *            - overall smoothing coefficient.
	 * @param beta
	 *            - seasonal smoothing coefficient.
	 * @param gamma
	 *            - trend smoothing coefficient.
	 * @param perdiod
	 *            - A complete season's data consists of L periods. And we need
	 *            to estimate the trend factor from one period to the next. To
	 *            accomplish this, it is advisable to use two complete seasons;
	 *            that is, 2L periods.
	 * @param k
	 *            - Extrapolated future data points. - 4 quarterly, - 7 weekly,
	 *            - 12 monthly
	 * 
	 */
	public double[] forecast(double[] timeSeries, int period) {
		return forecast(timeSeries, period, period);
	}
	public double[] forecast(double[] timeSeries, int period, int k) {
		if (timeSeries == null) 
			throw new IllegalArgumentException("The time series data cannot not null.");
		
		if (period > timeSeries.length)
			throw new IllegalArgumentException("The time series data must be >= the period.");
		
		if (k <= 0) 
			throw new IllegalArgumentException("Number of predicted k values must be greater than 0.");

		if (k > period) 
			throw new IllegalArgumentException("Number of predicted k values must be <= period.");
		
		this.period = period;
		this.seasons = timeSeries.length / period;
		
		double[] St = new double[timeSeries.length];
		double[] Bt = new double[timeSeries.length];
		double[] It = new double[timeSeries.length];
		double[] Ft = new double[timeSeries.length + k];

		// Initialize base values
		St[1] = getInitialLevel(timeSeries);
		Bt[1] = getInitialTrend(timeSeries, period);

		double[] initialSeasonalIndices = getSeasonalIndices(timeSeries, period, seasons);
		for (int i = 0; i < period; i++) {
			It[i] = initialSeasonalIndices[i];
		}
		boolean debug = true;
		if (debug) {
			System.out.printf("Time series observations: %d, seasons %d, period %d\n", timeSeries.length, seasons, period);
			System.out.println("Initial level value a0: " + St[1]);
			System.out.println("Initial trend value b0: " + Bt[1]);
			printArray("Seasonal Indices: ", initialSeasonalIndices);
		}

		// Start calculations
		for (int i = 2; i < timeSeries.length; i++) {
			// Calculate overall smoothing
			if ((i - period) >= 0) {
				St[i] = alpha * timeSeries[i] / It[i - period] + (1.0 - alpha) * (St[i - 1] + Bt[i - 1]);
			} else {
				St[i] = alpha * timeSeries[i] + (1.0 - alpha) * (St[i - 1] + Bt[i - 1]);
			}

			// Calculate trend smoothing
			Bt[i] = gamma * (St[i] - St[i - 1]) + (1 - gamma) * Bt[i - 1];

			// Calculate seasonal smoothing
			if ((i - period) >= 0) {
				It[i] = beta * timeSeries[i] / St[i] + (1.0 - beta) * It[i - period];
			}
			

			// Calculate forecast
			if (((i + k) >= period)) {
				Ft[i + k] = (St[i] + (k * Bt[i])) * It[i - period + k];
			}
			if (debug) {
				System.out.printf("i = %d, x = %f, St = %f, Bt = %f, It = %f, Ft = %f\n", i, timeSeries[i], St[i], Bt[i], It[i], Ft[i]);
			}
		}

		lastSeries = timeSeries;
		lastForecast = Ft;
		calculateErrorsIndicators();
		int offset = seasons * period;
		int len = Ft.length - offset;
		double[] predicted = new double[len];
		System.arraycopy(Ft, offset, predicted, 0, len);
		return predicted;
	}
	

	/**
	 * This method is the entry point. It calculates the initial values and
	 * returns the forecast for the future k periods.
	 * 
	 * @param timeSeries
	 *            - Time series data.
	 * @param alpha
	 *            - overall smoothing coefficient.
	 * @param beta
	 *            - seasonal smoothing coefficient.
	 * @param gamma
	 *            - trend smoothing coefficient.
	 * @param perdiod
	 *            - A complete season's data consists of L periods. And we need
	 *            to estimate the trend factor from one period to the next. To
	 *            accomplish this, it is advisable to use two complete seasons;
	 *            that is, 2L periods.
	 * @param k
	 *            - Extrapolated future data points. - 4 quarterly, - 7 weekly,
	 *            - 12 monthly
	 * 
	 * @param debug
	 *            - Print debug values. Useful for testing.
	 * 
	 */
	public double[] forecast(double[] timeSeries, double alpha, double beta, double gamma, int period, int k, boolean debug) {

		validateArguments(timeSeries, alpha, beta, gamma, period, k);

		int seasons = timeSeries.length / period;
		double a0 = getInitialLevel(timeSeries);
		double b0 = getInitialTrend(timeSeries, period);
		double[] initialSeasonalIndices = getSeasonalIndices(timeSeries, period, seasons);

		if (debug) {
			System.out.printf("Time series observations: %d, seasons %d, period %d\n", timeSeries.length, seasons, period);
			System.out.println("Initial level value a0: " + a0);
			System.out.println("Initial trend value b0: " + b0);
			printArray("Seasonal Indices: ", initialSeasonalIndices);
		}

		double[] forecast = computeHoltWinters(timeSeries, a0, b0, alpha, beta, gamma, initialSeasonalIndices, period, k, debug);
		//updateAccuracyIndicators(timeSeries, forecast, period);
		if (debug) {
			System.out.println("Mean Square Error " + mse);
			System.out.println("Mean Absolute % Error " + mape);
			//printArray("Forecast", forecast);
		}

		return forecast;
	}

	public double[] forecast(double[] timeSeries, double alpha, double beta, double gamma, int period, int k) {
		return forecast(timeSeries, alpha, beta, gamma, period, k, false);
	}

	/**
	 * Validate input.
	 * 
	 * @param timeSeries
	 * @param alpha
	 * @param beta
	 * @param gamma
	 * @param k
	 */
	private void validateArguments(double[] timeSeries, double alpha, double beta, double gamma, int period, int k) {
		if (timeSeries == null) {
			throw new IllegalArgumentException("The time series data cannot not null.");
		}

		if (period > timeSeries.length)
			throw new IllegalArgumentException("The time series data must be >= the period.");
		
		if (k <= 0) {
			throw new IllegalArgumentException("Number of predicted k values must be greater than 0.");
		}

		if (k > period) {
			throw new IllegalArgumentException("Number of predicted k values must be <= period.");
		}

		if ((alpha < 0.0) || (alpha > 1.0)) {
			throw new IllegalArgumentException("Value of Alpha should satisfy 0.0 <= alpha <= 1.0");
		}

		if ((beta < 0.0) || (beta > 1.0)) {
			throw new IllegalArgumentException("Value of Beta should satisfy 0.0 <= beta <= 1.0");
		}

		if ((gamma < 0.0) || (gamma > 1.0)) {
			throw new IllegalArgumentException("Value of Gamma should satisfy 0.0 <= gamma <= 1.0");
		}
	}

	/**
	 * This method realizes the Holt-Winters equations.
	 * 
	 * @param timeSeries
	 * @param a0
	 * @param b0
	 * @param alpha
	 * @param beta
	 * @param gamma
	 * @param initialSeasonalIndices
	 * @param period
	 * @param k
	 * @param debug
	 * @return - Forecast for m periods.
	 */
	private double[] computeHoltWinters(double[] timeSeries, double a0, double b0, double alpha, double beta, double gamma,
			double[] initialSeasonalIndices, int period, int k, boolean debug) {

		double[] St = new double[timeSeries.length];
		double[] Bt = new double[timeSeries.length];
		double[] It = new double[timeSeries.length];
		double[] Ft = new double[timeSeries.length + k];

		// Initialize base values
		St[1] = a0;
		Bt[1] = b0;

		for (int i = 0; i < period; i++) {
			It[i] = initialSeasonalIndices[i];
		}

		// Start calculations
		for (int i = 2; i < timeSeries.length; i++) {

			// Calculate overall smoothing
			if ((i - period) >= 0) {
				St[i] = alpha * timeSeries[i] / It[i - period] + (1.0 - alpha) * (St[i - 1] + Bt[i - 1]);
			} else {
				St[i] = alpha * timeSeries[i] + (1.0 - alpha) * (St[i - 1] + Bt[i - 1]);
			}

			// Calculate trend smoothing
			Bt[i] = gamma * (St[i] - St[i - 1]) + (1 - gamma) * Bt[i - 1];

			// Calculate seasonal smoothing
			if ((i - period) >= 0) {
				It[i] = beta * timeSeries[i] / St[i] + (1.0 - beta) * It[i - period];
			}


			// Calculate forecast
			if (((i + k) >= period)) {
				Ft[i + k] = (St[i] + (k * Bt[i])) * It[i - period + k];
			}

			if (debug) {
				System.out.printf("i = %d, x = %f, St = %f, Bt = %f, It = %f, Ft = %f\n", i, timeSeries[i], St[i], Bt[i], It[i], Ft[i]);
			}
		}
		return Ft;
	}

	/**
	 * See: http://robjhyndman.com/researchtips/hw-initialization/ 1st period's
	 * average can be taken. But y[0] works better.
	 * 
	 * @return - Initial Level value i.e. St[1]
	 */
	private double getInitialLevel(double[] timeSeries) {
		return timeSeries[0];
	}

	/**
	 * See: http://www.itl.nist.gov/div898/handbook/pmc/section4/pmc435.htm
	 * 
	 * @return - Initial trend - Bt[1]
	 */
	private double getInitialTrend(double[] timeSeries, int period) {
		if (timeSeries.length < 2 * period) return 0;
		double sum = 0;
		for (int i = 0; i < period; i++) {
			sum += (timeSeries[period + i] - timeSeries[i]);
		}
		return sum / (period * period);
	}

	/**
	 * See: http://www.itl.nist.gov/div898/handbook/pmc/section4/pmc435.htm
	 * 
	 * @return - Seasonal Indices.
	 */
	private double[] getSeasonalIndices(double[] timeSeries, int period, int seasons) {
		double[] seasonalAverage = new double[seasons];
		double[] seasonalIndices = new double[period];

		double[] averagedObservations = new double[timeSeries.length];

		for (int i = 0; i < seasons; i++) {
			for (int j = 0; j < period; j++) {
				seasonalAverage[i] += timeSeries[(i * period) + j];
			}
			seasonalAverage[i] /= period;
		}

		for (int i = 0; i < seasons; i++) {
			for (int j = 0; j < period; j++) {
				averagedObservations[(i * period) + j] = timeSeries[(i * period) + j] / seasonalAverage[i];
			}
		}

		for (int i = 0; i < period; i++) {
			for (int j = 0; j < seasons; j++) {
				seasonalIndices[i] += averagedObservations[(j * period) + i];
			}
			seasonalIndices[i] /= seasons;
		}

		return seasonalIndices;
	}
	
	
	public void calculateErrorsIndicators() {
        double sumAbsPercentErr = 0.0;
        double sumErrSquared = 0.0;
        
		for (int i = period; i < lastSeries.length; ++i) {
			double error = lastSeries[i] - lastForecast[i];
			sumAbsPercentErr += Math.abs(error / lastSeries[i]);
            sumErrSquared += error * error;
		}
		mse = sumErrSquared / (lastSeries.length - period);
		mape = sumAbsPercentErr / (lastSeries.length - period);
	}

	/**
	 * Utility method to print array values.
	 * 
	 * @param description
	 * @param data
	 */
	private void printArray(String description, double[] data) {
		System.out.println(description);
		for (int i = 0; i < data.length; i++) {
			System.out.println(data[i]);
		}
	}
	
    public static void main (String[] args) {
        try {
			double alpha = 0.3;
			double beta = 0.7;
			double gamma = 0.0;
			
        	TimeSeriesEnergyLoad tsel = new TimeSeriesEnergyLoad(2);
        	tsel.setRandomLoad();
        	TripleExponentialSmoothing t = new TripleExponentialSmoothing(alpha, beta, gamma);
        	//double[] pred = t.forecast();
        	double[] expect = tsel.toArray();
        	double[] pred = t.forecast(expect, tsel.getDailySlots());
        	//System.exit(0);
        	for (int i = 0; i < pred.length; ++i) {
				System.out.printf("%d  %12.13f  %12.13f\n", i, pred[i], i < expect.length ? expect[i] : 0);
			}
        	
        	/*
        	for (int i = 0; i < expect.length; ++i) {
        		System.out.printf("%d = %12.13f\n", i,expect[i]);
        	}
        	System.out.println("\n\n");
        	for (int i = 0; i < pred.length; ++i) {
        		System.out.printf("%d = %12.13f\n", i,pred[i]);
        	}
      	*/
        	System.out.println("Mean Square Error " + t.getMSE());
			System.out.println("Mean Absolute % Error " + t.getMAPE());
			System.exit(0);
			
			EnergyAllocator ea = new EnergyAllocator();
		
			int period = t.getPeriod();
			int offset = period * t.getSeasons();
			//ea.interpolateEnergyForecast(pred, 0, period);
			
			System.exit(0);
        	
			/**
			 * NIST data available at:
			 * http://www.itl.nist.gov/div898/handbook/pmc/section4/pmc436.htm
			 */
			double[] timeSeries = { 362, 385, 432, 341, 382, 409, 498, 387, 473, 513, 582, 474,
			        544, 582, 681, 557, 628, 707, 773, 592, 627, 725, 854, 661 };
			period = 4;
			int k = 4;

			TripleExponentialSmoothing tes = new TripleExponentialSmoothing();
			double[] prediction = tes.forecast(timeSeries, alpha, beta, gamma, period, k, true);

			// These are the expected results
			double[] expected = new double[] { 0.0, 0.0, 0.0, 0.0, 0.0, 0.0,
			        594.8043646513713, 357.12171044215734, 410.9203094983815,
			        444.67743912921156, 550.9296957593741, 421.1681718160631,
			        565.905732450577, 639.2910221068818, 688.8541669002238,
			        532.7122406111591, 620.5492369959037, 668.5662327429854,
			        773.5946568453546, 629.0602103529998, 717.0290609530134,
			        836.4643466657625, 884.1797655866865, 617.6686414831381,
			        599.1184450128665, 733.227872348479, 949.0708357438998,
			        748.6618488792186 };
			for (int i = 0; i < prediction.length; ++i) {
				//System.out.printf("%12.13f - %12.13f\n", prediction[i], i < expected.length ? expected[i] : 0);
			}
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
    }
}
