
import java.util.Arrays;
import java.util.ArrayList;
import java.util.stream.IntStream;

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
/**
 *
 * @author sanCloverSan
 */
public class Test_PID {

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        //int[] arrTemp = new int[]{1, 1, 1, 1, 1, 1, 0};
        //int[] arrTemp = new int[]{0, 0, 1, 1, 1, 1, 0};
        //int[] arrTemp = new int[]{0, 0, 1, 0, 0, 1, 0};
        int[] arrTemp = new int[]{1, 0, 1, 1, 0, 1, 0};
        ArrayList<Integer> arr = new ArrayList<>();
        IntStream.range(0, arrTemp.length).forEach(
                i -> {
                    arr.add(arrTemp[i]);
                }
        );

        ArrayList<Integer> listOfError = new ArrayList<>();
        while (arr.indexOf(1) >= 0) {
            int processValue = (int) Test_PID.sensorInputToPV(arr);
            double kSR = 0.25;
            int sizeAtATime = 8;
            double speedReduction = Test_PID.speedReduction(listOfError, kSR, sizeAtATime);
            double pidValue = Test_PID.countPID(listOfError, 0, processValue, 1, 1, 1);
            int setSPeed = 150;
            int direction = motorSpeedCalculation(listOfError, pidValue, setSPeed, speedReduction);

            if (direction == 1) {
                Test_PID.shiftArrayLeft(arr);
            } else  if (direction == 0) {
                Test_PID.shiftArrayRight(arr);
            }

            System.out.println("Array of Sensor : " + Arrays.toString(arr.toArray()));
            System.out.println("Size of Error: " + listOfError.size());
            System.out.println("Array of Error: " + Arrays.toString(listOfError.toArray()));
            System.out.println("");
            
             //if (direction == -1) break;
        }

    }

    /**
     * Calculate speed for 2 motor based on the PID value that has been calculated, 
     * speed reduction that calculated using several last errors, 
     * last error to determine direction of the car
     * @param arrError  array of 10 last errors
     * @param pidValue  PID value that has been calculated
     * @param setSpeed  speed value of the car when speed reduction is 0 and pidValue equal to 0 (zero)
     * @param speedReduction   value that is going to used to maintain the raise of the speed, so that the car can move smoothly
     * @return 
     */
    public static int motorSpeedCalculation( ArrayList<Integer> arrError, double pidValue, int setSpeed, double speedReduction) {
        if (arrError.get(arrError.size() - 1) < 0) {
            System.out.println("Moving Right");

            int leftSpeed = (int) (setSpeed - (pidValue) - ((int) speedReduction));
            int rightSpeed = (int) (setSpeed - ((int) speedReduction));
            System.out.printf("Left Speed : %d, Right Speed: %d\n", leftSpeed, rightSpeed);
            if (arrError.size() > 10) {
                arrError.remove(0);
            }
            return 1;
        } else if(arrError.get(arrError.size() - 1) > 0 ){
            System.out.println("Moving Left");
            int leftSpeed = (int) (setSpeed - ((int) speedReduction));
            int rightSpeed = (int) (setSpeed + (pidValue) - ((int) speedReduction));
            System.out.printf("Left Speed : %d, Right Speed: %d\n", leftSpeed, rightSpeed);
            if (arrError.size() > 10) {
                arrError.remove(0);
            }
            return 0;
        }else{
            System.out.println("Moving Straight");
            int leftSpeed = (int) (setSpeed);
            int rightSpeed = (int) (setSpeed);
            System.out.printf("Left Speed : %d, Right Speed: %d\n", leftSpeed, rightSpeed);
            if (arrError.size() > 10) {
                arrError.remove(0);
            }
        return -1;
        }
    }

    /**
     * Method that is going to be used to calculate the PID (Proportional-Integrative-Derivative) value
     * @param arrError  array of 10 last errors
     * @param setPoint  the setPoint that is going to be used, the position that we want so that the error is zero
     * @param processVariable   the value that received from the sensor that has been converted
     * @param kP   Proportional constant
     * @param kI    Integrative constant
     * @param kD    Derivative Constant
     * @return 
     */
    public static double countPID(ArrayList<Integer> arrError, int setPoint, int processVariable, double kP, double kI, double kD) {
        double pidValue = 0;
        int prevError = 0;
        int idx = 0;
        if (arrError.size() < 1) {
            prevError = 0;
        } else {
            idx = arrError.size() - 1;
            prevError = arrError.get(idx);
        }

        int error = setPoint - processVariable;
        System.out.println("Error : "+error);

        double pGain = kP * error;
        double iGain = kI * (error - prevError);
        double dGain = kD * (error + prevError);
        
        arrError.add(error);
        if (arrError.size() > 10) {
            arrError.remove(0);
        }

        pidValue = pGain + iGain + dGain;
        System.out.println("PID Value : "+pidValue);
        return pidValue;
    }

    /**
     * Method to calculate the speed reduction of the motor based on several last error value
     * @param arrError  array of 10 last errors
     * @param kSR   constant of speed reduction
     * @param sizeAtATime   the n amount last errors that is going to be used to calculate the speed reduction
     * @return 
     */
    public static double speedReduction(ArrayList<Integer> arrError, double kSR, int sizeAtATime) {
        double sigmaSROfN = 0;
        int startIdx = 0;
        int endIdx = arrError.size();

        if (arrError.size() >= sizeAtATime) {
            startIdx = arrError.size() - sizeAtATime;
        } else {
            endIdx = arrError.size();
        }
        for (int i = startIdx; i < endIdx; i++) {
            sigmaSROfN += kSR * arrError.get(i);
        }
        System.out.println("Speed Reduction of n : "+sigmaSROfN);
        return sigmaSROfN;
    }

    /**
     * Method to convert the readings of sensors to value
     * @param arr   an array that represents the reading of sensors
     * @return 
     */
    public static double sensorInputToPV(ArrayList<Integer> arr) {
        int errorModelValue = 0;
        int numerator = 0;
        int denumerator = 0;
        int centerValue = 3;
        for (int i = 0; i < arr.size(); i++) {
            //$numerator += $array[$i]*($center-$i);
            numerator += arr.get(i) * ((centerValue*(centerValue-i)));
            denumerator += arr.get(i);
        }

        if (denumerator == 0) {
            denumerator = 1;
        }
        errorModelValue = 2 * (numerator / denumerator);
        System.out.println("Process Value : "+errorModelValue);
        return errorModelValue;
    }

    public static void shiftArrayLeft(ArrayList<Integer> arr) {
        if (arr.size() > 1) {
            arr.remove(0);
        }
        arr.add(0);
        
    }

    public static void shiftArrayLeft(ArrayList<Integer> arr, int insertValue) {
        if (arr.size() > 1) {
            arr.remove(0);
        }
        arr.add(insertValue);
        
    }

    public static void shiftArrayRight(ArrayList<Integer> arr) {
        arr.add(0, 0);
        if (arr.size() > 1) {
            arr.remove(arr.size() - 1);
        }
    }

    public static void shiftArrayRight(ArrayList<Integer> arr, int insertValue) {
        arr.add(0, insertValue);
        if (arr.size() > 1) {
            arr.remove(arr.size() - 1);
        }
    }

}
