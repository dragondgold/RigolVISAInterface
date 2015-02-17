package com.andres.rigol;

import jvisa.JVisa;
import jvisa.JVisaReturnString;

import java.util.Scanner;

public class Main {

    public static void main(String[] args) {
        JVisa jVisa = new JVisa();
        jVisa.openDefaultResourceManager();

        String instrumentString = "USB0::0x1AB1::0x0588::DS1ET164267347::INSTR";
        jVisa.openInstrument(instrumentString);

        jVisa.write("*IDN?");
        JVisaReturnString r = new JVisaReturnString();
        jVisa.read(r);
        System.out.println(r.returnString);

        RigolOscilloscope scope = new RigolOscilloscope(jVisa);
        Scanner s = new Scanner(System.in);
        scope.releaseScope();

        while(true){
            String line = s.nextLine();
            if(line.equalsIgnoreCase("R")) {
                scope.runSampling();
                System.out.println("Running!");
            } else if(line.equalsIgnoreCase("S")){
                System.out.println("Stop!");
                scope.stopSampling();
            } else if(line.equalsIgnoreCase("G")) {
                System.out.println("Getting channel 1 data!");
                scope.stopSampling();
                scope.setPointMode(RigolOscilloscope.PointMode.RAW);
                byte[] data = scope.getChannelData(1);
                System.out.println("Read " + data.length + " bytes");
                scope.runSampling();
            } else if(line.equalsIgnoreCase("M")){
                System.out.print("Point mode: ");
                System.out.println(scope.getPointMode());
            }
            else if(line.equals("X")) {
                System.out.println("Bye!");
                return;
            // Send command
            } else {
                jVisa.write(line);
                jVisa.setTimeout(2000);
                JVisaReturnString returnString = new JVisaReturnString();
                jVisa.read(returnString);
                System.out.println(returnString.returnString);
                jVisa.setTimeout(20000);
            }
            scope.releaseScope();
        }
    }


}
